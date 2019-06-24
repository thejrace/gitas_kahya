package ui;

public class Bus  extends UIComponent {

    public String getBusCode() {
        return busCode;
    }

    private String busCode;



    private String stop;

    private int diff;

    public Bus( String busCode, String stop, int diff ){
        loadFXML("bus");
        ((BusController)(controller)).setData(busCode, stop, diff );
        this.diff = diff;
        this.busCode = busCode;
        this.stop = stop;
    }

    public void notifyUI(){
        ((BusController)(controller)).setData(busCode, stop, diff );
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }


}
