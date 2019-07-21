package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;
import fleet.StealCookie;
import routescanner.UIBusData;
import ui.Bus;
import ui.MainScreen;

import java.util.*;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage ) throws Exception{
        StealCookie stealCookie = new StealCookie();
        stealCookie.addListener(() -> {
            Platform.runLater(()-> {
                try {
                    MainScreen mainScreen = new MainScreen();
                    mainScreen.start(new Stage());
                } catch ( Exception e ){
                    e.printStackTrace();
                }
            });
        });
        stealCookie.action();

        /*Map<String, Bus> test = new HashMap<>();
        Bus bus1 = new Bus(new UIBusData("C-1882", "TEST", 44, "AT"));
        Bus bus2 = new Bus(new UIBusData("C-1883", "TEST", 6, "AT"));
        Bus bus3 = new Bus(new UIBusData("C-1884", "TEST", 9, "AT"));
        test.put("C-1882", bus1);
        test.put("C-1883", bus2);
        test.put("C-1884", bus3);

        List<Bus> sorted = new ArrayList<>(test.values());
        Collections.sort(sorted, new Comparator<Bus>(){
            @Override
            public int compare( Bus vb1, Bus vb2 ){
                return vb1.getData().getDiff() - vb2.getData().getDiff();
            }
        });
        int limit = 5;
        int activeBusPos = 6;
        String activeBusCode = "C-1883";
        int midIndex;
        for( int k = 0; k < sorted.size(); k++ ) {
            Bus busTemp = test.get(sorted.get(k));
            if( busTemp.getData().getBusCode().equals(activeBusCode) ) {
                for( int j = k; j < k+limit; j++ ){
                    if( sorted.get(j) != null ){

                    }
                }
            }
        }*/

    }


    public static void main(String[] args) {
        launch(args);
    }

}