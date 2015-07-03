package com.dopamin.markod.authentication;

import android.os.AsyncTask;
import android.util.Log;

import com.dopamin.markod.MainActivity;
import com.dopamin.markod.objects.User;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
public class FacebookSignup extends AsyncTask<String, Integer, String> {

    public static String MDS_SERVER = "http://192.168.43.120:8000";

    //String data = null;
    private JSONObject sendHttpPost(String firstName, String lastName, String email, String id, String token) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(MDS_SERVER + "/mds/signup/fb");
        JSONObject json = null;

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("firstName", firstName));
            nameValuePairs.add(new BasicNameValuePair("lastName", lastName));
            nameValuePairs.add(new BasicNameValuePair("id", id));
            nameValuePairs.add(new BasicNameValuePair("token", token));

            /* UTF-8 charset encoding support */
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));

            StringBuffer sb  = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            Log.v(MainActivity.TAG, "JSON Respond: " + sb.toString());
            json = new JSONObject(sb.toString());

            if (json.getString("status").equals("OK")) {
                return json.getJSONObject("user");
            }
            else
                return null;
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
            user.setPoints(Integer.parseInt(jsonUser.getString("points")));
            user.setId(jsonUser.getJSONObject("facebook").getString("id"));
            user.setToken(jsonUser.getJSONObject("facebook").getString("token"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return user;
    }

    // Invoked by execute() method of this object
    // Does not affect main activity
    @Override
    protected String doInBackground(String... args) {
        JSONObject jsonUser;
        String firstName = args[0];
        String lastName  = args[1];
        String email = args[2];
        String id    = args[3];
        String token = args[4];

        jsonUser = sendHttpPost(firstName, lastName, email, id, token);
        MainActivity.user = createUserObject(jsonUser);

        if (MainActivity.user != null)
            return "User object created.";
        else
            return "User object can NOT be created !";
    }

    // Executed after the complete execution of doInBackground() method
    // This method is executed in Main Activity
    @Override
    protected void onPostExecute(String result) {
        //ParserTask parserTask = new ParserTask();

        // Start parsing the Google places in JSON format
        // Invokes the "doInBackground()" method of the class ParseTask
        Log.d(MainActivity.TAG, "postFB result: " + result);
        //parserTask.execute(result);
    }
}
