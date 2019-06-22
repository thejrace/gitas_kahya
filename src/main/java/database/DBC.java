package database;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;


public class DBC {

    private static DBC  instance;
    private BasicDataSource ds;

    // GITAS SERVER 01.05.2018
    /*private String dbAddress = "jdbc:mysql://localhost:3306/";
    private String userPass = "?user=root&password=";
    private String dbName = "ahmet";
    private String userName = "ahmet";
    private String password = "KHLHjklh654";*/

    // LOCAL 2019
    private String dbAddress = "jdbc:mysql://localhost:3306/";
    private String userPass = "?user=root&password=";
    private String dbName = "db3_gitas_filo_takip";
    private String userName = "root";
    private String password = "";


    private DBC(){

        ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUsername(userName);
        ds.setPassword(password);
        ds.setUrl(dbAddress + dbName + "?useSSL=false&useJvmCharsetConverters=false&useDynamicCharsetInfo=false&useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&useEncoding=true");

        ds.setMinIdle(15);
        ds.setMaxIdle(30);
        ds.setMaxOpenPreparedStatements(150);
        ds.setMaxTotal(150);


        Thread th = new Thread(new Runnable() {
            public void run() {
                while( true ){
                    System.out.println("[ IDLE DB: " + ds.getNumIdle() + "] - [ ACTIVE DB CON: " +  ds.getNumActive() + " ] " );
                    try {
                        Thread.sleep(10000);
                    } catch( InterruptedException e ){
                        e.printStackTrace();
                    }
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }


    public static DBC getInstance(){
        if (instance == null) {
            instance = new DBC();
            return instance;
        } else {
            return instance;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.ds.getConnection();
    }




}
