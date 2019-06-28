package ui;

import client.KahyaActionListener;
import fleet.UIBusData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import utils.Common;

import java.net.URL;
import java.util.*;

public class MainScreenController implements Initializable {

    @FXML private AnchorPane uiContainer;
    @FXML private AnchorPane uiBusContainerOverlay;
    @FXML private VBox uiBusContainer;
    @FXML private TextField uiBusCodeInput;
    @FXML private Button uiActionBtn;
    @FXML private Label uiRouteLabel;
    @FXML private Label uiLastUpdatedLabel;
    @FXML private Label uiErrorLabel;

    protected ObservableList<Node> dataRowsTemp;

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

    public void splitDims( int totalHeight ){
        uiBusContainer.setPrefHeight(totalHeight);
        uiBusContainerOverlay.setPrefHeight(totalHeight);
    }

    public void addBus( Bus bus ){
        uiBusContainer.getChildren().add( bus.getUI() );
        busList.put(bus.getBusCode(), bus );
    }

    public void setError( String message ){
        Platform.runLater(() -> {
            uiErrorLabel.setText(message);
            uiLastUpdatedLabel.setText(Common.getDateTime());
        });
    }

    private void sort(){
        try {
            ObservableList<Node> dataRows = FXCollections.observableArrayList( uiBusContainer.getChildren() );
            Collections.sort(dataRows, new Comparator<Node>(){
                @Override
                public int compare( Node vb1, Node vb2 ){
                    return Integer.valueOf(vb1.getId()).compareTo(Integer.valueOf(vb2.getId()));
                }
            });
            uiBusContainer.getChildren().setAll(dataRows);
        } catch (IndexOutOfBoundsException e ){
            e.printStackTrace();
        }
    }

    public void update( UIBusData activeBusData,  ArrayList<UIBusData> fleetBusData ){
        Platform.runLater(() -> {
            activeBusPos = activeBusData.getDiff() * splitCount;
            for( UIBusData busData : fleetBusData ){
                if( !busList.containsKey(busData.getBusCode())){
                    Bus busTemp = new Bus(busData.getBusCode(), busData.getStop() +  " - " + busData.getRouteDetails(), busData.getDiff() );
                    busTemp.getUI().setId(String.valueOf(busTemp.getDiff()));
                    if( busData.getBusCode().equals(uiBusCodeInput.getText()) ) ((BusController)busTemp.getController()).setActiveBusFlag();
                    addBus( busTemp);
                } else {
                    busList.get(busData.getBusCode()).setDiff( busData.getDiff() );
                    busList.get(busData.getBusCode()).setStop( busData.getStop() +  " - " + busData.getRouteDetails() );
                    busList.get(busData.getBusCode()).getUI().setId(String.valueOf(busData.getDiff()));
                    busList.get(busData.getBusCode()).notifyUI();
                }
            }
            sort();
            uiLastUpdatedLabel.setText(Common.getDateTime());
            uiErrorLabel.setText("");
            uiRouteLabel.setText(route);
        });
    }

    public void setActionListener( KahyaActionListener listener ){
        actionListener = listener;
    }
}
