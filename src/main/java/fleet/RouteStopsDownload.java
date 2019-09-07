/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package fleet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.APIRequest;
import utils.Common;

public class RouteStopsDownload {
    /**
     * Route code
     */
    private String route;

    /**
     * API url
     */
    private String url;

    /**
     * First constructor
     *
     * @param url api url
     * @param route route code
     */
    public RouteStopsDownload( String url, String route ){
        this.route = route;
        this.url = url;
    }

    /**
     * Download data from API
     *
     * @return JSONArray
     */
    public JSONArray action(){
        try {
            return new JSONObject(APIRequest.GET(url+"?routeCode="+ Common.replaceTurkishChars(route))).getJSONArray("data").getJSONArray(0); //@todo FIX
        } catch( JSONException e ){
            e.printStackTrace();
        }
        return new JSONArray();
    }
}
