import java.io.*;
import java.net.*;
import java.util.*;

public class PaxosBroadcastServer {
    public static int MAX_MEMBER_COUNT;
    private List<Integer> ports;
    private int port;

    /**
     * @param port the port number to listen on
     */
    public PaxosBroadcastServer(int port, List<Integer> ports) {
        this.port = port;
        this.ports = ports;
        
        MAX_MEMBER_COUNT = ports.size();
    }


    /**
     * This method starts the Paxos broadcast server and listens for incoming socket connections on the specified port.
     * Any content received on the port will be broadcasted to all connected clients.
     */
    public void startServer() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            // Create a server socket on the specified port
            System.out.println("PaxosBroadcastServer started on port " + port);
            Utils.log("PaxosBroadcastServer started on port " + port);

            // Listen for incoming socket connections
            while (true) {
                // Accept a new client connection
                Socket clientSocket = serverSocket.accept();
                // Handle the client connection in a separate thread
                Thread clientThread = new Thread(() -> handleClientConnection(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the client connection and processes the incoming message.
     * 
     * @param clientSocket The socket representing the client connection.
     */
    private void handleClientConnection(Socket clientSocket) {
        try {
            // Read the incoming message from the client
            InputStream inputStream = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String message = reader.readLine();

            System.out.println("Received message: " + message +  " boardcast to all ports");

            // Broadcast the message to all ports
            broadcastProposal(message);
            Utils.log(clientSocket.getInetAddress()+ "Broadcast message: " + message);

            // Close the streams and socket
            reader.close();
            inputStream.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastProposal(String proposal) {
        for (Integer port : ports) {
            try {
                // Create a socket connection to the address
                Socket socket = new Socket("localhost", port);

                // Create an output stream to send the proposal message
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);

                // Send the proposal message
                writer.println(proposal);

                // Close the socket and streams
                writer.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                // Handle any exceptions that occur during the connection
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        List<Integer> testPorts = new ArrayList<>();
        testPorts.add(9000);
        testPorts.add(9001);
        testPorts.add(9002);
        PaxosBroadcastServer server = new PaxosBroadcastServer(10000, testPorts);
        server.startServer();    
    }
}

    




