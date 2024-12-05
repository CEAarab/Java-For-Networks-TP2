
# Network Programming in Java: TCP and UDP Clients and Servers

## Overview

This project demonstrates basic network programming in Java by implementing TCP and UDP clients and servers. It includes:

- **TCPClient.java**: A TCP client that connects to a server, sends messages, and displays responses.
- **TCPServer.java**: A TCP server that accepts client connections, receives messages, and echoes them back.
- **TCPMultiServer.java**: A multi-threaded TCP server that can handle multiple clients concurrently.
- **UDPClient.java**: A UDP client that sends messages to a UDP server.
- **UDPServer.java**: A UDP server that listens for messages from clients.

Additionally, the project includes test classes for each component using JUnit 5 to ensure functionality and reliability.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [Usage](#usage)
  - [Running the TCP Server and Client](#running-the-tcp-server-and-client)
  - [Running the Multi-threaded TCP Server](#running-the-multi-threaded-tcp-server)
  - [Running the UDP Server and Client](#running-the-udp-server-and-client)
- [Testing](#testing)
- [Java Network Programming: Multi-Protocol Chat Server and Client with GUI](#Java-Network-Programming:-Multi-Protocol-Chat-Server-and-Client-With-GUI)
- [License](#license)

## Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **JUnit 5** library for testing (if you plan to run tests)
- **Maven** or **Gradle** (optional, for dependency management)
- An IDE like **IntelliJ IDEA** or **Eclipse** (optional but recommended)

## Project Structure

```
.
├── src
│   ├── main
│   │   └── java
│   │       ├── TCPClient.java
│   │       ├── TCPServer.java
│   │       ├── TCPMultiServer.java
│   │       ├── UDPClient.java
│   │       └── UDPServer.java
│   └── test
│       └── java
│           ├── TCPClientTest.java
│           ├── TCPServerTest.java
│           ├── TCPMultiServerTest.java
│           ├── UDPClientTest.java
│           └── UDPServerTest.java
├── README.md
├── pom.xml                // If using Maven
└── build.gradle           // If using Gradle
```

## Setup and Installation

### Clone the Repository

```bash
git clone https://github.com/CEAarab/Java-For-Networks-TP2.git
cd Java-For-Networks-TP2
```

### Build the Project

You can use Maven or Gradle to manage dependencies and build the project.

#### Using Maven

1. Ensure you have a `pom.xml` file in the project root.
2. Run the following command to compile the project:

   ```bash
   mvn clean compile
   ```

#### Using Gradle

1. Ensure you have a `build.gradle` file in the project root.
2. Run the following command to compile the project:

   ```bash
   gradle build
   ```

### Add JUnit 5 Dependency (for Testing)

#### Maven Dependency

Add the following to your `pom.xml` file:

```xml
<dependencies>
    <!-- Other dependencies -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.8.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Gradle Dependency

Add the following to your `build.gradle` file:

```gradle
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
}
```

## Usage

### Running the TCP Server and Client

#### Compile the Java Files

```bash
javac src/main/java/TCPServer.java src/main/java/TCPClient.java
```

#### Run the TCP Server

In one terminal window, start the server:

```bash
java -cp src/main/java TCPServer [port]
```

- Replace `[port]` with the desired port number (default is `8080` if omitted).

#### Run the TCP Client

In a new terminal window, start the client:

```bash
java -cp src/main/java TCPClient [hostname/IP] [port]
```

- Replace `[hostname/IP]` with the server's address (e.g., `localhost`).
- Replace `[port]` with the port number the server is listening on.

#### Interact with the Client

- Type messages into the client terminal and press **Enter** to send them to the server.
- The server will echo back the messages.
- Type `exit` to close the client connection.

### Running the Multi-threaded TCP Server

#### Compile the Java File

```bash
javac src/main/java/TCPMultiServer.java
```

#### Run the Multi-threaded TCP Server

```bash
java -cp src/main/java TCPMultiServer [port]
```

- The server will accept multiple client connections simultaneously.

#### Run Multiple TCP Clients

- Start several instances of the TCP client as described above.
- Each client will be handled by a separate thread on the server.

### Running the UDP Server and Client

#### Compile the Java Files

```bash
javac src/main/java/UDPServer.java src/main/java/UDPClient.java
```

#### Run the UDP Server

Start the server:

```bash
java -cp src/main/java UDPServer [port]
```

#### Run the UDP Client

In a new terminal window, start the client:

```bash
java -cp src/main/java UDPClient [hostname/IP] [port]
```

#### Interact with the Client

- Type messages into the client terminal and press **Enter** to send them to the server.
- Since UDP is connectionless, the server will receive messages but won't send responses back to the client.
- Type `exit` to close the client.

## Testing

### Running the Tests

#### Using Maven

```bash
mvn test
```

#### Using Gradle

```bash
gradle test
```

#### Using an IDE

- Import the project into your IDE.
- Navigate to the `src/test/java` directory.
- Run the test classes individually or run all tests together.

### Test Classes

- **TCPClientTest.java**: Tests the TCP client by simulating a server.
- **TCPServerTest.java**: Tests the TCP server by simulating a client.
- **TCPMultiServerTest.java**: Tests the multi-threaded TCP server with multiple clients.
- **UDPClientTest.java**: Tests the UDP client by simulating a server.
- **UDPServerTest.java**: Tests the UDP server by sending a packet from a client.

### Notes on Testing

- The tests use threading to simulate servers and clients running concurrently.
- **JUnit 5** is required to run the tests.
- The tests redirect `System.in` and `System.out` streams to simulate user input and capture output.



# Java Network Programming: Multi-Protocol Chat Server and Client with GUI

## Overview

This project demonstrates advanced network programming in Java, featuring a multi-protocol chat application with support for both TCP and UDP communication, TLS encryption, and a GUI-based chat client. It integrates Lua scripting for enhanced server-side security.

## Features

- **Multi-protocol Server:** Supports both TCP and UDP communication.
- **TLS Encryption:** Secure communication for TCP connections using Java Keystores.
- **Command-Line and GUI Clients:** CLI-based client and a user-friendly graphical interface using Swing.
- **Lua Integration:** Server-side scripting for security features (e.g., bad word filtering, admin controls).
- **Multi-threading:** Handles multiple clients concurrently in TCP mode.
- **Flexible Configuration:** Command-line arguments for runtime customization.
- **Unit Tests:** Comprehensive tests for all components.

## Project Structure

```
Java-For-Networks-TP2-main/
├── LICENSE                   # License information
├── README.md                 # This file
├── TP2/
│   ├── security.lua          # Lua script for server-side security
│   ├── server.keystore       # Keystore for TLS
│   ├── src/
│   │   ├── main/java/        # Source code for main application
│   │   │   ├── ChatClientGUI.java  # GUI Client
│   │   │   ├── Client.java         # CLI Client
│   │   │   ├── MultiServer.java    # Main Server (TCP/UDP + TLS)
│   │   │   ├── TCPClient.java      # Simple TCP Client
│   │   │   ├── TCPMultiServer.java # Multi-threaded TCP Server
│   │   │   ├── TCPServer.java      # Single-threaded TCP Server
│   │   │   ├── UDPClient.java      # UDP Client
│   │   │   ├── UDPServer.java      # UDP Server
│   │   └── test/java/         # Unit tests
│   │       ├── MultiServerTest.java
│   │       ├── TCPClientTest.java
│   │       ├── TCPMultiServerTest.java
│   │       ├── TCPServerTest.java
│   │       ├── UDPClientTest.java
│   │       └── UDPServerTest.java
```

## Setup and Installation

### Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **Apache Maven** (optional for dependency management)
- **LuaJ Library:** For Lua scripting integration.
- **Keystore:** TLS encryption requires a valid keystore file.

### Clone the Repository

```bash
git clone https://github.com/your-repository/Java-For-Networks-TP2.git
cd Java-For-Networks-TP2
```

### Compile the Project

#### Using Maven

```bash
mvn clean compile
```

#### Using Command-Line

```bash
javac -cp ".:path/to/commons-cli.jar:path/to/luaj-jse.jar" src/main/java/*.java
```

## Usage

### MultiServer

#### Command-Line Arguments

| Option         | Description                                              |
|----------------|----------------------------------------------------------|
| `-p <port>`    | Port to listen on                                        |
| `-t`           | Enable TLS encryption (TCP only / Not reauired)          |
| `-r <tcp/udp>` | Run in UDP or TCP mode                                   |

### Client

#### Command-Line Arguments

| Option         | Description                                  |
|----------------|----------------------------------------------|
| `-h <host>`    | Hostname or IP address of the server         |
| `-p <port>`    | Port number of the server                   |
| `-t`           | Enable TLS encryption                       |


### GUI Client

#### Usage

```bash
java -cp ".:path/to/commons-cli.jar" ChatClientGUI
```

## Testing

### Run Unit Tests

```bash
mvn test
```

### Available Test Classes

- **MultiServerTest:** Tests for TCP/UDP modes and TLS integration.
- **TCPClientTest, TCPServerTest, UDPClientTest, UDPServerTest:** Tests for individual components.

## Dependencies

- **Apache Commons CLI:** Command-line argument parsing.
- **LuaJ:** Lua scripting support.
- **JUnit 5:** Unit testing framework.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.



