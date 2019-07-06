package server;

import fleet.Filo_Captcha_Scene;
import javafx.application.Application;
import javafx.stage.Stage;

public class ServerMain extends Application {


    @Override
    public void start(Stage primaryStage ) throws Exception{

            RouteIntersection.action();

//        Filo_Captcha_Scene captchaScene = new Filo_Captcha_Scene();
//        captchaScene.start(new Stage());

    }



    public static void main(String[] args) {
        launch(args);
    }



}
