package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class KahyaClient {

    private static Socket socket;

    public KahyaClient() {
    }

    public void sendData(){
        try {
            System.out.println("Client action started");
            String host = "localhost";
            int port = 25000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);

            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            String number = "2";

            String sendMessage = number + "\n";
            bw.write(sendMessage);
            bw.flush();
            System.out.println("Message sent to the server : "+sendMessage);

            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            System.out.println("Message received from the server : " +message);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            //Closing the socket
            try {
                socket.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
