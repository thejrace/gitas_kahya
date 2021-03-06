package server;

import fleet.RouteFleetDownload;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{

    // @TODO --- ARADA BİR YERLERE İSTEK YAPIP, SESSION I AKTIF TUTMAYI DENEMEK LAZIM

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

                            fleet.OADDDownload oaddDownload = new fleet.OADDDownload(request.getString("bus_code"));
                            oaddDownload.action();
                            if( oaddDownload.getErrorFlag() ){
                                output = new JSONObject("{ \"error\":true, \"message\":\""+oaddDownload.getErrorMessage()+"\" } ");
                            } else {
                                output = oaddDownload.getOutput();
                            }
                        } else if( request.getString("req").equals("download_fleet_data") ){

                            RouteFleetDownload routeFleetDownload = new RouteFleetDownload(request.getString("route") );
                            if( request.has("only_stops_flag") ) routeFleetDownload.setFetchOnlyStopsFlag(true);
                            routeFleetDownload.action();
                            if( routeFleetDownload.getErrorFlag() ){
                                output = new JSONObject("{ \"error\":true, \"message\":\""+routeFleetDownload.getErrorMessage()+"\" }");
                            } else {
                                output = routeFleetDownload.getOutput();
                            }
                        } else if( request.getString("req").equals("route_stops_download") ){
                            RouteStopsDownload routestopDownload = new RouteStopsDownload( request.getString("route") );
                            output.put("stops", routestopDownload.action() );
                        }
                    } catch (JSONException e ){
                        e.printStackTrace();
                    }


                    System.out.println("TO_CLIENT_FROM_SERVER:  " + output);


                    out.writeUTF(output.toString() + "\r");
                    out.flush();
                }
            } catch ( IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }



}
