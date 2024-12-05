import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ChatClientGUI extends Application {

    private TextArea messageArea;
    private TextField inputField;
    private Button sendButton;
    private TextField serverAddressField;
    private TextField portField;
    private Button connectButton;
    private Button disconnectButton;
    private ComboBox<String> protocolComboBox;

    private Socket tcpSocket;
    private PrintWriter tcpOut;
    private BufferedReader tcpIn;

    private DatagramSocket udpSocket;
    private InetAddress serverInetAddress;
    private int serverPort;

    private Thread readerThread;

    @Override
    public void start(Stage primaryStage) {
        // UI Elements
        messageArea = new TextArea();
        messageArea.setEditable(false);

        inputField = new TextField();
        inputField.setDisable(true);

        sendButton = new Button("Send");
        sendButton.setDisable(true);

        serverAddressField = new TextField("127.0.0.1");
        portField = new TextField("8888");

        protocolComboBox = new ComboBox<>();
        protocolComboBox.getItems().addAll("TCP", "UDP");
        protocolComboBox.setValue("TCP");

        connectButton = new Button("Connect");
        disconnectButton = new Button("Disconnect");
        disconnectButton.setDisable(true);

        // Layout
        HBox connectionBox = new HBox(5, new Label("Server:"), serverAddressField,
                new Label("Port:"), portField, new Label("Protocol:"), protocolComboBox,
                connectButton, disconnectButton);
        HBox inputBox = new HBox(5, inputField, sendButton);
        VBox root = new VBox(5, connectionBox, messageArea, inputBox);

        // Event Handlers
        connectButton.setOnAction(event -> connect());
        disconnectButton.setOnAction(event -> disconnect());
        sendButton.setOnAction(event -> sendMessage());
        inputField.setOnAction(event -> sendMessage());

        // Scene and Stage
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connect() {
        String serverAddress = serverAddressField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String protocol = protocolComboBox.getValue();

        if (protocol.equals("TCP")) {
            connectTCP(serverAddress, port);
        } else if (protocol.equals("UDP")) {
            connectUDP(serverAddress, port);
        }
    }

    private void connectTCP(String serverAddress, int port) {
        try {
            tcpSocket = new Socket(serverAddress, port);
            tcpOut = new PrintWriter(new OutputStreamWriter(tcpSocket.getOutputStream(), StandardCharsets.UTF_8), true);
            tcpIn = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream(), StandardCharsets.UTF_8));

            // Start reader thread
            readerThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = tcpIn.readLine()) != null) {
                        String finalResponse = response;
                        Platform.runLater(() -> messageArea.appendText(finalResponse + "\n"));
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> messageArea.appendText("Disconnected from server.\n"));
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Update UI
            updateUIOnConnect("TCP");

            messageArea.appendText("Connected to server: " + serverAddress + " on port " + port + " via TCP\n");

        } catch (IOException e) {
            messageArea.appendText("Failed to connect to server via TCP.\n");
        }
    }

    private void connectUDP(String serverAddress, int port) {
        try {
            udpSocket = new DatagramSocket();
            udpSocket.setSoTimeout(5000);
            serverInetAddress = InetAddress.getByName(serverAddress);
            serverPort = port;

            // Start reader thread
            readerThread = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (!udpSocket.isClosed()) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        udpSocket.receive(packet);
                        String response = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        Platform.runLater(() -> messageArea.appendText(response + "\n"));
                    }
                } catch (IOException e) {
                    if (!udpSocket.isClosed()) {
                        Platform.runLater(() -> messageArea.appendText("Disconnected from server.\n"));
                    }
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Update UI
            updateUIOnConnect("UDP");

            messageArea.appendText("Connected to server: " + serverAddress + " on port " + port + " via UDP\n");

        } catch (IOException e) {
            messageArea.appendText("Failed to connect to server via UDP.\n");
        }
    }

    private void updateUIOnConnect(String protocol) {
        connectButton.setDisable(true);
        disconnectButton.setDisable(false);
        sendButton.setDisable(false);
        inputField.setDisable(false);
        serverAddressField.setDisable(true);
        portField.setDisable(true);
        protocolComboBox.setDisable(true);
    }

    private void disconnect() {
        String protocol = protocolComboBox.getValue();
        if (protocol.equals("TCP")) {
            disconnectTCP();
        } else if (protocol.equals("UDP")) {
            disconnectUDP();
        }

        // Update UI
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        inputField.setDisable(true);
        serverAddressField.setDisable(false);
        portField.setDisable(false);
        protocolComboBox.setDisable(false);

        messageArea.appendText("Disconnected from server.\n");
    }

    private void disconnectTCP() {
        try {
            if (tcpSocket != null && !tcpSocket.isClosed()) {
                tcpSocket.close();
            }
            if (readerThread != null && readerThread.isAlive()) {
                readerThread.interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disconnectUDP() {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            String protocol = protocolComboBox.getValue();
            if (protocol.equals("TCP")) {
                tcpOut.println(message);
            } else if (protocol.equals("UDP")) {
                try {
                    byte[] buffer = message.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverInetAddress, serverPort);
                    udpSocket.send(packet);
                } catch (IOException e) {
                    messageArea.appendText("Failed to send message via UDP.\n");
                }
            }
            inputField.clear();
        }
    }

    @Override
    public void stop() throws Exception {
        // Clean up resources
        disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
