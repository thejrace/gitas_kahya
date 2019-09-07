/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package fleet;

import fakedatagenerator.FakeDataGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.Common;
import java.util.ArrayList;

public class RouteFleetDownload extends Filo_Task {
    /**
     *  Route list to be downloaded
     */
    private ArrayList<String> routes;

    /**
     * Constructor
     *
     * @param routes list of routes
     */
    public RouteFleetDownload( ArrayList<String> routes ){
        this.routes = routes;
    }

    /**
     *  Downloads the data
     */
    public void action(){

        if( FakeDataGenerator.ACTIVE ){
            output = FakeDataGenerator.getSimData();
        } else {
            for( int k = 0; k < routes.size(); k++ ){
                try {
                    org.jsoup.Connection.Response request = istek_yap("https://filotakip.iett.gov.tr/_FYS/000/sorgu.php?konum=ana&konu=sefer&hat="+routes.get(k));
                    Document document = parse_html( request );
                    parseData( document );
                } catch( Exception e ){
                    e.printStackTrace();
                }
            }
        }
        //output = FakeDataGenerator.statusChangeTest();
        //output = FakeDataGenerator.stopChangeTest();
        //output = FakeDataGenerator.routeIntersectionTest();
        //output = FakeDataGenerator.ringPositionTest();
    }

    /**
     * Parses the html to fetch fleet data
     *
     * @param document html page
     */
    public void parseData( Document document ){
        try {
            if( document == null ){}
        } catch( NullPointerException e ){
            e.printStackTrace();
            System.out.println("ÖLÜYORUZ KAPTAAAAN");
            errorMessage = "Document is empty.";
            return;
        }
        Elements table = null;
        Elements rows = null;
        Element row = null;
        Elements cols = null;

        try {
            table = document.select("table");
            rows = table.select("tr");
            int rowsSize = rows.size();
            if ( rowsSize == 0 || rowsSize == 1) {
                errorMessage = "No data to fetch.";
                System.out.println("ÖLÜYORUZ KAPTAAAAN22222");
                return;
            } else {
                JSONObject runTemp;
                String busCode;
                String route = "";
                String statusText;
                String status;
                String statusCode;
                boolean routeFetchedFlag = false;
                for (int i = 1; i < rowsSize; i++) {
                    row = rows.get(i);
                    cols = row.select("td");
                    busCode = Common.regexTrim(cols.get(5).text());
                    statusText = "";
                    status = "";
                    statusCode = "";
                    try {
                        statusText = cols.get(14).text();
                        status = statusText.substring(0,1);
                        statusCode = statusText.substring(2, statusText.length());
                    } catch (StringIndexOutOfBoundsException e ){
                        //e.printStackTrace();
                    }

                    runTemp = new JSONObject();
                    runTemp.put("bus_code", busCode);

                    if( !routeFetchedFlag ){
                        route = cols.get(2).text().trim();
                        if( cols.get(2).text().trim().contains("!")  ){
                            route = cols.get(2).text().trim().substring(3, cols.get(2).text().trim().length() - 1 );
                        } else if( cols.get(2).text().trim().contains("#") ) {
                            route = cols.get(2).text().trim().substring(3, cols.get(2).text().trim().length() - 1 );
                        } else  if( cols.get(2).text().trim().contains("*") ){
                            route = cols.get(2).text().trim().substring(3, cols.get(2).text().trim().length() - 1);
                        } else {
                            route = route.substring(2, route.length());
                        }
                        routeFetchedFlag = true;
                    }

                    // for normal runs we need all run data of the all busses to determine route direction
                    // using route_details data.
                    // status and status_text checks are done in the KahyaClient
                    runTemp.put("dep_time", Common.regexTrim(cols.get(9).getAllElements().get(2).text()));
                    runTemp.put("no", Common.regexTrim(cols.get(0).text()));
                    try{
                        runTemp.put("stop", cols.get(15).text() );
                    } catch( Exception e ){
                        runTemp.put("stop", "N/A");
                    }
                    runTemp.put("route", route );
                    runTemp.put("route_details", Common.regexTrim(cols.get(4).getAllElements().get(1).text()));
                    runTemp.put("status", status);
                    runTemp.put("status_code", statusCode);
                    if( output.isNull(busCode) ) output.put(busCode, new JSONArray() );
                    output.getJSONArray(busCode).put( runTemp );
                }
            }

        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Getter for output
     */
    public JSONObject getOutput(){
        return output;
    }

    /**
     * Getter for errorFlag
     */
    public boolean getErrorFlag() {
        return errorFlag;
    }

    /**
     * Getter for errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
