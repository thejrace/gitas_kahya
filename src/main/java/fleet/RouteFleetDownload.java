package fleet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.Common;

public class RouteFleetDownload extends Filo_Task {

    private String route;


    public RouteFleetDownload( String route ){
        this.route = route;
    }

    public void action(){
        org.jsoup.Connection.Response request = istek_yap("https://filotakip.iett.gov.tr/_FYS/000/sorgu.php?konum=ana&konu=sefer&hat="+route);
        Document document = parse_html( request );
        parseData( document );
    }

    public void parseData( Document document ){

        try {
            if( document == null ){}
        } catch( NullPointerException e ){
            e.printStackTrace();
            errorMessage = "Document is empty.";
            errorFlag = true;
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
            if (rowsSize == 0) return;
            if (rowsSize == 1) {
                errorMessage = "No data to fetch.";
                errorFlag = true;
                return;
            }
            JSONObject runTemp;
            String busCode;
            String statusText;
            String status;
            String statusCode;
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
                runTemp.put("dep_time", Common.regexTrim(cols.get(9).getAllElements().get(2).text()));
                runTemp.put("no", Common.regexTrim(cols.get(0).text()));
                runTemp.put("stop", cols.get(15).text() );
                runTemp.put("route_details", Common.regexTrim(cols.get(4).getAllElements().get(1).text()));
                runTemp.put("status", status);

                if( output.isNull(busCode) ) output.put(busCode, new JSONArray() );
                output.getJSONArray(busCode).put( runTemp );
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
    }

    public JSONObject getOutput(){
        return output;
    }

    public boolean getErrorFlag() {
        return errorFlag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
