package routescanner;

import fleet.OADDDownload;
import fleet.RouteFleetDownload;
import fleet.RunData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import server.FetchRouteIntersections;
import server.IntersectionData;
import server.RouteStopsDownload;
import utils.RunNoComparator;

import java.util.*;

public class RouteScanner {
    public static boolean DEBUG = true;
    private boolean shutdown = false;
    private String route;
    private ArrayList<String> routesToDownload;
    private String activeBusCode;
    private RouteMap routeMap;
    private Map<String, ArrayList<RunData>> fleetRunData = new HashMap<>();

    private boolean downloadIntersectedRoutesFlag = false;

    public RouteScanner( String activeBusCode ){
        if( DEBUG ) System.out.println("route scanner initialized ("+activeBusCode+")");
        this.activeBusCode = activeBusCode;
    }

    public void start(){
        Thread scannerThread = new Thread( () -> {
            findRoute();
            initializeRouteMap();
            // begin scan loop
            while( !shutdown ){

               downloadFleetData();

                try {
                    Thread.sleep(10000);
                } catch( InterruptedException e ){
                    e.printStackTrace();
                }
            }
        });
        scannerThread.setDaemon(true);
        scannerThread.start();

    }

    private void downloadFleetData(){
        routesToDownload = routeMap.getIntersectedRoutes();
        RouteFleetDownload routeFleetDownload = new RouteFleetDownload(routesToDownload);
        if( DEBUG ) System.out.println("downloading fleet data. ("+routesToDownload+")");
        routeFleetDownload.action();
        JSONObject fleetData = routeFleetDownload.getOutput();
        if( DEBUG ) System.out.println("fleet data:  ||"+fleetData+"||");
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
                        tempRunData.getString("stop"),
                        tempRunData.getString("dep_time"),
                        tempRunData.getString("route_details"),
                        tempRunData.getString("status"),
                        tempRunData.getString("status_code")
                ));
            }
            Collections.sort(fleetRunData.get(key), new RunNoComparator() ); // sort by run no
            routeMap.passBusData( key, fleetRunData.get(key) );
        }

    }

    private void initializeRouteMap(){
        if( DEBUG ) System.out.println("route map initializing ("+route+")");
        int directionMergePoint = -1; // index where merge happens
        Map<String, IntersectionData> routeIntersections = new HashMap<>(); // contains intersection data of the active route
        ArrayList<RouteStop> map = new ArrayList<>(); // forwardstops->backwardstops ( merge two directions together )
        // create the route map
        routeMap = new RouteMap(route);
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
        routeMap.setMap(map);
        routeMap.setDirectionMergePoint(directionMergePoint);
        routeMap.setRouteIntersections(routeIntersections);
        routeMap.setActiveBusCode(activeBusCode);
        if( DEBUG ) System.out.println("route map created. ("+route+")");
    }

    private void findRoute(){
        OADDDownload activeBusOADDDownload = new OADDDownload(activeBusCode);
        activeBusOADDDownload.action();
        JSONObject activeBusOADDData = activeBusOADDDownload.getOutput();
        try {
            route = activeBusOADDData.getString("route");
            if( DEBUG ) System.out.println("route found ("+route+")");
        } catch( JSONException e ){
            e.printStackTrace();
        }
    }
}
