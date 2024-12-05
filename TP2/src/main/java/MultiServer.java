import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import org.apache.commons.cli.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class MultiServer {
    private int port;
    private boolean useTLS;
    private String protocol;
    private Globals luaGlobals;

    private static Set<String> bannedIPs = new CopyOnWriteArraySet<>();

    public MultiServer(int port, boolean useTLS, String protocol) {
        this.port = port;
        this.useTLS = useTLS;
        this.protocol = protocol;

        // Initialize Lua interpreter
        luaGlobals = JsePlatform.standardGlobals();

        // Load the Lua script
        try {
            luaGlobals.get("dofile").call(LuaValue.valueOf("security.lua"));
        } catch (LuaError e) {
            System.err.println("Error loading Lua script: " + e.getMessage());
            System.exit(1);
        }
    }

    public void launch() {
        if (protocol.equals("tcp")) {
            launchTCPServer();
        } else if (protocol.equals("udp")) {
            launchUDPServer();
        }
    }

    private void launchTCPServer() {
        try {
            ServerSocket serverSocket;
            if (useTLS) {
                SSLServerSocketFactory ssf = getServerSocketFactory();
                serverSocket = ssf.createServerSocket(port);
                System.out.println("TLS TCP Server running on port " + port);
            } else {
                serverSocket = new ServerSocket(port);
                System.out.println("TCP Server running on port " + port);
            }

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientIP = clientSocket.getInetAddress().getHostAddress();

                if (bannedIPs.contains(clientIP)) {
                    System.out.println("Rejected connection from banned IP: " + clientIP);
                    clientSocket.close();
                    continue;
                }

                new ConnectionThread(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launchUDPServer() {
        try (DatagramSocket udpSocket = new DatagramSocket(port)) {
            System.out.println("UDP Server running on port " + port);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String clientIP = packet.getAddress().getHostAddress();
                if (bannedIPs.contains(clientIP)) {
                    System.out.println("Rejected packet from banned IP: " + clientIP);
                    continue;
                }

                new UDPConnectionThread(udpSocket, packet).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SSLServerSocketFactory getServerSocketFactory() throws Exception {
        // Load server keystore
        SSLContext ctx = SSLContext.getInstance("TLS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        KeyStore ks = KeyStore.getInstance("JKS");

        ks.load(new FileInputStream("server.keystore"), "cea123".toCharArray());
        kmf.init(ks, "cea123".toCharArray());
        ctx.init(kmf.getKeyManagers(), null, null);

        return ctx.getServerSocketFactory();
    }

    public static void main(String[] args) {
        // Apache Commons CLI for command-line parsing
        Options options = new Options();

        Option portOption = new Option("p", "port", true, "Port to listen on");
        portOption.setRequired(false);
        options.addOption(portOption);

        Option tlsOption = new Option("t", "tls", false, "Enable TLS encryption");
        tlsOption.setRequired(false);
        options.addOption(tlsOption);

        Option protocolOption = new Option("r", "protocol", true, "Protocol to use (tcp or udp)");
        protocolOption.setRequired(false);
        options.addOption(protocolOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        int port = 8888;
        boolean useTLS = false;
        String protocol = "tcp"; // Default to TCP

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("port")) {
                port = Integer.parseInt(cmd.getOptionValue("port"));
            }

            useTLS = cmd.hasOption("tls");

            if (cmd.hasOption("protocol")) {
                protocol = cmd.getOptionValue("protocol").toLowerCase();
            }

            if (!protocol.equals("tcp") && !protocol.equals("udp")) {
                System.out.println("Invalid protocol specified. Use 'tcp' or 'udp'.");
                formatter.printHelp("MultiServer", options);
                System.exit(1);
            }

            if (useTLS && protocol.equals("udp")) {
                System.out.println("TLS is not supported with UDP. Disabling TLS.");
                useTLS = false;
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MultiServer", options);
            System.exit(1);
            return;
        }

        MultiServer server = new MultiServer(port, useTLS, protocol);
        server.launch();
    }

    class ConnectionThread extends Thread {
        private Socket clientSocket;
        private String clientIP;
        private String nickname;
        private BufferedReader in;
        private PrintWriter out;
        private static ConcurrentHashMap<String, ConnectionThread> clients = new ConcurrentHashMap<>();

        public ConnectionThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.clientIP = clientSocket.getInetAddress().getHostAddress();
            this.nickname = clientIP;

            // Assign 'admin' nickname to connections from 127.0.0.1
            if (clientIP.equals("127.0.0.1") || clientIP.equals("::1")) {
                this.nickname = "admin";
            }

            clients.put(nickname, this);
        }

        @Override
        public void run() {
            try {
                // Start TLS handshake if using TLS
                if (useTLS && clientSocket instanceof SSLSocket) {
                    SSLSocket sslSocket = (SSLSocket) clientSocket;
                    sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
                    sslSocket.startHandshake();
                }

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

                broadcast(nickname + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from " + nickname + ": " + message);
                    if (message.startsWith("#")) {
                        handleCommand(message);
                    } else if (message.startsWith("@")) {
                        handlePrivateMessage(message);
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clients.remove(nickname);
                broadcast(nickname + " has left the chat.");
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleCommand(String message) {
            if (message.startsWith("#nickname ")) {
                String newNickname = message.substring(10).trim();

                // Check if the user is trying to change nickname to 'admin'
                if (newNickname.equalsIgnoreCase("admin")) {
                    // Use Lua function to check if the user can change to 'admin'
                    Varargs result = MultiServer.this.luaGlobals.get("can_change_to_admin")
                            .invoke(LuaValue.valueOf(clientIP));
                    boolean canChange = result.arg1().toboolean();

                    if (!canChange) {
                        sendMessage("You cannot change your nickname to 'admin'.");
                        return;
                    }
                }

                if (clients.containsKey(newNickname)) {
                    sendMessage("Nickname already in use.");
                } else {
                    clients.remove(nickname);
                    broadcast(nickname + " changed nickname to " + newNickname);
                    nickname = newNickname;
                    clients.put(nickname, this);
                    sendMessage("Nickname changed to " + nickname);
                }
            } else if (message.startsWith("#ban ")) {
                String userToBan = message.substring(5).trim();

                // Use Lua function to check if the user can execute 'ban' command
                Varargs result = MultiServer.this.luaGlobals.get("can_execute_command")
                        .invoke(LuaValue.valueOf(nickname), LuaValue.valueOf("ban"));
                boolean canExecute = result.arg1().toboolean();

                if (!canExecute) {
                    sendMessage("You do not have permission to use the 'ban' command.");
                    return;
                }

                // Proceed with ban logic
                ConnectionThread userThread = clients.get(userToBan);
                if (userThread != null) {
                    String bannedIP = userThread.clientSocket.getInetAddress().getHostAddress();
                    bannedIPs.add(bannedIP);
                    userThread.sendMessage("You have been banned by the admin.");
                    userThread.disconnect();
                    broadcast(userToBan + " has been banned by the admin.");
                } else {
                    sendMessage("User " + userToBan + " not found.");
                }
            } else if (message.equals("#stat")) {
                sendMessage("Number of users online: " + clients.size());
            } else {
                sendMessage("Unknown command.");
            }
        }

        private void handlePrivateMessage(String message) {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex != -1) {
                String targetNickname = message.substring(1, spaceIndex);
                String privateMessage = message.substring(spaceIndex + 1);

                // Check the message using Lua script
                Varargs result = MultiServer.this.luaGlobals.get("banned_words")
                        .invoke(LuaValue.valueOf(privateMessage));
                boolean containsBadWords = result.arg1().toboolean();

                if (containsBadWords) {
                    sendMessage("Your private message was blocked due to inappropriate content.");
                    return;
                }

                ConnectionThread targetClient = clients.get(targetNickname);
                if (targetClient != null) {
                    targetClient.sendMessage("(Private) " + nickname + ": " + privateMessage);
                    sendMessage("(Private to " + targetNickname + ") " + privateMessage);
                } else {
                    sendMessage("User " + targetNickname + " not found.");
                }
            } else {
                sendMessage("Invalid private message format. Use '@nickname message'.");
            }
        }

        private void broadcast(String message) {
            // Check the message using Lua script
            Varargs result = MultiServer.this.luaGlobals.get("banned_words")
                    .invoke(LuaValue.valueOf(message));
            boolean containsBadWords = result.arg1().toboolean();

            if (containsBadWords) {
                sendMessage("Your message was blocked due to inappropriate content.");
                return;
            }

            // Broadcast to all clients
            for (ConnectionThread client : clients.values()) {
                client.sendMessage(message);
            }
        }

        private void sendMessage(String message) {
            out.println(message);
        }

        private void disconnect() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clients.remove(nickname);
            Thread.currentThread().interrupt();
        }
    }

    class UDPConnectionThread extends Thread {
        private DatagramSocket udpSocket;
        private DatagramPacket packet;
        private InetAddress clientAddress;
        private int clientPort;
        private String clientKey; // Unique key for each client
        private static ConcurrentHashMap<String, String> clientNicknames = new ConcurrentHashMap<>();

        public UDPConnectionThread(DatagramSocket udpSocket, DatagramPacket packet) {
            this.udpSocket = udpSocket;
            this.packet = packet;
            this.clientAddress = packet.getAddress();
            this.clientPort = packet.getPort();
            this.clientKey = clientAddress.getHostAddress() + ":" + clientPort;
        }

        @Override
        public void run() {
            try {
                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                // Register the client if not already registered
                clientNicknames.putIfAbsent(clientKey, clientKey);

                String nickname = clientNicknames.get(clientKey);

                System.out.println("Received from " + nickname + ": " + message); // Debugging output

                if (message.startsWith("#")) {
                    handleCommand(message, nickname);
                } else if (message.startsWith("@")) {
                    handlePrivateMessage(message, nickname);
                } else {
                    broadcast(nickname + ": " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleCommand(String message, String nickname) {
            if (message.startsWith("#nickname ")) {
                String newNickname = message.substring(10).trim();

                // Check if the user is trying to change nickname to 'admin'
                if (newNickname.equalsIgnoreCase("admin")) {
                    Varargs result = MultiServer.this.luaGlobals.get("can_change_to_admin")
                            .invoke(LuaValue.valueOf(clientAddress.getHostAddress()));
                    boolean canChange = result.arg1().toboolean();

                    if (!canChange) {
                        sendMessage("You cannot change your nickname to 'admin'.");
                        return;
                    }
                }

                if (clientNicknames.containsValue(newNickname)) {
                    sendMessage("Nickname already in use.");
                } else {
                    clientNicknames.put(clientKey, newNickname);
                    sendMessage("Nickname changed to " + newNickname);
                }
            } else if (message.startsWith("#ban ")) {
                String userToBan = message.substring(5).trim();

                // Use Lua function to check if the user can execute 'ban' command
                Varargs result = MultiServer.this.luaGlobals.get("can_execute_command")
                        .invoke(LuaValue.valueOf(nickname), LuaValue.valueOf("ban"));
                boolean canExecute = result.arg1().toboolean();

                if (!canExecute) {
                    sendMessage("You do not have permission to use the 'ban' command.");
                    return;
                }

                // Proceed with ban logic
                String targetClientKey = null;
                for (Map.Entry<String, String> entry : clientNicknames.entrySet()) {
                    if (entry.getValue().equals(userToBan)) {
                        targetClientKey = entry.getKey();
                        break;
                    }
                }

                if (targetClientKey != null) {
                    String bannedIP = targetClientKey.split(":")[0];
                    bannedIPs.add(bannedIP);
                    sendMessageToClient("You have been banned by the admin.", targetClientKey);
                    clientNicknames.remove(targetClientKey);
                    broadcast(userToBan + " has been banned by the admin.");
                } else {
                    sendMessage("User " + userToBan + " not found.");
                }
            } else if (message.equals("#stat")) {
                sendMessage("Number of users online: " + clientNicknames.size());
            } else {
                sendMessage("Unknown command.");
            }
        }

        private void handlePrivateMessage(String message, String nickname) {
            int spaceIndex = message.indexOf(' ');
            if (spaceIndex != -1) {
                String targetNickname = message.substring(1, spaceIndex);
                String privateMessage = message.substring(spaceIndex + 1);

                // Check the message using Lua script
                Varargs result = MultiServer.this.luaGlobals.get("banned_words")
                        .invoke(LuaValue.valueOf(privateMessage));
                boolean containsBadWords = result.arg1().toboolean();

                if (containsBadWords) {
                    sendMessage("Your private message was blocked due to inappropriate content.");
                    return;
                }

                // Find the target client
                String targetClientKey = null;
                for (Map.Entry<String, String> entry : clientNicknames.entrySet()) {
                    if (entry.getValue().equals(targetNickname)) {
                        targetClientKey = entry.getKey();
                        break;
                    }
                }

                if (targetClientKey != null) {
                    sendMessageToClient("(Private) " + nickname + ": " + privateMessage, targetClientKey);
                    sendMessage("(Private to " + targetNickname + ") " + privateMessage);
                } else {
                    sendMessage("User " + targetNickname + " not found.");
                }
            } else {
                sendMessage("Invalid private message format. Use '@nickname message'.");
            }
        }

        private void broadcast(String message) {
            // Check the message using Lua script
            Varargs result = MultiServer.this.luaGlobals.get("banned_words")
                    .invoke(LuaValue.valueOf(message));
            boolean containsBadWords = result.arg1().toboolean();

            if (containsBadWords) {
                sendMessage("Your message was blocked due to inappropriate content.");
                return;
            }

            System.out.println("Broadcasting message: " + message); // Debugging output

            // Broadcast to all clients
            for (String client : clientNicknames.keySet()) {
                sendMessageToClient(message, client);
            }
        }

        private void sendMessage(String message) {
            sendMessageToClient(message, clientKey);
        }

        private void sendMessageToClient(String message, String clientKey) {
            try {
                String[] parts = clientKey.split(":");
                InetAddress address = InetAddress.getByName(parts[0]);
                int port = Integer.parseInt(parts[1]);

                byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                DatagramPacket outPacket = new DatagramPacket(buffer, buffer.length, address, port);
                udpSocket.send(outPacket);

                System.out.println("Sent message to " + clientKey + ": " + message); // Debugging output
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
