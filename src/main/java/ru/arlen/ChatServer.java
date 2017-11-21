package ru.arlen;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author satovritti
 */
public class ChatServer {
    // The server socket.
    private static ServerSocket serverSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    public static void main(String args[]) {

        // The default port number.
        int portNumber = 4000;

        /**
         * Open a server socket on the portNumber (default 4000). Note that we can not choose a port less than 1023
         * if we are not privileged users (root).
         */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        // Create a client socket for each connection and pass it to a new client thread.
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                int i;
                for (i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == maxClientsCount) {
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}


