import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class MultiServerTest {
    private static Thread serverThread;

    @BeforeAll
    public static void setupServer() {
        serverThread = new Thread(() -> {
            MultiServer server = new MultiServer(15263, false, "udp");
            server.launch();
        });
        serverThread.setDaemon(true); // Ensure the thread doesn't prevent JVM shutdown
        serverThread.start();

        // Give the server time to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore interruption during sleep
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    public static void tearDownServer() {
        // Since the server runs indefinitely, we can interrupt the thread
        serverThread.interrupt();
        // Optionally, you might want to implement a shutdown method in your server to close resources gracefully
    }

    @Test
    public void testTCPMultiServer() throws Exception {
        Runnable clientTask = () -> {
            try (Socket socket = new Socket("localhost", 12345);
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

                String testMessage = "Hello from client " + Thread.currentThread().getId();
                out.println(testMessage);

                // Read responses from the server
                String response;
                boolean receivedOwnMessage = false;
                long endTime = System.currentTimeMillis() + 5000; // Wait up to 5 seconds

                while (System.currentTimeMillis() < endTime && (response = in.readLine()) != null) {
                    if (response.contains(testMessage)) {
                        receivedOwnMessage = true;
                        break;
                    }
                }

                Assertions.assertTrue(receivedOwnMessage, "Client did not receive its own message in response.");

            } catch (IOException e) {
                e.printStackTrace();
                Assertions.fail("IOException occurred: " + e.getMessage());
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
