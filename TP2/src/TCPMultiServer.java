import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPMultiServer {
    private int port;
    public TCPMultiServer(int port) {
        this.port = port;
    }
    public TCPMultiServer() {
        this(8080);
    }

    public void launch() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Multi-threaded TCP Server running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ConnectionThread(clientSocket).start();  // Create a new thread for each client
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            TCPMultiServer server = new TCPMultiServer();
            server.launch();
        } else {
            int port = Integer.parseInt(args[0]);
            TCPMultiServer server = new TCPMultiServer(port);
            server.launch();
        }
    }
}

class ConnectionThread extends Thread {
    private Socket clientSocket;

    public ConnectionThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received from " + clientSocket.getInetAddress() + ": " + message);
                out.println(clientSocket.getInetAddress() + ": " + message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
