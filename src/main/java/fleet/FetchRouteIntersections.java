package fleet;

import database.DBC;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import utils.Web_Request;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetchRouteIntersections {


    public static JSONArray action( String activeRoute ){
        org.jsoup.Connection.Response res;
        try {
            res = Jsoup.connect(Web_Request.API_URL_PREFIX+"?req=route_intersection&route="+activeRoute)
                    .method(org.jsoup.Connection.Method.GET)
                    .timeout(0)
                    .execute();

            return new JSONArray(res.parse().text());
        } catch( IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }
}