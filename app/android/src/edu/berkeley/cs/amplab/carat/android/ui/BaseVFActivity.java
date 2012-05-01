package edu.berkeley.cs.amplab.carat.android.ui;

import android.app.Activity;
import android.os.Bundle;

public abstract class BaseVFActivity extends Activity implements VFActivity {

    protected int viewIndex = 0;

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
    
    
}
