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
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR", "TOBE"
    );

    // Server IP and Port
    private static final String IP = "localhost";
    private static final int PORT = 6666;
    private String inputCommands;
    // Sockets and IO streams
    private Socket clientSocket;
    // Handlers
    private ConnectionHandler connectionHandler;
    private FilesHandler filesHandler;
    private BufferedReader terminalReader;


    public Client() {
        // Create the socket, connection handler and files handler
        try {
            clientSocket = new Socket(IP, PORT);
            connectionHandler = new ConnectionHandler(clientSocket);
            filesHandler = new FilesHandler("Client/Files", "Client/Configs");
            terminalReader = new BufferedReader(new InputStreamReader(System.in));
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
        client.run();
    }

    public void run() {
        boolean correctInput;

        while (running) {
            correctInput = false;
            displayMessage();
            // decode input
            while (!correctInput) {
                if (DEBUG) System.out.println("Collecting commands");
                correctInput = decodeTokens(tokenizeInput());
            }
            // Send the command over
            if (running) sendCommands();
        }
        System.out.println("Client Session ended.");
    }

    private void displayMessage() {
        if (DEBUG) System.out.println("Waiting for incoming message");
        if (connectionHandler.readIncoming()) {
            System.out.println(connectionHandler.getIncomingMessage());
        } else {
            closeConnection();
        }
    }

    private String retrieveMessage() {
        if (connectionHandler.readIncoming()) {
            return connectionHandler.getIncomingMessage();
        } else {
            closeConnection();
            return "";
        }
    }


    private String[] tokenizeInput() {
        try {
            inputCommands = terminalReader.readLine();
            return inputCommands.split("\\s+");
        } catch (IOException e) {
            System.out.println("Can't read input from terminal, ending session.");
            closeConnection();
            return new String[0];
        }
    }

    /**
     * main function that decodes what commands are entered and responds accordingly
     * Reads the terminal (system.in) and runs appropriate method accordingly.
     */
    private boolean decodeTokens(String[] tokens) {
        // Selecting which command to run
        if (availableCommands.contains(tokens[0].toUpperCase())) {
            switch (tokens[0].toUpperCase()) {
                case "USER":
                    return user(tokens);
                case "ACCT":
                    return acct(tokens);
                case "PASS":
                    return pass(tokens);
                case "TYPE":
                    return type(tokens);
                case "LIST":
                    return list(tokens);
                case "CDIR":
                    return cdir(tokens);
                case "KILL":
                    return kill(tokens);
                case "NAME":
                    return name(tokens);
                case "TOBE":
                    return tobe(tokens);
                case "DONE":
                    return done(tokens);
                case "RETR":
                    return retr(tokens);
                case "STOR":
                    break;
                default:
                    break;
            }
        } else {
            System.out.println("Not a valid command, please choose from:\nUSER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
            return false;
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
     * @param tokens command line args
     */
    private boolean user(String[] tokens) {
        // check tokens is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("USER command format : USER <user name>");
            return false;
        }
        return true;
    }

    private boolean acct(String[] tokens) {
        // check commands is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("ACCT command format : ACCT <account name>");
            return false;
        }
        return true;
    }

    private boolean pass(String[] tokens) {
        // check commands is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("PASS command format : PASS <password>");
            return false;
        }
        return true;
    }

    private boolean type(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("TYPE command format : TYPE { A | B | C }");
            return false;
        }
        return true;
    }

    private boolean list(String[] tokens) {
        if (tokens.length != 2 && tokens.length != 3) {
            System.out.println("LIST command format : LIST { F | V } <directory-path>. check README for specific directory path syntax");
            return false;
        }

        if (!(tokens[1].toUpperCase().equals("V") || tokens[1].toUpperCase().equals("F"))) {
            System.out.println("Mode can only be V or F");
            return false;
        }
        return true;
    }

    private boolean cdir(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("LIST command format : LIST { F | V } <directory-path> , check README for specific directory path syntax");
            return false;
        }
        return true;
    }

    private boolean kill(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("KILL command format : KILL <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    private boolean name(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("NAME command format : NAME <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    private boolean tobe(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("TOBE command format : TOBE <new file name>");
            return false;
        }
        return true;
    }

    private boolean done(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("DONE command format : DONE");
            return false;
        }
        // send the DONE command and wait for a response
        sendCommands();
        if (connectionHandler.readIncoming()) {
            String msg = connectionHandler.getIncomingMessage();
            if (msg.charAt(0) == '+') {
                System.out.println(msg);
                closeConnection();
            }
        }
        return true;
    }

    private boolean retr(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("RETR command format : RETR <filename>");
            return false;
        }

        String fileName = tokens[1];
        // check if
        if (fileName.contains(File.separator)) {
            System.out.println("File name should not contain directories");
            return false;
        }
        sendCommands();

        // Either get fileSize or error message
        String response = retrieveMessage();
        // Error message
        if (response.charAt(0) == '-') {
            return true;
        }


        // Receive Message
        System.out.println(response);
        long fileSize = Long.parseLong(response);
        File file = filesHandler.generateFile(fileName);

        System.out.println("file size" + fileSize);
        System.out.println(file);

        // Get input should be stop or tobe
        String[] tks;
        boolean okInput = false;
        while (!okInput) {

            tks = tokenizeInput();

            switch (tks[0].toUpperCase()) {
                case "SEND" -> {
                    if (tks.length == 1) {
                        sendCommands();
                        displayMessage();

                        okInput = true;
                        try {
                            connectionHandler.receiveFile(file, fileSize, false);
                        } catch (IOException e) {
                            System.out.println("Cannot receive File, no access to hard drive?");
                        }
                    } else {
                        System.out.println("SEND command format : SEND");
                    }
                }
                case "STOP" -> {
                    if (tks.length == 1) {
                        sendCommands();
                        okInput = true;
                    } else {
                        System.out.println("STOP command format : STOP");
                    }
                }
                default -> {
                    System.out.println("Only can accept SEND or STOP at this point.");
                }
            }
        }
        return true;
    }
}
