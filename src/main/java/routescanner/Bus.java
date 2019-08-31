package routescanner;

import org.json.JSONObject;

import java.util.ArrayList;

public class Bus {

    // @todo hat degisiebiliyor onları ayikla
    /**
     * Bus code
     */
    private String code;

    /**
     * Active route
     */
    private String route;

    /**
     * Active stop name
     */
    private String stop;

    /**
     * Position on the RouteMap
     */
    private int position;

    /**
     * Bus direction
     */
    private int direction = -1;

    /**
     * Active run index in runData ( starts from 0 )
     */
    private int activeRunIndex = -1;

    /**
     * Active status of the bus
     */
    private BusStatus status;

    /**
     * Previous status of the bus for comparison
     */
    private BusStatus prevStatus;

    /**
     * Counter for accumulating stops, on ring routes we need 4 stops to correctly determine direction
     */
    private int stopAccumulateCounter = 0;

    /**
     * Merge point index in the RouteMap
     */
    private int directionMergePoint;

    /**
     * List of the bus runs, this is the main data source
     */
    private ArrayList<RunData> runData = new ArrayList<>();

    /**
     * List of the direction type of each run
     */
    private ArrayList<Integer> runTypes = new ArrayList<>();

    /**
     * List of the stops which accumulated while determining ring run's direction
     */
    private ArrayList<String> stopData = new ArrayList<>();

    /**
     * Counter object to decide whether run is forward or backward
     */
    private DirectionCounter dirCounter = new DirectionCounter();

    /**
     * RouteMap instance
     */
    private ArrayList<RouteStop> routeMap;

    /**
     * Flag to set when direction of the run is found
     */
    private boolean dirFoundFlag = false;

    /**
     * Debug flag for initialize method
     */
    private boolean INIT_DEBUG_FLAG = false;

    /**
     * Debug flag for checkStatus method
     */
    private boolean STATUS_DEBUG_FLAG = false;

    /**
     * Debug flag for determineRingRouteDirection method
     */
    private boolean DIR_DEBUG_FLAG = false;

    /**
     *  Constructor 1
     *
     * @param code code of the active bus
     */
    public Bus( String code ){
        this.code = code;
        this.status = BusStatus.UNDEFINED;
        this.position = -1;
    }

    /**
     * Constructor 2
     *
     * @param code code of the active bus
     * @param runData run data of the active bus
     * @param routeMap routeMap list
     * @param directionMergePoint direction merge point
     */
    public Bus( String code, ArrayList<RunData> runData, ArrayList<RouteStop> routeMap, int directionMergePoint ){
        this.code = code;
        this.runData = runData;
        this.status = BusStatus.UNDEFINED;
        this.routeMap = routeMap;
        this.directionMergePoint = directionMergePoint;
    }

    /**
     * Main logic method, triggered from RouteMap.
     * Called everytime fleet data is fetched.
     */
    public void updateStatus(){
        if( !dirFoundFlag ){
            initialize();
        } else {
            checkStatus();
        }
        if( INIT_DEBUG_FLAG ) System.out.println("");
        if( INIT_DEBUG_FLAG ) System.out.println("---------");
    }

    /**
     * This is the method where we try to find following:
     *   - activeRunIndex
     *   - run type of the active run
     *   - direction
     */
    private void initialize(){
        try {
            // 1) find the active run index
            // 2) find the run type of the all runs
            if( activeRunIndex == -1 ){
                if( INIT_DEBUG_FLAG ) System.out.println(code + " initializing..");
                // first init
                // 1 - find active run index
                ArrayList<String> runDetailsList = new ArrayList<>();
                route = runData.get(0).getRoute();
                for( RunData run : runData ){
                    runDetailsList.add(run.getRouteDetails());
                }
                // 2 - find run type of every run
                for( int k = 0; k < runData.size(); k++ ){
                    runTypes.add( RouteDirection.action(route, k, runDetailsList) );
                }
                checkStatus();

                if( status == BusStatus.UNDEFINED ){

                    if( INIT_DEBUG_FLAG ) System.out.println( code+  " UNDEFINED!");
                    return;
                }

                if( !runData.get(activeRunIndex).getCurrentStop().equals("N/A") ){
                    stopData.add(runData.get(activeRunIndex).getCurrentStop());
                    stopAccumulateCounter++;
                }

                prevStatus = status;
            } else {
                // this block is called when run is ring route and direction is not yet found
                if( prevStatus == BusStatus.UNDEFINED ){
                    if( INIT_DEBUG_FLAG ) System.out.println(code + " status UNDEFINED");
                } else if( prevStatus == BusStatus.ACTIVE ){
                    checkStatus();
                    if( status == BusStatus.ACTIVE ){ // still on active run
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and still active..  ##");
                        collectStopForDirection();
                    } else if( status == BusStatus.WAITING ){ // finished the run
                        // on ring routes, when run is finished, next run's direction can be determined from route details
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and now finished DIR: " + RouteDirection.returnText(direction)+ " ##  ");
                    }
                } else if( prevStatus == BusStatus.WAITING ){
                    checkStatus();
                    // todo gerek var mı la buraya
                    if( status == BusStatus.ACTIVE ){ // starting the run
                        // active run index wont' be changed, we can get the dir from route details
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was waiting and now active DIR: " + RouteDirection.returnText(direction) + " ##  ");
                    } else if( status == BusStatus.WAITING ){
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                    }
                }
            }
        } catch( Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Collects the active stop data to determine run direction
     */
    private void collectStopForDirection(){
        if( runData.get(activeRunIndex).getCurrentStop().equals("N/A") ) return;
        if( stopData.size() == 0 ){
            stopData.add(runData.get(activeRunIndex).getCurrentStop());
            stopAccumulateCounter++;
        } else {
            if( INIT_DEBUG_FLAG ) System.out.println("["+runData.get(activeRunIndex).getCurrentStop()+"]" + " -- [" + stopData.get(stopData.size()-1 )+"]  ");
            if( !runData.get(activeRunIndex).getCurrentStop().equals(stopData.get(stopData.size()-1 ) ) || !stopData.contains(runData.get(activeRunIndex).getCurrentStop())){ // accumulate if stop has changed
                stopData.add(runData.get(activeRunIndex).getCurrentStop());
                stopAccumulateCounter++;
                if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and accumulate stops ("+runData.get(activeRunIndex).getCurrentStop()+") COUNTER: "+stopAccumulateCounter+"  ###  ");
                if( stopAccumulateCounter == 4 ){ // after 4 collection determine the direction
                    //accumulatorListener.afterAcculumate(stopData);
                    determineRingRouteDirection();
                    stopAccumulateCounter = 0;
                    stopData = new ArrayList<>();
                    // @todo reset stop data as well after tests
                }
            }
        }
    }

    /**
     * After finding activeRunIndex in initialize, this is where
     * we determine the active status of the bus
     */
    private void checkStatus(){
        try {
            int index = 0, activeIndex = -1, waitingIndex = -1;
            for( RunData run : runData ){
                // find the current or next run
                if( run.getStatus().equals("A") && activeIndex == -1 ){ // first active sometimes there are two active runs ( duplicate )
                    activeIndex = index;
                }
                if( run.getStatus().equals("B") && waitingIndex == -1 ){ // first waiting
                    waitingIndex = index;
                }
                index++;
            }
            if( activeIndex > -1 ){
                status = BusStatus.ACTIVE;
                activeRunIndex = activeIndex;
                if( STATUS_DEBUG_FLAG ) System.out.print(code + " is active ## ");
            } else if( waitingIndex > -1 ){
                status = BusStatus.WAITING;
                activeRunIndex = waitingIndex;
                if( STATUS_DEBUG_FLAG ) System.out.print(code + " is waiting ##");
            } else {
                // done or failed the runs
                if( STATUS_DEBUG_FLAG ) System.out.print(code + "   FINISHED OR FAILED ##");
                status = BusStatus.UNDEFINED;
                return;
            }
            stop = runData.get(activeRunIndex).getCurrentStop();
            int tempDirection = runTypes.get(activeRunIndex); // get the direction of the current run
            // for ring routes we need to check active status in order to update
            // direction information after initialization
            if( tempDirection == RouteDirection.RING ){
                if( prevStatus == BusStatus.ACTIVE ){
                    if( status == BusStatus.WAITING ){
                        // was active, now finished means it will start over
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        dirFoundFlag = true;
                    } else if( status == BusStatus.ACTIVE ){
                        // if bus passed the merge point but direction is somehow calculated wrong
                        // we check it and correct it
                        if( direction == RouteDirection.FORWARD && position > directionMergePoint ){
                            dirFoundFlag = false;
                            return;
                        }
                        // if status is active, we have to check if bus crossed the intersection point
                        // if it did we change the direction
                        if( direction == RouteDirection.FORWARD ){
                            if( routeMap.get(directionMergePoint).getName().equals(stop) ){
                                direction = RouteDirection.BACKWARD;
                            }
                        }
                    } else { // undefined
                        // @todo what to do? warn RouteMap to remove it?
                    }
                } else if( prevStatus == BusStatus.WAITING ){
                    if( status != BusStatus.UNDEFINED ){
                        // was waiting now on the move or still waiting
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        dirFoundFlag = true;
                    } else{
                        // @todo what to do? warn RouteMap to remove it?
                    }
                }
            } else {
                direction = tempDirection;
                dirFoundFlag = true;
            }
            if( STATUS_DEBUG_FLAG )  System.out.print(code + " on a " + RouteDirection.returnText(direction) + "  RUN  ## ");
            prevStatus = status;
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * After accumulating 4 stop data, this is where we decide the direction
     */
    private void determineRingRouteDirection(){
        if( DIR_DEBUG_FLAG ) System.out.println(code +"  DIR ACITON!!!   " + stopData );
        ArrayList<Integer> prevFoundIndexes = new ArrayList<>();
        for( int j = 0; j < stopData.size(); j++ ){
            String stop = stopData.get(j);
            ArrayList<Integer> foundIndexes = RouteMap.findStopOccurences( routeMap, stop, 0 );
            if( DIR_DEBUG_FLAG ) System.out.println("FOUND INDEXES: " + foundIndexes );
            if( foundIndexes.size() == 1 ){ // singleton durak
                // if there is only one match, means this stop is on one direction
                // we can determine which way by comparing it with merge point
                if( foundIndexes.get(0) > directionMergePoint ){
                    if( DIR_DEBUG_FLAG ) System.out.println(code + "  singleton stop DIR INC: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                    dirCounter.increment(RouteDirection.BACKWARD);
                } else {
                    if( DIR_DEBUG_FLAG ) System.out.println(code + "  singleton stop DIR INC: " + RouteDirection.returnText(RouteDirection.FORWARD));
                    dirCounter.increment(RouteDirection.FORWARD);
                }
            } else if( foundIndexes.size() == 2 ){ // this is expected most of the time
                // if we have previous indexes, compare them with current pair by pair
                if( prevFoundIndexes.size() > 1 ){
                    if( ( prevFoundIndexes.get(0) < foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) > foundIndexes.get(1) ) ){ // best case
                        if( DIR_DEBUG_FLAG ) System.out.println(code + "  DIR INC: " + RouteDirection.returnText(RouteDirection.FORWARD));
                        dirCounter.increment(RouteDirection.FORWARD);
                    } else if( ( prevFoundIndexes.get(0) > foundIndexes.get(0) ) && ( prevFoundIndexes.get(1) < foundIndexes.get(1) )  ){ // best case
                        if( DIR_DEBUG_FLAG ) System.out.println(code + "  DIR INC: " + RouteDirection.returnText(RouteDirection.BACKWARD));
                        dirCounter.increment(RouteDirection.BACKWARD);
                    }
                } else if( prevFoundIndexes.size() == 1 ){ // prev singleton
                    if( prevFoundIndexes.get(0) > directionMergePoint){ // singleton stop on backward dir
                        if( prevFoundIndexes.get(0) < foundIndexes.get(1) ){
                            dirCounter.increment(RouteDirection.FORWARD);
                        } else {
                            dirCounter.increment(RouteDirection.BACKWARD);
                        }
                    } else { // singleton stop on forward dir
                        if( prevFoundIndexes.get(0) > foundIndexes.get(0) ){
                            dirCounter.increment(RouteDirection.BACKWARD);
                        } else {
                            dirCounter.increment(RouteDirection.FORWARD);
                        }
                    }
                } else { // no data previously

                }
            } else { // no match ??
                return;
            }
            prevFoundIndexes = foundIndexes;
        }
        int dir = dirCounter.getDirection();
        if( dir != -1 ){
            direction = dir;
            dirFoundFlag = true;
        } else {
            initialize(); // start over collecting
        }

    }

    /**
     * Update the run data
     *
     * @param runData updated run data
     */
    public void setRunData(ArrayList<RunData> runData) {
        this.runData = runData;
    }

    /**
     * Getter for stop
     * @return stop name
     */
    public String getStop() {
        return stop;
    }

    /**
     * Getter for direction
     * @return direction of the bus
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Setter for position
     * @param position new position on the route map
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Getter for status
     * @return status of the bus
     */
    public BusStatus getStatus() {
        return status;
    }

    /**
     * Serialize data for logging
     * @return serialzed data
     */
    public String toString(){
        return "["+code+"] ["+route+"] ["+RouteDirection.returnText(direction)+"] ["+status+"] [@"+stop+"] [POS:"+position+"] [ARInd:"+activeRunIndex+"]";
    }

    public JSONObject toJSON(){
        JSONObject data = new JSONObject();
        data.put("route", route);
        data.put("code", code);
        data.put("direction", direction);
        data.put("status", status);
        data.put("stop", stop);
        data.put("position", position);
        return data;
    }

    public String serialize(){
        return toJSON().toString();
    }

}
