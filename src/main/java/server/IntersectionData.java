package server;

import fleet.RouteDirection;

public class IntersectionData {
    private String activeRoute, comparedRoute, intersectedAt;



    private int totalDiff;

    public int getDirection() {
        return direction;
    }

    private int direction;

    public IntersectionData( String activeRoute, String comparedRoute, String intersectedAt, int direction, int totalDiff ){
        this.activeRoute = activeRoute;
        this.comparedRoute = comparedRoute;
        this.intersectedAt = intersectedAt;
        this.direction = direction;
        this.totalDiff = totalDiff;
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
        return activeRoute + "|" + comparedRoute + " - @"+intersectedAt + " - DIR:"+ RouteDirection.returnText(direction);
    }

    public int getTotalDiff() {
        return totalDiff;
    }

    public void setTotalDiff(int totalDiff) {
        this.totalDiff = totalDiff;
    }

}
