package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;

public class UIComponent {

    protected Object controller;
    protected Node UI;

    public void loadFXML( String fxmlName ){
        try {
            // load fxml layouts
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/"+fxmlName+".fxml"));
            UI = loader.load();
            controller = loader.getController();
        } catch( IOException e ){
            e.printStackTrace();
        }
    }

    public Object getController(){
        return controller;
    }

    public Node getUI(){
        return UI;
    }

}
