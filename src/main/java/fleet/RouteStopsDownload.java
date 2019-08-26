package fleet;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.APIRequest;

public class RouteStopsDownload {

    private String route, url;
    public RouteStopsDownload( String url, String route ){
        this.route = route;
        this.url = url;
    }

    public JSONArray action(){
        return new JSONObject(APIRequest.GET(url+route)).getJSONArray("data").getJSONArray(0);
    }

}
