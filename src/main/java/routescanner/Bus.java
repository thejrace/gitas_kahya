package routescanner;

import fleet.RunData;
import org.json.JSONArray;

import java.util.ArrayList;

public class Bus {

    private String code;
    private int direction;
    private int status;
    private ArrayList<RunData> runData;


    public Bus( String code ){
        this.code = code;
        runData = new ArrayList<>();
    }

    public void updateStatus( JSONArray fleetData ){ // called everytime new fleet data is fetched
        // determine status
        // determnine direction ( once direction is found, determine it according to status )

    }

}
