package fleet;

public class RunData {

    private String busCode;
    private String currentStop;
    private String departureTime;
    private String routeDetails;
    private String status;
    private int departureNo;

    public RunData( String busCode, int departureNo, String currentStop, String departureTime, String routeDetails, String status ){
        this.busCode = busCode;
        this.departureNo = departureNo;
        this.currentStop = currentStop;
        this.departureTime = departureTime;
        this.routeDetails = routeDetails;
        this.status = status;
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

}
