package server;

import database.DBC;
import fleet.Filo_Task;
import fleet.RouteDirection;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RouteIntersection extends Filo_Task {

    private static Map<String, ArrayList<ArrayList<String>>> routeStops = new HashMap<>();
    private static Map<String, IntersectionData> intersections = new HashMap<>();
    private static Map<String, Boolean> intersectionFlags = new HashMap<>();
    private static Map<String, Integer> matchCounts = new HashMap<>();

    public static void action(){

        Thread thread = new Thread(()->{

            // fetch all routes
            JSONArray routes = fetchRoutes();
            for( int k = 0; k < routes.length(); k++ ){
                String activeRoute = routes.getString(k);
                //if( !activeRoute.equals("55T")) continue;
                fetchStops(activeRoute);
                for( int j = 0; j < routes.length(); j++ ){
                    String comparedRoute = routes.getString(j);
                    if( activeRoute.equals(comparedRoute)) continue;
                    //if( !comparedRoute.equals("87")) continue;
                    fetchStops(comparedRoute);

                    for( int l = 0; l < 2; l++ ){ // gidiş - dönüş
                        String key = activeRoute+"|"+comparedRoute+"|"+l;

                        for( int x = 0; x < routeStops.get(activeRoute).get(l).size(); x++ ){ // aktif hattın duraklarını çevir
                            String activeStop = routeStops.get(activeRoute).get(l).get(x);
                            for( int a = 0; a < routeStops.get(comparedRoute).get(l).size(); a++ ){ // karşılaştırılan hattın duraklarını çevir
                                String comparedStop = routeStops.get(comparedRoute).get(l).get(a);
                                //System.out.println(activeStop + " ---  " + comparedStop );
                                if( comparedStop.equals(activeStop) ){
                                    int o = a;
                                    boolean leave = false;

                                    if( x == routeStops.get(activeRoute).get(l).size()-1 ) break;

                                    for( int u = x; u < routeStops.get(activeRoute).get(l).size(); u++ ){
                                        try {
                                            if( !routeStops.get(activeRoute).get(l).get(u).equals(routeStops.get(comparedRoute).get(l).get(o)) ){
                                                leave = true;
                                            }
                                        } catch( IndexOutOfBoundsException e ){
                                            leave = true;
                                        }
                                        o++;
                                    }
                                    if( !leave ){
                                        if( !intersections.containsKey(key ) ){
                                            intersections.put(key, new IntersectionData(activeRoute, comparedRoute, activeStop, l));
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }


            ArrayList<String> checked = new ArrayList<>();
            for( Map.Entry<String, IntersectionData> entry : intersections.entrySet() ){
                IntersectionData data = entry.getValue();
                if( !checked.contains(data.getComparedRoute()+"|"+data.getActiveRoute()+"|"+data.getDirection())){

                    System.out.println(entry.getValue().toString() + "   " + matchCounts.get(data.getActiveRoute()+"|"+data.getComparedRoute()+"|"+data.getDirection()) );
                    checked.add(data.getActiveRoute()+"|"+data.getComparedRoute()+"|"+data.getDirection());
                }
            }
        });
        thread.setDaemon(true);
        thread.start();


    }

    private static void fetchStops( String route ){
        if( !routeStops.containsKey(route) ){
            RouteStopsDownload download = new RouteStopsDownload(route);
            JSONArray downloaded = download.action();
            routeStops.put(route, new ArrayList<>());
            for( int l = 0; l < downloaded.length(); l++ ){
                ArrayList<String> tempStops = new ArrayList<>();
                for( int h = 0; h < downloaded.getJSONArray(l).length(); h++ ){
                    tempStops.add(downloaded.getJSONArray(l).getJSONObject(h).getString("name"));
                }
                routeStops.get(route).add(tempStops);
            }
        }
    }

    private static JSONArray fetchRoutes() {

        File folder = new File("C:/stops");
        File[] listOfFiles = folder.listFiles();
        JSONArray output = new JSONArray();
        try {

            Connection con = DBC.getInstance().getConnection();
            PreparedStatement pst = null;

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String name = listOfFiles[i].getName();
                    File file = new File("C:/stops/" + listOfFiles[i].getName());
                    JSONArray routeStopData = new JSONObject(Common.readJSONFile(file)).getJSONArray("duraklar");
                    name = name.substring(0, name.indexOf("_"));
                    output.put(name);
                }
            }
            if (pst != null) pst.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return output;
    }


}


class IntersectionData {
    private String activeRoute, comparedRoute, intersectedAt;

    public int getDirection() {
        return direction;
    }

    private int direction;

    public IntersectionData( String activeRoute, String comparedRoute, String intersectedAt, int direction ){
        this.activeRoute = activeRoute;
        this.comparedRoute = comparedRoute;
        this.intersectedAt = intersectedAt;
        this.direction = direction;
    }

    public String getActiveRoute() {
        return activeRoute;
    }

    public void setActiveRoute(String activeRoute) {
        this.activeRoute = activeRoute;
    }

    public String getComparedRoute() {
        return comparedRoute;
    }

    public void setComparedRoute(String comparedRoute) {
        this.comparedRoute = comparedRoute;
    }

    public String getIntersectedAt() {
        return intersectedAt;
    }

    public void setIntersectedAt(String intersectedAt) {
        this.intersectedAt = intersectedAt;
    }

    public String toString(){
        return activeRoute + "|" + comparedRoute + " - @"+intersectedAt + " - DIR:"+RouteDirection.returnText(direction);
    }

}





