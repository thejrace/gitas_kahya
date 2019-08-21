package routescanner;

import fakedatagenerator.FakeDataGenerator;
import fleet.RouteFleetDownload;
import org.json.JSONArray;
import org.json.JSONObject;
import fleet.FetchRouteIntersections;
import fleet.RouteStopsDownload;
import utils.RunNoComparator;

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

                try {
                    if( FakeDataGenerator.ACTIVE ){
                        Thread.sleep(100);
                    } else {
                        Thread.sleep(15000);
                    }
                } catch( InterruptedException e ){
                    e.printStackTrace();
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
        Map<String, IntersectionData> routeIntersections = new HashMap<>(); // contains intersection data of the active route
        ArrayList<RouteStop> map = new ArrayList<>(); // forwardstops->backwardstops ( merge two directions together )
        // create the route map
        routeMap = new RouteMap();
        // fetch stops
        RouteStopsDownload routeStopsDownload = new RouteStopsDownload(route);
        JSONArray routeStopsDownloaded = routeStopsDownload.action();
        JSONArray tempStops;
        JSONObject stopDataTemp;
        for( int j = 0; j < 2; j++ ){
            tempStops = routeStopsDownloaded.getJSONArray(j);
            int k;
            for( k = 0; k < tempStops.length(); k++ ){
                if( tempStops.isNull(k) ) continue;
                stopDataTemp = tempStops.getJSONObject(k);
                map.add(new RouteStop( stopDataTemp.getInt("no"), stopDataTemp.getString("isim")));
            }
            if( j == 0 ) directionMergePoint = k;
        }
        // fetch intersections
        JSONArray data = FetchRouteIntersections.action(route);
        for( int k = 0; k < data.length(); k++ ){
            JSONObject tempData = data.getJSONObject(k);
            routeIntersections.put(tempData.getString("kesisen_hat"), new IntersectionData(route,tempData.getString("kesisen_hat"), tempData.getString("durak_adi"), tempData.getInt("yon"),  tempData.getInt("total_diff")));
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

        // @todo create json file here!!!!!!
        System.out.println(route + " Route Scanner DONE!");

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
