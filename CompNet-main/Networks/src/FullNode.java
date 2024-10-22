import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends

public class FullNode implements FullNodeInterface {
    private int protocolVersion = 1; // Protocol version

    @Override
    public boolean listen(String ipAddress, int portNumber) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                System.out.println("FullNode listening on " + ipAddress + ":" + portNumber);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                    handleClient(clientSocket);
                }
            } catch (IOException e) {
                System.err.println("Error starting server on port " + portNumber + ": " + e.getMessage());
            }
        }).start();
        return true; // Indicate server started successfully
    }

    private void handleClient(Socket clientSocket) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Received: " + line);
                    if (line.startsWith("PUT?")) {
                        String[] parts = line.split(" ");
                        if (parts.length == 3) {
                            if (line.startsWith("GET?")) {
                                //String[] parts = line.split(" ");
                                if (parts.length == 2) {
                                    try {
                                        int keyLines = Integer.parseInt(parts[1]);
                                        StringBuilder keyBuilder = new StringBuilder();
                                        for (int i = 0; i < keyLines; i++) {
                                            keyBuilder.append(reader.readLine()).append("\n");
                                        }
                                        String key = keyBuilder.toString().trim();
                                        // Code for handling the GET request and sending the corresponding value if found
                                    } catch (NumberFormatException e) {
                                        // Handle parsing errors
                                    }
                                }
                            } try {
                                int keyLines = Integer.parseInt(parts[1]);
                                int valueLines = Integer.parseInt(parts[2]);
                                StringBuilder keyBuilder = new StringBuilder();
                                StringBuilder valueBuilder = new StringBuilder();
                                for (int i = 0; i < keyLines; i++) {
                                    keyBuilder.append(reader.readLine()).append("\n");
                                }
                                for (int i = 0; i < valueLines; i++) {
                                    valueBuilder.append(reader.readLine()).append("\n");
                                }

                                // Compute hashID for the key
                                // Check network map for the three closest nodes
                                // Compare distances and store or refuse to store accordingly
                                // Respond with SUCCESS or FAILED
                                // For now, we'll respond with SUCCESS for simplicity
                                writer.println("SUCCESS");

                            } catch (NumberFormatException | IOException e) {
                                writer.println("FAILED");
                                System.err.println("Error parsing request: " + e.getMessage());
                            }
                        } else {
                            writer.println("FAILED");
                            System.err.println("Invalid PUT request format.");
                        }
                    } else if (line.startsWith("START")) {
                        System.out.println("Received START message: " + line);
                    } else if (line.startsWith("ECHO?")) {
                        // Respond to ECHO? request with OHCE
                        writer.write("OHCE\n");
                        writer.flush();
                        System.out.println("OHCE sent");
                    } else if (line.startsWith("END")) {
                        System.out.println("Ending communication");
                    }
                    // Implement handling of other messages or commands as necessary
                }
            } catch (IOException e) {
                System.err.println("Failed to read/write from/to client: " + e.getMessage());
            /*} finally {
                try {
                    clientSocket.close(); // Ensure the socket is closed
                    System.out.println("Connection with client closed.");
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }*/
            }
        }).start();
    }


    @Override
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        try (Socket socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connection successful!");

        } catch (IOException e) {
            System.err.println("Failed to connect to " + startingNodeAddress + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        FullNode fullNode = new FullNode();
        fullNode.listen("127.0.0.1", 4566);
    }


    public void store(String key, String value) {
        try (Socket socket = new Socket("127.0.0.1", 4567);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("PUT? 1 1");
            out.println(key);
            out.println(value);

            String response;
            while ((response = in.readLine()) != null) {
                if (response.equals("SUCCESS")) {
                    System.out.println("Statement stored successfully in temporary node.");
                    break;
                } else if (response.equals("FAILED")) {
                    System.out.println("Failed to store statement in temporary node.");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error storing statement in temporary node: " + e.getMessage());
        }
    }
}
