package Server;

import Utils.ConnectionHandler;
import Utils.CredentialsHandler;
import Utils.FilesHandler;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Set;

/**
 * This is a single instance of a server.
 */
// todo change response code and message, and nested ifs and CDIR
public class ServerInstance implements Runnable {
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );
    private final Socket connectionSocket;
    private String responseMessage;
    private ConnectionHandler connectionHandler;
    private final FilesHandler filesHandler;
    private final CredentialsHandler credentialsHandler;
    private final boolean DEBUG = true;
    private String transferType = "B";
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
        connectionHandler.sendMessage("+MCHE226 SFTP Service");

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
        if (connectionHandler.readIncoming()) {
            if (DEBUG) System.out.println("message : " + connectionHandler.getIncomingMessage());

            commands = connectionHandler.getIncomingMessage().split("\\s+");
            switch (commands[0].toUpperCase()) {
                case "USER":
                    user(commands[1]);
                    break;
                case "ACCT":
                    acct(commands[1]);
                    break;
                case "PASS":
                    pass(commands[1]);
                    break;
                case "TYPE":
                    type(commands[1]);
                    break;
                case "LIST":
                    list(Arrays.copyOfRange(commands, 1, commands.length));
                    break;
                case "CDIR":
                    cdir(commands[1]);
                    break;
                case "KILL":
                    kill(commands[1]);
                    break;
                case "NAME":
                    name(commands[1]);
                    break;
                case "DONE":
                    done();
                    break;
                case "RETR":
                    retr(commands[1]);
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
            closeConnection();
        }
    }

    /**
     * Sends the response in ascii.
     */
    private void sendResponse() {
        if (!(this.connectionHandler.sendMessage(responseMessage))) {
            closeConnection();
        }
    }

    private void sendFile(File file) {
        if (!(connectionHandler.sendFile(file))) {
            closeConnection();
        }
    }


    /**
     * Closes stops the program and closes the connection safely.
     */
    public void closeConnection() {
        try {
            this.running = false;
            this.connectionSocket.close();
            System.out.println("Closing Server instance");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ******************************************
    // command functions
    // ******************************************

    private void user(String user) {
        credentialsHandler.checkUser(user);
        switch (credentialsHandler.getLoginState()) {

            case INIT -> {
                responseMessage = "-Invalid user-id, try again";
            }
            case USER_FOUND, ACCT_FOUND, PASS_FOUND -> {
                responseMessage = "+User-id valid, send account and password";
            }
            case LOGGED_IN -> {
                responseMessage = "!" + user + " logged in";
            }
        }
        sendResponse();
    }

    private void acct(String acct) {
        credentialsHandler.checkAcct(acct);
        switch (credentialsHandler.getLoginState()) {
            case INIT, USER_FOUND, PASS_FOUND -> {
                responseMessage = "-Invalid Account, try again";
            }
            case ACCT_FOUND -> {
                responseMessage = "+Account ok or not needed. Send your password next";
            }
            case LOGGED_IN -> {
                responseMessage = "!Account was ok or not needed. Skip the password.";
            }
        }
        sendResponse();
    }

    private void pass(String pass) {

        credentialsHandler.checkPass(pass);

        switch (credentialsHandler.getLoginState()) {
            case INIT, USER_FOUND, ACCT_FOUND -> {
                responseMessage = "-Wrong password, try again";
            }
            case PASS_FOUND -> {
                responseMessage = "+Password ok but you haven't specified the account";
            }
            case LOGGED_IN -> {
                responseMessage = "!Password is ok and you can begin file transfers.";
            }
        }
        sendResponse();
    }

    // ******************
    // These functions need to be logged in
    // ******************


    /**
     * Used to select the transfer type
     *
     * @param type
     */
    private void type(String type) {
        if (loggedIn()) {
            switch (type.toUpperCase()) {
                case "A" -> {
                    transferType = "A";
                    responseMessage = "+Using Ascii mode";
                }
                case "B" -> {
                    transferType = "B";
                    responseMessage = "+Using Binary mode";
                }
                case "C" -> {
                    transferType = "C";
                    responseMessage = "+Using Continuous mode";
                }
                default -> {
                    responseMessage = "-Type not valid";
                }
            }
        } else {
            responseMessage = "-You need to be logged in to use TYPE";
        }
        sendResponse();
    }

    /**
     * @param commands
     */
    private void list(String[] commands) {
        if (loggedIn()) {
            if (commands.length == 1) {
                // No dir, will list previous dir
                responseMessage = filesHandler.listFiles(commands[0]);
            } else if (commands.length == 2) {
                // dir included
                responseMessage = filesHandler.listFiles(commands[0], commands[1]);
            }
            if (responseMessage != null) {
                responseMessage = "+".concat(responseMessage);
            } else {
                responseMessage = "-Directory invalid";
            }
        } else {
            responseMessage = "-You need to be logged in to use LIST";
        }
        sendResponse();
    }

    private void cdir(String dir) {
        // Check if is logged in
        if (loggedIn()) {
            // See if change dir has been successful
            if (filesHandler.changeDir(dir)) {
                responseMessage = "!Changed working dir to ".concat(String.valueOf(filesHandler.getCurrentPath()));
            } else {
                responseMessage = "-Cannot connect to directory because directory does not exist or you do not have permission";
            }
        } else {
            // todo more cdir vs login shit.
            responseMessage = "-Cannot connect to directory because you are not logged in";
        }
        sendResponse();
    }

    private void kill(String fileName) {
        if (loggedIn()) {
            responseMessage = filesHandler.kill(fileName);
        } else {
            responseMessage = "-Cannot use kill because you're not logged in";
        }
        sendResponse();
    }

    private void name(String fileName) {
        if (loggedIn()) {
            if (filesHandler.fileExist(fileName)) {
                // check if specified file exists
                responseMessage = "+File Exists";
                sendResponse();
                // wait for tobe with new file name
                if (connectionHandler.readIncoming()) {
                    String[] commands = connectionHandler.getIncomingMessage().split("\\s+");
                    if (commands[0].toUpperCase().equals("TOBE")) {
                        // attempt to change and send response
                        responseMessage = filesHandler.rename(fileName, commands[1]);
                    } else {
                        responseMessage = "-Name change aborted as you need to send TOBE with new file name";
                    }
                    sendResponse();
                }
            } else {
                // cannot find file.
                responseMessage = String.format("-File wasn't renamed because %s does not exist", fileName);
                sendResponse();
            }
        } else {
            // not logged in
            responseMessage = "-You need to be logged in to use name";
            sendResponse();
        }
    }

    private void retr(String fileName) {
        // Check if logged in
        if (!loggedIn()) {
            responseMessage = "-You need to be logged into use retr";
            sendResponse();
            return;
        }
        // Check if file exists
        if (!filesHandler.fileExist(fileName)) {
            responseMessage = "-File doesn't exist";
            sendResponse();
            return;
        }


        // Send file size over
        File file = new File(filesHandler.getCurrentPath().toAbsolutePath() + File.separator + fileName);
        responseMessage = String.valueOf(file.length());
        sendResponse();

        // Check if they want to accept file
        String[] commands = getCommands();
        switch (commands[0].toUpperCase()) {
            case "SEND" -> {
                responseMessage = "+Sending, this might take a while depending on size";
                sendResponse();

                sendFile(file);
                responseMessage = "+Ok file sent";
                sendResponse();
            }
            case "STOP" ->{
                responseMessage = "+ok, RETR aborted";
                sendResponse();
            }default -> {
                responseMessage = "-Unrecognised command, RETR aborted";
                sendResponse();
            }
        }
    }

    private void done() {
        responseMessage = "+Thank you for using MCHE226 STFP service.";
        sendResponse();
        closeConnection();
    }

    private boolean loggedIn() {
        return (credentialsHandler.getLoginState() == CredentialsHandler.LoginState.LOGGED_IN);
    }

    private String[] getCommands() {
        if (connectionHandler.readIncoming()) {
            return connectionHandler.getIncomingMessage().split("\\s+");
        } else {
            closeConnection();
            return new String[0];
        }
    }
}
