import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
            InetAddress address = InetAddress.getByName(hostOrIp);
            Scanner scanner = new Scanner(System.in); // Use Scanner instead of Console

            while (true) {
                System.out.print("Write message: ");
                String message = scanner.nextLine(); // Read input from Scanner
                if (message.equalsIgnoreCase("exit")) {
                    break;
                }

                byte[] buffer = message.getBytes("UTF-8");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(packet);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

