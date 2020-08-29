package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Server Class
 */
public class Server {
    private static final String HOST = "localhost";
    private static final int PORT = 115;
    private ServerSocket welcomeSocket;

    /**
     * Starts the server which creates a welcome socket and redirects any incoming connections to a connection socket.
     */
    public static void main(String[] args) {
        Server server = new Server();
        // Accepts any new connections and opens a new socket for data transfer
        while (true) {
            Socket connectionSocket = server.AcceptIncomingConnections();
            if (connectionSocket != null) {
                new Thread(new ServerInstance(connectionSocket)).start();
            } else {
                // Error accepting , terminate server
                System.out.println("Error accepting incoming connections. Server shutting down");
                break;
            }
        }
    }

    /**
     * Constructor, creates the welcome socket
     */
    public Server() {
        try {
            welcomeSocket = new ServerSocket(PORT);
            System.out.println("Server listening on " + PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error creating welcome socket.");
        }
    }

    /**
     * Attempts to accept new incoming connections
     */
    public Socket AcceptIncomingConnections() {
        try {
            return welcomeSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
