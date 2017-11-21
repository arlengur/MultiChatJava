package ru.arlen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author satovritti
 *
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class ClientThread extends Thread {

    private String clientName = null;
    private PrintStream out = null;
    private Socket clientSocket = null;
    private final ClientThread[] threads;
    private int maxClientsCount;

    ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    public void run() {
        int maxClientsCount = this.maxClientsCount;
        ClientThread[] threads = this.threads;

        try {
            // Create input and output streams for this client.
            BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintStream(clientSocket.getOutputStream());
            String name;
            while (true) {
                out.println("Enter your name.");
                name = is.readLine().trim();
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                    out.println("The name should not contain '@' character.");
                }
            }

            // Welcome the new the client.
            out.println("Welcome " + name + " to our chat room.\nTo leave enter /quit in a new line.");
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].out.println("*** A new user " + name + " entered the chat room !!! ***");
                    }
                }
            }
            // Start the conversation.
            while (true) {
                String line = is.readLine();
                if (line.startsWith("/quit")) {
                    break;
                }
                // If the message is private sent it to the given client.
                if (line.startsWith("@")) {
                    String[] words = line.split("\\s", 2);
                    if (words.length > 1 && words[1] != null) {
                        words[1] = words[1].trim();
                        //noinspection Since15
                        if (!words[1].isEmpty()) {
                            synchronized (this) {
                                for (int i = 0; i < maxClientsCount; i++) {
                                    if (threads[i] != null && threads[i] != this
                                            && threads[i].clientName != null
                                            && threads[i].clientName.equals(words[0])) {
                                        threads[i].out.println("<" + name + "> " + words[1]);
                                        // Echo this message to let the client know the private message was sent.
                                        this.out.println(">" + name + "> " + words[1]);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // The message is public, broadcast it to all other clients.
                    synchronized (this) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i].clientName != null) {
                                threads[i].out.println("<" + name + "> " + line);
                            }
                        }
                    }
                }
            }
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this
                            && threads[i].clientName != null) {
                        threads[i].out.println("*** The user " + name + " is leaving the chat room !!! ***");
                    }
                }
            }
            out.println("*** Bye " + name + " ***");

            // Clean up. Set the current thread variable to null so that a new client could be accepted by the server.
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }
            // Close the output stream, close the input stream, close the socket.
            is.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
        }
    }
}
