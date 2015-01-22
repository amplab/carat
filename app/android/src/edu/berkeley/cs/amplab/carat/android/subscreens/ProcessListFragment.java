package edu.berkeley.cs.amplab.carat.android.subscreens;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.lists.ProcessInfoAdapter;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.utils.Tracker;
import edu.berkeley.cs.amplab.carat.thrift.ProcessInfo;

public class ProcessListFragment extends Fragment {

	private static ProcessListFragment instance = null;
	
	public static ProcessListFragment getInstance() {
		if (instance == null)
			instance = new ProcessListFragment();
		return instance;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.processlist, container, false);
		ListView lv = (ListView) view.findViewById(R.id.processList);
        List<ProcessInfo> searchResults = SamplingLibrary
                .getRunningAppInfo(getActivity());
        lv.setAdapter(new ProcessInfoAdapter(getActivity(), searchResults));
        
        Tracker tracker = Tracker.getInstance();
		tracker.trackUser("ProcessList");
        
		return view;
	}
	
}
