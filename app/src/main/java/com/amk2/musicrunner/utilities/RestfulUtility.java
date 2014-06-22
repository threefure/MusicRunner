package com.amk2.musicrunner.utilities;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by paulou1009 on 6/19/14.
 */
public class RestfulUtility {

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
}
