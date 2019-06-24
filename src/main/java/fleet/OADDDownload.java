package fleet;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OADDDownload extends Filo_Task {

    private String busCode;

    public OADDDownload( String busCode ){
        this.busCode = busCode;
    }

    public void action(){
        oto = "";
        org.jsoup.Connection.Response request = istek_yap("https://filotakip.iett.gov.tr/_FYS/000/sorgu.php?konum=ana&konu=sefer&otobus="+busCode );
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
        String route = "";
        boolean routeFetched = false;
        System.out.println(document);
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
            String status;
            for (int i = 1; i < rowsSize; i++) {
                row = rows.get(i);
                cols = row.select("td");
                status = "";
                try {
                    status =  cols.get(14).text().substring(0,1);
                } catch (StringIndexOutOfBoundsException e ){
                    //e.printStackTrace();
                }
                if( !routeFetched ){
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
                    routeFetched = true;
                }
                if( status.equals("A") ){
                    output.put("bus_code", busCode);
                    output.put("route", route);
                    errorFlag = false;
                    return;
                } else {
                    errorMessage = "Aktif sefer yok!";
                    errorFlag = true;
                }
            }
        } catch( Exception e ){
            e.printStackTrace();
        }
    }





}
