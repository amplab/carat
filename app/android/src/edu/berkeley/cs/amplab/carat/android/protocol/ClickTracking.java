package edu.berkeley.cs.amplab.carat.android.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import android.os.AsyncTask;
import android.util.Log;

public class ClickTracking {
    
    private static final String TAG = "ClickTracking";

    private static final String ADDRESS_OLD = "http://data-bakharzy.rhcloud.com/api/app/applications/70dff194-2871-4ad8-9795-3f27f0021713/actions";
    private static final String ADDRESS_VM = "http://86.50.18.40:8080/data/app/applications/f233a990-0421-4d84-b333-d0c93e7f171f/actions";

    public static void track(String user, String name, HashMap<String, String> options) {
        HttpAsyncTask task = new HttpAsyncTask(user, name, options);
        /* TODO: We need to store the click in this task, not send yet.
         * SampleSender will then later get them from storage and send.
         */
        task.execute(ADDRESS_OLD, ADDRESS_VM);

    }

    private static String POST(String url, Action action) {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            // 3. build json String using Jacksin Library
            String json = "";
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(action);

            Log.i(TAG, "JSON=\n" + json);
            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    static class HttpAsyncTask extends AsyncTask<String, Void, String> {
        HashMap<String, String> options = null;
        private String name = null;
        private String user = null;

        public HttpAsyncTask(String user, String name, HashMap<String, String> options) {
            this.name = name;
            this.user = user;
            this.options = options;
            // Every event should have time, so add it here.
            options.put("time", System.currentTimeMillis() + "");
        }

        @Override
        protected String doInBackground(String... urls) {

            Action action = new Action();

            /*
             * Toast t = Toast.makeText(context, "execute for uuid="+user+" button="+name+" app="+options.get("app"), Toast.LENGTH_LONG);
             * t.show();
             */
            action.setName(name);
            action.setUsername(user);
            action.setOptions(options);
            String ret = null;
            for (String url : urls) {
                ret = POST(url, action);
            }
            return ret;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //Log.e("InputStream", result);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        StringBuilder result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null)
            result.append(line + "\n");

        inputStream.close();
        return result.toString();
    }
}
