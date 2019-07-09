package client;

import fleet.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import server.FetchRouteIntersections;
import server.IntersectionData;
import server.RouteIntersection;
import server.RouteStopsDownload;
import utils.RunNoComparator;
import utils.Web_Request;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class KahyaClient {

    private boolean run = true;
    private static Socket socket;
    private String route;

    // active bus defs
    private String activeBusCode;
    private int activeBusDirection = -1;
    private String activeBusStop = "N/A";
    private int activeBusStopNo;

    // stops list for ring routes
    private Map<Integer, ArrayList<String>> stops = new HashMap<>();
    private ArrayList<IntersectionData> routeIntersections = new ArrayList<>();
    private ArrayList<String> routesToDownload = new ArrayList<>();
    private boolean ringRouteFlag = false;

    private boolean errorFlag = false;
    private String errorMessage;
    private boolean debugFlag = true;
    private boolean activeBusDirectionFoundFlag = false;

    private ArrayList<UIBusData> output = new ArrayList<>();
    private ClientFinishListener listener;
    private StatusListener statusListener;

    // fleet definitions ( same for ring and normal )
    private Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();
    private Map<String, RunData> fleetStopsData = new HashMap<>(); // keeps the fleet stop data for route determination ( used in only ring )
    private Map<String, Integer> fleetDirections = new HashMap<>(); // array that holds fleet's direction information
    private ArrayList<String> bussesWithSameDirection = new ArrayList<>(); // busses that's going to same direction with active Bus
    private Map<String, Integer> fleetActiveRunIndexes = new HashMap<>(); // holds active run index for fleet busses
    private Map<Integer, String> fleetPositions = new HashMap<>();
    private ArrayList<String> bussesFromOtherRoutes = new ArrayList<>(); // @todo hack, fix this!!!!

    public KahyaClient( String busCode ) {
        this.activeBusCode = busCode;
        stops.put(RouteDirection.FORWARD, new ArrayList<>());
        stops.put(RouteDirection.BACKWARD, new ArrayList<>());
    }

    public void start(){

        errorFlag = false;
        errorMessage = "";
        output = new ArrayList<>(); // reset

        statusListener.onNotify("start action...");

        //JSONObject oaddData = request( new JSONObject("{ \"req\":\"oadd_download\", \"bus_code\":\""+activeBusCode+"\" }").toString() );
        JSONObject oaddData = oaddDownload();
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

        if( !routesToDownload.contains(route) ) routesToDownload.add(route);
        fetchRouteIntersections();

        JSONArray activeBusRouteDetailsData = oaddData.getJSONArray("run_details_data");
        ArrayList<String> activeBusRouteDetailsList = new ArrayList<>();
        for( int k = 0; k < activeBusRouteDetailsData.length(); k++ ) activeBusRouteDetailsList.add( activeBusRouteDetailsData.getString(k) ); // convert jsonarray to arraylist
        activeBusStopNo = 0;

        // ring - normal route determination
        activeBusDirection = RouteDirection.action( route, oaddData.getInt("active_run_index"), activeBusRouteDetailsList ); // active bus's direction
        // for ring routes, we need route stops to determine directions
        if( stops.get(RouteDirection.FORWARD).size() == 0 && stops.get(RouteDirection.BACKWARD).size() == 0 ){
            //JSONObject stopData = request( new JSONObject("{ \"req\":\"route_stops_download\", \"route\":\""+oaddData.getString("route")+"\" }").toString() );
            //JSONObject stopData = routeStopsDownload();
            //JSONArray routeStopData = stopData.getJSONArray("stops");

            JSONArray routeStopData = routeStopsDownload();

            statusListener.onNotify("durak download...");

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


        if( activeBusDirection == RouteDirection.RING ){
            ringRouteFlag = true;

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
            while( run ){
                statusListener.onNotify("RING: yön loop...");
                output = new ArrayList<>(); // reset
                requestFleetData();
                if( debugFlag ) System.out.println(fleetStopsData.size());
                if( debugFlag ) System.out.println(fleetDirections.size());
                if( counter > 0 ){
                    // compare stops until all busses' directions are found
                    for( Map.Entry<String, RunData> entry : fleetStopsData.entrySet() ){
                        if( fleetDirections.containsKey(entry.getKey()) ) continue; // already found
                        String activeStopName = fetchStopName(entry.getValue().getCurrentStop());
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
                                                                                              ^
                                                                                              |
                                                                              <---  from here scan backwards
                                - In this case Alvarlızade Camii is found, so we can say bus goes forward.
                                - If Alvarlızade Camii is not found, we can say bus goes backward.
                             */
                            for( int x = activeStopIndex - 1; x > 0; x-- ){
                                if(  stops.get(RouteDirection.FORWARD).get(x).equals( stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())) ) ){
                                    // if active bus direction is found, we dont put it in fleetDirections, we update the class properties
                                    if( entry.getKey().equals(activeBusCode)  ) {
                                        // @FIX aktif otobusun yönü değişince sıçıyor suan
                                        activeBusStopNo = fetchStopNo(entry.getValue().getCurrentStop());
                                        activeBusStop = entry.getValue().getCurrentStop();
                                        if( !activeBusDirectionFoundFlag ) { // @todo: sefer bittiginde vs. bunun sifirlayacagiz
                                            activeBusDirection = RouteDirection.FORWARD;
                                            activeBusDirectionFoundFlag = true;
                                        }

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
                                    activeBusStopNo = fetchStopNo(entry.getValue().getCurrentStop());
                                    activeBusStop = entry.getValue().getCurrentStop();
                                    if( !activeBusDirectionFoundFlag ) {
                                        activeBusDirection = RouteDirection.BACKWARD;
                                        activeBusDirectionFoundFlag = true;
                                    }
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
            // @todo: aktif oto normalken, diger otobusler ring olabiliyormuş!!!
            statusListener.onNotify("NORMAL: filo tarama yapılıyor..");
            // for normal routes, find other busses' directions
            requestFleetData();
            for (Map.Entry<String, ArrayList<RunData>> entry : fleetRunData.entrySet()) {
                System.out.println(entry.getKey() +"  " + entry.getValue().get(0).getRoute() );
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
                        fleetDirections.put(entry.getKey(), RouteDirection.action(entry.getValue().get(0).getRoute(), activeRunIndex, tempDirectionDetails));
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
        bussesWithSameDirection = new ArrayList<>(); // reset
        fleetPositions = new HashMap<>();
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
                stop = fleetStopsData.get(busCode).getCurrentStop();
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
                System.out.println( "DIFF = " + entry.getValue() + "  @  " + entry.getKey() );
            }
        }

    }

    private void requestFleetData(){
        // request all bus data working on the route
        JSONObject fleetData = new JSONObject();
        if( activeBusDirection > -1 && !activeBusStop.equals("N/A")){
            // if we know the direction, we can look at the intersections
            for( IntersectionData intersectionData : routeIntersections ){
                if( intersectionData.getDirection() != activeBusDirection || routesToDownload.contains(intersectionData.getComparedRoute())) return;
                int activeStopIndex = 0, intersectionStopIndex = 0;
                // check if activebus passed that stop or not
                for( int k = 0; k < stops.get(activeBusDirection).size(); k++ ){
                    if( stops.get(activeBusDirection).get(k).equals(fetchStopName(activeBusStop)) ) activeStopIndex = k;
                    if( stops.get(activeBusDirection).get(k).equals(intersectionData.getIntersectedAt()) ) intersectionStopIndex = k;
                }
                if( activeStopIndex >= intersectionStopIndex ){
                    routesToDownload.add(intersectionData.getComparedRoute());
                }
            }
        }
        System.out.println(routesToDownload);

        if( ringRouteFlag ){
            //fleetData = requestFormIETT( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":\""+route+"\", \"only_stops_flag\":true }").toString() );

            RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
            routeFleetDownload.setFetchOnlyStopsFlag(true);
            routeFleetDownload.action();
            if( !routeFleetDownload.getErrorFlag() ){
                fleetData = routeFleetDownload.getOutput();
            }

        } else {
            //fleetData = requestFormIETT( new JSONObject("{ \"req\":\"download_fleet_data\", \"route\":\""+route+"\" }").toString() );
            RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
            routeFleetDownload.action();
            if( !routeFleetDownload.getErrorFlag() ){
                fleetData = routeFleetDownload.getOutput();
            }
        }

        /*if( fleetData.has("error") ){
            try {
                errorMessage = fleetData.getString("message");
            } catch( JSONException e ){
                errorMessage = "Kahya Server failed.";
                //e.printStackTrace();
            }
            errorFlag = true;
            return;
        }*/


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
                    fleetStopsData.put(key, new RunData( tempRunData.getString("bus_code"), tempRunData.getString("route"), tempRunData.getString("stop")  )); // @think key yerine tempRunData.getString("buscode")
                } else {
                    fleetRunData.get(key).add( new RunData(
                            tempRunData.getString("bus_code"),
                            tempRunData.getString("route"),
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

    public void fetchRouteIntersections(){
        if( routeIntersections.size() > 0 ) return;
        JSONArray data = FetchRouteIntersections.action(route);
        for( int k = 0; k < data.length(); k++ ){
            JSONObject tempData = data.getJSONObject(k);
            routeIntersections.add(new IntersectionData(route,tempData.getString("intersected_route"), tempData.getString("stop_name"), tempData.getInt("direction")));
        }
    }

    public JSONArray routeStopsDownload(){
        RouteStopsDownload routestopDownload = new RouteStopsDownload( route );
        return routestopDownload.action();
    }

    private JSONObject oaddDownload(){
        fleet.OADDDownload oaddDownload = new fleet.OADDDownload(activeBusCode);
        oaddDownload.action();
        return oaddDownload.getOutput();
    }

    public static JSONObject requestFromServer( String data ){
        Web_Request request = new Web_Request(Web_Request.SERVIS_URL, data );
        request.action();
        return new JSONObject(request.get_value());
    }

    public static JSONObject requestServer( String data ){
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

    public void shutdown( ){
        System.out.println(activeBusCode + " SHUTDOWN!!!!!!!!");
        this.run = false;
    }
    public String getRoutes(){
        return routesToDownload.toString();
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
