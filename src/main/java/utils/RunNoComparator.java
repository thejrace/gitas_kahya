package utils;

import fleet.RunData;

import java.util.Comparator;

public class RunNoComparator implements Comparator<RunData> {
    @Override
    public int compare( RunData data1, RunData data2 ){
        return Integer.compare( data1.getDepartureNo(), data2.getDepartureNo() );
    }
}
