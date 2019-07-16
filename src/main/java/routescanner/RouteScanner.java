package routescanner;

import fleet.OADDDownload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import server.FetchRouteIntersections;
import server.IntersectionData;
import server.RouteStopsDownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteScanner {

    private boolean shutdown = false;
    private String route;
    private ArrayList<String> routesToDownload;
    private String activeBusCode;
    private RouteMap routeMap;

    private boolean downloadIntersectedRoutesFlag = false;

    public RouteScanner( String activeBusCode ){
        this.activeBusCode = activeBusCode;
        routesToDownload = new ArrayList<>();
        routesToDownload.add(route);
    }

    public void start(){
        Thread scannerThread = new Thread( () -> {
            findRoute();
            initializeRouteMap();
            // begin scan loop
            while( !shutdown ){

                downloadIntersectedRoutesFlag = routeMap.getRouteIntersectionOccuredFlag();

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
        if( downloadIntersectedRoutesFlag ){

        }
    }

    private void initializeRouteMap(){
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
    }

    private void findRoute(){
        OADDDownload activeBusOADDDownload = new OADDDownload(activeBusCode);
        activeBusOADDDownload.action();
        JSONObject activeBusOADDData = activeBusOADDDownload.getOutput();
        try {
            route = activeBusOADDData.getString("route");
        } catch( JSONException e ){
            e.printStackTrace();
        }
    }
}
