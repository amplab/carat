package edu.berkeley.cs.amplab.carat.android.ui;

import android.content.Context;
import android.view.View;
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
    private int sampleCountWo = 0;
    private double error = 0.0;
    private double errorWo = 0.0;
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
    
    public double getError() {
        return error;
    }
    
    public double getErrorWithout() {
        return errorWo;
    }

    public int getSampleCount() {
        return sampleCount;
    }
    
    public int getSampleCountWithout() {
        return sampleCountWo;
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
        this.error = bugOrHog.getError();
        this.errorWo = bugOrHog.getErrorWithout();
        this.sampleCount = (int) bugOrHog.getSamples();
        this.sampleCountWo = (int) bugOrHog.getSamplesWithout();

        this.type = isBug ? Type.BUG : Type.HOG;
        this.appName = appName;
        setFields(parent);
    }

    public void setParams(Type type, String appName,
            double ev, double evWithout, int sampleCount, int sampleCountWo, double significance,
            double error, double errorWo, View parent) {
        this.ev = ev;
        this.evWithout = evWithout;
        this.sampleCount = sampleCount;
        this.sig = significance;
        this.error = error;
        this.errorWo = errorWo;
        this.sampleCount = (int) sampleCount;
        this.sampleCountWo = (int) sampleCountWo;
        
        this.type = type;
        this.appName = appName;
        setFields(parent);
    }

    private void setFields(View parent) {
        TextView samples = (TextView) parent.findViewById(R.id.samples);
        TextView killBenefit = (TextView) parent.findViewById(R.id.killBenefit);
        
        TextView samplesWoT = (TextView) parent.findViewById(R.id.samplesWo);
        TextView errorT = (TextView) parent.findViewById(R.id.error);
        TextView errorWoT = (TextView) parent.findViewById(R.id.errorWo);
        
        if (sampleCount > 0){
        samplesWoT.setText(sampleCountWo +"");
        // TODO: Should be sample count == n, not number of x vals
        samples.setText(sampleCount + "");
        } else {
            samples.setText("?");
            samplesWoT.setText("?");
        }
        
        if (ev > 0 && error > 0){
          int errorM = (int) (100 / ev - 100 / (ev + error)) / 60;
          errorT.setText("\u00B1 "+errorM+"m");
          int errorMWo = (int) (100 / evWithout - 100 / (evWithout + errorWo)) / 60;
          errorWoT.setText("\u00B1 "+errorMWo+"m");
        }else {
            errorT.setText("?");
            errorWoT.setText("?");  
        }
        
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
