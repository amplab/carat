package edu.berkeley.cs.amplab.carat.android.ui;

import edu.berkeley.cs.amplab.carat.android.CaratMainActivity;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ViewFlipper;

public abstract class BaseVFActivity extends Activity implements VFActivity {

    protected int viewIndex = 0;
    protected int baseViewIndex = 0;
    protected ViewFlipper vf = null;

    @Override
    public void setViewId(int id) {
        this.viewIndex = id;
    }
    
    public int getViewId() {
        return this.viewIndex;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onRetainNonConfigurationInstance()
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        return this;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object oldInstance = getLastNonConfigurationInstance();
        if (oldInstance != null)
            viewIndex = ((VFActivity) oldInstance).getViewId();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onBackPressed() {
        if (vf.getDisplayedChild() != baseViewIndex) {
            vf.setOutAnimation(CaratMainActivity.outtoRight);
            vf.setInAnimation(CaratMainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
