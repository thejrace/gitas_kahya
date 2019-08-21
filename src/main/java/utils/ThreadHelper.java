package utils;

public class ThreadHelper {

    public static void delay(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch( InterruptedException e ){
            e.printStackTrace();
        }
    }

    public static void logStatus(String threadName, String message){
        System.out.println( Common.getDateTime() + "[ " + threadName + " ] :: " + message );
    }

}
