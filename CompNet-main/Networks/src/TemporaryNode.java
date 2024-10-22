import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends

public class TemporaryNode implements TemporaryNodeInterface {

   private boolean isCommunicationStarted = false;

    private String nodeName = "TemporaryNodeName"; // Placeholder name
    private int protocolVersion = 1; // Assuming protocol version 1
    BufferedWriter out;
    BufferedReader in;
    Socket socket;

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            String[]splitString = startingNodeAddress.split(":");
            String testIP = splitString[0];
            int testPort = Integer.parseInt(splitString[1]);

            socket = new Socket(testIP,testPort);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            out.write("START 1 " + startingNodeName + "\n");
            out.flush();
            System.out.println("Start sent!");
            System.out.println(in.readLine());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }










//    @Override
//    public boolean start(String startingNodeName, String startingNodeAddress) {
//        String[] parts = startingNodeAddress.split(":");
//        if (parts.length != 2) {
//            System.err.println("Invalid starting node address.");
//            return false;
//        }
//        String host = parts[0];
//        int port = Integer.parseInt(parts[1]);
//
//        try {
//            socket = new Socket(host, port);
//            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            // Send START message
//            out.write("START " + protocolVersion + " " + startingNodeName + " " + startingNodeAddress + "\n");
//            out.flush();
//            System.out.println("START " + protocolVersion + " " + startingNodeName + " " + startingNodeAddress + "\n");
//            return true; // Successfully contacted and communicated with the starting node
//        } catch (IOException e) {
//            System.err.println("Failed to contact the starting node: " + e.getMessage());
//            return false;
//        }
//    }


    private void sendEndMessage(String reason) {
        try {
            out.write("END " + reason);
            out.flush();
            System.out.println("Sent!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean store(String key, String value) {
        // Your existing code before modification
        if (key == null || value == null) {
            System.err.println("Invalid key or value.");
            return false;
        }

        try {
            // New code based on specifications
            int keyLines = key.split("\n").length;
            int valueLines = value.split("\n").length;

            // Output the PUT request header and key-value pair
            System.out.println("PUT? " + keyLines + " " + valueLines);
            System.out.println(key);
            System.out.println(value);

            // Send PUT request header
            out.write("PUT? " + keyLines + " " + valueLines + "\n");
            out.write(key + "\n");
            out.write(value + "\n");
            out.flush();

            // Your existing code after modification

            String reply = in.readLine();
            if (Objects.equals(reply, "SUCCESS")) {
                System.out.println("Successfully stored (key, value) pair.");
                return true;
            } else {
                System.err.println("Failed to store (key, value) pair.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error sending PUT request: " + e.getMessage());
            return false;
        }
    }




    /* @Override
    public boolean store(String key, String value) {
        if (key == null || value == null) {
            System.err.println("Invalid key or value.");
            return false;
        }

        try {
            out.write("PUT? 1 1\n"); // Send PUT request header
            out.write(key + "\n"); // Send key
            out.write(value + "\n"); // Send value
            out.flush(); // Flush the writer to ensure the message is sent immediately

            String reply = in.readLine(); // Read the response from the server
            if (Objects.equals(reply, "SUCCESS")) {
                System.out.println("Successfully stored (key, value) pair.");
                return true;
            } else {
                System.err.println("Failed to store (key, value) pair.");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error sending PUT request: " + e.getMessage());
            return false;
        }
    }*/
//    @Override
//    public String get(String key) {
//    return null;
//}

//    @Override
//    public String get(String key) {
//        // Placeholder for get method implementation
//        try {
//            // Calculate the number of lines for the key
//            int keyLines = key.split("\n").length + 1; // Add 1 for the initial "GET? <number>" line
//
//            // Send GET request header
//            out.write("GET? " + keyLines + "\n");
//            out.write(key + "\n");
//            out.flush();
//
//            // Read the response from the server
//            String reply = in.readLine();
//            System.out.println("Reply is " + reply);
//            if (reply.startsWith("VALUE")) {
//                // Extract the number of lines in the value
//                int valueLines = Integer.parseInt(reply.split(" ")[1]);
//                // Read and construct the value from server response
//                StringBuilder valueBuilder = new StringBuilder();
//                for (int i = 0; i < valueLines; i++) {
//                    valueBuilder.append(in.readLine()).append("\n");
//                }
//                return valueBuilder.toString().trim();
//            } else if (Objects.equals(reply, "NOPE")) {
//                System.err.println("No value found for the given key.");
//                return null;
//            } else {
//                System.err.println("Unexpected response from server.");
//                return null;
//            }
//        } catch (IOException e) {
//            System.err.println("Error sending GET request: " + e.getMessage());
//            return null;
//        }
//    }

    public String get(String key) {
        try {
            if (!isCommunicationStarted) {
                System.out.println("Communication not started. Cannot send GET request.");
                return null;
            }

            // Send GET request
            out.write("GET? 1\n");
            out.write(key + "\n");
            out.flush();
            System.out.println("GET request sent for key: " + key);

            // Read response from server
            String response = in.readLine();
            System.out.println("Received response: " + response);

            if (response != null) {
                if (response.startsWith("VALUE")) {
                    String[] parts = response.split(" ");
                    int numLines = Integer.parseInt(parts[1]);

                    // Read the value lines
                    StringBuilder value = new StringBuilder();
                    for (int i = 0; i < numLines; i++) {
                        String line = in.readLine();
                        if (line != null) {
                            value.append(line).append("\n");
                        } else {
                            System.out.println("Unexpected end of response.");
                            return null;
                        }
                    }
                    System.out.println("Received value: " + value);
                    return value.toString().trim(); // Return the received value
                } else if ("NOPE".equals(response)) {
                    System.out.println("No value found for key: " + key);
                    return null; // Return null if no value found
                } else {
                    System.out.println("Unexpected response: " + response);
                    return null; // Return null for unexpected responses
                }
            } else {
                System.out.println("Empty response received.");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; // Return null in case of IO exception
        }
    }






//    @Override
//    public String get(String key) {
//        // Placeholder for get method implementation
//        try {
//            // Calculate the number of lines for the key
//            int keyLines = key.split("\n").length;
//
//            // Send GET request header
//            out.write("GET? " + keyLines + "\n");
//            out.write(key + "\n");
//            out.flush();
//
//            // Read the response from the server
//            String reply = in.readLine();
//            System.out.println("Reply is " + reply);
//            if (reply.startsWith("VALUE")) {
//                // Extract the number of lines in the value
//                int valueLines = Integer.parseInt(reply.split(" ")[1]);
//                // Read and construct the value from server response
//                StringBuilder valueBuilder = new StringBuilder();
//                for (int i = 0; i < valueLines; i++) {
//                    valueBuilder.append(in.readLine()).append("\n");
//                }
//                return valueBuilder.toString().trim();
//            } else if (Objects.equals(reply, "NOPE")) {
//                System.err.println("No value found for the given key.");
//                return null;
//            } else {
//                System.err.println("Unexpected response from server.");
//                return null;
//            }
//        } catch (IOException e) {
//            System.err.println("Error sending GET request: " + e.getMessage());
//            return null;
//        }
//    }

    // public String §
    public void echo() {
        try {
            out.write("ECHO?\n"); // Write OHCE followed by newline character
            out.flush(); // Flush the writer to ensure the message is sent immediately

            String reply = in.readLine();
            System.out.println("reply is: " + reply);
            if (Objects.equals(reply, "OHCE")){
                System.out.println("OHCE received!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        TemporaryNode tempNode = new TemporaryNode();
        String startingNodeName = "exampleNode";
        String startingNodeAddress = "127.0.0.1:4566";
        if (tempNode.start(startingNodeName, startingNodeAddress)) {
            System.out.println("Successfully connected to the network.");
            tempNode.echo();
            tempNode.sendEndMessage("Test!");
            System.out.println("FINISH");
        } else {
            System.out.println("Failed to connect to the network.");

        }
        tempNode.store("Welcome\n", "Hello\nWorld!" );
       // tempNode.echo(new PrintWriter());
    }
}