/**
 * 
 */
package edu.berkeley.cs.amplab.carat.android.ui;

import java.io.IOException;
import java.util.Locale;

import edu.berkeley.cs.amplab.carat.android.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

/**
 * A WebView that, when asked to load a document, will look for it in
 * directories named after the currently set language's two-letter ISO code
 * within assets. Otherwise, behaves exactly like a regular WebView.
 * 
 * @author Eemil Lagerspetz
 * 
 */
public class LocalizedWebView extends WebView {

    private static final String TAG = "LocalizedWebView";

    /**
     * @see android.webkit.WebView(android.content.Context)
     */
    public LocalizedWebView(Context context) {
        super(context);

    }

    /**
     * @see android.webkit.WebView(android.content.Context,
     *      android.util.AttributeSet)
     */
    public LocalizedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    /**
     * @see android.webkit.WebView(android.content.Context,
     *      android.util.AttributeSet, int)
     */
    public LocalizedWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.webkit.WebView#loadUrl(java.lang.String)
     */
    @Override
    public void loadUrl(String url) {
        Context c = this.getContext();

        // Fixes the white flash when showing the page for the first time.
        String black = c.getString(R.string.blackBackground);
        if (black != null && black.equals("true"))
            setBackgroundColor(0);

        Log.d(TAG, "Opening url=" + url);
        if (!url.startsWith("file:///android_asset/")) {
            super.loadUrl(url);
            return;
        }

        // 01234567890123456789012
        String fname = url.substring(22);
        Locale l = Locale.getDefault();
        if (l == null) {
            super.loadUrl(url);
            return;
        }

        String lang = l.getLanguage();
        Log.d(TAG, "Lang=" + lang);
        if (lang == null || lang.length() <= 0) {
            super.loadUrl(url);
            return;
        }

        String localizedPath = lang + "/" + fname;
        String localizedUrl = "file:///android_asset/" + localizedPath;
        Log.d(TAG, "localizedUrl=" + localizedUrl);

        try {
            String[] langFiles = c.getAssets().list(lang);
            for (String file : langFiles) {
                if (file.equals(fname)) {
                    super.loadUrl(localizedUrl);
                    return;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
