package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Set;


/**
 * Single instead of the client used in implementing RFC-913 SFTP
 */
public class Client {
    // debug boolean
    private static boolean DEBUG = false;
    private static boolean FINISHED = false;
    // Available Commands
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );

    // Server IP and Port
    private static final String IP = "localhost";
    private static final int PORT = 115;
    // Sockets and IO streams
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;


    /**
     * Main
     *
     */
    public static void main(String[] args) throws IOException {
        init();
        while (true) {
            decodeCommands();
            if (FINISHED) {
                break;
            }
        }
        clientSocket.close();
    }

    /**
     * Initials file folder and sockets.
     */
    private static void init(){
        // Folder used for storing files
        Utils.FileIO.createFtpFolder("Client/Files");
        // Socket and IO streams
        try {
            clientSocket = new Socket(IP, PORT);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader((clientSocket.getInputStream())));
            System.out.println("Client connecting to IP " + IP + "Port " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating client socket or creating");
        }

    }
    /**
     * Monitors the inputStream of the clientSocket until a '\0' null terminator is received.
     * Blocks until null is received
     *
     * @return String read from server
     */
    private static String readFromServer() {
        StringBuilder incomingMessage = new StringBuilder();
        int incomingChar = 0;

        while (true) {
            // Read incoming character
            try {
                incomingChar = inFromServer.read();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error reading message from server. Connection lost");
            }

            // Break out of blocking loop if null terminator
            if ((char) incomingChar == '\0') {
                if (incomingMessage.length() > 0) {
                    break;
                }
            } else {
                incomingMessage.append((char) incomingChar);
            }
        }
        return incomingMessage.toString();
    }

    /**
     * Appends a null terminator and sends a message to the server ,
     *
     * @param outgoingMessage message to the server
     */
    private static void sendToServer(String outgoingMessage) {
        try {
            outToServer.writeBytes(outgoingMessage + '\0');
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error sending message to server. Connection lost");
        }

    }

    private static void decodeCommands() throws IOException {

        // Reading input
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(System.in));
        String inputCommands = inputStreamReader.readLine();
        String[] commands = inputCommands.split("\\s+");

        if (availableCommands.contains(commands[0].toUpperCase())) {
            switch (commands[0].toUpperCase()) {
                case "USER":
                    sendToServer("hello");
                    break;
                case "ACCT":

                    break;
                case "PASS":

                    break;
                case "TYPE":

                    break;
                case "LIST":

                    break;
                case "CDIR":

                    break;
                case "KILL":

                    break;
                case "NAME":

                    break;
                case "TOBE":

                    break;
                case "DONE":
                    break;
                case "RETR":

                    break;
                case "SEND":

                    break;
                case "STOP":

                    break;
                case "STOR":

                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Not a valid command, please choose from:");
            System.out.println("USER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
        }


    }


}
