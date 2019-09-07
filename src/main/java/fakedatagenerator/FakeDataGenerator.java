/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package fakedatagenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Common;

import java.io.File;

public class FakeDataGenerator {

    public static int counter = -1;
    public static boolean ACTIVE = false;
    public static JSONArray SIM_DATA;
    public static String ROUTE;

    public static JSONObject getSimData(){
        counter++;
        return SIM_DATA.getJSONObject(counter);
    }

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

    public static void reset(){
        SIM_DATA = new JSONArray();
        counter = -1;
    }

}
