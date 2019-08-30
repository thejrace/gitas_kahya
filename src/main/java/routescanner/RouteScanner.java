package routescanner;

import fakedatagenerator.FakeDataGenerator;
import fleet.RouteFleetDownload;
import org.json.JSONArray;
import org.json.JSONObject;
import fleet.RouteStopsDownload;
import utils.APIRequest;
import utils.Common;
import utils.RunNoComparator;
import utils.ThreadHelper;
import java.util.*;

public class RouteScanner {

    /**
     * Debug flag for logging
     */
    public boolean DEBUG = true;

    /**
     * Flag to kill main thread
     */
    private boolean shutdown = false;

    /**
     * Flag to notifiy pool that thread is started
     */
    private boolean started = false;

    /**
     * Route code
     */
    private String route;

    /**
     *  RouteMap instance
     */
    private RouteMap routeMap;

    /**
     * Config
     */
    private JSONObject settings;

    /**
     * Constructor
     *
     * @param route route code
     */
    public RouteScanner( String route ){
        if( DEBUG ) System.out.println("route scanner initialized ("+route+")");
        this.route = route;
    }

    /**
     * Main logic thread
     */
    public void start(){
        Thread scannerThread = new Thread( () -> {
            initializeRouteMap();
            // begin scan loop
            while( !shutdown ){
                downloadFleetData();

                    if( FakeDataGenerator.ACTIVE ){
                        ThreadHelper.delay(100);
                    } else {
                        ThreadHelper.delay(settings.getInt("scanner_active_interval"));
                    }
            }
            System.out.println( route + " shutdown!");
        });
        scannerThread.setDaemon(true);
        scannerThread.start();
        started = true;
    }

    /**
     * Download route stops and create the RouteMap
     */
    private void initializeRouteMap(){
        if( DEBUG ) System.out.println("route map initializing ("+route+")");
        int directionMergePoint = -1; // index where merge happens
        ArrayList<RouteStop> map = new ArrayList<>(); // forwardstops->backwardstops ( merge two directions together )

        // create the route map
        routeMap = new RouteMap();

        // fetch stops
        RouteStopsDownload routeStopsDownload = new RouteStopsDownload(settings.getString("route_stops_download_url"), route);
        JSONArray routeStopsDownloaded = routeStopsDownload.action();
        JSONObject stopDataTemp;
        int activeDir = -2, prevDir = -1;

        // merge directions
        for( int k = 0; k < routeStopsDownloaded.length(); k++ ){
            stopDataTemp = routeStopsDownloaded.getJSONObject(k);
            activeDir = stopDataTemp.getInt("direction");
            map.add(new RouteStop( stopDataTemp.getInt("no"), stopDataTemp.getString("name")));
            if( prevDir != activeDir ) directionMergePoint = k;
            prevDir = activeDir;
        }

        routeMap.initialize(route, map, directionMergePoint);
        if( DEBUG ) System.out.println("route map created. ("+route+")");
    }

    /**
     * Get updated settings
     *
     * @param settings updated settings
     */
    public void updateSettings(JSONObject settings){
        this.settings = settings;
    }

    /**
     * Download the fleet data of the route
     */
    private void downloadFleetData(){
        Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();
        ArrayList<String> routesToDownload = routeMap.getIntersectedRoutes();
        RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
        if( DEBUG ) System.out.println("downloading fleet data. ("+routesToDownload+")");
        routeFleetDownload.action();
        JSONObject fleetData = routeFleetDownload.getOutput();
        System.out.println("fleet data:  ||"+fleetData+"||");
        // convert jsonobjects to <BusCode, ArrayList<RunData>>
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
                        tempRunData.getString("route"),
                        Integer.valueOf(tempRunData.getString("no")),
                        RouteStop.fetchStopName(tempRunData.getString("stop")),
                        tempRunData.getString("dep_time"),
                        tempRunData.getString("route_details"),
                        tempRunData.getString("status"),
                        tempRunData.getString("status_code")
                ));
            }
            Collections.sort(fleetRunData.get(key), new RunNoComparator() ); // sort by run no
            routeMap.passBusData( key, fleetRunData.get(key) );
        }

        System.out.println(route + " Route Scanner DONE!");

        JSONArray totalData = new JSONArray();
        for( Map.Entry<String, Bus> entry : routeMap.getBuses().entrySet() ){
            totalData.put(entry.getValue().toJSON());
        }

        //sendDataToAPI("http://kahya_api.test/api/uploadRouteScannerData/"+route, totalData.toString());
        JSONObject data = new JSONObject();
        data.put("data", totalData);
        data.put("timestamp", Common.getDateTime());
        APIRequest.POST(settings.getString("upload_data_url")+route, data.toString());
    }

    /**
     * Getter for start
     *
     * @return started flag
     */
    public boolean isStarted(){
        return started;
    }
}
