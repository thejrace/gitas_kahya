package ui;

import client.KahyaClient;
import fleet.ClientFinishListener;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainScreen extends Application {

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
            String activeBus = "C-1760";
            KahyaClient client = new KahyaClient(activeBus);
            client.addListener(new ClientFinishListener() {
                @Override
                public void onFinish() {

                    System.out.println("finished client.");

                }
            });

            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    while( true ){
                        client.start();
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
