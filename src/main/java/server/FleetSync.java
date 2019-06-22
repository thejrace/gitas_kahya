package server;

import database.DBC;
import database.GitasDBT;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FleetSync {

    public static String getActiveDate() {

        try {
            Connection con = null;
            Statement st = null;
            ResultSet res = null;
            con = DBC.getInstance().getConnection();
            st = con.createStatement();
            res = st.executeQuery("SELECT * FROM " + GitasDBT.ORER_LOG + " WHERE durum = '1' ");
            res.next();
            String date = res.getString("tarih");
            res.close();
            st.close();
            con.close();
            return date;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "BEKLEMEDE";
    }

}
