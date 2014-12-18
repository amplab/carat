package edu.berkeley.cs.amplab.carat.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SummaryFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		int wellbehavedAppsCount = getArguments().getInt("wellbehaved");
		int hogsCount = getArguments().getInt("hogs");
		int bugsCount = getArguments().getInt("bugs");
		
        final View inflatedView = inflater.inflate(R.layout.fragment_summary, container, false);
        final Resources resources = getResources();
        final PieGraph pg = (PieGraph) inflatedView.findViewById(R.id.piegraph);
        
        PieSlice slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.green));
        slice.setSelectedColor(resources.getColor(R.color.transparent_orange));
        slice.setValue(wellbehavedAppsCount);
        slice.setTitle("first");
        pg.addSlice(slice);
        
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.orange));
        slice.setValue(hogsCount);
        pg.addSlice(slice);
        
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.purple));
        slice.setValue(bugsCount);
        pg.addSlice(slice);
        
        pg.setOnSliceClickedListener(new OnSliceClickedListener() {

            @Override
            public void onClick(int index) {
            	
            	switch (index) {
            	case 0:
            		Toast.makeText(getActivity(),
                            R.string.userswithoutany,
                            Toast.LENGTH_SHORT)
                            .show();
            		break;
            	case 1:
            		Toast.makeText(getActivity(),
                            R.string.userswithhogs,
                            Toast.LENGTH_SHORT)
                            .show();
            		break;
            	case 2:
            		Toast.makeText(getActivity(),
                            R.string.userswithbugs,
                            Toast.LENGTH_SHORT)
                            .show();
            		break;
            	}
                
            }
        });

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        pg.setBackgroundBitmap(b);

        // onCreateView() method should always return the inflated view
        return inflatedView;
    }
	
	@Override
    public void onResume() {
    	Log.i("SummaryFragment", "resumed");
    	
        ((CaratApplication) getActivity().getApplication()).refreshUi();
        
        super.onResume();
    }
}
