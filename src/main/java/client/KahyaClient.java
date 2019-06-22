package client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Common;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class KahyaClient {

    private static Socket socket;
    private String busCode;

    private String route;
    private String activeStop;
    private int activeStopIncrement = 2; // how further we are gonna fetch from active stop
    private Map<Integer, String> stops = new HashMap<>();

    public KahyaClient( String busCode ) {
        this.busCode = busCode;
    }

    public JSONObject fetchOADD(){
        JSONObject request = new JSONObject();
        request.put("bus_code", busCode);
        request.put("req","oadd_download");
        return request( request.toString() );
    }

    public void start(){

        JSONObject oaddData = fetchOADD();
        System.out.println(oaddData.toString());

        if( oaddData.getString("status").equals("A") ){
            // first the setup work before worker loop action


            // fetch route stops ( static for now )
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File file = new File(classLoader.getResource("stops/"+oaddData.getString("route")+"_durak.json").getFile());
            JSONArray routeStopData = new JSONObject(Common.readJSONFile(file)).getJSONArray("duraklar");
            JSONObject stopDataTemp;
            for( int k = 0; k < routeStopData.length(); k++ ){
                if( routeStopData.isNull(k) ) continue;
                stopDataTemp = routeStopData.getJSONObject(k);
                stops.put( stopDataTemp.getInt("sira"), String.valueOf(stopDataTemp.getInt("sira")) +"-"+stopDataTemp.getString("ad") );
            }

            // request all bus data working on the route
            JSONObject fleetData = request( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":"+oaddData.getString("route")+" }").toString() );




        } else { // active check
            System.out.println("Aktif sefer yok!");
        }

    }

    public static JSONObject request( String data ){
        try {
            System.out.println("CLIENT_ACTION_STARTED");
            String host = "localhost";
            int port = 25000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, port);

            // write
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);

            String sendMessage = data + "\n";
            bw.write(sendMessage);
            bw.flush();
            System.out.println("TO_SERVER_FROM_CLIENT : "+sendMessage);

            // read
            InputStream is = socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            String message = dis.readUTF();
            System.out.println("FROM_SERVER_TO_CLIENT : " +message);

            try {
                return new JSONObject(message);
            } catch( JSONException e ){
                return new JSONObject("{ \"error\":true, \"message\":\""+e.getMessage()+"\" }");
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return new JSONObject("{ \"error\":true, \"message\":\""+exception.getMessage()+"\" }");
        } finally {
            //Closing the socket
            try {
                socket.close();
            } catch(Exception e) {
                return new JSONObject("{ \"error\":true, \"message\":\""+e.getMessage()+"\" }");
            }
        }

    }

}
