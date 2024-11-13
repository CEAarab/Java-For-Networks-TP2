import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class TCPServerTest {
    private static Thread serverThread;

    @BeforeAll
    public static void setupServer() {
        serverThread = new Thread(() -> {
            TCPServer server = new TCPServer(12345);
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
    public void testTCPServer() throws Exception {
        try (Socket socket = new Socket("localhost", 12345);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            String testMessage = "Hello Server";
            out.println(testMessage);

            String response = in.readLine();
            Assertions.assertNotNull(response);
            Assertions.assertTrue(response.contains(testMessage));
        }
    }
}
