package Client;

import Utils.ConnectionHandler;
import Utils.FilesHandler;

import java.io.*;
import java.net.Socket;
import java.util.Set;


/**
 * Single instead of the client used in implementing RFC-913 SFTP
 */
public class Client {
    // Infinite loop boolean
    private boolean running = true;
    private final static boolean DEBUG = true;
    // Available Commands
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );

    // Server IP and Port
    private static final String IP = "localhost";
    private static final int PORT = 115;
    // Sockets and IO streams
    private Socket clientSocket;
    private ConnectionHandler connectionHandler;
    private FilesHandler filesHandler;


    public Client() {
        // Create the socket, connection handler and files handler
        try {
            clientSocket = new Socket(IP, PORT);
            connectionHandler = new ConnectionHandler(clientSocket);
            filesHandler = new FilesHandler("Client/Files", "Client/Configs");
        } catch (IOException e) {
            System.out.println("Error connecting to server, is the server online?");
            running = false;
        }

    }

    /**
     * Main
     */
    public static void main(String[] args) {
        Client client = new Client();
        boolean correctInput;
        while (client.running) {
            correctInput = false;
            // print out incoming message
            if (DEBUG) System.out.println("Reading Message");
            if (client.connectionHandler.readAscii()) {
                System.out.println(client.connectionHandler.getIncomingMessage());
            }
            // decode input
            while(!correctInput){
                if (DEBUG) System.out.println("Collecting commands");
                correctInput = client.decodeCommands();
            }

        }
    }

    /**
     * main function that decodes what commands are entered and responds accordingly
     * Reads the terminal (system.in) and runs appropriate method accordingly.
     */
    private boolean decodeCommands() {
        try {
            // Reading input
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(System.in));
            String inputCommands = inputStreamReader.readLine();
            String[] commands = inputCommands.split("\\s+");
            // Selecting which command to run
            if (availableCommands.contains(commands[0].toUpperCase())) {
                switch (commands[0].toUpperCase()) {
                    case "USER":
                        return user(commands);
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
                return false;
            }
        } catch (IOException e) {
            System.out.println("Can't read input from terminal, Should really never be here.");
            closeConnection();
        }
        return true;
    }

    /**
     * Sends User name over to the server.
     * @param commands command line args
     */
    private boolean user(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("USER command takes exactly 1 argument. Please try again");
            return false;
        }
        // Send user credentials, closes connection if cannot connect
        if (!(connectionHandler.sendAscii(commands[1]))) {
            System.out.println("USER details did not send due to connection error");
            closeConnection();
        }
        return true;
    }


    /**
     * Closes stops the program and closes the connection safely.
     */
    public void closeConnection() {
        try {
            this.running = false;
            this.clientSocket.close();
            System.out.println("Client Session Ended");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not close connection");
        }
    }
}
