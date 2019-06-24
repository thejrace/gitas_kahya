package ui;

import client.KahyaClient;
import fleet.ClientFinishListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainScreen extends Application {

    private boolean UIInit = false;

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
            String activeBus = "C-1757";
            KahyaClient client = new KahyaClient(activeBus);
            client.addListener(new ClientFinishListener() {
                @Override
                public void onFinish() {
                    controller.update( client.getActiveBusData(), client.getOutput() );
                }
            });

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while( true ){
                        client.start();
                        if( !UIInit ){
                            controller.splitDims(750, client.getStopCount() );
                            UIInit = true;
                        }
                        try {
                            Thread.sleep(10000);
                        } catch( InterruptedException e ){
                            e.printStackTrace();
                        }
                    }

                }
            });
            th.setDaemon(true);
            th.start();



        } catch( Exception e ){
            e.printStackTrace();
        }
    }


}
