package ui;

import interfaces.KahyaActionListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import routescanner.RouteScanner;

public class MainScreen extends Application {

    private RouteScanner routeScanner;

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/kahya_client.fxml"));
            Parent content = loader.load();
            primaryStage.setTitle("Kahya Client");

            primaryStage.setScene(new Scene(content, 1024, 700 )); // @todo - calculate client's width-height, give offset to that
            primaryStage.show();
            MainScreenController controller = loader.getController();
            controller.setInitEvents();
            controller.setActionListener(new KahyaActionListener() {
                @Override
                public void onStart(String busCode) {
                    if( routeScanner != null ){
                        routeScanner.shutdown();
                        controller.reset();
                    }
                    routeScanner = new RouteScanner(busCode);
                    routeScanner.addStatusListener( (status) -> {
                        controller.setStatus(status);
                        controller.setRoute(routeScanner.getRoutes());
                    });
                    routeScanner.addKahyaUIListener( ( UIBusData ) -> {
                        controller.updateBusData( UIBusData );
                    });
                    routeScanner.start();
                }
            });

            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    Platform.exit();
                    System.exit(0);
                }
            });

        } catch( Exception e ){
            e.printStackTrace();
        }
    }
}
