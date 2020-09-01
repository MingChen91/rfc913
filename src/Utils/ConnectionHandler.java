package Utils;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

/**
 * Used for sending and receiving information from a connection socket. Attempts to resolve any errors
 */
public class ConnectionHandler {
    private final DataOutputStream messageOut;
    private final BufferedReader messageIn;

    private final BufferedInputStream dataIn;
    private final DataOutputStream dataOut;

    private String incomingMessage;
    // Debug flag
    private final static boolean STACKTRACE = false;


    /**
     * Constructor : Creates the necessary objects used for reading and writing to the connection.
     *
     * @param connectionSocket the connection socket to read and write from
     * @throws IOException error if connection socket is bad
     */
    public ConnectionHandler(Socket connectionSocket) throws IOException {
        // Messages
        this.messageOut = new DataOutputStream(connectionSocket.getOutputStream());
        this.messageIn = new BufferedReader(new InputStreamReader((connectionSocket.getInputStream())));
        // Data
        this.dataIn = new BufferedInputStream(connectionSocket.getInputStream());
        this.dataOut = new DataOutputStream(connectionSocket.getOutputStream());
    }


    /**
     * Monitors the socket for any incoming data. Blocks until a null terminator is received.
     * Saves the incoming message, retrieve using getIncomingMessage()
     *
     * @return Boolean indicating if read has been successful, false if error with connection
     */
    public boolean readIncoming() {
        StringBuilder incomingMessage = new StringBuilder();
        int incomingChar;
        boolean finishedReading = false;

        try {
            // Read and append char to string until null terminator is received
            while (!finishedReading) {
                incomingChar = messageIn.read();
                if ((char) incomingChar == '\0') {
                    if (incomingMessage.length() > 0) {
                        finishedReading = true;
                    }
                } else {
                    incomingMessage.append((char) incomingChar);
                }
            }
            // Return the message
            this.incomingMessage = incomingMessage.toString();
            return true;
        } catch (IOException e) {
            // Catching error when trying to read, and close connection
            if (STACKTRACE) e.printStackTrace();
            System.out.println("Error reading from socket. Socket will be closed");
            return false;
        }
    }


    /**
     * Sends an ascii string over the connection, ends with null terminator
     *
     * @return boolean for if sending is successful , false if error with connection
     */
    public boolean sendMessage(String outgoingMessage) {
        try {
            // Sends the message
            messageOut.writeBytes(outgoingMessage + '\0');
            return true;
        } catch (IOException e) {
            // Catches connection error
            if (STACKTRACE) e.printStackTrace();
            System.out.println("Error sending Message. Socket will be closed");
            return false;
        }
    }

    /**
     * Returns the incoming message
     *
     * @return Last known incomingMessage stored
     */
    public String getIncomingMessage() {
        return this.incomingMessage;
    }

    public boolean sendFile(File file) {
        // Declare byte buffer the size of the file
        try {
            byte[] bArray = Files.readAllBytes(file.toPath());
            // Send all the bytes
            dataOut.write(bArray, 0, bArray.length);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void receiveFile(File file, long fileSize, boolean append) throws IOException {
        // Output stream to write file to
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, append));
        for (int i = 0; i < fileSize; i++) {
            bos.write(dataIn.read());
        }
        bos.close();
    }
}
