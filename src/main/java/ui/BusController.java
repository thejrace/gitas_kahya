package ui;

import fleet.UIBusData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import routescanner.BusStatus;
import routescanner.RouteMap;
import routescanner.RouteScanner;

import java.net.URL;
import java.util.ResourceBundle;

public class BusController implements Initializable {

    @FXML private HBox uiContainer;
    @FXML private Circle uiLed;
    @FXML private Label uiDiffLabel;
    @FXML private Label uiBusCodeLabel;
    @FXML private Label uiStopLabel;

    private boolean activeBusFlag = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    public void setActiveBusFlag(){
        activeBusFlag = true;
        try {
            uiContainer.getStyleClass().remove(0);
        } catch( IndexOutOfBoundsException e ){ }
        uiContainer.getStyleClass().add("active");
    }

    public void setData( UIBusData data ){
        uiBusCodeLabel.setText(data.getBusCode());
        uiStopLabel.setText(data.getStop() + " ["+data.getDirectionText()+"] (" + data.getRouteDetails() + ")");
        uiDiffLabel.setText(String.valueOf(data.getDiff()));

        try {
            uiContainer.getStyleClass().remove(0);
        } catch( IndexOutOfBoundsException e ){ }

        if( !data.getBusCode().equals(RouteScanner.ACTIVE_BUS_CODE )){
            if( data.getStatus() == BusStatus.ACTIVE ){
                uiContainer.getStyleClass().add(0, "bs-active");
            } else if( data.getStatus() == BusStatus.WAITING ){
                uiContainer.getStyleClass().add(0, "bs-waiting");
            }
        }


       /*
        if( data.getDiff() < 0 ){
            uiContainer.getStyleClass().add(0, "backward");
        } else {
            uiContainer.getStyleClass().add(0, "forward");
        }*/
    }

}
