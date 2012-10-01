package edu.berkeley.cs.amplab.carat.android.ui;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.berkeley.cs.amplab.carat.android.CaratApplication.Type;
import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

public class DrawView extends View {

    private Context c = null;
    
    private Type type = null;
    
    private double ev = 0.0;
    private double evWithout = 0.0;
    private int sampleCount = 0;
    private double sig = 0.0;
    private String textBenefit = null;
    

    private String appName = null;

    public String getAppName() {
        return this.appName;
    }

    public DrawView(Context context) {
        super(context);
        this.c = context;
    }

    public Type getType() {
        return this.type;
    }

    public double getEvWithout() {
        return evWithout;
    }

    public double getEv() {
        return ev;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public double getSignificance() {
        return sig;
    }

    public void setHogsBugs(SimpleHogBug bugOrHog, String appName,
            boolean isBug, View parent) {

        this.ev = bugOrHog.getExpectedValue();
        this.evWithout = bugOrHog.getExpectedValueWithout();
        this.sampleCount = bugOrHog.getxVals().length;
        this.sig = bugOrHog.getwDistance();
        this.textBenefit = bugOrHog.textBenefit();

        this.type = isBug ? Type.BUG : Type.HOG;
        this.appName = appName;
        setFields(parent);
    }

    public void setParams(Type type, String appName, double[] xVals,
            double[] yVals, double[] xValsWithout, double[] yValsWithout,
            double ev, double evWithout, int sampleCount, double significance,
            View parent) {
        this.ev = ev;
        this.evWithout = evWithout;
        this.sampleCount = sampleCount;
        this.sig = significance;
        this.type = type;
        this.appName = appName;
        setFields(parent);
    }

    private void setFields(View parent) {
        TextView samples = (TextView) parent.findViewById(R.id.samples);
        TextView killBenefit = (TextView) parent.findViewById(R.id.killBenefit);
        ProgressBar significance = (ProgressBar) parent
                .findViewById(R.id.significanceBar);
        // TODO: Should be sample count == n, not number of x vals
        samples.setText(sampleCount + "");
        // TODO: Should be real significance
        significance.setProgress((int) (sig * 100));
        
        // TODO: Should be real error for os/model, currently 1m
        // TODO: Should be real error for hogs/bugs, currently ev/10

        if (textBenefit != null) {
            killBenefit.setText(textBenefit);
        } else {
            int errorMins = 1;
            double benefit = 100.0 / evWithout - 100.0 / ev;
            if (benefit < 0) {
                killBenefit.setText(c.getString(R.string.best));
            } else {

                int min = (int) (benefit / 60);
                int hours = (int) (min / 60);
                min -= hours * 60;

                killBenefit.setText(hours + "h " + min + "m \u00B1 "
                        + errorMins + "m");
            }
        }
    }
}
