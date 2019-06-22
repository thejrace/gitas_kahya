package server;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{

    protected Socket socket;

    public ClientThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String line;
        while (true) {
            try {
                JSONObject output = new JSONObject();
                line = brinp.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {

                    // read request, comes as a json

                    System.out.println("FROM_CLIENT_TO_SERVER:  " + line);

                    try {
                        JSONObject request = new JSONObject(line);

                        if( request.getString("req").equals("oadd_download") ){
                            OADDDownload oaddDownload = new OADDDownload(request.getString("bus_code"));
                            output = oaddDownload.action();
                        }

                    } catch (JSONException e ){
                        e.printStackTrace();
                    }


                    System.out.println("TO_CLIENT_FROM_SERVER:  " + output);


                    out.writeUTF(output.toString() + "\r");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }



}
