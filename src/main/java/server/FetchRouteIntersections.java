package server;

import database.DBC;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FetchRouteIntersections {
    public static JSONArray action( String activeRoute ){
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