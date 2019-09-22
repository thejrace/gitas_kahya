package fleet;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public class FiloLoginTask {

    public static boolean attempt(){
        Connection.Response res;
        try {
            res = Jsoup.connect("https://filotakip.iett.gov.tr/login.php")
                    .method(Connection.Method.POST)
                    .timeout(0)
                    .execute();

            CookieAgent.FILO5_COOKIE = res.cookies().get("PHPSESSID");
            CookieAgent.READY = true;
        } catch( IOException e) {
            return false;
        }
        return true;
    }

}
