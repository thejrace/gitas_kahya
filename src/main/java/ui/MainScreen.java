package ui;

import client.KahyaActionListener;
import client.KahyaClient;
import fleet.ClientFinishListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainScreen extends Application {

    private boolean UIInit = false;
    private Thread clientThread;
    private boolean runFlag = true;
    private KahyaClient client;

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/kahya_client.fxml"));
            Parent content = loader.load();
            primaryStage.setTitle("Kahya Client");

            primaryStage.setScene(new Scene(content, 750, 750 )); // @todo - calculate client's width-height, give offset to that
            primaryStage.show();
            MainScreenController controller = loader.getController();

            // active bus definitions
            controller.setActionListener(new KahyaActionListener() {
                @Override
                public void onStart(String busCode) {
                    controller.reset();
                    UIInit = false;
                    client = new KahyaClient(busCode);
                    client.addListener(new ClientFinishListener() {
                        @Override
                        public void onFinish() {
                            controller.update( client.getActiveBusData(), client.getOutput() );
                        }
                    });

                    clientThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while( runFlag ){
                                client.start();
                                if( client.getErrorFlag() ){
                                    controller.setError(client.getErrorMessage());
                                } else {
                                    if( !UIInit ){
                                        controller.splitDims(750, client.getStopCount() );
                                        controller.setRoute(client.getRoute());
                                        UIInit = true;
                                    }
                                }
                                try {
                                    Thread.sleep(20000);
                                } catch( InterruptedException e ){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    clientThread.setDaemon(true);
                    clientThread.start();
                }
            });







        } catch( Exception e ){
            e.printStackTrace();
        }
    }


}
