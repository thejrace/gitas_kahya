package ui;

import fleet.UIBusData;

public class Bus  extends UIComponent {

    private UIBusData data;

    public Bus( UIBusData data ){
        loadFXML("bus");
        ((BusController)(controller)).setData(data);
        this.data = data;
    }

    public void notifyUI(){
        ((BusController)(controller)).setData(data);
    }

    public UIBusData getData(){
        return data;
    }

    public void setData( UIBusData data ){
        this.data = data;
    }

}
