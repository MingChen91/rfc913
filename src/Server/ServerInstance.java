package Server;

import Utils.ConnectionHandler;
import Utils.FilesHandler;

import java.io.IOException;
import java.net.Socket;
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
    private ConnectionHandler connectionHandler;
    private FilesHandler filesHandler;
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
            readOK = connectionHandler.readAscii();
            if (readOK) {
                a = connectionHandler.getIncomingMessage();
            }

            if(!(connectionHandler.sendAscii(a.toUpperCase()))) closeConnection();

            if (a.equals("done")) closeConnection();
        }
        System.out.println("Server instance terminated");
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
}