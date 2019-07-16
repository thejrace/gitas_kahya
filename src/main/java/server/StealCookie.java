package server;

import fleet.Refresh_Listener;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import utils.Web_Request;

import java.io.IOException;

public class StealCookie {


    public static String FILO5_COOKIE;
    private Refresh_Listener listener;
    public void action(){
        Thread thread = new Thread( () -> {
            if( !request("http://192.168.2.177/filotakip/get_cookie?key=nJAHJjksd13" ) ){
                request("http://gitsistem.com/filotakip/get_cookie?key=nJAHJjksd13");
                Web_Request.SERVER_URL = "http://gitsistem.com";
                Web_Request.API_URL_PREFIX = "http://gitsistem.com:81/kahya_test.php";
            } else {
                Web_Request.SERVER_URL = "http://192.168.2.177";
                Web_Request.API_URL_PREFIX = "http://192.168.2.177:81/kahya_test.php";
            }
        });
        thread.setDaemon(true);
        thread.start();


    }
    public void addListener( Refresh_Listener listener ){
        this.listener = listener;
    }

    private boolean request( String url ){
        Connection.Response res;
        try {
            res = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .timeout(5000)
                    .execute();

            FILO5_COOKIE = res.parse().text();
            listener.on_refresh();
        } catch( IOException e) {
            System.out.println("switch request adress!");
            //e.printStackTrace();
            return false;
        }
        return true;
    }

}
