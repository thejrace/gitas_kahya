/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package routescanner;

public class IntersectionData {
    /**
     * Active route code
     */
    private String activeRoute;
    /**
     * Compared route code
     */
    private String comparedRoute;
    /**
     * Intersected stop name
     */
    private String intersectedAt;
    /**
     * Difference of the index of intersection route in the routes' stop list
     */
    private int totalDiff;
    /**
     * Direction of the intersection
     */
    private int direction;

    /**
     * Data constructor
     *
     * @param activeRoute active route code
     * @param comparedRoute compared route code
     * @param intersectedAt intersection stop
     * @param direction intersection direction
     * @param totalDiff intersection stop index difference
     */
    public IntersectionData( String activeRoute, String comparedRoute, String intersectedAt, int direction, int totalDiff ){
        this.activeRoute = activeRoute;
        this.comparedRoute = comparedRoute;
        this.intersectedAt = intersectedAt;
        this.direction = direction;
        this.totalDiff = totalDiff;
    }

    /**
     * Serialize data
     *
     * @return string
     */
    public String toString(){
        return activeRoute + "|" + comparedRoute + " - @"+intersectedAt + " - DIR:"+ RouteDirection.returnText(direction);
    }

    /**
     * Getter for direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Getter for activeRoute
     */
    public String getActiveRoute() {
        return activeRoute;
    }

    /**
     * Getter for intersectedAt
     */
    public String getIntersectedAt() {
        return intersectedAt;
    }

    /**
     * Getter for totalDiff
     */
    public int getTotalDiff() {
        return totalDiff;
    }
}
