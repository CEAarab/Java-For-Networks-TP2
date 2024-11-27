import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.net.ssl.*;
import org.apache.commons.cli.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

public class MultiServer {
    private int port;
    private boolean useTLS;
    private Globals luaGlobals;

    public MultiServer(int port, boolean useTLS) {
        this.port = port;
        this.useTLS = useTLS;

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

    private static Set<String> bannedIPs = new CopyOnWriteArraySet<>();

    public void launch() {
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

                new ConnectionThread(clientSocket).start(); // Create a new thread for each client
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

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        int port = 8888;
        boolean useTLS = false;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("port")) {
                port = Integer.parseInt(cmd.getOptionValue("port"));
            }

            useTLS = cmd.hasOption("tls");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("TCPMultiServer", options);
            System.exit(1);
            return;
        }

        MultiServer server = new MultiServer(port, useTLS);
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
                    Varargs result = MultiServer.this.luaGlobals.get("can_change_to_admin").invoke(LuaValue.valueOf(clientIP));
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
                Varargs result = MultiServer.this.luaGlobals.get("can_execute_command").invoke(LuaValue.valueOf(nickname), LuaValue.valueOf("ban"));
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
                Varargs result = MultiServer.this.luaGlobals.get("banned_words").invoke(LuaValue.valueOf(privateMessage));
                boolean containsBadWords = result.arg1().toboolean();

                if (containsBadWords) {
                    // Message contains bad words
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
            Varargs result = MultiServer.this.luaGlobals.get("banned_words").invoke(LuaValue.valueOf(message));
            boolean containsBadWords = result.arg1().toboolean();

            if (containsBadWords) {
                // Message contains bad words
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
}
