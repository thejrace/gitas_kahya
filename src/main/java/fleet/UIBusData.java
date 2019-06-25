package fleet;

public class UIBusData {

    private String busCode, stop, routeDetails;

    private int diff;

    public UIBusData(String busCode, String stop, int diff, String routeDetails ){
        this.busCode = busCode;
        this.stop = stop;
        this.diff = diff;
        this.routeDetails = routeDetails;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public String getRouteDetails() {
        return routeDetails;
    }

    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }


}
