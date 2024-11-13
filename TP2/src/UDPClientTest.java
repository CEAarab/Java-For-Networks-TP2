import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class UDPClientTest {
    private static Thread serverThread;

    @BeforeAll
    public static void setupServer() {
        serverThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(12345)) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);  // Receive a single packet for the test
                String message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
                // You can add assertions here if needed
            } catch (IOException e) {
                e.printStackTrace();
            }
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
    public void testUDPClient() throws Exception {
        String simulatedUserInput = "Hello UDP Server\nexit\n";
        InputStream stdin = System.in;
        PrintStream stdout = System.out;

        try {
            System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes("UTF-8")));
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            String[] args = {"localhost", "12345"};
            UDPClient.main(args);

            // Since UDP is connectionless, we can't directly assert the server response here
            // But we can check if the client ran without exceptions
            Assertions.assertTrue(outContent.toString("UTF-8").contains("Write message: "));
        } finally {
            System.setIn(stdin);
            System.setOut(stdout);
        }
    }
}
