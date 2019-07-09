package fleet;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import server.StealCookie;

import java.io.IOException;

public class Filo_Task {

    protected JSONObject output = new JSONObject();
    protected boolean errorFlag = false;
    protected String errorMessage;

    protected String oto;
    protected org.jsoup.Connection.Response istek_yap( String url ){
        try {
            return Jsoup.connect(url + oto)
                    .cookie("PHPSESSID", StealCookie.FILO5_COOKIE)
                    .method(org.jsoup.Connection.Method.POST)
                    .timeout(40*1000)
                    .execute();
        } catch (IOException | NullPointerException e) {
            //System.out.println( "["+Common.get_current_hmin() + "]  "+ aktif_tarih  + " " +  oto + " " + logprefix + "veri alım hatası. Tekrar deneniyor.");
            e.printStackTrace();
        }
        return null;
    }
    protected Document parse_html(org.jsoup.Connection.Response req ){
        try {
            return req.parse();
        } catch( IOException | NullPointerException e ){
            e.printStackTrace();
            //System.out.println(  "["+Common.get_current_hmin() + "]  "+ aktif_tarih  + " " +  oto + " "+ logprefix + " parse hatası. Tekrar deneniyor.");
        }
        return null;
    }

    public JSONObject getOutput(){
        return output;
    }

    public boolean getErrorFlag() {
        return errorFlag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

