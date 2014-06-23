package com.amk2.musicrunner.utilities;

import android.os.StrictMode;

import com.amk2.musicrunner.Constant;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by paulou1009 on 6/19/14.
 */
public class RestfulUtility {

    public static final String LOGIN_ENDPOINT = "/login";
    public static final String REGISTER_ENDPOINT = "/register";

    //extract response content
    public static String getStatusCode(HttpResponse response){
        if(response == null)
            return null;
        StringBuilder sb = new StringBuilder();
        String line = "";
        try{
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException io){

        }
        return sb.toString();
    }

    public static HttpResponse restfulPostRequest(String endpoint, List<NameValuePair> pairs) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(Constant.AWS_HOST + endpoint);
        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            response = client.execute(post);

        } catch (UnsupportedEncodingException uee) {

        } catch (ClientProtocolException cpe) {
            //ignore this exception for now
        } catch (IOException ioe) {
            //ignore this exception for now
        }
        return response;
    }
}
