package client;

import fleet.ClientFinishListener;
import fleet.RouteDirection;
import fleet.RunData;
import fleet.UIBusData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Common;
import utils.RunNoComparator;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.*;

public class KahyaClient {

    private static Socket socket;
    private String activeBusCode;
    private int stopCount;

    private String route;
    private String activeBusStop;
    private int activeBusStopNo;
    private int activeStopIncrement = 2; // how further we are gonna fetch from active stop
    private Map<Integer, String> stops = new HashMap<>();
    private ArrayList<UIBusData> output = new ArrayList<>();
    private ClientFinishListener listener;

    public KahyaClient( String busCode ) {
        this.activeBusCode = busCode;
    }

    public JSONObject fetchOADD(){
        JSONObject request = new JSONObject();
        request.put("bus_code", activeBusCode);
        request.put("req","oadd_download");
        return request( request.toString() );
    }

    public void start(){

        JSONObject oaddData = fetchOADD();
        System.out.println(oaddData.toString());
        String route = oaddData.getString("route");
        activeBusStopNo = 0;
        int activeBusDirection = 0; // active bus's direction

        boolean debugFlag = false;

        if( oaddData.getString("status").equals("A") ){
            // first the setup work before worker loop action


            // fetch route stops ( static for now )
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File file = new File(classLoader.getResource("stops/"+oaddData.getString("route")+"_durak.json").getFile());
            JSONArray routeStopData = new JSONObject(Common.readJSONFile(file)).getJSONArray("duraklar");
            stopCount = routeStopData.length();
            JSONObject stopDataTemp;
            for( int k = 0; k < stopCount; k++ ){
                if( routeStopData.isNull(k) ) continue;
                stopDataTemp = routeStopData.getJSONObject(k);
                stops.put( stopDataTemp.getInt("sira"), String.valueOf(stopDataTemp.getInt("sira")) +"-"+stopDataTemp.getString("ad") );
            }


            // fleet definitions
            Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();
            Map<String, Integer> fleetDirections = new HashMap<>(); // array that holds fleet's direction information
            ArrayList<String> bussesWithSameDirection = new ArrayList<>(); // busses that's going to same direction with active Bus
            Map<String, Integer> fleetActiveRunIndexes = new HashMap<>(); // holds active run index for fleet busses
            Map<Integer, String> fleetPositions = new HashMap<>();

            // request all bus data working on the route
            JSONObject fleetData = request( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":"+oaddData.getString("route")+" }").toString() );

            Iterator<String> busCodes = fleetData.keys();
            JSONArray tempData;
            JSONObject tempRunData;
            while( busCodes.hasNext() ) {
                String key = busCodes.next(); // bus code
                if( !fleetRunData.containsKey(key) ) fleetRunData.put(key, new ArrayList<>());
                tempData = fleetData.getJSONArray(key);
                for( int k = 0; k < tempData.length(); k++ ){
                    tempRunData = tempData.getJSONObject(k);
                    fleetRunData.get(key).add( new RunData(
                            tempRunData.getString("bus_code"),
                            Integer.valueOf(tempRunData.getString("no")),
                            tempRunData.getString("stop"),
                            tempRunData.getString("dep_time"),
                            tempRunData.getString("route_details"),
                            tempRunData.getString("status")
                    ));

                }
                Collections.sort(fleetRunData.get(key), new RunNoComparator() );
            }

            for (Map.Entry<String, ArrayList<RunData>> entry : fleetRunData.entrySet()) {
                //System.out.println(entry.getValue());
                ArrayList<String> tempDirectionDetails = new ArrayList<>();
                int activeRunIndex = 0;
                int counter = 0;
                for( RunData data : entry.getValue() ){
                    counter++;
                    if( data.getStatus().equals("A") && data.getBusCode().equals(activeBusCode) ){
                        activeBusStopNo = fetchStopNo(data.getCurrentStop());
                        activeBusStop = data.getCurrentStop();
                    } else if( data.getStatus().equals("A") ){
                        activeRunIndex = counter;
                    }
                    tempDirectionDetails.add(data.getRouteDetails());
                }
                // if activeRunIndex = 0, means no active run currently
                if( activeRunIndex > 0 ){
                    if( entry.getKey().equals(activeBusCode) ) {
                        activeBusDirection = RouteDirection.action(route, activeRunIndex, tempDirectionDetails);
                    } else {
                        fleetDirections.put(entry.getKey(), RouteDirection.action(route, activeRunIndex, tempDirectionDetails));
                        fleetActiveRunIndexes.put(entry.getKey(), activeRunIndex - 1);
                    }
                }
            }

            if( debugFlag ) {
                System.out.println("Active BUS: " + activeBusCode + " DIR: " + activeBusDirection );
                System.out.println(fleetDirections);
            }


            // find other busses with same direction with activeBus
            for (Map.Entry<String, Integer> entry : fleetDirections.entrySet()) {
                if( entry.getValue() == activeBusDirection ){
                    bussesWithSameDirection.add( entry.getKey() );
                }
            }

            if( debugFlag ) System.out.println("BUSSES OF INTEREST : " + bussesWithSameDirection );

            for( String busCode : bussesWithSameDirection ){
                String stop = fleetRunData.get(busCode).get(fleetActiveRunIndexes.get(busCode)).getCurrentStop();
                //System.out.println(busCode + " @ " + stop );
                try {
                    if( debugFlag ) System.out.println(busCode + " @ " + stop.substring(0, stop.indexOf('-')) );
                    int diff = fetchStopNo(stop) - activeBusStopNo;
                    fleetPositions.put( diff, busCode );

                    output.add( new UIBusData(busCode, fleetRunData.get(busCode).get(fleetActiveRunIndexes.get(busCode)).getCurrentStop(), diff));

                } catch( StringIndexOutOfBoundsException e ){
                    if( debugFlag )  System.out.println(busCode + " @ UNDEFINED" );
                }
            }
            // sort busses according to diff
            Collections.sort(output, KahyaClient.DIFF_COMPARATOR );

            for (Map.Entry<Integer, String> entry : fleetPositions.entrySet()) {
                System.out.println(entry.getValue() + "  @  " + entry.getKey() );
            }

            listener.onFinish();

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

    public static final Comparator<UIBusData> DIFF_COMPARATOR = new Comparator<UIBusData>() {
        public int compare(UIBusData d1, UIBusData d2) {
            return d1.getDiff() - d2.getDiff();
        }
    };


    public int getStopCount(){
        return stopCount;
    }

    public ArrayList<UIBusData> getOutput(){
        return output;
    }

    public UIBusData getActiveBusData(){
        return new UIBusData(activeBusCode, activeBusStop, activeBusStopNo );
    }

    public void addListener( ClientFinishListener listener ){
        this.listener = listener;
    }

    private int fetchStopNo( String stop ){
        try {
            return Integer.valueOf(stop.substring(0, stop.indexOf('-')));
        } catch( StringIndexOutOfBoundsException e ){

        }
        return 0;
    }

}
