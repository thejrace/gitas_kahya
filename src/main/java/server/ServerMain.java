package server;

import fleet.Filo_Captcha_Scene;
import fleet.RouteFleetDownload;
import javafx.application.Application;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.util.ArrayList;

public class ServerMain extends Application {


    @Override
    public void start(Stage primaryStage ) throws Exception{

            //RouteIntersection.action();

//        Filo_Captcha_Scene captchaScene = new Filo_Captcha_Scene();
//        captchaScene.start(new Stage());


//        Thread th = new Thread(()->{
//            ArrayList<String> test = new ArrayList<>();
//            test.add("15BK");
//            test.add("14A");
//
//            StealCookie stealCookie = new StealCookie();
//            stealCookie.addListener(()->{
//                while(true ){
//
//                    RouteFleetDownload routeFleetDownload = new RouteFleetDownload(test);
//                    routeFleetDownload.action();
//                    JSONObject fleetData = routeFleetDownload.getOutput();
//                    System.out.println(fleetData);
//
//                    try {
//                        Thread.sleep(10000);
//                    } catch( InterruptedException e ){
//                        e.printStackTrace();
//                    }
//                }
//            });
//            stealCookie.action();
//
//
//
//
//        });
//        th.setDaemon(true);
//        th.start();


    }



    public static void main(String[] args) {
        launch(args);
    }



}
