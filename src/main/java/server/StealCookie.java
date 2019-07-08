package server;

import fleet.Refresh_Listener;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class StealCookie {


    public static String FILO5_COOKIE;
    private Refresh_Listener listener;
    public void action(){
        Thread thread = new Thread( () -> {
            Connection.Response res;
            try {
                res = Jsoup.connect("http://gitsistem.com/filotakip/get_cookie?key=nJAHJjksd13")
                        .method(Connection.Method.POST)
                        .timeout(0)
                        .execute();

                FILO5_COOKIE = res.parse().text();
                listener.on_refresh();
            } catch( IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();

    }
    public void addListener( Refresh_Listener listener ){
        this.listener = listener;
    }

}
