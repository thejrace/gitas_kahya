package fakedatagenerator;

import org.json.JSONObject;
import utils.Common;

public class FakeRunData {

    private String busCode, route, routeDetail, status, stop = "N/A";

    public FakeRunData( String busCode, String route, String routeDetail, String status ){
        this.busCode = busCode;
        this.route = route;
        this.routeDetail = routeDetail;
        this.status = status;
    }

    public boolean check(){
         return  !Common.regexTrim(busCode).equals("") &&
                 !Common.regexTrim(route).equals("") &&
                 !Common.regexTrim(routeDetail).equals("") &&
                 !Common.regexTrim(status).equals("");
    }

    public String toString(){
        return "["+busCode+"] ["+route+"] ["+routeDetail+"] ["+status+"]";
    }

    public JSONObject convertToJSON(){
        JSONObject output = new JSONObject();
        output.put("no", "0");
        output.put("route", route);
        output.put("status_code", "");
        output.put("dep_time", "10:00");
        output.put("route_details", route+"_"+routeDetail+"_XXX");
        output.put("bus_code", busCode);
        if ( stop.equals("N/A") ) {
            output.put("stop", stop);
        } else {
            output.put("stop", "70-"+stop+" (11:25 Üsküdar - 87m)");
        }

        output.put("status", status);
        return output;
    }



    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRouteDetail() {
        return routeDetail;
    }

    public void setRouteDetail(String routeDetail) {
        this.routeDetail = routeDetail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

}
