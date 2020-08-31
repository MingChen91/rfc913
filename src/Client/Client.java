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
    private final static boolean DEBUG = false;
    // Available Commands
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );

    // Server IP and Port
    private static final String IP = "localhost";
    private static final int PORT = 115;
    private String inputCommands;
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
            client.displayMessage();
            // decode input
            while (!correctInput) {
                if (DEBUG) System.out.println("Collecting commands");
                correctInput = client.decodeCommands();
            }
            // Send the command over
            client.sendCommands();
        }
        System.out.println("Client Session ended.");
    }

    private void displayMessage() {
        if (DEBUG) System.out.println("Waiting for incoming message");
        if (connectionHandler.readAscii()) {
            System.out.println(connectionHandler.getIncomingMessage());
        } else {
            closeConnection();
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
            inputCommands = inputStreamReader.readLine();
            String[] commands = inputCommands.split("\\s+");
            // Selecting which command to run
            if (availableCommands.contains(commands[0].toUpperCase())) {
                switch (commands[0].toUpperCase()) {
                    case "USER":
                        return user(commands);
                    case "ACCT":
                        return acct(commands);
                    case "PASS":
                        return pass(commands);
                    case "TYPE":
                        return type(commands);
                    case "LIST":
                        return list(commands);
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
                System.out.println("Not a valid command, please choose from:\nUSER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
                return false;
            }
        } catch (IOException e) {
            System.out.println("Can't read input from terminal, ending session.");
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
            System.out.println("Closing Client Session");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not close connection");
        }
    }

    /**
     * Wrapper for sending commands. Closes connection if fails.
     */
    private void sendCommands() {
        if (!(connectionHandler.sendMessage(inputCommands))) {
            closeConnection();
        }
    }

    //***********
    // command functions
    //**********


    /**
     * Sends User name over to the server.
     *
     * @param commands command line args
     */
    private boolean user(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("USER command takes exactly 1 argument. Please try again");
            return false;
        }
        return true;
    }

    private boolean acct(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("ACCT command takes exactly 1 argument. Please try again");
            return false;
        }
        return true;
    }

    private boolean pass(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("Pass command takes exactly 1 argument. Please try again");
            return false;
        }
        return true;
    }

    private boolean type(String[] commands) {
        if (commands.length != 2) {
            System.out.println("Type command takes exactly 1 argument. Please try again");
            return false;
        }
        return true;
    }

    private boolean list(String[] commands) {
        if (commands.length != 2 && commands.length != 3) {
            System.out.println("List command takes 1 or 2 arguments. Please try again.");
            return false;
        }

        if (!(commands[1].toUpperCase().equals("V") || commands[1].toUpperCase().equals("F"))) {
            System.out.println("Mode can only be V or F");
            return false;
        }
        return true;
    }
}
