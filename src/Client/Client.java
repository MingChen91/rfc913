package Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Single instead of the client used in implementing RFC-913 SFTP
 */
public class Client {
    private Socket clientSocket;
    //TODO get ip and port from config file
    private static final String IP = "localhost";
    private static final int PORT = 6666;

    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket(IP, PORT);
        DataOutputStream sendToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader fromServer = new BufferedReader(new InputStreamReader((clientSocket.getInputStream())));
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Client connecting to IP " + IP + "Port " + PORT);
        String incomingMessage;

        while (true) {
            String userInput = input.readLine();
            if (userInput.equals("DONE")) break;
            sendToServer.writeBytes(userInput + '\n');
            incomingMessage = fromServer.readLine();
            System.out.println(incomingMessage);
        }
        clientSocket.close();
    }
}
