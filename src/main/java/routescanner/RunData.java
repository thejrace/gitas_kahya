package routescanner;

public class RunData {

    private String busCode;
    private String currentStop;
    private String departureTime;
    private String routeDetails;
    private String status;


    private String route;

    private String statusCode;
    private int departureNo;


    private int direction;

    public RunData( String busCode, String route, int departureNo, String currentStop, String departureTime, String routeDetails, String status, String statusCode ){
        this.busCode = busCode;
        this.route = route;
        this.departureNo = departureNo;
        this.currentStop = currentStop;
        this.departureTime = departureTime;
        this.routeDetails = routeDetails;
        this.status = status;
        this.statusCode = statusCode;
    }

    // ring
    public RunData( String busCode, String route,String currentStop ){
        this.busCode = busCode;
        this.route = route;
        this.currentStop = currentStop;
    }

    public RunData( String busCode, String route, int direction ){
        this.busCode = busCode;
        this.route = route;
        this.direction = direction;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }


    public int getDepartureNo() {
        return departureNo;
    }

    public void setDepartureNo(int departureNo) {
        this.departureNo = departureNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getCurrentStop() {
        return currentStop;
    }

    public void setCurrentStop(String currentStop) {
        this.currentStop = currentStop;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }


}
