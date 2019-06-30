package client;

import fleet.ClientFinishListener;
import fleet.RouteDirection;
import fleet.RunData;
import fleet.UIBusData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.RunNoComparator;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class KahyaClient {

    private static Socket socket;
    private String route;

    // active bus defs
    private String activeBusCode;
    private int activeBusDirection;
    private String activeBusStop;
    private int activeBusStopNo;

    // stops list for ring routes
    private Map<Integer, ArrayList<String>> stops = new HashMap<>();
    private boolean ringRouteFlag = false;

    private boolean errorFlag = false;
    private String errorMessage;
    private boolean debugFlag = true;

    private ArrayList<UIBusData> output = new ArrayList<>();
    private ClientFinishListener listener;
    private StatusListener statusListener;

    // fleet definitions ( same for ring and normal )
    private Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();
    private Map<String, String> fleetStopsData = new HashMap<>(); // keeps the fleet stop data for route determination ( used in only ring )
    private Map<String, Integer> fleetDirections = new HashMap<>(); // array that holds fleet's direction information
    private ArrayList<String> bussesWithSameDirection = new ArrayList<>(); // busses that's going to same direction with active Bus
    private Map<String, Integer> fleetActiveRunIndexes = new HashMap<>(); // holds active run index for fleet busses
    private Map<Integer, String> fleetPositions = new HashMap<>();

    public KahyaClient( String busCode ) {
        this.activeBusCode = busCode;
    }

    public void start(){

        errorFlag = false;
        errorMessage = "";
        output = new ArrayList<>(); // reset

        statusListener.onNotify("start action...");

        JSONObject oaddData = request( new JSONObject("{ \"req\":\"oadd_download\", \"bus_code\":\""+activeBusCode+"\" }").toString() );
        if( debugFlag )  System.out.println(oaddData.toString());

        if( oaddData.has("error") ){
            errorMessage = oaddData.getString("message");
            errorFlag = true;
            return;
        }
        try {
            route = oaddData.getString("route");
        } catch (JSONException e ){
            errorMessage = "Hat bilgisi alınamadı.";
            errorFlag = true;
            return;
        }

        JSONArray activeBusRouteDetailsData = oaddData.getJSONArray("run_details_data");
        ArrayList<String> activeBusRouteDetailsList = new ArrayList<>();
        for( int k = 0; k < activeBusRouteDetailsData.length(); k++ ) activeBusRouteDetailsList.add( activeBusRouteDetailsData.getString(k) ); // convert jsonarray to arraylist
        activeBusStopNo = 0;

        // ring - normal route determination
        activeBusDirection = RouteDirection.action( route, oaddData.getInt("active_run_index"), activeBusRouteDetailsList ); // active bus's direction


        if( activeBusDirection == RouteDirection.RING ){
            ringRouteFlag = true;

            stops.put(RouteDirection.FORWARD, new ArrayList<>());
            stops.put(RouteDirection.BACKWARD, new ArrayList<>());

            // for ring routes, we need route stops to determine directions
            if( stops.get(RouteDirection.FORWARD).size() == 0 && stops.get(RouteDirection.BACKWARD).size() == 0 ){
                JSONObject stopData = request( new JSONObject("{ \"req\":\"route_stops_download\", \"route\":\""+oaddData.getString("route")+"\" }").toString() );
                JSONArray routeStopData = stopData.getJSONArray("stops");

                statusListener.onNotify("RING: durak download...");

                int[] dirs = { RouteDirection.FORWARD, RouteDirection.BACKWARD };
                JSONObject stopDataTemp;
                JSONArray tempStops;
                for( int j = 0; j < dirs.length; j++ ){
                    tempStops = routeStopData.getJSONArray(j);
                    for( int k = 0; k < tempStops.length(); k++ ){
                        if( tempStops.isNull(k) ) continue;
                        stopDataTemp = tempStops.getJSONObject(k);
                        stops.get(dirs[j]).add(stopDataTemp.getString("name"));
                    }
                }
                if( debugFlag ){
                    System.out.println(stops.get(RouteDirection.BACKWARD));
                    System.out.println(stops.get(RouteDirection.FORWARD));
                }
            } else {
                if( debugFlag ) System.out.println("STOPS ARE ALREADY DOWNLOADED");
            }

            Map<String, Integer> prevActiveStopIndexData = new HashMap<>();
            int counter = 0;
            int activeStopIndex = -1;
            // we will fetch the whole fleets data( including active bus )
            // and will compare active stop with previous stop etc to determine direction.
            // #NOTE: this loop will continue during whole client session, after determined the direction
            // of the any bus, only the stop information, diffs etc of it is requested and updated.
            // #NOTE2: this loop only updates the fleetStopsData and fleetDirections lists, to be used in
            // compareDirections method.
            // #NOTE3: this loop won't trigger the UI listeners until activeBus's direction is found
            while( true ){
                statusListener.onNotify("RING: yön loop...");
                requestFleetData();
                if( debugFlag ) System.out.println(fleetStopsData.size());
                if( debugFlag ) System.out.println(fleetDirections.size());
                if( counter > 0 ){
                    // compare stops until all busses' directions are found
                    for( Map.Entry<String, String> entry : fleetStopsData.entrySet() ){
                        if( fleetDirections.containsKey(entry.getKey()) ) continue; // already found
                        String activeStopName = fetchStopName(fleetStopsData.get(entry.getKey()));
                        // if there is no stop data of the current bus, we skip the cycle
                        if( activeStopName.equals("[NODATA]") ) continue;
                        for( int k = 0; k < stops.get(RouteDirection.FORWARD).size(); k++ ){
                            if( activeStopName.equals(stops.get(RouteDirection.FORWARD).get(k)) ){
                                activeStopIndex = k;
                                if( debugFlag ) System.out.println("FOUND INDEX " + entry.getKey() + "   " + activeStopIndex);
                                break;
                            }
                        }
                        try {
                            if( debugFlag ) System.out.println(entry.getKey() + " ["+stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey()))+"] ["+activeStopName+"]");
                            // this flag will let us know if current busses direction is found or not
                            boolean found = false;
                            // if last two stop information is same for the current bus, we will skip the direction check
                            if( stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())).equals(activeStopName) ) continue;
                            // following for loop:: from active stop, we will make a backward scan in stops lists to check if we can find the previous
                            // stop data. If we would we can say bus moves forward direction.
                            /*
                               DEMO:
                                    [prev]               [active]
                                Alvarlızade Camii    Ümraniye Eğitim. Arş

                                stops = [ ..., Kavacık Mezarlık, Alvarlızade Camii, Ümraniye Eğitim. Arş, ... ]
                                                                                              A
                                                                                              |
                                                                              <---  from here scan backwards
                                - In this case Alvarlızade Camii is found, so we can say bus goes forward.
                                - If Alvarlızade Camii is not found, we can say bus goes backward.
                             */
                            for( int x = activeStopIndex - 1; x > 0; x-- ){
                                if(  stops.get(RouteDirection.FORWARD).get(x).equals( stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())) ) ){
                                    // if active bus direction is found, we dont put it in fleetDirections, we update the class properties
                                    if( entry.getKey().equals(activeBusCode)) {
                                        // @FIX aktif otobusun yönü değişince sıçıyor suan
                                        activeBusStopNo = fetchStopNo(fleetStopsData.get(entry.getKey()));
                                        activeBusStop = fleetStopsData.get(entry.getKey());
                                        activeBusDirection = RouteDirection.FORWARD;
                                    } else {
                                        fleetDirections.put(entry.getKey(), RouteDirection.FORWARD);
                                        if( debugFlag ) System.out.println(entry.getKey() + "  GİDİŞ");
                                    }
                                    found = true;
                                    break;
                                }
                            }
                            if( !found ){
                                if( entry.getKey().equals(activeBusCode)) {
                                    activeBusStopNo = fetchStopNo(fleetStopsData.get(entry.getKey()));
                                    activeBusStop = fleetStopsData.get(entry.getKey());
                                    activeBusDirection = RouteDirection.BACKWARD;
                                } else {
                                    fleetDirections.put(entry.getKey(), RouteDirection.BACKWARD);
                                }
                                if( debugFlag )  System.out.println(entry.getKey() + "  DÖNÜŞ");
                            }
                        } catch( NullPointerException | IndexOutOfBoundsException e ){
                            //e.printStackTrace();
                        }
                        // we save the last stop data indexes for compare later loop cycles.
                        prevActiveStopIndexData.put(entry.getKey(), activeStopIndex );
                    }
                    // if active busses direction is not found, we dont update UI
                    if( activeBusDirection != RouteDirection.RING ){
                        statusListener.onNotify("RING: durak farkları hesaplanıyor..");
                        compareDirections();
                        statusListener.onNotify("RING: updateUI");
                        try{
                            listener.onFinish();
                        } catch( NullPointerException e ){ }
                    } else {
                        statusListener.onNotify("RING: aktif otobüs yön bulunmamış..");
                    }
                }
                if( counter == 0 ) counter++;
                try{
                    Thread.sleep(10000);
                } catch( InterruptedException e ){
                    e.printStackTrace();
                }
            }
        } else {
            statusListener.onNotify("NORMAL: filo tarama yapılıyor..");
            // for normal routes, find other busses' directions
            requestFleetData(  );
            for (Map.Entry<String, ArrayList<RunData>> entry : fleetRunData.entrySet()) {
                //System.out.println(entry.getValue());
                ArrayList<String> tempDirectionDetails = new ArrayList<>();
                int activeRunIndex = 0;
                int counter = 0;
                for( RunData data : entry.getValue() ){
                    counter++;
                    if( data.getStatus().equals("A") && !data.getStatusCode().equals("CA") && data.getBusCode().equals(activeBusCode) ){
                        activeBusStopNo = fetchStopNo(data.getCurrentStop());
                        activeBusStop = data.getCurrentStop();
                        activeRunIndex = counter;
                    } else if( data.getStatus().equals("A") && !data.getStatusCode().equals("CA") ){
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
            statusListener.onNotify("NORMAL: durak farkları hesaplanıyor..");
            compareDirections();
            try{
                statusListener.onNotify("NORMAL: updateUI");
                listener.onFinish();
            } catch( NullPointerException e ){ }
        }
    }

    private void compareDirections(){
        // calculate the stop differences of the other busses from active bus
        // this method is common for both normal and ring routes
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
            String stop;
            if( ringRouteFlag ){
                stop = fleetStopsData.get(busCode);
            } else {
                stop = fleetRunData.get(busCode).get(fleetActiveRunIndexes.get(busCode)).getCurrentStop();
            }
            try {
                if( debugFlag ) System.out.println(busCode + " @ " + stop.substring(0, stop.indexOf('-')) );
                int diff;
                diff = fetchStopNo(stop) - activeBusStopNo;
                fleetPositions.put( diff, busCode );
                if( ringRouteFlag ){
                    output.add( new UIBusData(busCode, stop, diff, RouteDirection.returnText(fleetDirections.get(busCode))));
                } else {
                    output.add( new UIBusData(busCode, stop, diff, fleetRunData.get(busCode).get(fleetActiveRunIndexes.get(busCode)).getRouteDetails()));
                }
            } catch( StringIndexOutOfBoundsException e ){
                if( debugFlag )  System.out.println(busCode + " @ UNDEFINED" );
            }
        }
        // add active bus to the list with diff = 0
        output.add( new UIBusData(activeBusCode, activeBusStop, 0, RouteDirection.returnText(activeBusDirection)) );
        // sort busses according to diff
        Collections.sort(output, KahyaClient.DIFF_COMPARATOR );
        if( debugFlag ) {
            for (Map.Entry<Integer, String> entry : fleetPositions.entrySet()) {
                System.out.println(entry.getValue() + "  @  " + entry.getKey() );
            }
        }

    }

    private void requestFleetData(){
        // request all bus data working on the route
        JSONObject fleetData;
        if( ringRouteFlag ){
            fleetData = request( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":\""+route+"\", \"only_stops_flag\":true }").toString() );
        } else {
            fleetData = request( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":\""+route+"\" }").toString() );
        }
        if( fleetData.has("error") ){
            try {
                errorMessage = fleetData.getString("message");
            } catch( JSONException e ){
                errorMessage = "Kahya Server failed.";
                //e.printStackTrace();
            }
            errorFlag = true;
            return;
        }
        // list fleets rundata
        Iterator<String> busCodes = fleetData.keys();
        JSONArray tempData;
        JSONObject tempRunData;
        while( busCodes.hasNext() ) {
            String key = busCodes.next(); // bus code
            if( !fleetRunData.containsKey(key) ) fleetRunData.put(key, new ArrayList<>());
            tempData = fleetData.getJSONArray(key);
            for( int k = 0; k < tempData.length(); k++ ){
                tempRunData = tempData.getJSONObject(k);
                if( ringRouteFlag ){
                    fleetStopsData.put(key, tempRunData.getString("stop")); // @think key yerine tempRunData.getString("buscode")
                } else {
                    fleetRunData.get(key).add( new RunData(
                            tempRunData.getString("bus_code"),
                            Integer.valueOf(tempRunData.getString("no")),
                            tempRunData.getString("stop"),
                            tempRunData.getString("dep_time"),
                            tempRunData.getString("route_details"),
                            tempRunData.getString("status"),
                            tempRunData.getString("status_code")
                    ));
                }
            }
            if( !ringRouteFlag ) Collections.sort(fleetRunData.get(key), new RunNoComparator() ); // sort by run no
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
            //exception.printStackTrace();
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

    public ArrayList<UIBusData> getOutput(){
        return output;
    }

    public UIBusData getActiveBusData(){
        return new UIBusData(activeBusCode, activeBusStop, activeBusStopNo, "" );
    }

    public void setUIListener( ClientFinishListener listener ){
        this.listener = listener;
    }

    public void setStatusListener(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private int fetchStopNo( String stop ){
        try {
            return Integer.valueOf(stop.substring(0, stop.indexOf('-')));
        } catch( StringIndexOutOfBoundsException e ){

        }
        return 0;
    }
    private String fetchStopName( String stop ){
        try {
            return stop.substring(stop.indexOf('-')+1, stop.indexOf(" ("));
        } catch( StringIndexOutOfBoundsException e ){

        }
        return "[NODATA]";
    }
    public boolean getErrorFlag() {
        return errorFlag;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
    public String getRoute() {
        return route;
    }

}
