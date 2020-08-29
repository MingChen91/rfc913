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

    // Available Commands
    private static final Set<String> availableCommands = Set.of(
            "USER", "ACCT", "PASS", "TYPE", "LIST", "CDIR", "KILL", "NAME", "DONE", "RETR", "STOR"
    );

    // Server IP and Port
    private static final String IP = "localhost";
    private static final int PORT = 115;
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
            e.printStackTrace();
            System.out.println("Error connecting to server");
        }

    }

    /**
     * Main
     */
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        while (client.running) {
            client.decodeCommands();
            if(client.connectionHandler.readAscii()){
                System.out.println(client.connectionHandler.getIncomingMessage());
            }
        }
    }

    private void decodeCommands() throws IOException {
        // Reading input
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(System.in));
        String inputCommands = inputStreamReader.readLine();
        String[] commands = inputCommands.split("\\s+");

        if (availableCommands.contains(commands[0].toUpperCase())) {
            switch (commands[0].toUpperCase()) {
                case "USER":
                    if (!(connectionHandler.sendAscii("hello"))) {
                        System.out.println("message did not send");
                        closeConnection();
                    }
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
        } else {
            System.out.println("Not a valid command, please choose from:");
            System.out.println("USER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
        }
    }

    /**
     * Closes stops the program and closes the connection safely.
     */
    public void closeConnection() {
        try {
            this.running = false;
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Could not close connection");
        }
    }
}
