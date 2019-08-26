package utils;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;

public class APIRequest {

    public static String API_TOKEN;

    /**
     * Sends PUT request
     *
     * @param url request url
     * @param data data to be sent
     *
     */
    public static String PUT( String url, String data ){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.PUT)
                    .data("data", data)
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .execute();

           return response.parse().text();
        } catch (HttpStatusException e) {
            e.printStackTrace();
            System.out.println("sendDataToAPI !!!!check API Token!!!!");
        } catch( IOException e ) {
            System.out.println("sendDataToAPI error!");
            e.printStackTrace();
        }
        return "{}";
    }

    /**
     * Sends GET request
     *
     * @param url request url
     */
    public static String GET( String url ){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.GET)
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .execute();

            return response.parse().text();
        } catch (HttpStatusException e) {
            e.printStackTrace();
            System.out.println("sendDataToAPI !!!!check API Token!!!!");
        } catch( IOException e ) {
            System.out.println("sendDataToAPI error!");
            e.printStackTrace();
        }
        return "{}";
    }

    /**
     * Sends POST request
     *
     * @param url request url
     * @param data data to be sent
     */
    public static String POST( String url, String data ){
        try {
            Connection.Response response = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .data("data", data)
                    .header("Authorization", "Bearer " + API_TOKEN)
                    .header("Accept", "application/json")
                    .ignoreContentType(true)
                    .execute();

            return response.parse().text();
        } catch (HttpStatusException e) {
            e.printStackTrace();
            System.out.println("sendDataToAPI !!!!check API Token!!!!");
        } catch( IOException e ) {
            System.out.println("sendDataToAPI error!");
            e.printStackTrace();
        }
        return "{}";
    }
}
