package pool;

import org.json.JSONObject;
import routescanner.RouteScanner;
import utils.ThreadHelper;
import java.util.HashMap;
import java.util.Map;

public class RouteScannerPool extends Thread{

    /**
     * Config data
     */
    private JSONObject config;

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

            if(!config.getBoolean("status") ){
                ThreadHelper.logStatus(threadName, "IDLE!");
                ThreadHelper.delay(config.getInt("idle_interval"));
                continue;
            }

            getRouteScannerList();
            configureRouteScanners();

            ThreadHelper.delay(config.getInt("active_interval"));
        }

    }

    /**
     * Get service settings from API
     */
    private void getSettings(){

    }

    /**
     * Get scanner list from API
     */
    private void getRouteScannerList(){
        if( !routeScannerList.containsKey("15BK") ) routeScannerList.put("15BK", new RouteScanner("15BK"));
        ThreadHelper.delay(100);
        if( !routeScannerList.containsKey("11ÜS" ) ) routeScannerList.put("11ÜS",new RouteScanner("11ÜS"));
    }

    /**
     * Start/Stop route scanners according to their settings
     */
    private void configureRouteScanners(){
        for( Map.Entry<String, RouteScanner> entry : routeScannerList.entrySet() ){
            RouteScanner routeScanner = entry.getValue();
            routeScanner.updateSettings(new JSONObject());
            if( !routeScanner.isStarted() ) routeScanner.start();
        }
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
