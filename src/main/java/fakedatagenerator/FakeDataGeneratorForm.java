package fakedatagenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

        } catch( Exception e ){
            e.printStackTrace();
        }
    }

}
