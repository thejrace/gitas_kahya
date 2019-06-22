package client;

import fleet.RunData;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;
import utils.RunNoComparator;

import java.io.File;
import java.util.*;

public class ClientMain {

    public static void main(String[] args){


        String oto = "C-1751";


        KahyaClient client = new KahyaClient(oto);
        //client.start();


        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource("test_fleet_data.json").getFile());


        JSONObject testFleedData = new JSONObject(Common.readJSONFile(file));

        Iterator<String> busCodes = testFleedData.keys();


        Map<String, ArrayList<RunData> > fleetRunData = new HashMap<>();
        JSONArray tempData;
        JSONObject tempRunData;
        while( busCodes.hasNext() ) {
            String key = busCodes.next(); // bus code
            if( !fleetRunData.containsKey(key) ) fleetRunData.put(key, new ArrayList<>());
            tempData = testFleedData.getJSONArray(key);
            for( int k = 0; k < tempData.length(); k++ ){
                tempRunData = tempData.getJSONObject(k);
                fleetRunData.get(key).add( new RunData(
                        tempRunData.getString("bus_code"),
                        Integer.valueOf(tempRunData.getString("no")),
                        tempRunData.getString("stop"),
                        tempRunData.getString("dep_time"),
                        tempRunData.getString("route_details"),
                        tempRunData.getString("status")
                ));

            }
            Collections.sort(fleetRunData.get(key), new RunNoComparator() );
        }


    }

}
