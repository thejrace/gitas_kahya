package fakedatagenerator;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FakeDataGeneratorController implements Initializable {

    @FXML private AnchorPane uiContainer;
    @FXML private TextField uiFormCodeInput;
    @FXML private TextField uiFormRouteInput;
    @FXML private TextField uiFormRouteDetailInput;
    @FXML private TextField uiFormStatusInput;
    @FXML private Button uiFormSubmitBtn;
    @FXML private ListView<FakeRunData> uiDataOutputList;
    @FXML private Button uiFakeDataSaveBtn;
    @FXML private Button uiClearFormBtn;
    @FXML private ListView<String> uiStopDataOutputList;
    @FXML private TextField uiStopInput;
    @FXML private TextField uiActiveNoInput;
    @FXML private Button uiStopSubmitBtn;
    private String route;

    private ArrayList<FakeRunData> fakeRunDataList = new ArrayList<>();
    private ArrayList<String> fakeStopList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        uiFormSubmitBtn.setOnMousePressed( ev -> {
             FakeRunData fakeData = new FakeRunData(
                     uiFormCodeInput.getText(),
                     uiFormRouteInput.getText(),
                     uiFormRouteDetailInput.getText(),
                     uiFormStatusInput.getText()
             );
             if( fakeData.check() ){
                 route = fakeData.getRoute();
                 uiDataOutputList.getItems().add(fakeData);
                 fakeRunDataList.add(fakeData);
             }
        });

        uiStopSubmitBtn.setOnMousePressed( ev -> {
            String stop = Common.regexTrim(uiStopInput.getText());
            if( !stop.equals("") ){
                fakeStopList.add(stop);
                uiStopDataOutputList.getItems().add(stop);
            }
        });

        uiClearFormBtn.setOnMousePressed( ev -> {
            fakeRunDataList.clear();
            uiDataOutputList.getItems().clear();
            fakeStopList.clear();
            uiStopDataOutputList.getItems().clear();
        });

        uiFakeDataSaveBtn.setOnMousePressed( ev -> {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            try {
                File file = fileChooser.showSaveDialog(null);
                saveTextToFile( file);
            } catch( NullPointerException e ){ }
        });

    }

    private void saveTextToFile(File file) {
        try {
            String activeIndex = Common.regexTrim(uiActiveNoInput.getText());
            if( activeIndex.equals("") ) return;
            int activeNo = Integer.valueOf(activeIndex) - 1;
            JSONArray output = new JSONArray();
            JSONObject total;
            for( int k = 0; k < fakeStopList.size(); k++ ){
                JSONArray runData = new JSONArray();
                total = new JSONObject();
                for( int j = 0; j < fakeRunDataList.size(); j++ ){
                    if( j == activeNo ){
                        fakeRunDataList.get(j).setStop(fakeStopList.get(k));
                    }
                    runData.put(fakeRunDataList.get(j).convertToJSON());
                }
                total.put(fakeRunDataList.get(0).getBusCode(), runData);
                output.put(total);
            }
            JSONObject full = new JSONObject();
            JSONObject info = new JSONObject();
            info.put("route", route);
            full.put("data", output);
            full.put("info",info );
            PrintWriter writer;
            writer = new PrintWriter(file);
            writer.println(full.toString());
            writer.close();
            System.out.println("TAMAM!");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


}
