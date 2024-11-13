import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class UDPServerTest {
    private static Thread serverThread;
    private static ByteArrayOutputStream serverOutput;

    @BeforeAll
    public static void setupServer() {
        serverOutput = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(serverOutput);

        serverThread = new Thread(() -> {
            PrintStream originalOut = System.out;
            System.setOut(ps);
            UDPServer server = new UDPServer(12345);
            server.launch();
            System.setOut(originalOut);
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
    public void testUDPServer() throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            String message = "Hello UDP Server";
            byte[] buffer = message.getBytes("UTF-8");
            InetAddress address = InetAddress.getByName("localhost");
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 12345);
            socket.send(packet);

            // Give the server time to process the packet
            Thread.sleep(500);

            String serverLogs = serverOutput.toString("UTF-8");
            Assertions.assertTrue(serverLogs.contains("sent : " + message));
        }
    }
}
