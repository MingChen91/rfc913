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
    private ConnectionHandler connectionHandler;
    private FilesHandler filesHandler;


    public Client() {
        // Create the socket, connection handler and files handler
        try {
            clientSocket = new Socket(IP, PORT);
            connectionHandler = new ConnectionHandler(clientSocket);
            filesHandler = new FilesHandler("Client/Files", "Client/Configs");
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
            // print out incoming message
            displayMessage();
            // decode input
            while (!correctInput) {
                if (DEBUG) System.out.println("Collecting commands");
                correctInput = getInput();
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

    /**
     * main function that decodes what commands are entered and responds accordingly
     * Reads the terminal (system.in) and runs appropriate method accordingly.
     */
    private boolean getInput() {
        try {
            // Reading input
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(System.in));
            inputCommands = inputStreamReader.readLine();
            String[] commands = inputCommands.split("\\s+");
            // Selecting which command to run
            if (availableCommands.contains(commands[0].toUpperCase())) {
                switch (commands[0].toUpperCase()) {
                    case "USER":
                        return user(commands);
                    case "ACCT":
                        return acct(commands);
                    case "PASS":
                        return pass(commands);
                    case "TYPE":
                        return type(commands);
                    case "LIST":
                        return list(commands);
                    case "CDIR":
                        return cdir(commands);
                    case "KILL":
                        return kill(commands);
                    case "NAME":
                        return name(commands);
                    case "TOBE":
                        return tobe(commands);
                    case "DONE":
                        return done(commands);
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
                System.out.println("Not a valid command, please choose from:\nUSER, ACCT, PASS, TYPE, LIST, CDIR, KILL, NAME, DONE, RETR, STOR");
                return false;
            }
        } catch (IOException e) {
            System.out.println("Can't read input from terminal, ending session.");
            closeConnection();
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
     * @param commands command line args
     */
    private boolean user(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("USER command format : USER <user name>");
            return false;
        }
        return true;
    }

    private boolean acct(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("ACCT command format : ACCT <account name>");
            return false;
        }
        return true;
    }

    private boolean pass(String[] commands) {
        // check commands is exactly 2 fields.
        if (commands.length != 2) {
            System.out.println("PASS command format : PASS <password>");
            return false;
        }
        return true;
    }

    private boolean type(String[] commands) {
        if (commands.length != 2) {
            System.out.println("TYPE command format : TYPE { A | B | C }");
            return false;
        }
        return true;
    }

    private boolean list(String[] commands) {
        if (commands.length != 2 && commands.length != 3) {
            System.out.println("LIST command format : LIST { F | V } <directory-path>. check README for specific directory path syntax");
            return false;
        }

        if (!(commands[1].toUpperCase().equals("V") || commands[1].toUpperCase().equals("F"))) {
            System.out.println("Mode can only be V or F");
            return false;
        }
        return true;
    }

    private boolean cdir(String[] commands) {
        if (commands.length != 2) {
            System.out.println("LIST command format : LIST { F | V } <directory-path> , check README for specific directory path syntax");
            return false;
        }
        return true;
    }

    private boolean kill(String[] commands) {
        if (commands.length != 2) {
            System.out.println("KILL command format : KILL <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    private boolean name(String[] commands) {
        if (commands.length != 2) {
            System.out.println("NAME command format : NAME <file> (File has to be within currently directory)");
            return false;
        }
        return true;
    }

    private boolean tobe(String[] commands) {
        if (commands.length != 2) {
            System.out.println("TOBE command format : TOBE <new file name>");
            return false;
        }
        return true;
    }

    private boolean done(String[] commands) {
        if (commands.length != 1) {
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


}
