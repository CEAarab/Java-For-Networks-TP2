import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
    private int port;

    // Constructor with port parameter
    public UDPServer(int port) {
        this.port = port;
    }

    // Default constructor with port 8080
    public UDPServer() {
        this(8080);
    }

    // Launch method to start the server
    public void launch() {
        try (DatagramSocket socket = new DatagramSocket(port)) { // Start a UDP Socket on a defined port
            System.out.println("UDP Server running on port " + port);
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);  // Receive packet
                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                System.out.println("Client : " + clientAddress + ":" + clientPort + " sent : " + message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "UDP Server running on port " + port;
    }

    // Main method
    public static void main(String[] args) {
        if(args.length != 1) {
            UDPServer server = new UDPServer();
            server.launch();
        } else {
            int port = Integer.parseInt(args[0]);
            UDPServer server = new UDPServer(port);
            server.launch();
        }
    }
}