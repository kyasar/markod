package com.dopamin.markod.authentication;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dopamin.markod.LoginActivity;
import com.dopamin.markod.MainActivity;
import com.dopamin.markod.objects.User;
import com.dopamin.markod.objects.UserLoginType;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kadir on 02.07.2015.
 */
public class SignInUp extends AsyncTask<String, Integer, Boolean> {

    public AsyncLoginResponse delegate = null;
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;
    /** application context. */
    private Activity activity;

    public SignInUp(Activity activity) {
        this.activity = activity;
        dialog = new ProgressDialog(activity);
    }

    protected void onPreExecute() {
        this.dialog.setMessage("Logging into markod.com");
        this.dialog.show();
    }

    //String data = null;
    private JSONObject sendHttpPost(String firstName, String lastName, String email, String id, String type) {
        // set the connection timeout value to 10 seconds (10000 milliseconds)
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

        HttpClient client = new DefaultHttpClient(httpParams);
        HttpPost post = new HttpPost(MainActivity.MDS_SERVER + "/mds/signup/fb");
        JSONObject json = null;

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("firstName", firstName));
        nameValuePairs.add(new BasicNameValuePair("lastName", lastName));
        nameValuePairs.add(new BasicNameValuePair("id", id));
        nameValuePairs.add(new BasicNameValuePair("type", type));

        /* UTF-8 charset encoding support */
        try {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            Log.v(MainActivity.TAG, "JSON Respond: " + sb.toString());
            json = new JSONObject(sb.toString());

            if (json.getString("status").equals("OK")) {
                return json.getJSONObject("user");
            } else
                return null;
        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private User createUserObject(JSONObject jsonUser) {
        if (jsonUser == null)
            return null;
        User user = new User();
        try {
            user.setFirstName(jsonUser.getString("firstName"));
            user.setLastName(jsonUser.getString("lastName"));
            user.setEmail(jsonUser.getString("email"));
            user.setId(jsonUser.getString("_id"));
            user.setPoints(Integer.parseInt(jsonUser.getString("points")));
            if (jsonUser.getJSONObject("facebook") != null) {
                user.setSocial_id(jsonUser.getJSONObject("facebook").getString("id"));
                user.setUserLoginType(UserLoginType.FACEBOOK_USER);
            } else if (jsonUser.getJSONObject("google") != null) {
                user.setSocial_id(jsonUser.getJSONObject("google").getString("id"));
                user.setUserLoginType(UserLoginType.GOOGLE_USER);
            } else {
                user.setUserLoginType(UserLoginType.LOCAL_USER);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Invoked by execute() method of this object
    // Does not affect main activity
    @Override
    protected Boolean doInBackground(String... args) {
        JSONObject jsonUser;
        String firstName = args[0];
        String lastName  = args[1];
        String email = args[2];
        String id    = args[3];
        String type    = args[4];

        jsonUser = sendHttpPost(firstName, lastName, email, id, type);
        MainActivity.user = createUserObject(jsonUser);

        if (MainActivity.user != null)
            return true;
        else
            return false;
    }

    // Executed after the complete execution of doInBackground() method
    // This method is executed in Main Activity
    @Override
    protected void onPostExecute(Boolean result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        // Start parsing the Google places in JSON format
        // Invokes the "doInBackground()" method of the class ParseTask
        Log.d(MainActivity.TAG, "postFB result, User acquired: " + result);
        //parserTask.execute(result);
        delegate.processFinish(result);
    }
}
