package edu.berkeley.cs.amplab.carat.android.fragments;

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

import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.R.color;
import edu.berkeley.cs.amplab.carat.android.R.drawable;
import edu.berkeley.cs.amplab.carat.android.R.id;
import edu.berkeley.cs.amplab.carat.android.R.layout;
import edu.berkeley.cs.amplab.carat.android.R.string;

/**
 * 
 * @author Javad Sadeqzadeh
 *
 */
public class SummaryFragment extends Fragment {
	
	private final String TAG = "SummaryFragment";
	private int wellbehavedAppCount,
				hogCount,
				bugCount;
	Resources resources;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 wellbehavedAppCount = getArguments().getInt("wellbehaved");
		 hogCount = getArguments().getInt("hogs");
		 bugCount = getArguments().getInt("bugs");
		 
		 resources = getResources();
		 
		 super.onCreate(savedInstanceState);
	 }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.summary, container, false);
        
        PieGraph pg = (PieGraph) inflatedView.findViewById(R.id.piegraph);
        
        PieSlice slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.green));
        slice.setSelectedColor(resources.getColor(R.color.transparent_orange));
        slice.setValue(wellbehavedAppCount);
        slice.setTitle("first");
        pg.addSlice(slice);
        
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.orange));
        slice.setValue(hogCount);
        pg.addSlice(slice);
        
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.purple));
        slice.setValue(bugCount);
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
	
}
