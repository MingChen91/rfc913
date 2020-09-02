package Server;

import Utils.ConnectionHandler;
import Utils.CredentialsHandler;
import Utils.FilesHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * This is a single instance of a server.
 */
public class ServerInstance implements Runnable {
    // Sockets
    private final Socket connectionSocket;
    // Handlers
    private ConnectionHandler connectionHandler;
    private CredentialsHandler credentialsHandler;
    private final FilesHandler filesHandler;
    private String responseMessage;
    private String transferType = "B";
    // Indicating Server is running
    private boolean running = true;

    /**
     * Takes in the socket allocated by the welcome socket Creates the needed
     * handler for sending and receiving over the connection Creates the file
     * handler used for writing and reading files
     *
     * @param connectionSocket socket allocated to this instance of server from the
     *                         welcome socket
     */
    public ServerInstance(Socket connectionSocket) {
        // Socket
        this.connectionSocket = connectionSocket;
        // Files handler
        this.filesHandler = new FilesHandler("Server/Files", "Server/Configs");
        // Connections handler
        try {
            credentialsHandler = new CredentialsHandler(filesHandler.getConfigFilePath("userInfo.csv"));
            connectionHandler = new ConnectionHandler(connectionSocket);
        } catch (IOException e) {
            closeConnection();
            System.out.println(e.getMessage());
        }
    }

    /**
     * Main Runnable, loops and listens for commands.
     */
    @Override
    public void run() {
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
     * Decodes the incoming commands from the client and determines appropriate
     * response
     */
    private void decodeCommands() {
        String[] commands;

        commands = tokenizedCommands();

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
            case "STOR":
                stor(commands[1], commands[2]);
                break;
            default:
                break;
        }

    }

    // ****************
    // Helper function
    // ****************

    /**
     * Checks if the client is logged in
     *
     * @return boolean True if user is logged in
     */
    private boolean isLoggedIn() {
        return (credentialsHandler.getLoginState() == CredentialsHandler.LoginState.LOGGED_IN);
    }

    /**
     * Reads the incoming message from the client and tokenize
     *
     * @return Tokenized commands
     */
    private String[] tokenizedCommands() {
        if (connectionHandler.readIncoming()) {
            return connectionHandler.getIncomingMessage().split("\\s+");
        } else {
            closeConnection();
            String[] failed = new String[] { "" };
            return failed;
        }
    }

    /**
     * Sends the response message to the client Wrapper to include error handling.
     */
    private void sendResponse() {
        if (!(this.connectionHandler.sendMessage(responseMessage))) {
            closeConnection();
        }
    }

    /**
     * Sends a file to the client, Wrapper to include error handling.
     *
     * @param file File to send.
     */
    private void sendFile(File file, String transferType) {
        if (!(connectionHandler.sendFile(file, transferType))) {
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

    /**
     * Checks if User is a valid user. Updates the login state and sends response.
     *
     * @param user user name
     */
    private void user(String user) {
        credentialsHandler.checkUser(user);
        switch (credentialsHandler.getLoginState()) {
            case INIT -> responseMessage = "-Invalid user-id, try again";
            case USER_FOUND, ACCT_FOUND, PASS_FOUND -> responseMessage = "+User-id valid, send account and password";
            case LOGGED_IN -> responseMessage = "!" + user + " logged in";
        }
        sendResponse();
    }

    /**
     * Checks if acct matches the user. Updates the login state and sends response.
     *
     * @param acct account name
     */
    private void acct(String acct) {
        credentialsHandler.checkAcct(acct);
        switch (credentialsHandler.getLoginState()) {
            case INIT, USER_FOUND, PASS_FOUND -> responseMessage = "-Invalid Account, try again";
            case ACCT_FOUND -> responseMessage = "+Account ok or not needed. Send your password next";
            case LOGGED_IN -> responseMessage = "!Account was ok or not needed. Skip the password.";
        }
        sendResponse();
    }

    /**
     * Checks if pass matches the usr Updates the login state and sends response
     *
     * @param pass password
     */
    private void pass(String pass) {
        credentialsHandler.checkPass(pass);
        switch (credentialsHandler.getLoginState()) {
            case INIT, USER_FOUND, ACCT_FOUND -> responseMessage = "-Wrong password, try again";
            case PASS_FOUND -> responseMessage = "+Password ok but you haven't specified the account";
            case LOGGED_IN -> responseMessage = "!Password is ok and you can begin file transfers.";
        }
        sendResponse();
    }

    // ******************
    // The following command functions need to be logged in
    // ******************

    /**
     * Used to select the transfer type
     *
     * @param type A , B , C to indicate transfer type
     */
    private void type(String type) {
        if (!isLoggedIn()) {
            responseMessage = "-You need to be logged in to use TYPE";
        } else {
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
                default -> responseMessage = "-Type not valid";
            }
        }
        sendResponse();
    }

    /**
     * Lists the current directory the file handler is in. V for verbose, F for
     * standard format. Lists current folder if no dir given. Else attempts to
     * change to that folder then lists.
     *
     * @param args 2 args, first is selecting V or F, second is dir name
     */
    private void list(String[] args) {
        if (!isLoggedIn()) {
            responseMessage = "-You need to be logged in to use LIST";
        } else {
            // Logged in
            try {
                if (args.length == 1) {
                    // No directory provided, will list previous dir
                    String mode = args[0];
                    responseMessage = filesHandler.listFiles(mode);
                } else if (args.length == 2) {
                    // dir included, switch to that dir then list
                    String mode = args[0];
                    String dir = args[1];
                    responseMessage = filesHandler.listFiles(mode, dir);
                }
            } catch (FileNotFoundException e) {
                // Cannot find Dir
                responseMessage = e.getMessage();
            }
        }
        sendResponse();
    }

    /**
     * Changes the you're directly in See readme for specific syntax
     *
     * @param dir directory name.
     */
    private void cdir(String dir) {
        // Check if is logged in
        switch (credentialsHandler.getLoginState()) {
            // Not logged in at all
            case INIT -> {
                responseMessage = "-Cannot use CDIR when not logged in";
                sendResponse();
                return;
            }
            // Somewhat logged in but not completed process
            case USER_FOUND, ACCT_FOUND, PASS_FOUND -> {
                // Check if the dir exists
                if (filesHandler.changeDir(dir)) {
                    responseMessage = "+directory ok, send account/password";
                    sendResponse();
                    // Not logged in so reset current path to default
                    filesHandler.resetCurrentPath();
                } else {
                    responseMessage = "-Can't connect to directory because directory does not exist or you have no permission";
                    sendResponse();
                    return;
                }
            }
            case LOGGED_IN -> {
                if (filesHandler.changeDir(dir)) {
                    // See if change dir has been successful
                    responseMessage = "!Changed working dir to ".concat(String.valueOf(filesHandler.getCurrentPath()));
                } else {
                    // not logged in , check if direct exists
                    responseMessage = "-Can't connect to directory because directory does not exist or you have no permission";
                }
                sendResponse();
                return;
            }
        }

        // -- Getting pass or account--
        String[] tks;

        // Keeps trying to get pass and account until ! or - response code.
        boolean plusRespCode = true;
        while (plusRespCode) {
            tks = tokenizedCommands();
            if (tks[0].equalsIgnoreCase("pass")) {
                // Check the password then see what the state is
                credentialsHandler.checkPass(tks[1]);
                switch (credentialsHandler.getLoginState()) {
                    case PASS_FOUND -> responseMessage = "+password ok send account";
                    case LOGGED_IN -> {
                        filesHandler.changeDir(dir);
                        responseMessage = "!Changed working dir to " + filesHandler.getCurrentPath();
                        plusRespCode = false;
                    }
                    default -> {
                        responseMessage = "-invalid password";
                        plusRespCode = false;
                    }
                }
            } else if (tks[0].equalsIgnoreCase("acct")) {
                // Check the account the see what the state is
                credentialsHandler.checkAcct(tks[1]);
                switch (credentialsHandler.getLoginState()) {
                    case ACCT_FOUND -> responseMessage = "+account ok send password";
                    case LOGGED_IN -> {
                        filesHandler.changeDir(dir);
                        responseMessage = "!Changed working dir to " + filesHandler.getCurrentPath();
                        plusRespCode = false;
                    }
                    default -> {
                        responseMessage = "-invalid account";
                        plusRespCode = false;
                    }
                }
            }
            sendResponse();
        }
    }

    /**
     * Deletes a file from current directory
     *
     * @param fileName File to delete.
     */
    private void kill(String fileName) {
        if (isLoggedIn()) {
            responseMessage = filesHandler.delete(fileName);
        } else {
            responseMessage = "-Cannot use kill because you're not logged in";
        }
        sendResponse();
    }

    /**
     * Renames a file in the current directory. Need to be used with tobe command
     * together.
     *
     * @param fileName File to change name.
     */
    private void name(String fileName) {
        // Check login status
        if (!isLoggedIn()) {
            responseMessage = "-You need to be logged in to use name";
            sendResponse();
            return;
        }
        // Check if file exists
        if (!filesHandler.fileExist(fileName)) {
            // cannot find file.
            responseMessage = String.format("-File wasn't renamed because %s does not exist", fileName);
            sendResponse();
            return;
        }

        responseMessage = "+File Exists";
        sendResponse();

        // wait for tobe with new file name
        String[] commands = tokenizedCommands();
        if (commands[0].toUpperCase().equals("TOBE")) {
            // attempt to change and send response
            responseMessage = filesHandler.rename(fileName, commands[1]);
        } else {
            responseMessage = "-Name change aborted as you need to send TOBE with new file name";
        }
        sendResponse();

    }

    /**
     * Used to retrieve a file from the server to the client.
     *
     * @param fileName file to retrieve
     */
    private void retr(String fileName) {
        // Check if logged in
        if (!isLoggedIn()) {
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
        File file = filesHandler.generateFile(fileName, true);
        responseMessage = String.valueOf(file.length());
        sendResponse();

        // Check if they want to accept file
        String[] commands = tokenizedCommands();
        switch (commands[0].toUpperCase()) {
            case "SEND" -> {
                responseMessage = "+Sending, this might take a while depending on size";
                sendResponse();

                sendFile(file, transferType);

                responseMessage = "+Ok file sent";
                sendResponse();
            }
            case "STOP" -> {
                responseMessage = "+ok, RETR aborted";
                sendResponse();
            }
            default -> {
                responseMessage = "-Unrecognised command, RETR aborted";
                sendResponse();
            }
        }
    }

    /**
     * Used to store a from into the server
     *
     * @param mode     New, Old, App - Check readme for specs
     * @param fileName Name of file to store.
     */
    private void stor(String mode, String fileName) {
        // Check log in
        if (!isLoggedIn()) {
            responseMessage = "-You need to be logged in to use stor";
            sendResponse();
            return;
        }
        // Use current dir for STOR command
        final boolean currentDir = true;
        // Open the file specified at current directory
        File file = filesHandler.generateFile(fileName, currentDir);

        // Check what mode they want to send in
        boolean append = false;
        switch (mode.toUpperCase()) {
            case "NEW" -> {
                // New cannot over ride existing file
                if (file.isFile()) {
                    responseMessage = "-File Exists, but system doesn't support generations";
                    sendResponse();
                    return;
                }
                responseMessage = "+File does not exists, will create a new file";
                sendResponse();
            }
            case "OLD" -> {
                // Old file will, overwrite
                if (file.isFile()) {
                    responseMessage = "+Will write over old file";
                } else {
                    responseMessage = "+Will create new file";
                }
                sendResponse();
            }
            case "APP" -> {
                // Append to existing file.
                append = true;
                if (file.isFile()) {
                    responseMessage = "Will append to file";
                } else {
                    responseMessage = "Will create new file";
                }
                sendResponse();
            }
            default -> {
                responseMessage = "-Invalid Mode";
                sendResponse();
            }
        }

        // Check fileSize
        String[] tks = tokenizedCommands();
        long fileSize = Long.parseLong(tks[1]);
        if (!filesHandler.enoughFreeSpace(fileSize, currentDir)) {
            responseMessage = "-Not enough room, don't send it";
            sendResponse();
            return;
        }
        responseMessage = "+ok, waiting for file";
        sendResponse();

        // waiting for file to send
        try {
            connectionHandler.receiveFile(file, fileSize, append);
            responseMessage = "+Saved " + fileName;
        } catch (IOException e) {
            responseMessage = "-Couldn't save because" + e.getMessage();
        }
        sendResponse();
    }

    /**
     * Closes the Connection safely
     */
    private void done() {
        responseMessage = "+Thank you for using MCHE226 STFP service.";
        sendResponse();
        closeConnection();
    }
}
