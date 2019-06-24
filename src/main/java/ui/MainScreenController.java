package ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {

    @FXML private AnchorPane uiContainer;
    @FXML private AnchorPane uiBusContainerOverlay;
    @FXML private AnchorPane uiBusContainer;
    @FXML private TextField uiBusCodeInput;
    @FXML private Button uiActionBtn;


    private double splitCount;
    private double activeBusPos;


    @Override
    public void initialize(URL location, ResourceBundle resources) {



    }



    public void splitDims( int totalHeight, int stopCount, int activeBusPos ){
        splitCount = Math.ceil(totalHeight / stopCount);
        this.activeBusPos = activeBusPos;
        uiBusContainer.setPrefHeight(totalHeight);
        uiBusContainerOverlay.setPrefHeight(totalHeight);
    }

    public void addBus( Bus bus ){
        uiBusContainer.getChildren().add( bus.getUI() );
        ((BusController)bus.getController()).setPos( splitCount + activeBusPos + bus.getDiff() );
    }


}
