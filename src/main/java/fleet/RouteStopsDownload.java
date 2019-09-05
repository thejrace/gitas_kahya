package fleet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.APIRequest;
import utils.Common;

public class RouteStopsDownload {

    private String route, url;
    public RouteStopsDownload( String url, String route ){
        this.route = route;
        this.url = url;
    }

    public JSONArray action(){
        try {
            return new JSONObject(APIRequest.GET(url+"?routeCode="+ Common.replaceTurkishChars(route))).getJSONArray("data").getJSONArray(0); //@todo FIX
        } catch( JSONException e ){
            e.printStackTrace();
        }
        return new JSONArray();
    }

}
