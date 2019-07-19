package ui;

import interfaces.KahyaActionListener;
import fakedatagenerator.FakeDataGenerator;
import routescanner.UIBusData;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Common;

import java.io.File;
import java.net.URL;
import java.util.*;

public class MainScreenController implements Initializable {

    @FXML private VBox uiBusContainer;
    @FXML private TextField uiBusCodeInput;
    @FXML private Button uiActionBtn;
    @FXML private Button uiSimulationBtn;
    @FXML private Button uiFDGBtn;
    @FXML private Button uiSimulationResetBtn;
    @FXML private Label uiRouteLabel;
    @FXML private Label uiLastUpdatedLabel;
    @FXML private Label uiErrorLabel;
    @FXML private CheckBox uiDebugCheckbox;

    @FXML private ListView<String> uiStatusContainer;

    private String activeBusCode;
    private String route;
    private Map<String, Bus> busList = new HashMap<>();
    private KahyaActionListener actionListener;
    private boolean debugFlag = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        uiActionBtn.setOnMousePressed( ev -> {
            kahyaActionStart();
        });

        uiDebugCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if( !newValue ){
                    uiStatusContainer.getItems().clear();
                    uiStatusContainer.setVisible(false);
                    debugFlag = false;
                } else {
                    uiStatusContainer.setVisible(true);
                }
                debugFlag = newValue;
            }
        });

        uiSimulationBtn.setOnMousePressed( ev -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(null);
            try {
                JSONObject fakeData = new JSONObject(Common.readJSONFile(file));
                System.out.println(fakeData);
                FakeDataGenerator.ACTIVE = true;
                FakeDataGenerator.SIM_DATA = fakeData.getJSONArray("data");
                FakeDataGenerator.ROUTE = fakeData.getJSONObject("info").getString("route");
                actionListener.onStart("SIMULATION");
            } catch( JSONException e ){

            }
        });

        uiFDGBtn.setOnMousePressed( ev -> {
            try {
                FakeDataGeneratorForm fakeDataGeneratorForm = new FakeDataGeneratorForm();
                fakeDataGeneratorForm.start(new Stage());
            } catch ( Exception e ){
                e.printStackTrace();
            }
        });

        uiSimulationResetBtn.setOnMousePressed( ev ->{
            FakeDataGenerator.reset();
            reset();
        });

    }

    public void setStatus( String status ){
        Platform.runLater( () -> {
            if( !debugFlag ) return;
            if( uiStatusContainer.getItems().size() > 250 ){
                uiStatusContainer.getItems().clear();
            }
            uiStatusContainer.getItems().add(status);
        });
    }

    public void reset(){
        Platform.runLater(() -> {
            busList = new HashMap<>();
            uiBusContainer.getChildren().clear();
            uiStatusContainer.getItems().clear();
        });
    }

    public void setRoute( String route ){
        this.route = route;
    }

    public void addBus( Bus bus ){
        uiBusContainer.getChildren().add( bus.getUI() );
        busList.put(bus.getData().getBusCode(), bus );
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
            Collections.reverse(dataRows);
            uiBusContainer.getChildren().setAll(dataRows);
        } catch (IndexOutOfBoundsException e ){
            e.printStackTrace();
        }
    }

    public void updateBusData( UIBusData data ){
        Platform.runLater(() ->{
            if( !busList.containsKey(data.getBusCode())){
                Bus busTemp = new Bus(data);
                busTemp.getUI().setId(String.valueOf(data.getDiff()));
                if( data.getBusCode().equals(activeBusCode) ) ((BusController)busTemp.getController()).setActiveBusFlag();
                addBus( busTemp );
            } else {
                busList.get(data.getBusCode()).setData(data);
                busList.get(data.getBusCode()).getUI().setId(String.valueOf(data.getDiff()));
                busList.get(data.getBusCode()).notifyUI();
            }
            sort();
            //@todo burda olan otobus, kahyaclient ten gelmediyse UI den uçur
            uiLastUpdatedLabel.setText(Common.getDateTime());
            uiErrorLabel.setText("");
            uiRouteLabel.setText(route);
        });
    }

    private void kahyaActionStart(){
        String busCode = Common.regexTrim(uiBusCodeInput.getText());
        if( busCode.equals("") ) return;
        activeBusCode = ( busCode.contains("-") ) ? busCode.toUpperCase() : busCode.substring(0,1).toUpperCase() + "-" + busCode.substring(1).toUpperCase();
        uiStatusContainer.getItems().add("Kahya tetik.");
        actionListener.onStart( activeBusCode );
    }

    public void setActionListener( KahyaActionListener listener ){
        actionListener = listener;
        uiActionBtn.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                kahyaActionStart();
            }
        });
    }
}
