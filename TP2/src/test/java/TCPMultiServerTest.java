import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class TCPMultiServerTest {
    private static Thread serverThread;

    @BeforeAll
    public static void setupServer() {
        serverThread = new Thread(() -> {
            TCPMultiServer server = new TCPMultiServer(12345);
            server.launch();
        });
        serverThread.start();

        // Give the server time to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
    }

    @AfterAll
    public static void tearDownServer() {
        serverThread.interrupt();
    }

    @Test
    public void testTCPMultiServer() throws Exception {
        Runnable clientTask = () -> {
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

                String testMessage = "Hello from client " + Thread.currentThread().getId();
                out.println(testMessage);

                String response = in.readLine();
                Assertions.assertNotNull(response);
                Assertions.assertTrue(response.contains(testMessage));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        Thread client1 = new Thread(clientTask);
        Thread client2 = new Thread(clientTask);
        client1.start();
        client2.start();

        client1.join();
        client2.join();
    }
}
