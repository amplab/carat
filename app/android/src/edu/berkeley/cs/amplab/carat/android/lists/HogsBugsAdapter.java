package edu.berkeley.cs.amplab.carat.android.lists;

import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public abstract class HogsBugsAdapter extends BaseAdapter {
    private SimpleHogBug[] allBugsOrHogs = null;

    private LayoutInflater mInflater;
    private CaratApplication a = null;

    public HogsBugsAdapter(CaratApplication a, SimpleHogBug[] results) {
        this.a = a;
        // Skip system apps.
        int items = 0;
        if (results != null)
            for (SimpleHogBug b : results) {
                if (!SamplingLibrary.isSystem(a.getApplicationContext(),
                        b.getAppName()))
                    items++;
            }
        allBugsOrHogs = new SimpleHogBug[items];
        int i = 0;
        if (results != null)
            for (SimpleHogBug b : results) {
                if (!SamplingLibrary.isSystem(a.getApplicationContext(),
                        b.getAppName())) {
                    allBugsOrHogs[i] = b;
                    i++;
                }
            }
        mInflater = LayoutInflater.from(a.getApplicationContext());
    }

    public int getCount() {
        return allBugsOrHogs.length;
    }

    public Object getItem(int position) {
        return allBugsOrHogs[position];
    }

    public long getItemId(int position) {
        return position;
    }

    protected abstract int getId();

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(getId(), null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            holder.txtName = (TextView) convertView.findViewById(R.id.bugName);
            holder.progConfidence = (ProgressBar) convertView
                    .findViewById(R.id.confidenceBar);
            holder.moreInfo = (ImageView) convertView
                    .findViewById(R.id.moreinfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SimpleHogBug item = allBugsOrHogs[position];
        if (item == null)
            return convertView;
        
        Drawable icon = a.iconForApp(item.getAppName());
        String label = a.labelForApp(item.getAppName());
        if (label == null)
            label = "Unknown";
        holder.txtName.setText(label);
        holder.appIcon.setImageDrawable(icon);
        holder.progConfidence.setProgress((int) (item.getwDistance() * 100));
        // holder.moreInfo...

        return convertView;
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView txtName;
        ProgressBar progConfidence;
        ImageView moreInfo;
    }
}