package edu.berkeley.cs.amplab.carat.android;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.berkeley.cs.amplab.carat.android.storage.CaratSampleDB;
import edu.berkeley.cs.amplab.carat.thrift.Sample;
import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.os.Bundle;
import android.widget.TextView;

public class SampleDebugActivity extends Activity {

    /** Called when the activity is first created. */

    TextView batteryResult;
    TextView CpuResult;
    TextView MemoryResult;
    TextView RunProcResult;
    TextView WifiSignalResult;

    long totalCpuTime;
    long totalIdleTime;
    long totalCpuUsage;
    /* Running process */
    List<RunningAppProcessInfo> runningProcess;

    /* Memory Info */
    String MemoryTotalInfo;

    /* battery Variable */
    double Batterylevel = 0;
    String Batterystatus = null;
    String Batteryhealth = null;
    StringBuilder sbattery = new StringBuilder();
    CaratSampleDB db = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample);
        db = new CaratSampleDB(getApplicationContext());

        TextView uuid = (TextView) findViewById(R.id.uuid);
        uuid.setText("UUID: "
                + SamplingLibrary.getUuid(getApplicationContext()));
        CpuResult = (TextView) findViewById(R.id.CpuResult);
        MemoryResult = (TextView) findViewById(R.id.MemoryResult);
        batteryResult = (TextView) findViewById(R.id.Result);
        RunProcResult = (TextView) findViewById(R.id.runningProcesses);
        // WifiSignalResult =(TextView)findViewById(R.id.WifiSignalStrength);

        // TelephonyManager TM = ( TelephonyManager
        // )getSystemService(Context.TELEPHONY_SERVICE);
        // MyPhoneStateListener cdmaStregnthInfo= new MyPhoneStateListener();
        // TM.listen(cdmaStregnthInfo
        // ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        // Allow swipe to change tabs
        findViewById(R.id.sampleScroll).setOnTouchListener(
                SwipeListener.instance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        updateSampleScreen();
        super.onResume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#finish()
     */
    @Override
    public void finish() {
        super.finish();
    }

    private void updateSampleScreen() {
        Sample[] list = db.querySamples();
        StringBuilder sb = new StringBuilder();
        String dfs = "EEE MMM dd HH:mm:ss zzz yyyy";
        SimpleDateFormat df = new SimpleDateFormat(dfs);
        for (Sample s : list) {
            String trig = s.getTriggeredBy()
                    .replace("android.intent.action.", "")
                    .replace("edu.berkeley.cs.amplab.carat.android.", "");
            sb.append(df.format(new Date((long) (s.getTimestamp() * 1000)))
                    + " " + trig + "\n");
            int i = 0;
            try {
                Field[] sampleFields = s.getClass().getFields();
                for (Field f : sampleFields) {
                    // Skip Thrift internal stuff and already shown fields
                    if (f.getName().equals("metaDataMap"))
                        continue;
                    if (f.getName().equals("triggeredBy"))
                        continue;
                    if (f.getName().equals("timestamp"))
                        continue;
                    Object val = f.get(s);
                    if (val == null)
                        val = "null";
                    if (val instanceof List)
                        val = ((List) val).size() + " items";
                    sb.append(f.getName() + " = " + val.toString());
                    i++;
                    if (i == 2) {
                        sb.append('\n');
                        i = 0;
                    } else
                        sb.append(" ");
                }
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (i != 0)
                sb.append('\n');
            sb.append('\n');
        }

        TextView v = (TextView) findViewById(R.id.Result);
        v.setText(sb.toString());
    }

    /*
     * private class MyPhoneStateListener extends PhoneStateListener {
     * 
     * @Override public void onSignalStrengthsChanged(SignalStrength ss) {
     * super.onSignalStrengthsChanged(ss); int ecio= ss.getCdmaEcio(); int
     * strength = ss.getCdmaDbm();
     * Log.v("cdmaSignal","CDMA:"+strength+"CDMA ecio:"+ecio); }
     * 
     * }
     */
}
