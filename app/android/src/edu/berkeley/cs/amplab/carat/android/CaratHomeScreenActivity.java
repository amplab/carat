package edu.berkeley.cs.amplab.carat.android;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import edu.berkeley.cs.amplab.carat.android.protocol.ProtocolClient;
import edu.berkeley.cs.amplab.carat.android.storage.CaratDataStorage;
import edu.berkeley.cs.amplab.carat.thrift.CaratService;
import edu.berkeley.cs.amplab.carat.thrift.Feature;
import edu.berkeley.cs.amplab.carat.thrift.Reports;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ProgressBar;

/**
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class CaratHomeScreenActivity extends Activity {

  // Freshness timeout. Default: one hour
  public static final long FRESHNESS_TIMEOUT = 3600000L;
  // 1 minute
  // public static final long FRESHNESS_TIMEOUT = 60000L;

  CaratService.Client c = null;
  CaratDataStorage s = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home);
  }

  /**
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onResume()
   */
  @Override
  protected void onResume() {
    this.setTitle(getString(R.string.app_name) +" " + getString(R.string.version_name) + " - " +  getString(R.string.tab_my_device));
    s = new CaratDataStorage(this);
    setModelAndVersion();
    setMemory();
    setReportData();
    super.onResume();
  }

  /**
   * Called when View Process List is clicked.
   * 
   * @param v
   *          The source of the click.
   */
  public void onClickViewProcessList(View v) {
    toggleColors();
  }

  private void refreshReports() throws TException {
    if (System.currentTimeMillis() - s.getFreshness() > FRESHNESS_TIMEOUT) {
      String uuId = "2DEC05A1-C2DF-4D57-BB0F-BA29B02E4ABE";
      List<Feature> features = new ArrayList<Feature>();

      Feature feature = new Feature();
      feature.setKey("Model");
      String model = "iPhone 3GS";
      feature.setValue(model);
      features.add(feature);

      feature = new Feature();
      feature.setKey("OS");
      String OS = "5.0.1";
      feature.setValue(OS);
      features.add(feature);
      c = ProtocolClient.getInstance(getApplicationContext());
      Reports r = c.getReports(uuId, features);
      ProtocolClient.close();
      s.writeReports(r);
      s.writeFreshness();
    }
  }

  private void setModelAndVersion() {
    // Device model
    String model = SamplingLibrary.getModel();

    // Android version
    String version = SamplingLibrary.getOsVersion();

    Window win = this.getWindow();
    // The info icon needs to change from dark to light.
    TextView mText = (TextView) win.findViewById(R.id.dev_value);
    mText.setText(model);
    mText = (TextView) win.findViewById(R.id.os_ver_value);
    mText.setText(version);

    Log.i("SetModel", "board:" + android.os.Build.BOARD);
    Log.i("SetModel", "bootloader:" + android.os.Build.BOOTLOADER);
    Log.i("SetModel", "brand:" + android.os.Build.BRAND);
    Log.i("SetModel", "CPU_ABI 1 and 2:" + android.os.Build.CPU_ABI + ", "
        + android.os.Build.CPU_ABI2);
    Log.i("SetModel", "dev:" + android.os.Build.DEVICE);
    Log.i("SetModel", "disp:" + android.os.Build.DISPLAY);
    Log.i("SetModel", "FP:" + android.os.Build.FINGERPRINT);
    Log.i("SetModel", "HW:" + android.os.Build.HARDWARE);
    Log.i("SetModel", "host:" + android.os.Build.HOST);
    Log.i("SetModel", "ID:" + android.os.Build.ID);
    Log.i("SetModel", "manufacturer:" + android.os.Build.MANUFACTURER);
    Log.i("SetModel", "prod:" + android.os.Build.PRODUCT);
    Log.i("SetModel", "radio:" + android.os.Build.RADIO);
    // FIXME: SERIAL not available on 2.2
    // Log.i("SetModel", "ser:" + android.os.Build.SERIAL);
    Log.i("SetModel", "tags:" + android.os.Build.TAGS);
    Log.i("SetModel", "time:" + android.os.Build.TIME);
    Log.i("SetModel", "type:" + android.os.Build.TYPE);
    Log.i("SetModel", "unknown:" + android.os.Build.UNKNOWN);
    Log.i("SetModel", "user:" + android.os.Build.USER);
    Log.i("SetModel", "model:" + android.os.Build.MODEL);
    Log.i("SetModel", "codename:" + android.os.Build.VERSION.CODENAME);
    Log.i("SetModel", "release:" + android.os.Build.VERSION.RELEASE);
  }

  private void setMemory() {
    Window win = this.getWindow();
    // Set memory values to the progress bar.
    ProgressBar mText = (ProgressBar) win.findViewById(R.id.progressBar1);
    int[] totalAndUsed = SamplingLibrary.readMeminfo();
    mText.setMax(totalAndUsed[0]);
    mText.setProgress(totalAndUsed[1]);
    mText = (ProgressBar) win.findViewById(R.id.progressBar2);

    if (totalAndUsed.length > 2) {
      mText.setMax(totalAndUsed[2]);
      mText.setProgress(totalAndUsed[3]);
    }

    /* CPU usage */
    mText = (ProgressBar) win.findViewById(R.id.cpubar);
    int cpu = (int) (SamplingLibrary.readUsage() * 100);
    mText.setMax(100);
    mText.setProgress(cpu);
  }

  private void setReportData() {
    Thread th = new Thread() {
      public void run() {
        try {
          refreshReports();
        } catch (TException e) {
          e.printStackTrace();
        }
        final Reports r = s.getReports();
        Log.i("CaratHomeScreen", "Got reports: " + r);
        long l = System.currentTimeMillis() - s.getFreshness();
        final long min = l / 60000;
        final long sec = (l - min * 60000) / 1000;
        
        double bl = 100 / r.getModel().expectedValue;
        int blh = (int) (bl / 3600);
        bl -= blh*3600;
        int blmin = (int) (bl / 60);
        int bls = (int) (bl - blmin*60);
        final String blS = blh +"h " + blmin +"m " + bls +"s";

        runOnUiThread(new Runnable() {
          public void run() {
            setText(R.id.jscore_value, ((int) (r.getJScore() * 100)) + "");
            setText(R.id.updated, "(Updated " + min + "m " + sec + "s ago)");
            setText(R.id.batterylife_value, blS);
          }
        });
      }
    };
    th.start();
  }

  private void setText(int viewId, String text) {
    Window win = this.getWindow();
    TextView t = (TextView) win.findViewById(viewId);
    t.setText(text);
  }

  private int lastColor = R.color.black;

  private void toggleColors() {
    /*
     * Use arrays to make code easier to understand below. These elements need
     * to change from a shade of brown/green to another.
     */
    int[] browns = { R.id.jscore, R.id.updated, R.id.batterylife, R.id.apps, R.id.memactive,
        R.id.memused, R.id.dev, R.id.os_ver, R.id.cpu };

    int[] greens = { R.id.jscore_value, R.id.batterylife_value, R.id.dev_value, R.id.os_ver_value };

    Window win = this.getWindow();
    // The info icon needs to change from dark to light.
    ImageView infoIcon = (ImageView) win.findViewById(R.id.moreinfo);

    int green = R.color.green_dark;
    int brown = R.color.brown_dark;
    int w = R.color.white;
    int b = R.color.black;

    /* Change from dark to light or the other way */
    if (lastColor == b) {
      green = R.color.green;
      brown = R.color.brown;
      win.setBackgroundDrawableResource(w);
      infoIcon.setImageResource(R.drawable.infoicon);
      lastColor = w;
    } else {
      win.setBackgroundDrawableResource(b);
      infoIcon.setImageResource(R.drawable.infoicon_dark);
      lastColor = b;
    }

    /* Handle the green/brown shades */
    for (int k : browns)
      changeColor(win, k, brown);
    for (int k : greens)
      changeColor(win, k, green);
  }

  /**
   * Utility method to change the textColor
   */
  private void changeColor(Window w, int viewId, int colorId) {
    TextView target = ((TextView) w.findViewById(viewId));
    target.setTextColor(getResources().getColor(colorId));
  }
}
