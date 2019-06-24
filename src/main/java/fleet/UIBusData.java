package fleet;

public class UIBusData {

    private String busCode, stop;
    private int diff;

    public UIBusData(String busCode, String stop, int diff ){
        this.busCode = busCode;
        this.stop = stop;
        this.diff = diff;
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


}
