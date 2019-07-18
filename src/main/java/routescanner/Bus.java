package routescanner;

import fleet.RouteDirection;
import fleet.RunData;
import fleet.UIBusData;

import java.util.ArrayList;

public class Bus {

    // @todo hat degisiebiliyor onları ayikla
    private String code;
    private String route;
    private String stop;
    private int position;
    private int direction = -1;
    private int activeRunIndex = -1;
    private BusStatus status;
    private BusStatus prevStatus;
    private int stopAccumulateCounter = 0;

    private ArrayList<RunData> runData = new ArrayList<>();
    private ArrayList<Integer> runTypes = new ArrayList<>();
    private ArrayList<String> stopData = new ArrayList<>();

    private boolean dirFoundFlag = false;
    private BusStopAccumulatorListener accumulatorListener;

    private static boolean INIT_DEBUG_FLAG = false;
    private static boolean STATUS_DEBUG_FLAG = false;

    public Bus( String code ){
        this.code = code;
        this.status = BusStatus.UNDEFINED;
        this.position = -1;
    }

    public Bus( String code, ArrayList<RunData> runData ){
        this.code = code;
        this.runData = runData;
        this.status = BusStatus.UNDEFINED;
    }

    public void updateStatus(){ // called everytime new fleet data is fetched
        if( !dirFoundFlag ){
            initialize();
        } else {
            checkStatus();
        }
        if( INIT_DEBUG_FLAG ) System.out.println("");
        if( INIT_DEBUG_FLAG ) System.out.println("---------");
    }

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
                prevStatus = status;
            } else {
                // this block is called when run is ring route and direction is not yet found
                if( prevStatus == BusStatus.UNDEFINED ){
                    if( INIT_DEBUG_FLAG ) System.out.println(code + " status UNDEFINED");
                } else if( prevStatus == BusStatus.ACTIVE ){
                    checkStatus();
                    if( status == BusStatus.ACTIVE ){ // still on active run
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and still active..  ##");
                        if( runData.get(activeRunIndex).getCurrentStop().equals("N/A") ) return;
                        if( stopData.size() == 0 ){
                            stopData.add(runData.get(activeRunIndex).getCurrentStop());
                            stopAccumulateCounter++;
                        } else {
                            if( INIT_DEBUG_FLAG ) System.out.println("["+runData.get(activeRunIndex).getCurrentStop()+"]" + " -- [" + stopData.get(stopData.size()-1 )+"]  ");
                            if( !runData.get(activeRunIndex).getCurrentStop().equals(stopData.get(stopData.size()-1 ) ) ){ // accumulate if stop has changed
                                stopData.add(runData.get(activeRunIndex).getCurrentStop());
                                stopAccumulateCounter++;
                                if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and accumulate stops ("+runData.get(activeRunIndex).getCurrentStop()+") COUNTER: "+stopAccumulateCounter+"  ###  ");
                                if( stopAccumulateCounter == 4 ){ // after 4 collection determine the direction
                                    accumulatorListener.afterAcculumate(stopData);
                                    stopAccumulateCounter = 0;
                                    //stopData = null;
                                    // @todo reset stop data as well after tests
                                }
                            }
                        }
                    } else if( status == BusStatus.WAITING ){ // finished the run
                        // on ring routes, when run is finished, next run's direction can be determined from route details
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was active and now finished DIR: " + RouteDirection.returnText(direction)+ " ##  ");
                    }
                } else if( prevStatus == BusStatus.WAITING ){
                    checkStatus();
                    if( status == BusStatus.ACTIVE ){ // starting the run
                        // active run index wont' be changed, we can get the dir from route details
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        if( INIT_DEBUG_FLAG ) System.out.print(code + " was waiting and now active DIR: " + RouteDirection.returnText(direction) + " ##  ");
                    }
                }
            }
        } catch( Exception e){
            e.printStackTrace();
        }
    }

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
                    } else if( status == BusStatus.UNDEFINED ){
                        // @todo what to do? warn RouteMap to remove it?
                    }
                } else if( prevStatus == BusStatus.WAITING ){
                    if( status == BusStatus.ACTIVE ){
                        // was waiting now on the move
                        direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                        dirFoundFlag = true;
                    } else if( status == BusStatus.UNDEFINED ){
                        // @todo what to do? warn RouteMap to remove it?
                    }
                }
            } else {
                direction = tempDirection;
                dirFoundFlag = true;
                // remove direction listener
                this.accumulatorListener = null;
            }
            if( STATUS_DEBUG_FLAG )  System.out.print(code + " on a " + RouteDirection.returnText(direction) + "  RUN  ## ");
            prevStatus = status;
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public void setDirectionListener( BusStopAccumulatorListener listener ){
        this.accumulatorListener = listener;
    }

    public ArrayList<RunData> getRunData() {
        return runData;
    }

    public void setRunData(ArrayList<RunData> runData) {
        this.runData = runData;
    }

    public void setDirection( int direction ){
        this.direction = direction;
        dirFoundFlag = true;
    }
    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public int getDirection() {
        return direction;
    }

    public UIBusData getUIData(){
        return new UIBusData(code, stop, RouteMap.ACTIVE_BUS_POSITION - position, runData.get(activeRunIndex).getRouteDetails(), RouteDirection.returnText(direction), status );
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public BusStatus getStatus() {
        return status;
    }

    public void setStatus(BusStatus status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public boolean getDirFoundFlag(){
        return dirFoundFlag;
    }

    public String toString(){
        return "["+code+"] ["+route+"] ["+RouteDirection.returnText(direction)+"] ["+status+"] [@"+stop+"] [POS:"+position+"] [ARInd:"+activeRunIndex+"]";
    }

}
