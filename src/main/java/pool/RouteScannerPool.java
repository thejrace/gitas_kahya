/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package pool;

import fleet.CookieAgent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import routescanner.RouteScanner;
import utils.APIRequest;
import utils.Common;
import utils.RunTimeDiff;
import utils.ThreadHelper;
import java.util.HashMap;
import java.util.Map;

public class RouteScannerPool extends Thread{

    /**
     * Config data
     */
    private JSONObject config;

    /**
     * Settings data
     */
    private JSONObject settings = new JSONObject();

    /**
     * Status flag
     */
    private boolean status = false;

    /**
     * Thread instance
     */
    private Thread thread;

    /**
     * Name of the thread
     */
    private String threadName = "Route Scanner Pool Thread";

    /**
     * List to hold route scanners
     */
    private Map<String, RouteScanner> routeScannerList = new HashMap<>();

    private static int activeThreadCount = 0;
    private static int threadLimit = 10;

    /**
     * Constructor
     *
     * @param config config data ( apiToken, url's etc. )
     */
    public RouteScannerPool( JSONObject config ){
        this.config = config;
    }

    /**
     * Main logic method
     */
    public void run(){

        while (true) {

            getSettings();

            if( !CookieAgent.isReady() ){
                ThreadHelper.logStatus(threadName, " WAITING CookieAgent!!");
                ThreadHelper.delay(settings.getInt("cookie_agent_delay"));
                continue;
            }

            if( !checkInWorkingHours() ){
                ThreadHelper.logStatus(threadName, "IDLE!");
                ThreadHelper.delay(settings.getInt("idle_interval"));
                clearScanners();
                continue;
            }

            getRouteScannerList();

            if(!status){
                ThreadHelper.logStatus(threadName, "IDLE!");
                ThreadHelper.delay(settings.getInt("idle_interval"));
                continue;
            }

            configureRouteScanners();

            ThreadHelper.delay(settings.getInt("active_interval"));

        }
    }

    /**
     * Get service settings from API
     */
    private void getSettings(){
        try {
            JSONObject apiResponse = new JSONObject(APIRequest.GET(config.getString("get_settings_api_url"))).getJSONObject("data");
            JSONArray settingsArray = apiResponse.getJSONArray("settings");
            for( int k = 0; k < settingsArray.length(); k++ ){
                JSONObject setting = settingsArray.getJSONObject((k));
                settings.put(setting.getString("key"), setting.get("value"));
            }
            status = apiResponse.getBoolean("status");
            threadLimit = settings.getInt("parallel_scanner_thread_limit");
        } catch( JSONException e ){
            e.printStackTrace();
        }
        System.out.println(settings);
    }

    /**
     * Check if we're in working hours or not
     *
     * @return
     */
    private boolean checkInWorkingHours(){
        String currentHmin = Common.getCurrentHmin();
        if( RunTimeDiff.isPast(currentHmin, settings.getString("idle_start_hmin")) && !RunTimeDiff.isPast(currentHmin, settings.getString("idle_end_hmin") )){
            return false;
        }
        return true;
    }

    /**
     * Stops and clears route scanners
     */
    private void clearScanners(){
        for( Map.Entry<String, RouteScanner> entry : routeScannerList.entrySet() ){
            RouteScanner routeScanner = entry.getValue();
            routeScanner.destroy();
        }
        routeScannerList = new HashMap<>();
        System.out.println("Route scanners are cleared!");
    }

    /**
     * Get scanner list from API
     */
    private void getRouteScannerList(){
        try {
            JSONArray routeScanners = new JSONObject(APIRequest.GET(config.getString("get_route_scanners_list_url"))).getJSONArray("data");
            for( int k = 0; k < routeScanners.length(); k++ ){
                String code = routeScanners.getJSONObject(k).getString("code");
                if( !routeScannerList.containsKey(code) ){
                    ThreadHelper.delay(100);
                    routeScannerList.put(code, new RouteScanner(code));
                }
                routeScannerList.get(code).updateStatus(routeScanners.getJSONObject(k).getBoolean("status"));
            }
        } catch( JSONException e ) {
            e.printStackTrace();
        }
        // @todo loop through routeScannerList if there is a shutdown, kill that scanner instance
    }

    /**
     * Start/Stop route scanners according to their settings
     */
    private void configureRouteScanners(){
        for( Map.Entry<String, RouteScanner> entry : routeScannerList.entrySet() ){
            RouteScanner routeScanner = entry.getValue();
            routeScanner.updateSettings(settings);
            if( !routeScanner.isStarted() ){
                routeScanner.start();
            }
            ThreadHelper.delay(settings.getInt("configure_route_scanner_delay"));
        }
    }

    synchronized public static void incThreadCount(){
        activeThreadCount++;
        System.out.println("ACTIVE THREAD COUNT  : " + activeThreadCount);
    }

    synchronized public static void decThreadCount(){
        activeThreadCount--;
        System.out.println("ACTIVE THREAD COUNT  : " + activeThreadCount);
    }

    synchronized public static boolean getConfirmation(){
        return activeThreadCount < threadLimit;
    }

    /**
     * Thread start method
     */
    public void start(){
        if( thread == null ){
            thread = new Thread(this, threadName);
            thread.start();
        }
    }

}
