import java.util.*;

public class PaxosTest {
    public static void main(String[] args) {
        PaxosParticipant p1 = new PaxosParticipant(1, 9000, 0);
        PaxosParticipant p2 = new PaxosParticipant(2, 9001, 0);
        PaxosParticipant p3 = new PaxosParticipant(3, 9002, 0);

        List<PaxosParticipant> participants = new ArrayList<>();
        participants.add(p1);
        participants.add(p2);
        participants.add(p3);

        List<Integer> ports = new ArrayList<>();
        ports.add(9000);
        ports.add(9001);
        ports.add(9002);
        
        LogServer logServer = new LogServer();
        PaxosBroadcastServer server = new PaxosBroadcastServer(10000, ports);

        Thread logThread = new Thread(() -> {
            logServer.startServer();
        });

        Thread boardcastThread = new Thread(() -> {
            server.startServer();
        });

        logThread.start();
        boardcastThread.start();

        for (PaxosParticipant participant : participants) {
            Thread participantThread = new Thread(() -> {
                participant.startListening();
            });
            participantThread.start();
        }

        p1.prepareRequest();

    }

    // public static void main(String[] args) {
    //     PaxosParticipant p1 = new PaxosParticipant(1, 9000, 0);
    //     List<Integer> ports = new ArrayList<>();
    //     ports.add(9000);
    //     ports.add(9005);
    //     PaxosBroadcastServer server = new PaxosBroadcastServer(10000, ports);

    //     Thread boardcastThread = new Thread(() -> {
    //         server.startServer();
    //     });

    //     Thread p1Thread = new Thread(() -> {
    //         p1.startListening();
    //     });

    //     p1Thread.start();
    //     boardcastThread.start();
        
    //     p1.prepareRequest();
        
    // }
}
