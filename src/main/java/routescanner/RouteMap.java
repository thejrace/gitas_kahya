package routescanner;

import interfaces.StatusListener;
import interfaces.KahyaUIListener;
import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    public static int ACTIVE_BUS_POSITION = -1;
    public static ArrayList<RouteStop> map; // forwardstops->backwardstops ( merge two directions together )
    public static String route;
    public static String activeBusCode;
    public static int directionMergePoint; // index where merge happens

    private Map<String, Bus> buses; // buses on the map
    private Map<String, Boolean> intersectionBeginFlags; // holds the flag for whether intersection action began or not for given route
    private Map<String, DirectionCounter> dirCounters = new HashMap<>(); // direction counters

    private static boolean DIR_DEBUG_FLAG = true;
    private static boolean BUS_POS_DEBUG_FLAG = true;

    private StatusListener statusListener;
    private KahyaUIListener kahyaUIListener;

    public RouteMap( String route ){
        this.route = route;
        this.map = new ArrayList<>();
        this.buses = new HashMap<>();
        this.intersectionBeginFlags = new HashMap<>();
    }

    private void updateBusPosition( String busCode ){
        int pos = findStopIndex( buses.get(busCode).getDirection(), buses.get(busCode).getStop() );
        if( pos != -1 ){
            buses.get(busCode).setPosition(pos);
        } else {
            return;
        }
        // check if we update the position of active bus,
        // if so, check whether it crossed the intersection with the other routes
        if( busCode.equals(activeBusCode) ){
            ACTIVE_BUS_POSITION = buses.get(busCode).getPosition();
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
        statusListener.onNotify(buses.get(busCode).toString());
        if( buses.get(busCode).getDirFoundFlag() ) kahyaUIListener.onFinish( buses.get(busCode).getUIData());
        System.out.println( buses.get(busCode).toString() );
    }

    // called from RouteScanner to update the given buses run data
    public void passBusData( String busCode, ArrayList<RunData> runData ){
        if( !buses.containsKey(busCode) ){
            if( RouteScanner.DEBUG ) System.out.println("adding a bus to route map:  ||"+busCode+"||");
            Bus bus = new Bus( busCode, runData );
            buses.put( busCode, bus );
        } else {
            buses.get(busCode).setRunData(runData);
        }
        buses.get(busCode).updateStatus();
        updateBusPosition(busCode);
    }

    public static int findStopIndex( int direction, String stopName ){
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

    public static ArrayList<Integer> findStopOccurences( String stopName, int startIndex ){
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

    public void addStatusListener( StatusListener listener ){
        this.statusListener = listener;
    }

    public void addKahyaUIListener( KahyaUIListener listener ){
        this.kahyaUIListener = listener;
    }

    public String getRoute() {
        return route;
    }

    public ArrayList<RouteStop> getMap() {
        return map;
    }

    public void setMap(ArrayList<RouteStop> map) {
        this.map = map;
    }

}
