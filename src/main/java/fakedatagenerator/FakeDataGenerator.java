package fakedatagenerator;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

import java.io.File;

public class FakeDataGenerator {

    public static int counter = -1;

    public static JSONObject statusChangeTest(){
        return getData("statusChangeTest_normal.json");
    }

    public static JSONObject stopChangeTest(){
        return getData("stopChangeTest_mixTest2.json");
    }

    public static JSONObject routeIntersectionTest(){
        return getData("routeIntersectionTest_normal.json");
    }

    public static JSONObject ringPositionTest(){
        return getData("ringPositionTest.json");
    }

    private static JSONObject getData( String path ){
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(path).getFile());
        counter++;
        return new JSONArray(Common.readJSONFile(file)).getJSONObject(counter);
    }

}
