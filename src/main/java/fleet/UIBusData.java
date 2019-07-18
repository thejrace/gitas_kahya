package fleet;

public class UIBusData {

    private String busCode;
    private String stop;
    private String routeDetails;
    private String directionText;

    private int diff;

    public UIBusData(String busCode, String stop, int diff, String routeDetails ){
        this.busCode = busCode;
        this.stop = stop;
        this.diff = diff;
        this.routeDetails = routeDetails;
    }


    public UIBusData(String busCode, String stop, int diff, String routeDetails, String directionText ){
        this.busCode = busCode;
        this.stop = stop;
        this.diff = diff;
        this.routeDetails = routeDetails;
        this.directionText = directionText;
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

    public String getDirectionText() {
        return directionText;
    }

    public void setDirectionText(String directionText) {
        this.directionText = directionText;
    }


    public String toString(){
        return "UIBUSDATA: ["+busCode+"] ["+stop+"] ["+routeDetails+"] ["+diff+"]";
    }
}
