package fakedatagenerator;

import client.KahyaActionListener;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import routescanner.RouteScanner;
import ui.MainScreenController;

import java.net.URL;
import java.util.ResourceBundle;

public class FakeDataGeneratorForm extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fakedata_generator_v2.fxml"));
            Parent content = loader.load();
            primaryStage.setTitle("Kahya FDG");

            primaryStage.setScene(new Scene(content, 670, 700 ));
            primaryStage.show();
            FakeDataGeneratorController controller = loader.getController();


            /*primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent e) {
                    Platform.exit();
                    System.exit(0);
                }
            });*/

        } catch( Exception e ){
            e.printStackTrace();
        }
    }

}
