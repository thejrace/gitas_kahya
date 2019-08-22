package routescanner;

import fakedatagenerator.FakeDataGenerator;
import fleet.RouteFleetDownload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import fleet.RouteStopsDownload;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import utils.RunNoComparator;

import java.io.IOException;
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
        ArrayList<RouteStop> map = new ArrayList<>(); // forwardstops->backwardstops ( merge two directions together )

        // create the route map
        routeMap = new RouteMap();

        // fetch stops
        RouteStopsDownload routeStopsDownload = new RouteStopsDownload(route);
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

        // @todo create json file here!!!!!!
        System.out.println(route + " Route Scanner DONE!");

        JSONArray totalData = new JSONArray();
        for( Map.Entry<String, Bus> entry : routeMap.getBuses().entrySet() ){
            totalData.put(entry.getValue().toJSON());
        }

        sendDataToAPI("http://kahya_api.test/api/uploadRouteScannerData/"+route, totalData.toString());
    }

    /**
     * Sends the downloaded data to API
     *
     * @param data downloaded data
     *
     */
    private void sendDataToAPI( String url, String data ){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .data("data", data)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .execute();

            JSONObject apiResponse = new JSONObject(response.parse().text());
            try {
                if( !apiResponse.getJSONObject("data").getBoolean("success") ){
                    System.out.println("sendDataToAPI API response error!");
                }
            } catch( JSONException e ){
                e.printStackTrace();
            }
        } catch (HttpStatusException e) {
            e.printStackTrace();
            System.out.println("sendDataToAPI !!!!check API Token!!!!");
            return;
        } catch( IOException e ) {
            System.out.println("sendDataToAPI error!");
            e.printStackTrace();
            return;
        }
        System.out.println("sendDataToAPI completed!");
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
