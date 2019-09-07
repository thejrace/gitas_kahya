/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package utils;

public class ThreadHelper {

    /**
     * Delay the thread for given time
     *
     * @param milliseconds delay interval
     */
    public static void delay(int milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch( InterruptedException e ){
            e.printStackTrace();
        }
    }

    /**
     * Logger for threads
     *
     * @param threadName name of the thread
     * @param message message to be printed
     */
    public static void logStatus(String threadName, String message){
        System.out.println( Common.getDateTime() + "[ " + threadName + " ] :: " + message );
    }
}