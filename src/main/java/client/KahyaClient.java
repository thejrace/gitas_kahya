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
import utils.StringSimilarity;
import utils.Web_Request;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

public class KahyaClient {

    private boolean run = true; // loop guard for ring loop
    private static Socket socket; // not used anymore

    // active bus defs
    private String activeBusCode;
    private int activeBusDirection = -1;
    private String activeBusStop = "N/A";
    private int activeBusStopNo;
    private String route;

    private Map<Integer, ArrayList<String>> stops = new HashMap<>(); // holds the stops in form; [0: JSONARRAY{name, no}, 1: JSONARRAY{name, no}]
    private Map<String, IntersectionData> routeIntersections = new HashMap<>(); // contains intersection data of the active route
    private ArrayList<String> routesToDownload = new ArrayList<>(); // contains routes to scan including active route )
    private boolean ringRouteFlag = false;

    private boolean errorFlag = false;
    private String errorMessage; // error flags are used to notify UI
    private boolean debugFlag = false;
    private String debugMessage;
    private boolean activeBusDirectionFoundFlag = false;

    private ArrayList<UIBusData> output = new ArrayList<>(); // main output ( contains activeBus data as well )
    private ClientFinishListener listener; // notify main UI
    private StatusListener statusListener; // notify status label UI
    private StatusListener debugListener; // notify debug listeners


    // fleet definitions ( same for ring and normal )
    private Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();
    private Map<String, RunData> fleetStopsData = new HashMap<>(); // keeps the fleet stop data for route determination ( used in only ring )
    private Map<String, RunData> fleetDirections = new HashMap<>(); // array that holds fleet's direction information
    private Map<String, Integer> fleetActiveRunIndexes = new HashMap<>(); // holds active run index for fleet busses
    private Map<String, Integer> totalStopDiffs = new HashMap<>(); // holds stop count difference between active routes from parallel routes
    private Map<String, Integer> intersectionIndexesInActiveRoute = new HashMap<>(); // holds the stop index of the intersections in active route stops list
    private Map<String, DirectionCounter> directionCounters = new HashMap<>();
    private Map<String, ArrayList<String>> busDownloadedStops = new HashMap<>();


    public KahyaClient( String busCode ) {
        this.activeBusCode = busCode;
        // initialize stops list
        stops.put(RouteDirection.FORWARD, new ArrayList<>());
        stops.put(RouteDirection.BACKWARD, new ArrayList<>());
    }

    public void start(){
        errorFlag = false;
        errorMessage = "";
        output = new ArrayList<>(); // reset
        statusListener.onNotify("start action...");
        JSONObject oaddData = oaddDownload();
        if( debugFlag )  System.out.println(oaddData.toString()); // @todo sadece hattı al, aktif seferin onemi yok
        if( oaddData.has("error") ){
            errorMessage = oaddData.getString("message");
            errorFlag = true;
            return;
        }
        try {
            route = oaddData.getString("route");
        } catch (JSONException e ){
            errorMessage = "Aktif sefer bilgisi alınamadı.";
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
                    stops.get(dirs[j]).add(stopDataTemp.getString("isim"));
                }
            }
            if( debugFlag ){
                System.out.println(stops.get(RouteDirection.BACKWARD));
                System.out.println(stops.get(RouteDirection.FORWARD));
            }
        } else {
            if( debugFlag ) System.out.println("STOPS ARE ALREADY DOWNLOADED");
        }
        debugMessage = "";

        if( activeBusDirection == RouteDirection.RING ){
            ringRouteFlag = true;

            Map<String, Integer> prevActiveStopIndexData = new HashMap<>();
            int counter = 0;
            int activeStopIndex;
            // we will fetch the whole fleets data( including active bus )
            // and will compare active stop with previous stop etc to determine direction.
            // #NOTE: this loop will continue during whole client session, after determined the direction
            // of the any bus, only the stop information, diffs etc of it is requested and updated.
            // #NOTE2: this loop only updates the fleetStopsData and fleetDirections lists, to be used in
            // compareDirections method.
            // #NOTE3: this loop won't trigger the UI listeners until activeBus's direction is found
            while( run ){
                debugMessage = "";
                statusListener.onNotify("RING: yön loop...");
                output = new ArrayList<>(); // reset
                requestFleetData();
                if( debugFlag ) System.out.println(fleetStopsData.size());
                if( debugFlag ) System.out.println(fleetDirections.size());
                if( counter > 0 ){
                    // compare stops until all busses' directions are found
                    for( Map.Entry<String, RunData> entry : fleetStopsData.entrySet() ){
                        if( fleetDirections.containsKey(entry.getKey()) ) continue; // already found
                        activeStopIndex = -1; // reset
                        String activeStopName = fetchStopName(entry.getValue().getCurrentStop());
                        // if there is no stop data of the current bus, we skip the cycle
                        if( activeStopName.equals("[NODATA]") ) continue;
                        if( !busDownloadedStops.containsKey(entry.getKey())) busDownloadedStops.put(entry.getKey(), new ArrayList<>());
                        busDownloadedStops.get(entry.getKey()).add(activeStopName);
                        for( int k = 0; k < stops.get(RouteDirection.FORWARD).size(); k++ ){
                            if( activeStopName.equals(stops.get(RouteDirection.FORWARD).get(k)) ){
                                activeStopIndex = k; // @todo Ya bulamazsa
                                if( debugFlag ) System.out.println("FOUND INDEX " + entry.getKey() + "   " + activeStopIndex);
                                break;
                            }
                        }
                        try {
                            //System.out.println(entry.getKey() + " ["+stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey()))+"] ["+activeStopName+"]");
                            System.out.println(entry.getKey() + " ["+busDownloadedStops.get(entry.getKey()).get(busDownloadedStops.get(entry.getKey()).size()-2)+"] ["+activeStopName+"]");
                            debugMessage += entry.getKey() + " ["+busDownloadedStops.get(entry.getKey()).get(busDownloadedStops.get(entry.getKey()).size()-2)+"] ["+activeStopName+"]\n";
                            // if stoplist does not contain the active stop, skip for now
                            if( activeStopIndex == - 1 ){
                                System.out.println("SKIP  " + entry.getKey());
                                continue;
                            }
                            // this flag will let us know if current busses direction is found or not
                            boolean found = false;
                            // if last two stop information is same for the current bus, we will skip the direction check
                            //if( stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())).equals(activeStopName) ) continue;

                            // skip loop if last downloaded stop is the same with the last one
                            if( activeStopName.equals(busDownloadedStops.get(entry.getKey()).get(busDownloadedStops.get(entry.getKey()).size()-2)) ) continue;

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
                                if(  stops.get(RouteDirection.FORWARD).get(x).equals( stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())) ) ||
                                        StringSimilarity.similarity(stops.get(RouteDirection.FORWARD).get(x),stops.get(RouteDirection.FORWARD).get(prevActiveStopIndexData.get(entry.getKey())) ) > 0.8){
                                    // initalize counter
                                    if( !directionCounters.containsKey(entry.getKey()) ){
                                        directionCounters.put(entry.getKey(), new DirectionCounter());
                                    }
                                    int directionTemp = directionCounters.get(entry.getKey()).getDirection();
                                    if( directionTemp == -1 ){
                                        System.out.println(entry.getKey() + "   increment " + RouteDirection.returnText(RouteDirection.FORWARD));
                                        directionCounters.get(entry.getKey()).increment( RouteDirection.FORWARD );
                                    } else {
                                        System.out.println(entry.getKey() + "  DIR: " + RouteDirection.returnText(directionTemp));
                                        if( entry.getKey().equals(activeBusCode) ){
                                            activeBusDirection = directionTemp;
                                            activeBusDirectionFoundFlag = true;
                                            activeBusStopNo = fetchStopNo(entry.getValue().getCurrentStop());
                                            activeBusStop = entry.getValue().getCurrentStop();
                                        } else {
                                            fleetDirections.put(entry.getKey(), new RunData( entry.getKey(), entry.getValue().getRoute(), RouteDirection.FORWARD) );
                                            if( debugFlag ) System.out.println(entry.getKey() + "  " + RouteDirection.returnText(directionTemp));
                                        }
                                    }
                                    found = true;
                                    break;
                                }
                            }
                            if( !found ){
                                System.out.println(entry.getKey() + "   increment " + RouteDirection.returnText(RouteDirection.BACKWARD));
                                int directionTemp = directionCounters.get(entry.getKey()).getDirection();
                                if( directionTemp == -1 ){
                                    directionCounters.get(entry.getKey()).increment( RouteDirection.BACKWARD );
                                } else {
                                    System.out.println(entry.getKey() + "  DIR: " + RouteDirection.returnText(directionTemp));
                                    if( entry.getKey().equals(activeBusCode) ){
                                        activeBusDirection = directionTemp;
                                        activeBusDirectionFoundFlag = true;
                                        activeBusStopNo = fetchStopNo(entry.getValue().getCurrentStop());
                                        activeBusStop = entry.getValue().getCurrentStop();
                                    } else {
                                        fleetDirections.put(entry.getKey(), new RunData( entry.getKey(), entry.getValue().getRoute(), RouteDirection.BACKWARD) );
                                        if( debugFlag ) System.out.println(entry.getKey() + "  " + RouteDirection.returnText(directionTemp));
                                    }
                                }
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
                debugListener.onNotify(debugMessage);
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
                ArrayList<String> tempDirectionDetails = new ArrayList<>();
                int activeRunIndex = 0;
                int counter = 0;
                // in the following loop; for each bus including active bus
                // # Find the active run index and list the route details for the RouteDirection
                for( RunData data : entry.getValue() ){
                    counter++;
                    if( data.getStatus().equals("A") && !data.getStatusCode().equals("CA") ){
                        if( data.getBusCode().equals(activeBusCode) ){
                            activeBusStopNo = fetchStopNo(data.getCurrentStop());
                            activeBusStop = data.getCurrentStop();
                            activeRunIndex = counter;
                        } else {
                            activeRunIndex = counter;
                        }
                    }
                    tempDirectionDetails.add(data.getRouteDetails());
                }
                // if activeRunIndex = 0, means no active run currently
                if( activeRunIndex > 0 ){
                    if( entry.getKey().equals(activeBusCode) ) {
                        activeBusDirection = RouteDirection.action(route, activeRunIndex, tempDirectionDetails);
                    } else {
                        fleetDirections.put(entry.getKey(), new RunData( entry.getKey(), entry.getValue().get(0).getRoute(), RouteDirection.action(entry.getValue().get(0).getRoute(), activeRunIndex, tempDirectionDetails)));
                        fleetActiveRunIndexes.put(entry.getKey(), activeRunIndex - 1);
                    }
                }
            }
            statusListener.onNotify("NORMAL: durak farkları hesaplanıyor..");
            compareDirections();
            debugListener.onNotify(debugMessage);
            try{
                statusListener.onNotify("NORMAL: updateUI");
                listener.onFinish();
            } catch( NullPointerException e ){ }
        }
    }


    private void requestFleetData(){
        // request all bus data working on the route
        JSONObject fleetData = new JSONObject();
        fleetRunData = new HashMap<>();
        if( activeBusDirection > -1 && !activeBusStop.equals("N/A")){
            // if we know the direction, we can look at the intersections
            for( Map.Entry<String, IntersectionData> entry : routeIntersections.entrySet() ){
                if( entry.getValue().getDirection() != activeBusDirection || routesToDownload.contains(entry.getValue().getComparedRoute())) continue;
                int activeStopIndex = 0, intersectionStopIndex = 0;
                // check if activebus passed that stop or not
                for( int k = 0; k < stops.get(activeBusDirection).size(); k++ ){
                    if( stops.get(activeBusDirection).get(k).equals(fetchStopName(activeBusStop)) ) activeStopIndex = k;
                    if( stops.get(activeBusDirection).get(k).equals(entry.getValue().getIntersectedAt()) ) intersectionStopIndex = k;
                }
                if( activeStopIndex >= intersectionStopIndex ){
                    routesToDownload.add(entry.getValue().getComparedRoute());
                    totalStopDiffs.put(entry.getValue().getComparedRoute(), entry.getValue().getTotalDiff());
                }
            }
        }
        if( debugFlag ) System.out.println(routesToDownload);
        if( ringRouteFlag ){
            RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
            routeFleetDownload.setFetchOnlyStopsFlag(true);
            routeFleetDownload.action();
            fleetData = routeFleetDownload.getOutput();
        } else {
            try {
                RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
                routeFleetDownload.action();
                fleetData = routeFleetDownload.getOutput();
                if( debugFlag ) System.out.println(fleetData);
            } catch( Exception e ){
                e.printStackTrace();
            }
        }
        if( fleetData.length() == 0 ){
            errorMessage = "Filo verisi yok.";
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

    private void compareDirections(){
        // calculate the stop differences of the other busses from active bus
        // this method is common for both normal and ring routes
        if( debugFlag ) {
            System.out.println("Active BUS: " + activeBusCode + " DIR: " + activeBusDirection );
            System.out.println(fleetDirections);
        }
        ArrayList<RunData> bussesWithSameDirection = new ArrayList<>(); // holds buses which are going to same dir as active bus
        Map<Integer, String> fleetPositions = new HashMap<>(); // holds the diffs of the other buses
        // find other busses with same direction with activeBus
        for (Map.Entry<String, RunData> entry : fleetDirections.entrySet()) {
            if( entry.getValue().getDirection() == activeBusDirection ){
                bussesWithSameDirection.add( entry.getValue() );
            }
        }
        if( debugFlag ) System.out.println("BUSSES OF INTEREST : " + bussesWithSameDirection );
        for( RunData runData : bussesWithSameDirection ){
            String stop;
            if( ringRouteFlag ){
                stop = fleetStopsData.get(runData.getBusCode()).getCurrentStop();
            } else {
                try{
                    stop = fleetRunData.get(runData.getBusCode()).get(fleetActiveRunIndexes.get(runData.getBusCode())).getCurrentStop(); // @todo entry den al direk
                } catch( NullPointerException e ){
                    e.printStackTrace();
                    continue;
                }
            }
            try {
                if( debugFlag ) System.out.println(runData.getBusCode() + " @ " + stop.substring(0, stop.indexOf('-')) );
                int diff;

                diff = fetchStopNoDB(stop) - fetchStopNoDB(activeBusStop);

               /* if( !runData.getRoute().equals(route) ){
                    // we only take the ones which are passed the intersection stop.
                    // find the stop index where the intersection happens from active route's stop list
                    int intersectionStopIndexActiveRoute = 0;
                    if( intersectionIndexesInActiveRoute.containsKey(runData.getRoute())){ // check if previously found
                        intersectionStopIndexActiveRoute = intersectionIndexesInActiveRoute.get(runData.getRoute());
                    } else {
                        for( int k = 0; k < stops.get(activeBusDirection).size(); k++ ){
                            if( stops.get(activeBusDirection).get(k).equals(routeIntersections.get(runData.getRoute()).getIntersectedAt())  ){
                                intersectionStopIndexActiveRoute = k + 1; // stops indexed from 1
                                intersectionIndexesInActiveRoute.put(runData.getRoute(), intersectionStopIndexActiveRoute);
                                break;
                            }
                        }
                    }
                    // sum the index with the total difference to find the stop index from intersection route's list
                    intersectionStopIndexActiveRoute += ( -1 * routeIntersections.get(runData.getRoute()).getTotalDiff() );
                    // if intersection route is not yet arrived the intersection point, we skip it
                    if( fetchStopNo(stop) < intersectionStopIndexActiveRoute ) continue;
                    diff = fetchStopNo(stop) - activeBusStopNo + totalStopDiffs.get(runData.getRoute());
                } else {
                    diff = fetchStopNo(stop) - activeBusStopNo;
                }*/

                fleetPositions.put( diff, runData.getBusCode() );
                if( ringRouteFlag ){
                    output.add( new UIBusData(runData.getBusCode(), stop, diff, RouteDirection.returnText(fleetDirections.get(runData.getBusCode()).getDirection())));
                } else {
                    output.add( new UIBusData(runData.getBusCode(), stop, diff, fleetRunData.get(runData.getBusCode()).get(fleetActiveRunIndexes.get(runData.getBusCode())).getRouteDetails()));
                }
            } catch( StringIndexOutOfBoundsException e ){
                if( debugFlag )  System.out.println(runData.getBusCode() + " @ UNDEFINED" );
            }
        }
        // add active bus to the list with diff = 0
        output.add( new UIBusData(activeBusCode, activeBusStop, 0, RouteDirection.returnText(activeBusDirection)) );
        // sort busses according to diff
        Collections.sort(output, KahyaClient.DIFF_COMPARATOR );
        if( debugFlag ) {
            for (Map.Entry<Integer, String> entry : fleetPositions.entrySet()) {
                if( debugFlag ) System.out.println( "DIFF = " + entry.getValue() + "  @  " + entry.getKey() );
            }
        }

    }

    // finds the db index of the gien stop
    public int fetchStopNoDB( String stop ){
        ArrayList<String> stopList = stops.get(activeBusDirection);
        for( int k = 0; k < stopList.size(); k++ ){
            if( stopList.get(k).equals(fetchStopName(stop)) || StringSimilarity.similarity(stopList.get(k), fetchStopName(stop)) > 0.8 ){
                return k+1;
            }
        }
        return -999;
    }

    public void fetchRouteIntersections(){
        if( routeIntersections.size() > 0 ) return; // we do it only for once
        JSONArray data = FetchRouteIntersections.action(route);
        for( int k = 0; k < data.length(); k++ ){
            JSONObject tempData = data.getJSONObject(k);
            routeIntersections.put(tempData.getString("kesisen_hat"), new IntersectionData(route,tempData.getString("kesisen_hat"), tempData.getString("durak_adi"), tempData.getInt("yon"),  tempData.getInt("total_diff")));
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

    @Deprecated
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

    public void setDebugListener( StatusListener listener ){
        debugListener = listener;
    }

    public void shutdown( ){
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
