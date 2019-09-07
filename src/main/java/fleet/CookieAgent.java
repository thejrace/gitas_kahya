/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package fleet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import utils.ThreadHelper;
import java.io.IOException;

public class CookieAgent {
    /**
     * Shared cookie
     */
    public static String FILO5_COOKIE;

    /**
     * Status flag
     */
    public static boolean READY = false;

    /**
     * Config
     */
    private JSONObject config;

    public CookieAgent(JSONObject config){
        this.config = config;
    }

    /**
     * Request cookie from API
     */
    public void action(){
        Thread thread = new Thread(() -> {
            while(true){
                JSONArray urls = config.getJSONArray("urls");
                for( int k = 0; k < urls.length(); k++ ){
                    if( request(urls.getString(k)) ) break;
                }
                ThreadHelper.delay(config.getInt("cookie_agent_delay"));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Request method to get cookie
     *
     * @param url API url
     */
    private boolean request( String url ){
        Connection.Response res;
        try {
            res = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .timeout(3000)
                    .execute();

            String newCookie = res.parse().text();
            if( !FILO5_COOKIE.equals( newCookie ) ){
                FILO5_COOKIE = newCookie;
                READY = true;
            }
        } catch( IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Getter for READY flag
     *
     * @return ready flag
     */
    public static boolean isReady(){
        return READY;
    }
}
