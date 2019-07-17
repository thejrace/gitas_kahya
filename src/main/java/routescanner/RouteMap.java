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


    public void updateBusPosition( String busCode ){
        if( buses.get(busCode).getDirection() == RouteDirection.RING ) return;
        int pos = findStopIndex( buses.get(busCode).getDirection(), buses.get(busCode).getStop() );
        if( pos != -1 ){
            buses.get(busCode).setPosition(pos);
        } else {
            if( RouteScanner.DEBUG ) System.out.println(busCode + " stop index is not found!");
            return;
        }
        // check if we update the position of active bus,
        // if so, check whether it crossed the intersection with the other routes
        if( busCode.equals(activeBusCode) ){
            ArrayList<IntersectionData> routeIntersectionData = map.get(pos).getIntersections();
            if( routeIntersectionData.size() > 0 ){
                for( IntersectionData intersectionData : routeIntersectionData ){
                    if( intersectionData.getDirection() != buses.get(busCode).getDirection() ) continue;
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
        System.out.println( buses.get(busCode).toString() );
    }

    public void passBusData( String busCode, ArrayList<RunData> runData ){
        if( !buses.containsKey(busCode) ){
            if( RouteScanner.DEBUG ) System.out.println("adding a bus to route map:  ||"+busCode+"||");
            Bus bus = new Bus( busCode, runData );
            bus.setDirectionMergePoint(directionMergePoint);
            bus.setDirectionListener( ( stops ) -> {
                if( RouteScanner.DEBUG ) System.out.println(busCode +"  DIRACITON!!!   " + stops);
                ArrayList<Integer> prevFoundIndexes = new ArrayList<>();
                for( int j = 0; j < stops.size(); j++ ){
                    String stop = stops.get(j);
                    ArrayList<Integer> foundIndexes = findStopOccurences( stop, 0 );
                    if( RouteScanner.DEBUG ) System.out.println("FOUND INDEXES: " + foundIndexes );
                    if( foundIndexes.size() == 1 ){ // @todo eger sacma sapan bi durak algılarsa onu da bulamayacak direk yöne karar vermek dogru mu? OLUYOR!!
                        // if there is only one match, means this stop is on one direction
                        // we can determine which way by comparing it with merge point
                        if( foundIndexes.get(0) > directionMergePoint ){
                             System.out.println(bus.getCode() + "  singleton stop DIR: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                            bus.setDirection(RouteDirection.BACKWARD);
                            break;
                        } else {
                           System.out.println(bus.getCode() + "  singleton stop DIR: " + RouteDirection.returnText(RouteDirection.FORWARD));
                            bus.setDirection(RouteDirection.FORWARD);
                            break;
                        }
                    } else if( foundIndexes.size() == 2 ){ // this is expected most of the time
                        // if we have previous indexes, compare them with current pair by pair
                        if( prevFoundIndexes.size() > 0 ){
                           if( ( prevFoundIndexes.get(0) < foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) > foundIndexes.get(1) ) ){ // best case
                                System.out.println(bus.getCode() + "  DIR: " + RouteDirection.returnText(RouteDirection.FORWARD));
                                bus.setDirection(RouteDirection.FORWARD);
                                break;
                           } else if( ( prevFoundIndexes.get(0) > foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) < foundIndexes.get(1) )  ){ // best case
                                System.out.println(bus.getCode() + "  DIR: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                               bus.setDirection(RouteDirection.BACKWARD);
                               break;
                           }
                        }
                    }
                    prevFoundIndexes = foundIndexes;
                }
            });

            buses.put( busCode, bus );
        } else {
            buses.get(busCode).setRunData(runData);
        }
        buses.get(busCode).updateStatus();
        updateBusPosition(busCode);
    }

    public int findStopIndex( int direction, String stopName ){
        try{
            if( stopName.equals("N/A") ) return -1; // @todo BUG FIX!!!!!
        } catch( NullPointerException e ){
            return -1;
        }
        int k = 0;
        if( direction == RouteDirection.BACKWARD ) k = directionMergePoint;
        try {
            return findStopOccurences(stopName, k ).get(0);
        } catch( IndexOutOfBoundsException e ){
            //e.printStackTrace();
        }
        return -1;
    }

    private ArrayList<Integer> findStopOccurences( String stopName, int startIndex ){
        ArrayList<Integer> occurences = new ArrayList<>();
        if( stopName.equals("N/A")) return occurences;
        for( ; startIndex < map.size(); startIndex++ ){
            if( map.get(startIndex).getName().equals(stopName) || StringSimilarity.similarity(map.get(startIndex).getName(), stopName ) >= 0.8 ){
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
