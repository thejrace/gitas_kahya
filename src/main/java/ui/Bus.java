package ui;

public class Bus  extends UIComponent {

    private String busCode, stop;

    private int diff;

    public Bus( String busCode, String stop, int diff ){
        loadFXML("bus");
        ((BusController)(controller)).setData(busCode, stop, diff );
        this.diff = diff;
        this.busCode = busCode;
        this.stop = stop;
    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }

}
