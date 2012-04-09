package edu.berkeley.cs.amplab.carat.android;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class CaratAboutActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		WebView webview = (WebView) findViewById(R.id.aboutView);
		/*
		 * getWindow().requestFeature(Window.FEATURE_PROGRESS);
		 * 
		 * webview.getSettings().setJavaScriptEnabled(true);
		 */
		/*
		 * To display the amplab_logo, we need to have it stored in assets as
		 * well. If we don't want to do that, the loadConvoluted method below
		 * avoids it.
		 */
		webview.loadUrl("file:///android_asset/about.html");
	}

	/**
	 * This is a hack so that the image does not have to be copied from drawable
	 * to assets. Another solution would be to modify the html so it refers to
	 * ../android_res/drawable/amplab_logo.png.
	 */
	/*
	private void loadConvoluted() {
		try {
			InputStream is = getApplicationContext().getAssets().open(
					"about.html");
			Scanner r = new Scanner(is);
			StringBuilder all = new StringBuilder();
			while (r.hasNextLine()) {
				all.append(r.nextLine());
			}
			r.close();
			// Changing the base URL allows loading the images.
			// webview.loadDataWithBaseURL("file:///android_res/drawable/",
			// all.toString(), "text/html", "UTF-8", "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
