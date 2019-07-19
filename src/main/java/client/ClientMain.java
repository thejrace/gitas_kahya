package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import fleet.StealCookie;
import ui.MainScreen;

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


    }


    public static void main(String[] args) {
        launch(args);
    }

}