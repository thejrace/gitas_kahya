package server;

import database.DBC;
import database.GitasDBT;
import org.json.JSONArray;
import org.json.JSONObject;

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
        JSONArray stops = new JSONArray();
        try {
            Connection con = DBC.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM " + GitasDBT.HAT_DURAKLAR + " WHERE hat = ? ");
            st.setString(1, route );
            ResultSet res = st.executeQuery();
            JSONObject stopData;
            while(res.next()){
                stopData = new JSONObject();
                stopData.put("name", res.getString("ad"));
                stopData.put("no", res.getString("sira"));
                stops.put(stopData);
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
