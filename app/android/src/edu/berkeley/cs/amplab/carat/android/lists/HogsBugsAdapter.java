package edu.berkeley.cs.amplab.carat.android.lists;

import java.util.Arrays;

import edu.berkeley.cs.amplab.carat.android.R;
import edu.berkeley.cs.amplab.carat.android.CaratApplication;
import edu.berkeley.cs.amplab.carat.android.sampling.SamplingLibrary;
import edu.berkeley.cs.amplab.carat.android.storage.SimpleHogBug;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HogsBugsAdapter extends BaseAdapter {
    private SimpleHogBug[] allBugsOrHogs = null;

    private LayoutInflater mInflater;
    private CaratApplication a = null;

    public HogsBugsAdapter(CaratApplication a, SimpleHogBug[] results) {
        this.a = a;

        Context c = a.getApplicationContext();
        // Skip system apps.
        int items = 0;
        if (results != null)
            for (SimpleHogBug b : results) {
                String appName = b.getAppName();
                if (appName == null)
                    appName = a.getString(R.string.unknown);
                if (appName.equals(CaratApplication.CARAT_PACKAGE)
                        || appName.equals(CaratApplication.CARAT_OLD))
                    continue;
                if (!SamplingLibrary.isHidden(c, appName))
                    items++;
            }
        allBugsOrHogs = new SimpleHogBug[items];
        int i = 0;
        if (results != null && results.length > 0 && allBugsOrHogs.length > 0
                && i < allBugsOrHogs.length)
            for (SimpleHogBug b : results) {
                String appName = b.getAppName();
                if (appName == null)
                    appName = a.getString(R.string.unknown);
                if (appName.equals(CaratApplication.CARAT_PACKAGE)
                        || appName.equals(CaratApplication.CARAT_OLD))
                    continue;
                // Apparently the number of items changes from "items" above?
                if (!SamplingLibrary.isHidden(c, appName)
                        && i < allBugsOrHogs.length) {
                    allBugsOrHogs[i] = b;
                    i++;
                }
            }
        Arrays.sort(allBugsOrHogs);
        mInflater = LayoutInflater.from(c);
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

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.hog, null);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
            holder.txtName = (TextView) convertView.findViewById(R.id.appName);
            holder.textBenefit = (TextView) convertView
                    .findViewById(R.id.benefit);
            holder.moreInfo = (ImageView) convertView
                    .findViewById(R.id.moreinfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (allBugsOrHogs == null || position < 0
                || position >= allBugsOrHogs.length)
            return convertView;

        SimpleHogBug item = allBugsOrHogs[position];
        if (item == null)
            return convertView;

        Drawable icon = CaratApplication.iconForApp(a.getApplicationContext(),
                item.getAppName());
        String label = CaratApplication.labelForApp(a.getApplicationContext(),
                item.getAppName());
        if (label == null)
            label = a.getString(R.string.unknown);

        PackageInfo pak = SamplingLibrary.getPackageInfo(
                a.getApplicationContext(), item.getAppName());
        String ver = "";
        if (pak != null){
            ver = pak.versionName;
            if (ver == null)
                ver = pak.versionCode+"";
        }
        
        holder.txtName.setText(label + " " + ver);
        holder.appIcon.setImageDrawable(icon);
        holder.textBenefit.setText(item.textBenefit());
        // holder.moreInfo...

        return convertView;
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView txtName;
        TextView textBenefit;
        ImageView moreInfo;
    }
}