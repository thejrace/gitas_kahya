package client;

import org.json.JSONArray;
import org.json.JSONObject;
import utils.Common;

public class ClientMain {

    public static void main(String[] args){


        String oto = "C-1751";


        KahyaClient client = new KahyaClient(oto);
        client.start();






    }

}
