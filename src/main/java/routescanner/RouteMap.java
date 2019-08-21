package routescanner;

import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    /**
     * Map of the route
     * forwardstops->backwardstops ( merge two directions together )
     */
    public ArrayList<RouteStop> map;
    /**
     * Route code
     */
    public String route;
    /**
     * Index where merge happens
     */
    public int directionMergePoint;
    /**
     * Buses on the map
     */
    private Map<String, Bus> buses;
    /**
     * Holds the flag for whether intersection action began or not for given route
     */
    private Map<String, Boolean> intersectionBeginFlags; // @DEPRECATED

    /**
     * Constructor
     */
    public RouteMap(){
        this.buses = new HashMap<>();
        this.intersectionBeginFlags = new HashMap<>();
    }

    /**
     * Initialize the RouteMap
     *
     * @param route route code
     * @param routeMap route map
     * @param directionMergePoint merge point
     */
    public void initialize( String route, ArrayList<RouteStop> routeMap, int directionMergePoint ){
        this.route = route;
        this.map = routeMap;
        this.directionMergePoint = directionMergePoint;
    }

    /**
     * Called from RouteScanner to update the given buses run data
     *
     * @param busCode code of the bus
     * @param runData run data of the bus
     */
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

    /**
     * Updates the position of the bus according to it's direction and stop
     * @param busCode code of the bus of interest
     */
    private void updateBusPosition( String busCode ){
        int pos = findStopIndex( map, buses.get(busCode).getDirection(), buses.get(busCode).getStop(), directionMergePoint );
        if( pos != -1 ){
            buses.get(busCode).setPosition(pos);
        } else {
            return;
        }
        //System.out.println( buses.get(busCode).toString() );
    }

    /**
     * Get intersected routes and mark them on the map
     *
     * @param routeIntersections intersection data of the route
     */
    @Deprecated
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

    /**
     * Returns the routes to be fetched to route scanner
     *
     * @return route list
     */
    @Deprecated
    public ArrayList<String> getIntersectedRoutes(){
        ArrayList<String> output = new ArrayList<>();
        for( Map.Entry<String, Boolean> entry : intersectionBeginFlags.entrySet() ){
            if( entry.getValue() ) output.add(entry.getKey());
        }
        output.add(route); // we fetch the active route all the time
        return output;
    }

    /**
     * Getter for route
     *
     * @return route code
     */
    public String getRoute() {
        return route;
    }

    /**
     *  Finds the index of the stop in given map
     *
     * @param map haystack
     * @param direction direction data
     * @param stopName needle
     * @param directionMergePoint merge point to calulcate index
     * @return index of the stop
     */
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

    /**
     * Finds the occurrence of the stop in given map
     *
     * @param map haystack
     * @param stopName needle
     * @param startIndex starting point to start search ( direction related )
     * @return indexes of the occurrences
     */
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
}
