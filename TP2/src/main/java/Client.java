import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.net.ssl.*;
import org.apache.commons.cli.*;

public class Client {
    public static void main(String[] args) {
        // Apache Commons CLI for command-line parsing
        Options options = new Options();

        Option hostOption = new Option("h", "host", true, "Hostname or IP address");
        hostOption.setRequired(true);
        options.addOption(hostOption);

        Option portOption = new Option("p", "port", true, "Port number");
        portOption.setRequired(true);
        options.addOption(portOption);

        Option tlsOption = new Option("t", "tls", false, "Enable TLS encryption");
        tlsOption.setRequired(false);
        options.addOption(tlsOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        String hostOrIp = null;
        int port = 0;
        boolean useTLS = false;

        try {
            cmd = parser.parse(options, args);

            hostOrIp = cmd.getOptionValue("host");
            port = Integer.parseInt(cmd.getOptionValue("port"));

            useTLS = cmd.hasOption("tls");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("TCPClient", options);
            System.exit(1);
            return;
        }

        try {
            Socket socket;
            if (useTLS) {
                SSLSocketFactory sf = getSocketFactory();
                socket = sf.createSocket(hostOrIp, port);
                System.out.println("Connected to TLS server: " + hostOrIp + " on port " + port);
            } else {
                socket = new Socket(hostOrIp, port);
                System.out.println("Connected to server: " + hostOrIp + " on port " + port);
            }

            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            Scanner scanner = new Scanner(System.in);

            // Thread to read messages from server
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Read input from user and send to server
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(message);
            }

            socket.close();
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SSLSocketFactory getSocketFactory() throws Exception {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { new SimpleX509TrustManager() }, null);
        return ctx.getSocketFactory();
    }
}

class SimpleX509TrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        // Trust all clients
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
        // Trust all servers
    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
