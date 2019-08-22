package fleet;

import database.DBC;
import database.GitasDBT;
import routescanner.RouteDirection;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import utils.Web_Request;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RouteStopsDownload {

    private String route;
    public RouteStopsDownload( String route ){
        this.route = route;
    }

    public JSONArray action(){
        org.jsoup.Connection.Response res;
        try {
            //res = Jsoup.connect(Web_Request.API_URL_PREFIX+"?req=route_stops_download&route="+route)
            res = Jsoup.connect("http://kahya_api.test/api/routeStops/"+route)
                    .method(org.jsoup.Connection.Method.GET)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .timeout(0)
                    .execute();

            System.out.println(res.parse().text());
            JSONArray data = new JSONObject(res.parse().text()).getJSONArray("data").getJSONArray(0);
            return data;
        } catch( IOException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public JSONArray actionOLD(){
        JSONArray stops = new JSONArray();
        stops.put(RouteDirection.FORWARD, new JSONArray());
        stops.put(RouteDirection.BACKWARD, new JSONArray());
        try {
            Connection con = DBC.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("SELECT isim, no FROM " + GitasDBT.HAT_DURAKLAR_V2 + " WHERE hat = ? && yon = ?");

            st.setString(1, route );
            st.setInt(2, RouteDirection.FORWARD );
            ResultSet res = st.executeQuery();
            JSONObject tempStop;
            while(res.next()){
                tempStop = new JSONObject();
                tempStop.put("name", res.getString("isim"));
                tempStop.put("no",res.getInt("no"));
                stops.getJSONArray(RouteDirection.FORWARD).put(tempStop);
            }
            st = con.prepareStatement("SELECT isim, no FROM " + GitasDBT.HAT_DURAKLAR_V2 + " WHERE hat = ? && yon = ?");
            st.setString(1, route );
            st.setInt(2, RouteDirection.BACKWARD );
            res = st.executeQuery();
            while(res.next()){
                tempStop = new JSONObject();
                tempStop.put("name", res.getString("isim"));
                tempStop.put("no",res.getInt("no"));
                stops.getJSONArray(RouteDirection.BACKWARD).put(tempStop);
            }
            res.close();
            st.close();
            con.close();
        } catch( SQLException e ){
            e.printStackTrace();
        }
        return stops;
    }

}
