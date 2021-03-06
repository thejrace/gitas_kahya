package ui;

import client.KahyaActionListener;
import client.KahyaClient;
import fleet.ClientFinishListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.StringSimilarity;

import java.util.HashMap;
import java.util.Map;

public class MainScreen extends Application {

    private boolean UIInit = false;

    private KahyaClient client;
    private String prevBusCode;

    private Map<String, Boolean> threadFlags = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/kahya_client.fxml"));
            Parent content = loader.load();
            primaryStage.setTitle("Kahya Client");

            primaryStage.setScene(new Scene(content, 1018, 700 )); // @todo - calculate client's width-height, give offset to that
            primaryStage.show();
            MainScreenController controller = loader.getController();

            // active bus definitions
            controller.setActionListener(new KahyaActionListener() {
                @Override
                public void onStart(String busCode) {
                    controller.reset();
                    UIInit = false;
                    threadFlags.put(prevBusCode, false);
                    threadFlags.put(busCode, true);
                    // if there is a previous client, kill it
                    if( client != null ) client.shutdown();
                    client = new KahyaClient(busCode);
                    client.setUIListener( () -> {
                        controller.update( client.getActiveBusData(), client.getOutput() );
                        controller.setRoute(client.getRoutes());
                    });
                    client.setStatusListener( message -> {
                        controller.updateStatus(message);
                    });
                    Thread clientThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while( threadFlags.get(busCode) ){
                                client.start();
                                if( client.getErrorFlag() ){
                                    controller.setError(client.getErrorMessage());
                                } else {
                                    if( !UIInit ){
                                        controller.splitDims(750);
                                        UIInit = true;
                                    }
                                }
                                try {
                                    Thread.sleep(20000);
                                } catch( InterruptedException e ){
                                    e.printStackTrace();
                                }
                            }
                            System.out.println(busCode + " thread is killed!!!!!");
                        }
                    });
                    clientThread.setDaemon(true);
                    clientThread.start();
                    prevBusCode = busCode;
                }
            });
        } catch( Exception e ){
            e.printStackTrace();
        }
    }


}
