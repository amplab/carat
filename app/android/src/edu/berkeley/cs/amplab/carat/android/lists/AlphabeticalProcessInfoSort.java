package edu.berkeley.cs.amplab.carat.android.lists;

import java.util.Comparator;

import android.content.Context;

import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;

public class AlphabeticalProcessInfoSort implements
        Comparator<ProcessInfo> {

    private Context c;
    
    public AlphabeticalProcessInfoSort(Context c){
        this.c = c;
    }

    @Override
    public int compare(ProcessInfo lhs, ProcessInfo rhs) {
        String l = lhs.getPName();
        l = CaratApplication.labelForApp(c, l);
        String r = rhs.getPName();
        r = CaratApplication.labelForApp(c, r);
        if (l != null && r != null){
            return l.compareTo(r);
        }else
            return 0;
    }
}
