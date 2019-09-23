/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package fleet;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Filo_Task {

    protected JSONObject output = new JSONObject();
    protected boolean errorFlag = false;
    protected String errorMessage;
    protected String oto;

    protected org.jsoup.Connection.Response istek_yap( String url ){
        try {
            return Jsoup.connect(url + oto)
                    .cookie("PHPSESSID", CookieAgent.FILO5_COOKIE)
                    .method(org.jsoup.Connection.Method.POST)
                    .timeout(40*1000)
                    .execute();
        } catch (Exception e) {}
        return null;
    }
    protected Document parse_html(org.jsoup.Connection.Response req ){
        try {
            return req.parse();
        } catch( Exception e ){}
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

