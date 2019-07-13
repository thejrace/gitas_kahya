package server;

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

    public static JSONArray actionDB( String activeRoute ){
        JSONArray output = new JSONArray();
        try {
            Connection con = DBC.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM hat_kesisim WHERE aktif_hat = ?");
            st.setString(1, activeRoute );
            ResultSet res = st.executeQuery();
            JSONObject tempData;
            while(res.next()){
                tempData = new JSONObject();
                tempData.put("intersected_route", res.getString("kesisen_hat"));
                tempData.put("direction",res.getInt("yon"));
                tempData.put("stop_name",res.getString("durak_adi"));
                tempData.put("total_diff",res.getInt("total_diff"));
                output.put(tempData);
            }
            res.close();
            st.close();
            con.close();
        } catch( SQLException e ){
            e.printStackTrace();
        }
        return output;
    }
}