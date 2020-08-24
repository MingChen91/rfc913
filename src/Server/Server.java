package Server;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private enum CommandType {USER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR}

    private static final String host = "localhost";
    private static final int PORT = 115;

    public static void main(String[] args) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(PORT);
        System.out.println("Server listening on " + PORT);

        // Accepts any new connections and opens a new socket for data transfer
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            new Thread(new ServerInstance(connectionSocket)).start();
        }
    }
}
