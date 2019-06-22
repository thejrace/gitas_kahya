package fleet;

import fleet.Filo_Captcha_Controller;
import fleet.Refresh_Listener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import server.KahyaServer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Filo_Captcha_Scene extends Application {

    private VBox root;
    private Stage stage;
    private Filo_Captcha_Controller controller;
    @Override
    public void start( Stage primaryStage ) throws Exception{

        FXMLLoader fxmlLoader;
        try {
            fxmlLoader = new FXMLLoader(getClass().getResource("/captcha_scene.fxml"));
            Parent root = fxmlLoader.load();
            controller = fxmlLoader.getController();
            primaryStage.setTitle("Gitaş Filo Takip");
            primaryStage.initStyle(StageStyle.DECORATED);
            primaryStage.setScene(new Scene(root, 500, 250));
            primaryStage.show();
            stage = primaryStage;
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    Platform.exit();
                    System.exit(0);
                }
            });

            controller.add_finish_listener(new Refresh_Listener() {
                @Override
                public void on_refresh() {
                    try{


                        KahyaServer server = new KahyaServer();
                        server.start();


                    } catch( Exception e ){
                        e.printStackTrace();
                    }
                }
            });

        } catch( Exception e ){
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        launch(args);
    }






}
