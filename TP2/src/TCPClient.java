import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java TCPClient <hostname/IP> <port>");
            return;
        }

        String hostOrIp = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostOrIp, port);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to server: " + hostOrIp + " on port " + port);

            // Keep reading input from user until they close input
            while (true) {
                System.out.print("Write message: ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Send message to the server (encoded in UTF-8)
                out.write((message + "\n").getBytes("UTF-8"));
                out.flush();

                // Read the response from the server
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server closed the connection.");
                    break;
                }

                // Print the response
                System.out.println("Server response: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}