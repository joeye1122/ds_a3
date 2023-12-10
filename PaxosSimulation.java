import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaxosSimulation {
    public static void main(String[] args) {

        String configFilePath = null;

        if (args.length > 0) {
            configFilePath = args[0];
        } else {
            configFilePath = "config_default.txt";
        }

        if (configFilePath != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
                // Read and process the config file here

                ArrayList<PaxosParticipant> participants = new ArrayList<>();
                List<Integer> participantPorts = new ArrayList<>(); // List to store participant ports

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("/");
                    
                    if (parts.length != 3) {
                        System.out.println("Invalid config file format");
                        System.exit(1);
                    }
                    int id = Integer.parseInt(parts[0]);
                    int delay = Integer.parseInt(parts[1]);
                    int port = Integer.parseInt(parts[2]);

                    PaxosParticipant participant = new PaxosParticipant(id, port, delay);
                    participants.add(participant);
                    participantPorts.add(port); // Add port to the list
                }
                reader.close();

                PaxosBroadcastServer server = new PaxosBroadcastServer(10000, participantPorts);
                LogServer logServer = new LogServer();

                Thread boardcastThread = new Thread(() -> {
                    server.startServer();
                });

                Thread logThread = new Thread(() -> {
                    logServer.startServer();
                });

                logThread.start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                boardcastThread.start();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (PaxosParticipant participant : participants) {
                    Thread testThread = new Thread(() -> {
                        participant.startListening();
                    });
                    testThread.start();
                }

                for (PaxosParticipant participant : participants) {
                    if (participant.getId() <= 3 ) {
                        participant.prepareRequest();
                    }
                }

            } catch (IOException e) {
                System.out.println("Error reading config file: " + e.getMessage());
            }
        }
    }
}
