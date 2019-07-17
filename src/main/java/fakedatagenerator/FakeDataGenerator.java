package fakedatagenerator;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

import java.io.File;

public class FakeDataGenerator {

    public static int counter = -1;

    public static JSONObject statusChangeTest(){
        return getData("statusChangeTest.json");
    }

    public static JSONObject stopChangeTest(){
        return getData("stopChangeTest_regular_forward.json");
    }

    private static JSONObject getData( String path ){
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        counter++;
        return new JSONArray(Common.readJSONFile(file)).getJSONObject(counter);
    }

}
