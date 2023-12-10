import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class Utils {

    public static void log(String message) {

        try {
            Socket socket = new Socket("localhost", 10001);

            OutputStream outputStream = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(outputStream, false);

            // Send the proposal message
            writer.println(message);

            // Close the socket and streams
            writer.flush();
            writer.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {

        }
    }

    public static Proposal fromString(String str) {
        String[] parts = str.split("/");
        int proposalNumber = Integer.parseInt(parts[0]);
        int proposalValue = Integer.parseInt(parts[1]);
        String messageType = parts[2];
        int senderPort = Integer.parseInt(parts[3]);
        return new Proposal(proposalNumber, proposalValue, messageType, senderPort);
    }

    private static final Map<String, Integer> stringToIntMap = new HashMap<>();
    private static int nextAvailableInt = 0;
    
    public static int convertStringToInt(String inputString) {
        if (stringToIntMap.containsKey(inputString)) {
            return stringToIntMap.get(inputString);
        } else {

            int newInt = nextAvailableInt++;
            stringToIntMap.put(inputString, newInt);
            return newInt;
        }
    }
}

