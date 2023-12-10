import java.io.*;
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
    private int delay;


    public PaxosParticipant(int id, int port, int delay) {
        this.id = id;
        this.port = port;
        this.delay = delay;

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

                // Simulate a delay
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Create a new thread to handle the client request
                Thread thread = new Thread(() -> {
                    try {
                        InputStream inputStream = clientSocket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        String message = reader.readLine();
                        System.out.println("m"+id+" Received message: " + message);

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
            Utils.log("m"+id+" recive promise");           
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
        if (reciveProposal.getProposalNumber() != -1) {
            proposerPromises.add(reciveProposal);
        }
        
        proposalPromisedCount++;

        if(isProposalPhasetwo == false && proposalPromisedCount >= PaxosBroadcastServer.MAX_MEMBER_COUNT/2){
            isProposalPhasetwo = true;
            int maxProposalNumber = -100;
            int maxProposalValue = -100;

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
            Utils.log("m"+id+" send decision");
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
        boolean isValueAccepted = false;
        for (Proposal proposal : learnerDecision.keySet()) {
            if (proposal.getProposalValue() == receiveProposal.getProposalValue()) {
                int count = learnerDecision.get(proposal);
                learnerDecision.put(proposal, count + 1);
                isValueAccepted = true;
                break;
            }
        }
        if (!isValueAccepted) {
            learnerDecision.put(receiveProposal, 1);
        }

        for (Proposal proposal : learnerDecision.keySet()) {
            int count = learnerDecision.get(proposal);
            if (count >=  PaxosBroadcastServer.MAX_MEMBER_COUNT / 2) {
                // Value accepted by majority, take action here
                System.out.println("Value " + proposal.getProposalValue() + " accepted by " + "m"+id);
                Utils.log("m"+id+" Value " + proposal.getProposalValue() + " accepted by " + "m"+id);

            }
        }
    }

    public int getId() {
        return id;
    }


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PaxosParticipant [id] [portnumber] [delay]");
            return;
        }

        int id = Integer.parseInt(args[0]);
        int portNumber = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);

        PaxosParticipant participant = new PaxosParticipant(id, portNumber, delay);
        
        Thread testThread = new Thread(() -> {
            participant.startListening();
        });
        testThread.start();

        if(id <= 3){
            participant.prepareRequest();
        }
    }
}

