package ui;

import routescanner.UIBusData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import routescanner.BusStatus;
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

    //  [ÜMRANİYE EĞİTİM ARAŞTIRM, ŞİLE YOLU, HUZUR MAHALLESİ, ÇEKMEKÖY BELEDİYE SAPAĞI]

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



        /*if( !data.getBusCode().equals(RouteScanner.ACTIVE_BUS_CODE )){
            try {
                uiContainer.getStyleClass().remove(0);
            } catch( IndexOutOfBoundsException e ){ }

            if( data.getStatus() == BusStatus.ACTIVE ){
                uiContainer.getStyleClass().add(0, "bs-active");
            } else if( data.getStatus() == BusStatus.WAITING ){
                uiContainer.getStyleClass().add(0, "bs-waiting");
            }
        }*/


       /*
        if( data.getDiff() < 0 ){
            uiContainer.getStyleClass().add(0, "backward");
        } else {
            uiContainer.getStyleClass().add(0, "forward");
        }*/
    }

}
