
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
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

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

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.
