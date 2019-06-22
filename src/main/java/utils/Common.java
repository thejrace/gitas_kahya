package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Common {

    public static boolean checkFile( String path ){
        File f = new File( path );
        return f.exists();
    }

    public static String readJSONFile( String src ){
        try {
            FileReader fr = new FileReader( src);
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

}
