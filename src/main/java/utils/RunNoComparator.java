/*
 *  Kahya - Gitas 2019
 *
 *  Contributors:
 *      Ahmet Ziya Kanbur 2019-
 *
 * */
package utils;

import routescanner.RunData;

import java.util.Comparator;

public class RunNoComparator implements Comparator<RunData> {
    /**
     * Comparator to sort data by run no
     *
     * @param data1 compared data x
     * @param data2 compared data y
     *
     * @return comparison result
     */
    @Override
    public int compare( RunData data1, RunData data2 ){
        return Integer.compare( data1.getDepartureNo(), data2.getDepartureNo() );
    }
}
