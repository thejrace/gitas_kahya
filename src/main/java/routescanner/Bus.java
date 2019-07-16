package routescanner;

import fleet.RouteDirection;
import fleet.RunData;
import org.json.JSONArray;

import java.util.ArrayList;

public class Bus {

    private String code;
    private String route;
    private int direction = -1;
    private BusStatus status;
    private BusStatus prevStatus;
    private int position;
    private int stopAccumulateCounter = 0;
    private ArrayList<RunData> runData = new ArrayList<>();
    private ArrayList<Integer> runTypes = new ArrayList<>();
    private ArrayList<String> stopData = new ArrayList<>();

    private boolean dirFoundFlag = false;
    private boolean ringDirectionFlag = false;
    private int activeRunIndex = -1;
    private BusStopAccumulatorListener accumulatorListener;
    public Bus( String code ){
        this.code = code;
        this.status = BusStatus.UNDEFINED;
        this.position = -1;
    }

    public Bus( String code, ArrayList<RunData> runData ){
        this.code = code;
        this.runData = runData;
    }

    public void updateStatus(){ // called everytime new fleet data is fetched
        if( !dirFoundFlag ){
            initialize();
        } else {
            update();
        }
    }

    private void initialize(){
        // 1) find the active run index
        // 2) find the run type of the all runs
        if( activeRunIndex == -1 ){
            if( RouteScanner.DEBUG ) System.out.println(code + " initializing..");
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
            if( prevStatus == BusStatus.ACTIVE ){
                checkStatus();
                if( status == BusStatus.ACTIVE ){ // still on active run
                    if( RouteScanner.DEBUG ) System.out.println(code + " was active and still active..");
                    if( stopAccumulateCounter == 3 ){ // after 4 collection determine the direction
                        accumulatorListener.afterAcculumate(stopData);
                        stopAccumulateCounter = 0;
                        // @todo reset stop data as well after tests
                        return;
                    }
                    if( !runData.get(activeRunIndex).getCurrentStop().equals(stopData.get(stopData.size()-1 ) ) ){ // accumulate if stop has changed
                        stopData.add(runData.get(activeRunIndex).getCurrentStop());
                        stopAccumulateCounter++;
                        if( RouteScanner.DEBUG ) System.out.println(code + " was active and accumulate stops ("+runData.get(activeRunIndex).getCurrentStop()+")");
                    }
                } else if( status == BusStatus.WAITING ){ // finished the run
                    // on ring routes, when run is finished, next run's direction can be determined from route details
                    direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                    if( RouteScanner.DEBUG ) System.out.println(code + " was active and now finished DIR: " + RouteDirection.returnText(direction));
                }
            } else if( prevStatus == BusStatus.WAITING ){
                checkStatus();
                if( status == BusStatus.ACTIVE ){ // starting the run
                    // active run index wont' be changed, we can get the dir from route details
                    direction = RouteDirection.getDirectionLetter(route.length(), runData.get(activeRunIndex).getRouteDetails());
                    if( RouteScanner.DEBUG ) System.out.println(code + " was waiting and now active DIR: " + RouteDirection.returnText(direction));
                }
            }

        }
    }

    private void update(){

        checkStatus();
        System.out.println(code + "  " + RouteDirection.returnText(direction) + "  -->  " + status );

    }

    private void checkStatus(){
        int index = 0, activeIndex = 0, waitingIndex = 0;
        for( RunData run : runData ){
            // find the current or next run
            if( run.getStatus().equals("A") && activeIndex == 0 ){ // first active sometimes there are two active runs ( duplicate )
                activeIndex = index;
            }
            if( run.getStatus().equals("B") && waitingIndex == 0 ){ // first waiting
                waitingIndex = index;
            }
            index++;
        }
        if( activeIndex > 0 ){
            status = BusStatus.ACTIVE;
            activeRunIndex = activeIndex;
            System.out.println(code + " is active!");
        } else if( waitingIndex > 0 ){
            status = BusStatus.WAITING;
            activeRunIndex = waitingIndex;
            System.out.println(code + " is waiting!");
        } else {
            // done or failed the runs
            System.out.println(code + "   FINISHED OR FAILED");
            return;
        }
        direction = runTypes.get(activeRunIndex); // get the direction of the current run
        System.out.println(code + " on a " + RouteDirection.returnText(direction) + "  RUN");
    }

    public void setDirectionListener( BusStopAccumulatorListener listener ){
        this.accumulatorListener = listener;
    }

    private void initializeOLD(){
        // find active run index -> status -> run type
        if( activeRunIndex == -1 ){
            // firstly we check the run type
            int index = 0, activeIndex = 0, waitingIndex = 0;
            ArrayList<String> runDetailsList = new ArrayList<>();
            route = runData.get(0).getRoute();
            for( RunData run : runData ){
                // find the current or next run
                if( run.getStatus().equals("A") && activeIndex == 0 ){ // first active sometimes there are two active runs ( duplicate )
                    activeIndex = index;
                }
                if( run.getStatus().equals("B") && waitingIndex == 0 ){ // first waiting
                    waitingIndex = index;
                }
                runDetailsList.add(run.getRouteDetails());
                index++;
            }
            if( activeIndex > 0 ){
                status = BusStatus.ACTIVE;
                direction = RouteDirection.action(route, activeIndex, runDetailsList );
                activeRunIndex = activeIndex;
            } else if( waitingIndex > 0 ){
                status = BusStatus.WAITING;
                direction = RouteDirection.action(route, waitingIndex, runDetailsList );
                activeRunIndex = waitingIndex;
            } else {
                // done or failed the runs
                System.out.println(code + "   FINISHED OR FAILED");
                return;
            }
            if( direction == RouteDirection.RING ){
                // ring, we will compare stop information
                ringDirectionFlag = true;
            } else {
                // normal
                dirFoundFlag = true;
            }
        } else {
            // if we're here, it means active index is found but direction is no yet found ( ring )
            // we will compare stop informations
            if( status == BusStatus.ACTIVE ){ // status in this if condition is actually previous status
                // check if run is done or not
                if( runData.get(activeRunIndex).getStatus().equals("T") ){

                } else if( runData.get(activeRunIndex).getStatus().equals("I") ||  runData.get(activeRunIndex).getStatus().equals("Y") ){
                    System.out.println(code + "   bus failed!");
                    return;
                }
                // we will start comparison
                stopData.add(runData.get(activeRunIndex).getCurrentStop());

            } else if( status == BusStatus.WAITING ){
                // we will wait until bus starts it's run
            }
        }
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

}
