package server;

import database.DBC;
import database.GitasDBT;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ImportStops {

    public static void action() {


        File folder = new File("C:/stops");
        File[] listOfFiles = folder.listFiles();

        try {

            Connection con = DBC.getInstance().getConnection();
            PreparedStatement pst = null;

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String name = listOfFiles[i].getName();
                    File file = new File("C:/stops/" + listOfFiles[i].getName());
                    JSONArray routeStopData = new JSONObject(Common.readJSONFile(file)).getJSONArray("duraklar");
                    name = name.substring(0, name.indexOf("_"));
                    int no = 1;
                    for( int k = 0; k < routeStopData.length(); k++ ){
                        JSONObject stopData = routeStopData.getJSONObject(k);
                        pst = con.prepareStatement("INSERT INTO  hat_duraklar ( hat, latitude, longitude, sira, kod, ad ) VALUES ( ?, ?, ?, ?, ?, ?)");
                        pst.setString(1, name);
                        pst.setString(2, String.valueOf(stopData.getDouble("lat")));
                        pst.setString(3, String.valueOf(stopData.getDouble("lng")));
                        pst.setInt(4, no);
                        pst.setString(5, String.valueOf(stopData.getInt("kod")));
                        pst.setString(6, stopData.getString("ad"));
                        pst.executeUpdate();
                        no++;
                    }
                }


            }
            if (pst != null) pst.close();
            con.close();
        } catch( SQLException e ){
        e.printStackTrace();
    }


}



}
