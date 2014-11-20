package edu.berkeley.cs.amplab.carat.android;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import com.echo.holographlibrary.PieGraph;
import com.echo.holographlibrary.PieGraph.OnSliceClickedListener;
import com.echo.holographlibrary.PieSlice;

public class SummaryFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_piegraph, container, false);
        final Resources resources = getResources();
        final PieGraph pg = (PieGraph) v.findViewById(R.id.piegraph);
        final Button animateButton = (Button) v.findViewById(R.id.animatePieButton);
        PieSlice slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.green_light));
        slice.setSelectedColor(resources.getColor(R.color.transparent_orange));
        slice.setValue(2);
        slice.setTitle("first");
        pg.addSlice(slice);
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.orange));
        slice.setValue(3);
        pg.addSlice(slice);
        slice = new PieSlice();
        slice.setColor(resources.getColor(R.color.purple));
        slice.setValue(8);
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

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
        animateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (PieSlice s : pg.getSlices())
                    s.setGoalValue((float)Math.random() * 10);
                pg.setDuration(1000);//default if unspecified is 300 ms
                pg.setInterpolator(new AccelerateDecelerateInterpolator());//default if unspecified is linear; constant speed
                pg.setAnimationListener(getAnimationListener());
                pg.animateToGoalValues();//animation will always overwrite. Pass true to call the onAnimationCancel Listener with onAnimationEnd

            }
        });
        return v;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public Animator.AnimatorListener getAnimationListener(){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d("piefrag", "anim end");
            }

            @Override
            public void onAnimationCancel(Animator animation) {//you might want to call slice.setvalue(slice.getGoalValue)
                Log.d("piefrag", "anim cancel");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
        else return null;

    }

	
}
