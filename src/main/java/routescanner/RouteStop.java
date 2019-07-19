package routescanner;

import java.util.ArrayList;

public class RouteStop {

    private int no;
    private String name;
    private ArrayList<IntersectionData> routeIntersections;

    public RouteStop( int no, String name ){
        this.no = no;
        this.name = name;
        routeIntersections = new ArrayList<>();
    }

    public void markIntersection( IntersectionData intersectionData ){
        this.routeIntersections.add(intersectionData);
    }

    public ArrayList<IntersectionData> getIntersections(){
        return this.routeIntersections;
    }

    public String toString(){
        return "["+no+"] - " + "["+name+"]";
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static String fetchStopName( String stop ){
        try {
            return stop.substring(stop.indexOf('-')+1, stop.indexOf(" ("));
        } catch( StringIndexOutOfBoundsException e ){

        }
        return "N/A";
    }

}
