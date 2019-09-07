/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package routescanner;

import java.util.ArrayList;

public class RouteStop {

    /**
     * Stop no
     */
    private int no;

    /**
     * Stop name
     */
    private String name;

    /**
     * Intersection list
     */
    private ArrayList<IntersectionData> routeIntersections;

    /**
     * Constructor
     *
     * @param no route no
     * @param name route name
     */
    public RouteStop( int no, String name ){
        this.no = no;
        this.name = name;
        routeIntersections = new ArrayList<>();
    }

    /**
     * Append a intersection
     *
     * @param intersectionData intersection details
     */
    public void markIntersection( IntersectionData intersectionData ){
        this.routeIntersections.add(intersectionData);
    }

    /**
     * Getter for intersections
     */
    public ArrayList<IntersectionData> getIntersections(){
        return this.routeIntersections;
    }

    /**
     * Serialize data
     *
     * @return string
     */
    public String toString(){
        return "["+no+"] - " + "["+name+"]";
    }

    /**
     * Getter for no
     */
    public int getNo() {
        return no;
    }

    /**
     * Setter for intersections
     */
    public void setNo(int no) {
        this.no = no;
    }

    /**
     * Getter for name
     */
    public String getName() {
        return name;
    }

    /**
     * Fetch stop name from string with no ( ex: 15-Beykoz -> Beykoz )
     *
     * @param stop stop name with no
     *
     * @return only stop name
     */
    public static String fetchStopName( String stop ){
        try {
            return stop.substring(stop.indexOf('-')+1, stop.indexOf(" ("));
        } catch( StringIndexOutOfBoundsException e ){

        }
        return "N/A";
    }
}
