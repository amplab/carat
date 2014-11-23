package edu.berkeley.cs.amplab.carat.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

public class SummaryFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		int wellbehavedAppsCount = getArguments().getInt("wellbehaved");
		int hogsCount = getArguments().getInt("hogs");
		int bugsCount = getArguments().getInt("bugs");
		
        final View v = inflater.inflate(R.layout.fragment_summary, container, false);
        final Resources resources = getResources();
        final PieGraph pg = (PieGraph) v.findViewById(R.id.piegraph);
        
        PieSlice slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.green_light));
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
                Toast.makeText(getActivity(),
                        "Slice " + index + " clicked",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        pg.setBackgroundBitmap(b);

        SeekBar seekBar = (SeekBar) v.findViewById(R.id.seekBarRatio);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pg.setInnerCircleRatio(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar = (SeekBar) v.findViewById(R.id.seekBarPadding);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pg.setPadding(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return v;
    }
}
