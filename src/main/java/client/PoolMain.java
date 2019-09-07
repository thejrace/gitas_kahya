/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package client;

import fleet.CookieAgent;
import org.json.JSONObject;
import pool.RouteScannerPool;
import utils.APIRequest;
import utils.Common;

import java.io.File;

public class PoolMain {

    /**
     * Entry point for PoolMain version
     *
     * @param args args[0] is the location of the config.json
     */
    public static void main(String[] args){
        JSONObject config = new JSONObject(Common.readJSONFile(new File(args[0])));
        APIRequest.API_TOKEN = config.getString("api_token");

        CookieAgent cookieAgent = new CookieAgent(config.getJSONObject("cookie_agent")); // @todo trigger li olacak bu
        cookieAgent.action();

        RouteScannerPool routeScannerPool = new RouteScannerPool(config);
        routeScannerPool.start();
    }
}
