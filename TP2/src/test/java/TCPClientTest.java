import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;

public class TCPClientTest {
    private static Thread serverThread;

    @BeforeAll
    public static void setupServer() {
        serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

                String message;
                while ((message = in.readLine()) != null) {
                    out.println("Echo: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Give the server time to start
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
    }

    @AfterAll
    public static void tearDownServer() {
        serverThread.interrupt();
    }

    @Test
    public void testTCPClient() throws Exception {
        // Simulate user input
        String simulatedUserInput = "Hello Server\nexit\n";
        InputStream stdin = System.in;
        PrintStream stdout = System.out;

        try {
            System.setIn(new ByteArrayInputStream(simulatedUserInput.getBytes("UTF-8")));
            ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));

            String[] args = {"localhost", "12345"};
            TCPClient.main(args);

            String output = outContent.toString("UTF-8");
            Assertions.assertTrue(output.contains("Server response: Echo: Hello Server"));
        } finally {
            System.setIn(stdin);
            System.setOut(stdout);
        }
    }
}
