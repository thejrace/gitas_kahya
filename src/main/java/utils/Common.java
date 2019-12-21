/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    /**
     * Checks if a file in given path exists
     *
     * @param path file path
     *
     * @return flag
     */
    public static boolean checkFile( String path ){
        File f = new File( path );
        return f.exists();
    }

    /**
     * Reads a json file
     *
     * @param src file to be read
     *
     * @return data as string
     */
    public static String readJSONFile( File src ){
        try {
            FileReader fr = new FileReader( src );
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while( line != null ){
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            br.close();
            fr.close();
            return sb.toString();
        } catch( IOException e ){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return a current date time string
     *
     * @return string datetime
     */
    public static String getDateTime(){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Return current hmin string.
     *
     * @return string date
     */
    public static String getCurrentHmin(){
        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Replaces Turkish chars of given string
     *
     * @param input string
     *
     * @return sef output
     */
    public static String replaceTurkishChars( String input ){
        return  input.replaceAll("Ü", "U").
                replaceAll("İ", "I").
                replaceAll("Ş", "S").
                replaceAll("Ç", "C").
                replaceAll("Ö", "O");

    }

    /**
     * @param str input
     *
     * @return trimmed output
     */
    public static String regexTrim( String str ){
        return str.replaceAll("\u00A0", "");
    }
}
