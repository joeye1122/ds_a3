import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PaxosParticipant {
    
    private final int id;
    private String memberName;
    private final int broadcastServerPort = 10000;

    private int port;
    private int proposalPromisedCount;
    private Proposal accepterCurrentProposal;
    private List<Proposal> proposerPromises;
    private boolean isProposalPhasetwo;
    private Map<Proposal, Integer> learnerDecision;


    public PaxosParticipant(int id, int port) {
        this.id = id;
        this.port = port;
        memberName = "m" + Integer.toString(id);
        proposerPromises = new ArrayList<>();
        learnerDecision = new HashMap<>();
        isProposalPhasetwo = false;
    }

    public void prepareRequest() {
        try {
            Socket socket = new Socket("localhost", broadcastServerPort);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            String vString = ProcessHandle.current().pid() + memberName;

            //TODO: v = id
            Proposal proposal = new Proposal(Utils.convertStringToInt(vString), id, "PrepareRequest", port);

            writer.println(proposal.toString());
            writer.flush();

            writer.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Add the following method to start listening on the specified port
    public void startListening(){
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Socket server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection accepted from " + clientSocket.getInetAddress());

                // Create a new thread to handle the client request
                Thread thread = new Thread(() -> {
                    try {
                        InputStream inputStream = clientSocket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String message = reader.readLine();
                        System.out.println("Received message: " + message);

                        // Process the message and perform the necessary actions
                        Proposal reciveProposal = Utils.fromString(message);

                        if(reciveProposal.getMessageTyep().equals("PrepareRequest")){
                            acceptorResponseToPrepareRequest(reciveProposal);
                        }else if(reciveProposal.getMessageTyep().equals("ResponseToPrepareRequest")){
                            //proposer listen to acceptor
                            proposerAcceptRequest(reciveProposal);
                        }else if(reciveProposal.getMessageTyep().equals("AcceptRequest")){
                            //acceptor listen to proposer accept request and send decision to learner
                            acceptorDecision(reciveProposal);
                        }else if(reciveProposal.getMessageTyep().equals("Decision")){
                            learnerListenDecision(reciveProposal);

                        }else{
                            System.out.println("Error: Unknown message type");
                        }


                        reader.close();
                        inputStream.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void acceptorResponseToPrepareRequest(Proposal reciveProposal){
        Proposal respondProposal = null;
        // this acceptor haven't promoise to any proposer --> make a promise
        if(accepterCurrentProposal == null){
            accepterCurrentProposal = reciveProposal;
            //promise
            respondProposal = new Proposal(-1,-1,"ResponseToPrepareRequest",port);            
        }

        //this is a new proposal
        if(reciveProposal.getProposalNumber() > accepterCurrentProposal.getProposalNumber()){
            respondProposal = new Proposal(accepterCurrentProposal.getProposalNumber(),accepterCurrentProposal.getProposalValue(),"ResponseToPrepareRequest",port);
            accepterCurrentProposal = reciveProposal;
        }

        if(respondProposal != null){
            try {
                Socket socket = new Socket("localhost", reciveProposal.getSenderPort());

                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream); 

                writer.println(respondProposal.toString());
                writer.flush();

                writer.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }     
        } 
    }

    private synchronized void proposerAcceptRequest(Proposal reciveProposal){
        proposerPromises.add(reciveProposal);
        proposalPromisedCount++;

        if(isProposalPhasetwo == false && proposalPromisedCount >= Utils.MAX_MEMBER_COUNT/2){
            isProposalPhasetwo = true;
            int maxProposalNumber = -2;
            int maxProposalValue = -2 ;

            if(proposerPromises.size() == 0){
                maxProposalNumber = Utils.convertStringToInt(ProcessHandle.current().pid() + memberName);
                maxProposalValue = id;
            }else{
                for(Proposal proposal : proposerPromises){
                    if(proposal.getProposalNumber() > maxProposalNumber){
                        maxProposalNumber = proposal.getProposalNumber();
                        maxProposalValue = proposal.getProposalValue();
                    }
                }
            }

            Proposal acceptRequestProposal = new Proposal(maxProposalNumber,maxProposalValue,"AcceptRequest",port);
            try {
                Socket socket = new Socket("localhost", broadcastServerPort);

                OutputStream outputStream = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream);

                writer.println(acceptRequestProposal.toString());
                writer.flush();

                writer.close();
                outputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptorDecision(Proposal reciveProposal){
        try {
            Socket socket = new Socket("localhost", broadcastServerPort);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            Proposal decisionProposal = new Proposal(reciveProposal.getProposalNumber(),reciveProposal.getProposalValue(),"Decision",port);

            writer.println(decisionProposal.toString());
            writer.flush();

            writer.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void learnerListenDecision(Proposal receiveProposal) {
        int proposalKey = 0;

        if (learnerDecision.containsKey(receiveProposal)) {
            proposalKey = learnerDecision.get(receiveProposal) + 1;
        }

        learnerDecision.put(receiveProposal, proposalKey);

        // Check if any proposal's occurrence exceeds the majority
        for (Map.Entry<Proposal, Integer> entry : learnerDecision.entrySet()) {
            if (entry.getValue() > Utils.MAX_MEMBER_COUNT / 2) {
                System.out.println("Proposal " + entry.getKey() + " has occurred more than the majority.");
            }
        }
    }

    

    public static void main(String[] args) {

        Thread testThread = new Thread(() -> {
            try (ServerSocket testServerSocket = new ServerSocket(9000)) {
                System.out.println("Test Socket server started on port 9000");

                while (true) {
                    Socket testClientSocket = testServerSocket.accept();
                    System.out.println("Test: New connection accepted from " + testClientSocket.getInetAddress());

                    InputStream testInputStream = testClientSocket.getInputStream();
                    BufferedReader testReader = new BufferedReader(new InputStreamReader(testInputStream));
                    String testMessage = testReader.readLine();
                    System.out.println("Test: Received message: " + testMessage);

                    testReader.close();
                    testInputStream.close();
                    testClientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        testThread.start();

        PaxosParticipant participant = new PaxosParticipant(1, 9010);

        Thread participantThread = new Thread(() -> {
            participant.startListening();
        });

        participantThread.start();
        participant.prepareRequest();
    }
}


