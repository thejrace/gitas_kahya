package ui;

import client.KahyaActionListener;
import fleet.UIBusData;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import utils.Common;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainScreenController implements Initializable {

    @FXML private AnchorPane uiContainer;
    @FXML private AnchorPane uiBusContainerOverlay;
    @FXML private VBox uiBusContainer;
    @FXML private TextField uiBusCodeInput;
    @FXML private Button uiActionBtn;
    @FXML private Label uiRouteLabel;
    @FXML private Label uiLastUpdatedLabel;
    @FXML private Label uiErrorLabel;

    private String route;
    private double splitCount;
    private double activeBusPos;
    private Map<String, Bus> busList = new HashMap<>();



    private KahyaActionListener actionListener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {



        uiActionBtn.setOnMousePressed( ev -> {
            String busCode = uiBusCodeInput.getText();
            if( !busCode.equals("") ) actionListener.onStart( busCode );
        });



    }

    public void reset(){
        Platform.runLater(() -> {
            busList = new HashMap<>();
            uiBusContainer.getChildren().clear();
        });
    }

    public void setRoute( String route ){
        this.route = route;
    }

    public void splitDims( int totalHeight, int stopCount ){
        splitCount = totalHeight /( stopCount + 10 );
        uiBusContainer.setPrefHeight(totalHeight);
        uiBusContainerOverlay.setPrefHeight(totalHeight);
    }

    public void addBus( Bus bus, double pos ){
        uiBusContainer.getChildren().add( bus.getUI() );
        //((BusController)bus.getController()).setPos( pos );
        busList.put(bus.getBusCode(), bus );
    }

    public void setError( String message ){
        Platform.runLater(() -> {
            uiErrorLabel.setText(message);
            uiLastUpdatedLabel.setText(Common.getDateTime());
        });
    }

    public void update( UIBusData activeBusData,  ArrayList<UIBusData> fleetBusData ){
        Platform.runLater(() -> {



            /*if( !busList.containsKey(activeBusData.getBusCode())){
                Bus activeBus = new Bus(activeBusData.getBusCode(), activeBusData.getStop(), activeBusData.getDiff() );
                ((BusController)activeBus.getController()).setActiveBusFlag();
                addBus( activeBus, activeBusData.getDiff() * splitCount );
            } else {
                busList.get(activeBusData.getBusCode()).setDiff( activeBusData.getDiff() );
                busList.get(activeBusData.getBusCode()).setStop( activeBusData.getStop() );
                busList.get(activeBusData.getBusCode()).notifyUI();
            }*/

            activeBusPos = activeBusData.getDiff() * splitCount;
            for( UIBusData busData : fleetBusData ){
                double pos = (busData.getDiff() < 0 ) ? activeBusPos - ( busData.getDiff() * splitCount * -1 ) : activeBusPos + ( busData.getDiff() * splitCount  );
                if( !busList.containsKey(busData.getBusCode())){
                    Bus busTemp = new Bus(busData.getBusCode(), busData.getStop() +  " - " + busData.getRouteDetails(), busData.getDiff() );
                    if( busData.getBusCode().equals(uiBusCodeInput.getText()) ) ((BusController)busTemp.getController()).setActiveBusFlag();
                    addBus( busTemp, pos);
                } else {
                    busList.get(busData.getBusCode()).setDiff( busData.getDiff() );
                    busList.get(busData.getBusCode()).setStop( busData.getStop() +  " - " + busData.getRouteDetails() );
                    busList.get(busData.getBusCode()).notifyUI();
                }
            }
            uiLastUpdatedLabel.setText(Common.getDateTime());
            uiErrorLabel.setText("");
            uiRouteLabel.setText(route);
        });
    }

    public void setActionListener( KahyaActionListener listener ){
        actionListener = listener;
    }
}
