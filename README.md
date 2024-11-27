
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



# Java For Network: Multi-threaded Chat Application

## Overview

This project demonstrates advanced network programming in Java by implementing a secure, multi-threaded chat application with extended functionality. It includes:

- **MultiServer.java**: A multi-threaded TCP server with TLS support, capable of handling multiple clients concurrently, and integrating Lua scripting for security policies.
- **Client.java**: A TCP client that connects to the server, sends messages, and displays responses, with support for TLS encryption.
- **security.lua**: A Lua script defining security policies, such as message filtering and command authorization.

Key features of this application:

- **TLS Encryption**: Secure communication between clients and server using TLS.
- **Command Implementation**: Support for commands like `#stat`, `#ban`, `#nickname` and `@private`.
- **Improved Command-Line Argument Parsing**: Utilizing Apache Commons CLI for robust command-line interfaces.
- **Lua Scripting Integration**: Dynamic security policies defined in Lua scripts.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [Usage](#usage)
  - [Running the Secure Multi-threaded TCP Server](#running-the-secure-multi-threaded-tcp-server)
  - [Running the Client](#running-the-client)
  - [Commands](#commands)
- [License](#license)

## Prerequisites

- **Java Development Kit (JDK) 8 or higher**
- **Apache Commons CLI** library for command-line parsing
- **LuaJ** library for Lua scripting integration
- **JUnit 5** library for testing (if you plan to run tests)
- **Maven** or **Gradle** (optional, for dependency management)
- An IDE like **IntelliJ IDEA** or **Eclipse** (optional but recommended)

## Project Structure

```
.
├── src
│   ├── main
│   │   ├── java
│   │       ├── MultiServer.java
│   │       ├── Client.java
│   │       └── SimpleX509TrustManager.java
│   │   
│   │       
│   └── test
│       └── java
│           ├── MultiServerTest.java (In Development phase)
│           └── ClientTest.java      (In Development phase)
├── README.md
└── security.lua
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
2. Add dependencies for Apache Commons CLI and LuaJ:

   ```xml
   <dependencies>
       <!-- Other dependencies -->
       <dependency>
           <groupId>commons-cli</groupId>
           <artifactId>commons-cli</artifactId>
           <version>1.4</version>
       </dependency>
       <dependency>
           <groupId>org.luaj</groupId>
           <artifactId>luaj-jse</artifactId>
           <version>3.0.1</version>
       </dependency>
       <!-- For testing -->
       <dependency>
           <groupId>org.junit.jupiter</groupId>
           <artifactId>junit-jupiter</artifactId>
           <version>5.8.1</version>
           <scope>test</scope>
       </dependency>
   </dependencies>
   ```

3. Run the following command to compile the project:

   ```bash
   mvn clean compile
   ```

#### Using Gradle

1. Ensure you have a `build.gradle` file in the project root.
2. Add dependencies for Apache Commons CLI and LuaJ:

   ```gradle
   dependencies {
       implementation 'commons-cli:commons-cli:1.4'
       implementation 'org.luaj:luaj-jse:3.0.1'
       testImplementation 'org.junit.jupiter:junit-jupiter:5.8.1'
   }
   ```

3. Run the following command to compile the project:

   ```bash
   gradle build
   ```

## Usage

### Running the Secure Multi-threaded TCP Server

#### Generate Keystore for TLS

Before running the server with TLS, you need to generate a keystore (or you can use my keystore file with password cea123):

```bash
keytool -genkeypair -alias serverkey -keyalg RSA -keysize 2048 -keystore server.keystore -storepass your_keystore_password -keypass your_key_password -validity 365
```

- Replace `your_keystore_password` and `your_key_password` with your desired passwords.

Place the `server.keystore` file in the project directory.

#### Compile the Java Files

If not using Maven or Gradle:

```bash
javac -cp ".:path/to/commons-cli.jar:path/to/luaj-jse.jar" src/main/java/MultiServer.java
```

#### Run the Server

In one terminal window, start the server:

```bash
java -cp ".:path/to/commons-cli.jar:path/to/luaj-jse.jar:src/main/java" MultiServer -p 8888 -t
```

- The `-p` option specifies the port (default is `8888`).
- The `-t` option enables TLS encryption.

### Running the Client

#### Compile the Java Files

If not using Maven or Gradle:

```bash
javac -cp ".:path/to/commons-cli.jar" src/main/java/Client.java
```

#### Run the Client

In a new terminal window, start the client:

```bash
java -cp ".:path/to/commons-cli.jar:src/main/java" Client -h localhost -p 8888 -t
```

- The `-h` option specifies the server hostname or IP address.
- The `-p` option specifies the port number.
- The `-t` option enables TLS encryption.

### Commands

Once connected, clients can use the following commands:

- `#nickname <new_nickname>`: Change your nickname.
- `#stat`: Display the number of users online.
- `#ban <nickname>`: **Admin only**. Ban a user from the chat.
- `@<nickname> <message>`: Send a private message to a specific user.

### Security Policies via Lua Scripting

The server uses a Lua script (`security.lua`) to enforce security policies:

- **Message Filtering**: Messages containing certain bad words are blocked.
- **Command Authorization**: Only the admin (user connected from `127.0.0.1`) can use the `#ban` command or change their nickname to `admin`.

#### Editing Security Policies

You can modify the `security.lua` script to change security policies without altering the Java code.

**Example `security.lua` content:**

```lua
-- List of bad words
bad_words = { "badword1", "badword2", "badword3" }

-- Function to check messages for bad words
function contains_bad_words(message)
    for _, word in ipairs(bad_words) do
        if string.find(message:lower(), word) then
            return true
        end
    end
    return false
end

-- Function to check if a user can change nickname to 'admin'
function can_change_to_admin(user_ip)
    return user_ip == "127.0.0.1" or user_ip == "::1"
end

-- Function to check if a user can execute a command
function can_execute_command(user_nickname, command)
    if command == "ban" then
        return user_nickname == "admin"
    else
        return true -- Allow other commands by default
    end
end
```

### Note on TLS Certificates

The client uses a `SimpleX509TrustManager` that trusts all server certificates. In a production environment, you should implement proper certificate validation.


# License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.




