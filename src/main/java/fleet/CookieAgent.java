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
import org.jsoup.nodes.Document;
import utils.ThreadHelper;
import java.io.IOException;
import java.util.Scanner;

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
            try {
                if( !FILO5_COOKIE.equals( newCookie ) ){
                    FILO5_COOKIE = newCookie;
                    READY = checkCookie();
                }
            } catch( NullPointerException e ){
                FILO5_COOKIE = newCookie;
                READY = checkCookie();
            }
        } catch( IOException e) {
            return false;
        }
        return true;
    }

    private boolean checkCookie(){
        Filo_Task testTask = new Filo_Task();
        org.jsoup.Connection.Response request = testTask.istek_yap("https://filotakip.iett.gov.tr/_FYS/000/sorgu.php?konum=ana&konu=sefer&hat=15BK");
        Document document = testTask.parse_html( request );
        try {
            document.getElementById("captcha").text();
            System.out.println("Invalid captcha, trying login without captcha!");
            return FiloLoginTask.attempt();
        } catch( NullPointerException e ){
            System.out.println("Valid captcha!");
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
