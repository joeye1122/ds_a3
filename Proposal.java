/**
 * The Proposal class represents a proposal with a proposal number and value.
 */
public class Proposal {
    private String messageTyep;
    private int proposalNumber;
    private int proposalValue;
    private int senderPort;

    /**
     * Constructs a Proposal object with the specified proposal number and value.
     *
     * @param proposalNumber the proposal number
     * @param proposalValue the proposal value
     */
    public Proposal(int proposalNumber, int proposalValue, String messageTyep, int senderPort) {
        this.proposalNumber = proposalNumber;
        this.proposalValue = proposalValue;
        this.messageTyep = messageTyep;
        this.senderPort = senderPort;
    }

    public int getProposalNumber() {
        return proposalNumber;
    }

    public int getProposalValue() {
        return proposalValue;
    }

    public String getMessageTyep() {
        return messageTyep;
    }

    public int getSenderPort() {
        return senderPort;
    }

    @Override
    public String toString() {
        return proposalNumber + "/" + proposalValue + "/" + messageTyep + "/" + senderPort;
    }

}