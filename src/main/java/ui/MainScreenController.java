package ui;

import fleet.UIBusData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {

    @FXML private AnchorPane uiContainer;
    @FXML private AnchorPane uiBusContainerOverlay;
    @FXML private AnchorPane uiBusContainer;
    @FXML private TextField uiBusCodeInput;
    @FXML private Button uiActionBtn;


    private double splitCount;
    private double activeBusPos;
    private Map<String, Bus> busList = new HashMap<>();


    private VBox test ;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        test = new VBox();
        test.setSpacing(15);
        Platform.runLater( () -> { uiBusContainer.getChildren().add(test); });

    }



    public void splitDims( int totalHeight, int stopCount ){
        splitCount = totalHeight /( stopCount + 10 );
        uiBusContainer.setPrefHeight(totalHeight);
        uiBusContainerOverlay.setPrefHeight(totalHeight);
    }

    public void addBus( Bus bus, double pos ){
        uiBusContainer.getChildren().add( bus.getUI() );
        ((BusController)bus.getController()).setPos( pos );
        busList.put(bus.getBusCode(), bus );
    }

    public void update( UIBusData activeBusData,  ArrayList<UIBusData> fleetBusData ){
        Platform.runLater(() -> {
            if( !busList.containsKey(activeBusData.getBusCode())){
                Bus activeBus = new Bus(activeBusData.getBusCode(), activeBusData.getStop(), activeBusData.getDiff() );
                ((BusController)activeBus.getController()).setActiveBusFlag();
                addBus( activeBus, activeBusData.getDiff() * splitCount );
            } else {
                busList.get(activeBusData.getBusCode()).setDiff( activeBusData.getDiff() );
                busList.get(activeBusData.getBusCode()).setStop( activeBusData.getStop() );
                busList.get(activeBusData.getBusCode()).notifyUI();
            }
            activeBusPos = activeBusData.getDiff() * splitCount;
            for( UIBusData busData : fleetBusData ){
                double pos = (busData.getDiff() < 0 ) ? activeBusPos - ( busData.getDiff() * splitCount * -1 ) : activeBusPos + ( busData.getDiff() * splitCount  );
                if( !busList.containsKey(busData.getBusCode())){
                    addBus( new Bus(busData.getBusCode(), busData.getStop(), busData.getDiff() ), pos);
                } else {
                    busList.get(busData.getBusCode()).setDiff( busData.getDiff() );
                    busList.get(busData.getBusCode()).setStop( busData.getStop() );
                    busList.get(busData.getBusCode()).notifyUI();
                }
            }
        });
    }
}
