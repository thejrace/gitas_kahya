package routescanner;

import fleet.RunData;
import org.json.JSONArray;

import java.util.ArrayList;

public class Bus {

    private String code;
    private int direction;
    private BusStatus status;
    private int position;
    private ArrayList<RunData> runData;


    public Bus( String code ){
        this.code = code;
        this.status = BusStatus.UNDEFINED;
        this.position = -1;
        runData = new ArrayList<>();
    }

    public void updateStatus( JSONArray fleetData ){ // called everytime new fleet data is fetched
        // determine status
        // determnine direction ( once direction is found, determine it according to status )

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
