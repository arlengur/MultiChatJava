package ru.arlen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * @author satovritti
 */
public class ChatClient {

    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream out = null;
    // The input stream
    private static BufferedReader in = null;
    // The client input
    private static Scanner inputLine = null;


    public static void main(String[] args) {

        // The default port.
        int portNumber = 4000;
        // The default host.
        String host = "localhost";

        // Open a socket on a given host and port. Open input and output streams.
        try {
            clientSocket = new Socket(host, portNumber);
            inputLine = new Scanner(System.in);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintStream(clientSocket.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }

        /*
          If everything has been initialized then we want to write some data to the
          socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && out != null && in != null) {
            try {

                // Create a thread to read from the server.
                Thread thread = new Thread(() -> {
                    String responseLine;
                    try {
                        while ((responseLine = in.readLine()) != null) {
                            System.out.println(responseLine);
                            if (responseLine.contains("*** Bye"))
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                thread.start();

                String requestLine;
                while (!(requestLine = inputLine.nextLine().trim()).equals("/quit")) {
                    out.println(requestLine);
                }
                out.println(requestLine);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Close the output stream, close the input stream, close the socket.
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e);
            }
        }
    }
}