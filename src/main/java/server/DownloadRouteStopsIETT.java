package server;

import database.DBC;
import database.GitasDBT;
import fleet.RouteDirection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DownloadRouteStopsIETT {

    public static void action(){
        File folder = new File("C:/stops");
        File[] listOfFiles = folder.listFiles();
        Thread th = new Thread( () -> {
            for (int i = 0; i < listOfFiles.length; i++) {
                String name = listOfFiles[i].getName();
                name = name.substring(0, name.indexOf("_"));
                System.out.println(name + " importing action started!");
                reqParse(name);
                try{
                   Thread.sleep(3000);
                } catch( InterruptedException e ){
                    e.printStackTrace();
                }
            }
            System.out.println("All importing action finished!");

        });
        th.setDaemon(true);
        th.start();
    }

    private static void reqParse( String route ){
        try {
            Connection.Response response = Jsoup.connect("https://www.iett.istanbul/tr/main/hatlar/"+route)
                    .method(org.jsoup.Connection.Method.GET)
                    .timeout(40*1000)
                    .execute();

            Document document = response.parse();
            Elements parentDivs = document.getElementsByAttribute("data-hat-yon");
            Element parentDiv;
            int yon = RouteDirection.FORWARD;
            java.sql.Connection con = DBC.getInstance().getConnection();
            PreparedStatement pst = null;
            for( int k = 0; k < parentDivs.size(); k++ ){
                parentDiv = parentDivs.get(k);
                Elements sitNames = parentDiv.getElementsByClass("LineStation_name");
                for( int j = 0; j < sitNames.size(); j++ ){
                    String fullName = sitNames.get(j).text();
                    String no = fullName.substring(0, fullName.indexOf(" -"));
                    String name = fullName.substring(fullName.indexOf("- ")+2);
                    pst = con.prepareStatement("INSERT INTO "+ GitasDBT.HAT_DURAKLAR_V2 +"( hat, yon, no, isim ) VALUES ( ?, ?, ?, ?)");
                    pst.setString(1, route);
                    pst.setInt(2, yon);
                    pst.setInt(3, Integer.valueOf(no));
                    pst.setString(4, name);
                    pst.executeUpdate();
                }
                yon = RouteDirection.BACKWARD;
            }
            System.out.println(route + " importing action finished!");
            pst.close();
            con.close();
        } catch (IOException | SQLException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
