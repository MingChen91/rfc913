package Client;

import Utils.ConnectionHandler;
import Utils.FilesHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Single instead of the client used in implementing RFC-913 SFTP
 */
public class Client {
    // Infinite loop boolean
    private boolean running = true;
    // Flag to prevent cdir sending pass/acct twice
    // Needed because you have to check the response from server before you can exit
    // cdir.
    private boolean cdirFlag = false;

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

    /**
     * Constructor for the client, initializes all the handlers.
     */
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
     * Main function, starts a client.
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    /**
     * Main loop for the client
     */
    public void run() {
        boolean validCommand;
        while (running) {
            // CDIR flag is needed because with cdir when sending over acct/pass,
            // you have to check response before you can finish
            // Display message
            if (!cdirFlag)
                displayServerMessage();
            cdirFlag = false;
            validCommand = false;
            // Gets input from terminal and selects the correct command to run
            while (!validCommand) {
                validCommand = decodeTokens(tokenizeInput());
            }
            // Send the command over
            if ((running) && !cdirFlag)
                sendCommands();
        }
        System.out.println("Client Session ended.");
    }

    // ***********************
    // Helper functions
    // ***********************

    /**
     * Displays the message from Server
     */
    private void displayServerMessage() {
        System.out.println(retrieveMessage());
    }

    /**
     * Retrieves the message from Server
     *
     * @return String - Message from the server
     */
    private String retrieveMessage() {
        if (connectionHandler.readIncoming()) {
            return connectionHandler.getIncomingMessage();
        } else {
            closeConnection();
            return "";
        }
    }

    /**
     * Gets input from terminal and splits into tokens
     *
     * @return tokenized input
     */
    private String[] tokenizeInput() {
        try {
            inputCommands = terminalReader.readLine();
            return inputCommands.split("\\s+");
        } catch (IOException e) {
            System.out.println("Can't read input from terminal, ending session.");
            closeConnection();
            String[] failed = new String[] { "" };
            return failed;
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

    /**
     * Stops the program and closes the connection safely.
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
     * Checks the first token in the input command and selects the correct command
     * to run
     */
    private boolean decodeTokens(String[] tokens) {
        // Selecting which command to run
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
                return stor(tokens);
            default:
                System.out.println(
                        "Not a valid command, please choose from:\nUSER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
                return false;
        }
    }

    // ***********************
    // Basic Command functions
    // Checks correct amount of tokens are present then sends the command over
    // ***********************

    /**
     * Checks user command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean user(String[] tokens) {
        // check tokens is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("USER command format : USER <user name>");
            return false;
        }
        return true;
    }

    /**
     * Checks acct command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean acct(String[] tokens) {
        // check commands is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("ACCT command format : ACCT <account name>");
            return false;
        }
        return true;
    }

    /**
     * Checks pass command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean pass(String[] tokens) {
        // check commands is exactly 2 fields.
        if (tokens.length != 2) {
            System.out.println("PASS command format : PASS <password>");
            return false;
        }
        return true;
    }

    /**
     * Checks type command has correct amount of tokens, and the argument is either
     * A or B or C Displays error message if not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean type(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("TYPE command format : TYPE { A | B | C }");
            return false;
        }
        return true;
    }

    /**
     * Checks list command has correct amount of tokens, and the argument is either
     * V or F Displays error message if not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean list(String[] tokens) {
        if (tokens.length != 2 && tokens.length != 3) {
            System.out.println(
                    "LIST command format : LIST { F | V } <directory-path>. check README for specific directory path syntax");
            return false;
        }
        if (!(tokens[1].toUpperCase().equals("V") || tokens[1].toUpperCase().equals("F"))) {
            System.out.println("Mode can only be V or F");
            return false;
        }
        return true;
    }

    /**
     * Checks cdir command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean cdir(String[] tokens) {
        // Check token lengths
        if (tokens.length != 2) {
            System.out.println(
                    "CDIR command format : <directory-path> , check README for specific directory path syntax");
            return false;
        }

        // Send command over
        String response;
        String[] tks;
        boolean okInput;
        while (true) {
            sendCommands();
            response = retrieveMessage();
            if (response.charAt(0) != '+') {
                // Breaks the loop when something other than "+" received
                System.out.println(response);
                cdirFlag = true; // flag to prevent pass / account sending twice
                break;
            }
            System.out.println(response);
            // + send pass or account
            // Loop until pass or acct command entered
            okInput = false;
            while (!okInput) {
                tks = tokenizeInput();
                if (tks[0].equalsIgnoreCase("pass")) {
                    okInput = pass(tks);
                } else if (tks[0].equalsIgnoreCase("acct")) {
                    okInput = pass(tks);
                } else {
                    System.out.println("please send pass or account over");
                }
            }
        }
        return true;
    }

    /**
     * Checks kill command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean kill(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("KILL command format : KILL <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    /**
     * Checks name command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean name(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("NAME command format : NAME <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    /**
     * Checks tobe command has correct amount of tokens, Displays error message if
     * not
     *
     * @param tokens tokenized input from command line
     * @return Boolean, true if command is ok, false if tokens not in correct format
     */
    private boolean tobe(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("TOBE command format : TOBE <new file name>");
            return false;
        }
        return true;
    }

    // **********************
    // Advanced Commands
    // Commands that require some more checking or depends on servers responses
    // **********************

    /**
     * Checks the done command has the right about of arguments. If so sends it over
     * then waits for a +response Shuts down the client
     *
     * @param tokens tokenized input from command line
     * @return True if successfully finished, false if tokens incorrect
     */
    private boolean done(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("DONE command format : DONE");
            return false;
        }
        // send the DONE command and wait for a response
        sendCommands();
        String msg = retrieveMessage();
        // Response ok from server, close this instance of client.
        if (msg.charAt(0) == '+') {
            System.out.println(msg);
            closeConnection();
        }
        return true;
    }

    /**
     * Performs checks on the tokens, then attempts to retrieve a file from server
     *
     * @param tokens tokenized input from command line
     * @return True if exited successfully, false if something wrong with command
     *         entered.
     */
    private boolean retr(String[] tokens) {
        // Checking tokens length
        if (tokens.length != 2) {
            System.out.println("RETR command format : RETR <filename>");
            return false;
        }

        String fileName = tokens[1];
        // check if file name is valid
        if (fileName.contains(File.separator)) {
            System.out.println("File name should not contain directories");
            return false;
        }
        // Send the retr command over
        sendCommands();

        // Either get fileSize or error message
        String response = retrieveMessage();
        // Error message
        if (response.charAt(0) == '-') {
            return true;
        }
        // Display size
        System.out.println(response);
        // Retrieve file
        File file = filesHandler.generateFile(fileName, false);
        long fileSize = Long.parseLong(response);

        // Get input, should be stop or tobe
        String[] tks;
        boolean okInput = false;
        // Loops until valid input
        while (!okInput) {
            tks = tokenizeInput();
            switch (tks[0].toUpperCase()) {
                case "SEND" -> okInput = send(tks, file, fileSize);
                case "STOP" -> okInput = stop(tks);
                default -> System.out.println("Only can accept SEND or STOP at this point.");
            }
        }
        return true;
    }

    /**
     * Used only from within retr, Checks if stop command is in the right format
     * Tells server to not send file
     *
     * @param tokens Input tokens from command line
     * @return True if valid tokens, false if not.
     */
    private boolean stop(String[] tokens) {
        if (tokens.length != 1) {
            System.out.println("STOP command format : STOP");
            return false;
        }
        sendCommands();
        return true;
    }

    /**
     * Used only from within retr, Checks if send command is in right format Tells
     * the server to send the file.
     *
     * @param tokens   Input tokens from command line
     * @param file     where the received file is to be saved
     * @param fileSize size of the file to be transferred
     * @return true if successful, false if error in the command tokens
     */
    private boolean send(String[] tokens, File file, Long fileSize) {
        if (tokens.length != 1) {
            System.out.println("SEND command format : SEND");
            return false;
        }
        // Send Message over
        sendCommands();
        displayServerMessage();
        try {
            connectionHandler.receiveFile(file, fileSize, false);
        } catch (IOException e) {
            System.out.println("Cannot receive File, no access to hard drive?");
        }
        return true;
    }

    /**
     * Used to send a file to the server.
     *
     * @param tokens input tokens from command line
     * @return true if successful, false if error in command tokens
     */
    private boolean stor(String[] tokens) {
        String mode;
        String fileName;
        // Check tokens length
        if (tokens.length != 3) {
            System.out.println("STOR command format : Store { NEW | OLD | APP } <fileName>");
            return false;
        }
        mode = tokens[1].toUpperCase();
        fileName = tokens[2];
        if (!(mode.equals("NEW") || (mode.equals("OLD")) || (mode.equals("APP")))) {
            System.out.println("STOR command format : Store { NEW | OLD | APP } <fileName>");
        }

        // File name should not have directories
        if (fileName.contains(File.separator)) {
            System.out.println("File name shouldn't include directories");
            return false;
        }

        // Check if source file exists
        if (!filesHandler.fileExist(fileName)) {
            System.out.println("Cannot find file in source folder.");
            return false;
        }

        File file = filesHandler.generateFile(fileName, false);
        // Check if file is actually a file or dir
        if (!file.isFile()) {
            System.out.println("Can't find file");
            return false;
        }

        // -- Basic checks done, send stor command --
        sendCommands();
        String response = retrieveMessage();
        if (response.charAt(0) == '-') {
            // error from server side about file, exit.
            return true;
        }
        System.out.println(response);
        // -- Send file size
        inputCommands = "SIZE " + (file.length());
        sendCommands();

        // -- Check if server has enough space.
        response = retrieveMessage();
        if (response.charAt(0) == '-') {
            return true;
        }
        System.out.println(response);

        // Sends file over, If connection bad close client.
        if (!connectionHandler.sendFile(file)) {
            closeConnection();
        }

        return true;
    }
}
