package edu.berkeley.cs.amplab.carat.android.ui;

import edu.berkeley.cs.amplab.carat.android.MainActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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
    
    /**
     * Switch to the given view by id and animate the change.
     * @param viewId The view to switch to, for findViewById().
     */
    public void switchView(int viewId){
        View target = findViewById(viewId);
        switchView(target);
    }
    
    /**
     * Switch to the given view by object and animate the change.
     * @param v The view to switch to.
     */
    public void switchView(View v){
        vf.setOutAnimation(MainActivity.outtoLeft);
        vf.setInAnimation(MainActivity.inFromRight);
        vf.setDisplayedChild(vf.indexOfChild(v));
        viewIndex = vf.indexOfChild(v);
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
            vf.setOutAnimation(MainActivity.outtoRight);
            vf.setInAnimation(MainActivity.inFromLeft);
            vf.setDisplayedChild(baseViewIndex);
            viewIndex = baseViewIndex;
        } else
            finish();
    }
}
