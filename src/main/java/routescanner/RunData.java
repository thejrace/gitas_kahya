/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package routescanner;

public class RunData {
    /**
     * Bus code
     */
    private String busCode;
    /**
     * Current stop name
     */
    private String currentStop;
    /**
     * ORER
     */
    private String departureTime;
    /**
     * Route details string
     */
    private String routeDetails;
    /**
     * Status string
     */
    private String status;
    /**
     * Code of the route
     */
    private String route;
    /**
     * Status code string
     */
    private String statusCode;
    /**
     * Run no
     */
    private int departureNo;
    /**
     * Run direction
     */
    private int direction;

    /**
     * Full Constructor
     *
     * @param busCode
     * @param route
     * @param departureNo
     * @param currentStop
     * @param departureTime
     * @param routeDetails
     * @param status
     * @param statusCode
     */
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

    /**
     * Getter for direction
     *
     * @return
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Setter for direction
     *
     * @param direction new direction
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Getter for departureNo
     *
     * @return
     */
    public int getDepartureNo() {
        return departureNo;
    }

    /**
     * Setter for departureNo
     *
     * @param departureNo new data
     */
    public void setDepartureNo(int departureNo) {
        this.departureNo = departureNo;
    }

    /**
     * Getter for status
     *
     * @return
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for status
     *
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter for busCode
     *
     * @return
     */
    public String getBusCode() {
        return busCode;
    }

    /**
     * Setter for busCode
     *
     * @param busCode new data
     */
    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    /**
     * Getter for currentStop
     *
     * @return
     */
    public String getCurrentStop() {
        return currentStop;
    }

    /**
     * Setter for currentStop
     *
     * @param currentStop new data
     */
    public void setCurrentStop(String currentStop) {
        this.currentStop = currentStop;
    }

    /**
     * Getter for departureTime
     *
     * @return
     */
    public String getDepartureTime() {
        return departureTime;
    }

    /**
     * Setter for departureTime
     *
     * @param departureTime new data
     */
    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Getter for routeDetails
     *
     * @return
     */
    public String getRouteDetails() {
        return routeDetails;
    }

    /**
     * Setter for routeDetails
     *
     * @param routeDetails new data
     */
    public void setRouteDetails(String routeDetails) {
        this.routeDetails = routeDetails;
    }

    /**
     * Getter for statusCode
     *
     * @return
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Setter for statusCode
     *
     * @param statusCode new data
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Getter for route
     *
     * @return
     */
    public String getRoute() {
        return route;
    }

    /**
     * Setter for route
     *
     * @param route new data
     */
    public void setRoute(String route) {
        this.route = route;
    }
}
