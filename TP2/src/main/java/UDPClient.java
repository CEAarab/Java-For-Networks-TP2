import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class UDPClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java UDPClient <hostname/IP> <port>");
            return;
        }

        String hostOrIp = args[0];
        int port = Integer.parseInt(args[1]);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(5000); // Set a timeout of 5 seconds for receiving

            InetAddress address = InetAddress.getByName(hostOrIp);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Write message: ");
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                // Send the message to the server
                byte[] buffer = message.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);

                // Try to receive a response from the server
                try {
                    byte[] recvBuffer = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(recvBuffer, recvBuffer.length);
                    socket.receive(recvPacket);
                    String receivedMessage = new String(recvPacket.getData(), 0, recvPacket.getLength(), "UTF-8");
                    System.out.println("Received: " + receivedMessage);
                } catch (SocketTimeoutException e) {
                    System.out.println("No response from server.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
