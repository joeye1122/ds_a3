import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class LogServer {
    public static void main(String[] args) {
        int port = 10001;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Log Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread clientThread = new Thread(() -> {
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String logMessage;

                        while ((logMessage = reader.readLine()) != null) {
                            System.out.println(logMessage);
                        }

                        reader.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
