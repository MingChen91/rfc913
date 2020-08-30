package Server;

import Utils.ConnectionHandler;
import Utils.CredentialsHandler;
import Utils.FilesHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;

/**
 * This is a single instance of a server.
 */
public class ServerInstance implements Runnable {
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );
    private Socket connectionSocket;
    private String responseCode;
    private String responseMessage;
    private ConnectionHandler connectionHandler;
    private FilesHandler filesHandler;
    private CredentialsHandler credentialsHandler;
    private final boolean DEBUG = true;

    private boolean running = true;

    /**
     * Takes in the socket allocated by the welcome socket
     * Creates the needed handler for sending and receiving over the connection
     * Creates the file handler used for writing and reading files
     *
     * @param connectionSocket socket allocated to this instance of server from the welcome socket
     */
    public ServerInstance(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
        this.filesHandler = new FilesHandler("Server/Files", "Server/Configs");
        try {
            this.connectionHandler = new ConnectionHandler(connectionSocket);
        } catch (IOException e) {
            closeConnection();
            System.out.println("Could not initiate connection handler in server instance. Terminating server");
        }
        credentialsHandler = new CredentialsHandler(filesHandler.getConfigFilePath("userInfo.csv"));
    }

    /**
     * Main Runnable, loops and listens for commands.
     */
    @Override
    public void run() {
        String a = "";
        boolean readOK;
        // Read input from Client
        System.out.println("Server instance started");
        // Send server online message
        connectionHandler.sendAscii("+MCHE226 SFTP Service");

        while (running) {
            decodeCommands();
        }
        System.out.println("Server instance terminated");
    }


    /**
     *
     */
    private void decodeCommands() {
        String[] commands;
        if (connectionHandler.readAscii()) {
            if(DEBUG) System.out.println("message received" + connectionHandler.getIncomingMessage());

            commands = connectionHandler.getIncomingMessage().split("\\s+");
            switch (commands[0].toUpperCase()) {
                case "USER":
                    user(commands[1]);
                    System.out.println(commands[1]);
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
        }
        sendResponse();
    }

    /**
     * Sends the response in ascii.
     */
    private void sendResponse() {
        this.connectionHandler.sendAscii(responseCode + responseMessage);
    }


    /**
     * Closes stops the program and closes the connection safely.
     */
    public void closeConnection() {
        try {
            this.running = false;
            this.connectionSocket.close();
            System.out.println("Server instance ended");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // *******************************************
    // command functions
    // ******************************************

    private void user(String user) {
        credentialsHandler.checkUser(user);
        switch (credentialsHandler.getState()) {

            case INIT -> {
                responseCode = "-";
                responseMessage = "Invalid user-id, try again";
            }
            case USER_FOUND, ACCT_FOUND -> {
                responseCode = "+";
                responseMessage = "User-id valid, send account and password";
            }
            case LOGGED_IN -> {
                responseCode = "!";
                responseMessage = user + " logged in";
            }
        }
    }
}