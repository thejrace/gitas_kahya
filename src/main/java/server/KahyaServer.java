package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class KahyaServer {

    public static final int PORT = 25000;

    public void start(){
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            System.out.println("Kahya server started at 25000");
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            new ClientThread(socket).start();
        }
    }

}
