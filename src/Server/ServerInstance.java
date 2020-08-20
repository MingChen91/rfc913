package Server;

import java.io.*;
import java.net.Socket;

/**
 * This is a single instance of a server.
 */
public class ServerInstance implements Runnable {
    private Socket connectionSocket;
    private String responseCode;

    /**
     * Constructor, takes in the socket allocated by the welcome socket
     *
     * @param connectionSocket socket allocated to this instance of server from the welcome socket
     */
    public ServerInstance(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    /**
     * Main Runnable, loops and listens for commands.
     */
    @Override
    public void run() {
        try {
            // Read input from Client
            BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream responseMessage = new DataOutputStream(connectionSocket.getOutputStream());
            while (true) {
                String inputString = input.readLine();
                System.out.println(inputString);
                // Check first part of command to determine what action to perform

                responseMessage.writeBytes(inputString.toUpperCase() +'\n');

            }
        } catch (IOException e) {// catch io exception}
        }
    }
}