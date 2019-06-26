package server;

import database.DBC;
import fleet.Filo_Captcha_Controller;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.Common;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;

public class DownloadRouteStopsIETT {

    public static void action(){


        File folder = new File("C:/stops");
        File[] listOfFiles = folder.listFiles();


        for (int i = 0; i < listOfFiles.length; i++) {

            String name = listOfFiles[i].getName();
            name = name.substring(0, name.indexOf("_"));

            reqParse(name);

            break;
        }




    }

    private static void reqParse( String route ){
        try {
            Connection.Response response = Jsoup.connect("https://www.iett.istanbul/tr/main/hatlar/"+route)
                    .method(org.jsoup.Connection.Method.GET)
                    .timeout(40*1000)
                    .execute();

            Document document = response.parse();





        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

}
