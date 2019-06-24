package client;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.MainScreen;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage ) throws Exception{

        MainScreen mainScreen = new MainScreen();
        mainScreen.start(new Stage());

    }


    public static void main(String[] args) {
        launch(args);
    }

}