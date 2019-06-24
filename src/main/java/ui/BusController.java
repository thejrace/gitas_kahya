package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;

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

    public void setPos( double yPos ){
        AnchorPane.setTopAnchor(uiContainer, yPos );
    }

    public void setActiveBusFlag(){
        activeBusFlag = true;
        uiContainer.getStyleClass().add("active");
    }

    public void setData( String busCode, String stop, int diff ){
        uiBusCodeLabel.setText(busCode);
        uiStopLabel.setText(stop);
        uiDiffLabel.setText(String.valueOf(diff));
        try {
            uiContainer.getStyleClass().remove(0);
        } catch( IndexOutOfBoundsException e ){ }
        if( diff < 0 ){
            uiContainer.getStyleClass().add(0, "backward");
        } else {
            uiContainer.getStyleClass().add(0, "forward");
        }
    }

}
