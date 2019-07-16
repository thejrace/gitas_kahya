package routescanner;

import fleet.RouteDirection;
import fleet.RunData;
import org.json.JSONArray;
import org.json.JSONObject;
import server.FetchRouteIntersections;
import server.IntersectionData;
import server.RouteStopsDownload;
import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    private String route;
    private String activeBusCode;
    private ArrayList<RouteStop> map; // forwardstops->backwardstops ( merge two directions together )
    private Map<String, Bus> buses; // buses on the map
    private int directionMergePoint; // index where merge happens
    private Map<String, Boolean> intersectionBeginFlags; // holds the flag for whether intersection action began or not for given route

    public RouteMap( String route ){
        this.route = route;
        this.map = new ArrayList<>();
        this.buses = new HashMap<>();
        this.intersectionBeginFlags = new HashMap<>();
    }


    public synchronized void updateBusPosition( String busCode, int direction, String stopName ){
        int pos = findStopIndex( direction, stopName );
        if( pos != -1 ){
            buses.get(busCode).setPosition(pos);
        } else {
            System.out.println(busCode + " stop index is not found!");
        }
        // check if we update the position of active bus,
        // if so, check whether it crossed the intersection with the other routes
        if( busCode.equals(activeBusCode) ){
            ArrayList<IntersectionData> routeIntersectionData = map.get(pos).getIntersections();
            if( routeIntersectionData.size() > 0 ){
                for( IntersectionData intersectionData : routeIntersectionData ){
                    if( intersectionData.getDirection() != direction ) continue;
                    // check if active bus is between intersection point and the end
                    int intersectionPos = findStopIndex(intersectionData.getDirection(), intersectionData.getIntersectedAt() );
                    if( pos >= intersectionPos && pos <= (( intersectionData.getDirection() == RouteDirection.FORWARD ) ? directionMergePoint : map.size()-1 ) ){
                        // set begin flag for that route
                        if( !intersectionBeginFlags.get(intersectionData.getComparedRoute())){
                            intersectionBeginFlags.put(intersectionData.getComparedRoute(), true);
                        }
                    } else {
                        // clear previous flags ( this will be reached when direction changes )
                        if( intersectionBeginFlags.get(intersectionData.getComparedRoute())){
                            intersectionBeginFlags.put(intersectionData.getComparedRoute(), false);
                        }
                    }
                }
            }
        }
    }

    public void passBusData( String busCode, ArrayList<RunData> runData ){
        if( !buses.containsKey(busCode) ){
            buses.put(busCode, new Bus( busCode, runData ) );
        } else {
            buses.get(busCode).setRunData(runData);
        }
        buses.get(busCode).updateStatus();
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

    // get intersected routes and mark them on the map
    public void setRouteIntersections( Map<String, IntersectionData> routeIntersections ){
        for( Map.Entry<String, IntersectionData> entry : routeIntersections.entrySet() ){
            if( !intersectionBeginFlags.containsKey(entry.getKey() ) ) intersectionBeginFlags.put(entry.getKey(), false);
            int k = 0;
            if( entry.getValue().getDirection() == RouteDirection.BACKWARD ){
                k += directionMergePoint;
            }
            for( ; k < map.size(); k++ ){
                if( entry.getValue().getIntersectedAt().equals(map.get(k).getName()) || StringSimilarity.similarity(entry.getValue().getIntersectedAt(), map.get(k).getName()) > 0.8 ){
                    map.get(k).markIntersection( entry.getValue() );
                    break;
                }
            }
        }
    }

    // returns the routes to be fetched to route scanner
    public ArrayList<String> getIntersectedRoutes(){
        ArrayList<String> output = new ArrayList<>();
        for( Map.Entry<String, Boolean> entry : intersectionBeginFlags.entrySet() ){
            if( entry.getValue() ) output.add(entry.getKey());
        }
        output.add(route); // we fetch the active route all the time
        return output;
    }

    public int getDirectionMergePoint() {
        return directionMergePoint;
    }

    public void setDirectionMergePoint(int directionMergePoint) {
        this.directionMergePoint = directionMergePoint;
    }

    public void setActiveBusCode( String activeBusCode ){
        this.activeBusCode = activeBusCode;
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
