package routescanner;

import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    public ArrayList<RouteStop> map; // forwardstops->backwardstops ( merge two directions together )
    public String route;
    public String activeBusCode;
    public int directionMergePoint; // index where merge happens

    private Map<String, Bus> buses; // buses on the map
    private Map<String, Boolean> intersectionBeginFlags; // holds the flag for whether intersection action began or not for given route

    public RouteMap(){
        this.buses = new HashMap<>();
        this.intersectionBeginFlags = new HashMap<>();
    }

    private void updateBusPosition( String busCode ){
        int pos = findStopIndex( map, buses.get(busCode).getDirection(), buses.get(busCode).getStop(), directionMergePoint );
        if( pos != -1 ){
            buses.get(busCode).setPosition(pos);
        } else {
            return;
        }
        //System.out.println( buses.get(busCode).toString() );
    }

    // called from RouteScanner to update the given buses run data
    public void passBusData( String busCode, ArrayList<RunData> runData ){
        if( !buses.containsKey(busCode) ){
            Bus bus = new Bus( busCode, runData, map, directionMergePoint );
            buses.put( busCode, bus );
        } else {
            buses.get(busCode).setRunData(runData);
        }
        buses.get(busCode).updateStatus();
        updateBusPosition(busCode);
    }

    public static int findStopIndex( ArrayList<RouteStop> map, int direction, String stopName, int directionMergePoint ){
        try{
            if( stopName.equals("N/A") ) return -1; // @todo BUG FIX!!!!!
        } catch( NullPointerException e ){
            return -1;
        }
        int k = 0;
        if( direction == RouteDirection.BACKWARD ) k = directionMergePoint;
        try {
            return findStopOccurences(map, stopName, k ).get(0);
        } catch( IndexOutOfBoundsException e ){
            //e.printStackTrace();
        }
        return -1;
    }

    public static ArrayList<Integer> findStopOccurences( ArrayList<RouteStop> map, String stopName, int startIndex ){
        ArrayList<Integer> occurences = new ArrayList<>();
        if( stopName.equals("N/A")) return occurences;
        for( ; startIndex < map.size(); startIndex++ ){
            if( map.get(startIndex).getName().equals(stopName) || StringSimilarity.similarity(map.get(startIndex).getName(), stopName ) >= 0.6 ){
                occurences.add(startIndex);
            }
        }
        return occurences;
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
                if( entry.getValue().getIntersectedAt().equals(map.get(k).getName()) || StringSimilarity.similarity(entry.getValue().getIntersectedAt(), map.get(k).getName()) > 0.6 ){
                    map.get(k).markIntersection( entry.getValue() );
                    break;
                }
            }
        }
    }

    public void initialize( String route, ArrayList<RouteStop> routeMap, int directionMergePoint ){
        this.route = route;
        this.map = routeMap;
        this.directionMergePoint = directionMergePoint;
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

    public String getRoute() {
        return route;
    }

}
