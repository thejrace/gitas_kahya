package routescanner;

import fleet.RouteDirection;
import org.json.JSONArray;
import org.json.JSONObject;
import server.RouteStopsDownload;
import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    private String route;
    private ArrayList<RouteStop> map; // forwardstops->backwardstops ( merge two directions together )
    private Map<String, Integer> busPositions; // collect bus positions
    private int directionMergePoint; // index where merge happens

    public RouteMap( String route ){
        this.route = route;
        this.map = new ArrayList<>();
        this.busPositions = new HashMap<>();
    }

    public void create(){
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
    }

    public void updateBusPosition( String busCode, int direction, String stopName ){
        if( !busPositions.containsKey(busCode) ) busPositions.put(busCode, -999); // default
        int pos = findStopIndex( direction, stopName );
        if( pos != -1 ){
            busPositions.put(busCode, pos);
        } else {
            System.out.println(busCode + " stop index is not found!");
        }
    }

    private int findStopIndex( int direction, String stopName ){
        int k = 0;
        if( direction == RouteDirection.BACKWARD ) k = directionMergePoint;
        for( ; k < map.size(); k++ ){
            if( map.get(k).getName().equals(stopName) || StringSimilarity.similarity(map.get(k).getName(), stopName ) >= 0.8 ){
                return k;
            }
        }
        return -1;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public ArrayList<RouteStop> getMap() {
        return map;
    }

    public void setMap(ArrayList<RouteStop> map) {
        this.map = map;
    }

}
