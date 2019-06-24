package server;

import database.DBC;
import database.GitasDBT;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Deprecated
public class OADDDownload {


    private String busCode;
    public OADDDownload( String bus ){
        busCode = bus;
    }

    public JSONObject action(){
        JSONObject output = new JSONObject();
        try {
            Connection con = DBC.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement("SELECT * FROM " + GitasDBT.OTOBUS_AKTIF_DURUM + " WHERE oto = ? ");
            st.setString(1, busCode );
            ResultSet res = st.executeQuery();
            if( res.next() ){
                output.put("status", res.getString("durum"));
                output.put("stop", res.getString("notf"));
                output.put("route", res.getString("hat"));
                output.put("data_time_stamp", res.getString("tarih"));
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
