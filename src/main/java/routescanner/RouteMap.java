package routescanner;

import client.StatusListener;
import fleet.DirectionCounter;
import fleet.RouteDirection;
import fleet.RunData;
import org.json.JSONArray;
import org.json.JSONObject;
import server.FetchRouteIntersections;
import server.IntersectionData;
import server.RouteStopsDownload;
import ui.KahyaUIListener;
import utils.StringSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteMap {

    public static int ACTIVE_BUS_POSITION = -1;
    private String route;
    private String activeBusCode;
    private ArrayList<RouteStop> map; // forwardstops->backwardstops ( merge two directions together )
    private Map<String, Bus> buses; // buses on the map
    private int directionMergePoint; // index where merge happens
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
            // when we initialize a new bus instance, we connect it with RouteMap to determine direction
            // if bus is on a Ring route.
            //( since all stop data is here, we do it this way rather than passing everything to every object )
            // @todo map, mergePoint etc could be made static so we can access it everywhere, we wouldnt need directionListener
            bus.setDirectionListener( ( stops ) -> {
                if( DIR_DEBUG_FLAG ) System.out.println(busCode +"  DIR ACITON!!!   " + stops);
                dirCounters.put(busCode, new DirectionCounter() );
                ArrayList<Integer> prevFoundIndexes = new ArrayList<>();
                for( int j = 0; j < stops.size(); j++ ){
                    String stop = stops.get(j);
                    ArrayList<Integer> foundIndexes = findStopOccurences( stop, 0 );
                    if( DIR_DEBUG_FLAG ) System.out.println("FOUND INDEXES: " + foundIndexes );
                    if( foundIndexes.size() == 1 ){ // singleton durak
                        // if there is only one match, means this stop is on one direction
                        // we can determine which way by comparing it with merge point
                        if( foundIndexes.get(0) > directionMergePoint ){
                            if( DIR_DEBUG_FLAG ) System.out.println(bus.getCode() + "  singleton stop DIR INC: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                            dirCounters.get(busCode).increment(RouteDirection.BACKWARD);
                        } else {
                            if( DIR_DEBUG_FLAG ) System.out.println(bus.getCode() + "  singleton stop DIR INC: " + RouteDirection.returnText(RouteDirection.FORWARD));
                            dirCounters.get(busCode).increment(RouteDirection.FORWARD);
                        }
                    } else if( foundIndexes.size() == 2 ){ // this is expected most of the time
                        // if we have previous indexes, compare them with current pair by pair
                        if( prevFoundIndexes.size() > 1 ){
                           if( ( prevFoundIndexes.get(0) < foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) > foundIndexes.get(1) ) ){ // best case
                               if( DIR_DEBUG_FLAG ) System.out.println(bus.getCode() + "  DIR INC: " + RouteDirection.returnText(RouteDirection.FORWARD));
                               dirCounters.get(busCode).increment(RouteDirection.FORWARD);
                           } else if( ( prevFoundIndexes.get(0) > foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) < foundIndexes.get(1) )  ){ // best case
                               if( DIR_DEBUG_FLAG ) System.out.println(bus.getCode() + "  DIR INC: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                               dirCounters.get(busCode).increment(RouteDirection.BACKWARD);
                           }
                        } else if( prevFoundIndexes.size() == 1 ){ // prev singleton
                            if( prevFoundIndexes.get(0) > directionMergePoint){ // singleton stop on backward dir
                                if( prevFoundIndexes.get(0) < foundIndexes.get(1) ){
                                    dirCounters.get(busCode).increment(RouteDirection.BACKWARD);
                                } else {
                                    dirCounters.get(busCode).increment(RouteDirection.FORWARD);
                                }
                            } else { // singleton stop on forward dir
                                if( prevFoundIndexes.get(0) > foundIndexes.get(0) ){
                                    dirCounters.get(busCode).increment(RouteDirection.FORWARD);
                                } else {
                                    dirCounters.get(busCode).increment(RouteDirection.BACKWARD);
                                }
                            }
                        } else { // no data previously

                        }
                    } else { // no match ??
                        return;
                    }
                    prevFoundIndexes = foundIndexes;
                }
                int dir = dirCounters.get(busCode).getDirection();
                if( dir != -1 ){
                    bus.setDirection(dir);
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

    public void addStatusListener( StatusListener listener ){
        this.statusListener = listener;
    }

    public void addKahyaUIListener( KahyaUIListener listener ){
        this.kahyaUIListener = listener;
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
