public class PaxosSimulation {
    public static void main(String[] args) {
        PaxosParticipant p1 = new PaxosParticipant(1, 9000);
        PaxosParticipant p2 = new PaxosParticipant(2, 9001);
        PaxosParticipant p3 = new PaxosParticipant(3, 9002);



        Thread testThread1 = new Thread(() -> {
            p1.startListening();
        });
        testThread1.start();

        Thread testThread2 = new Thread(() -> {
            p2.startListening();
        });                
        
        testThread2.start();

        Thread testThread3 = new Thread(() -> {
            p3.startListening();
        });
        testThread3.start(); 

        p1.prepareRequest();
    }
}
