package com.flaremars.classmanagers.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * @author FlareMars on 2014-11-20 23:15
 * 负责各项网络操作
 */
public class HttpUtils {

    private final static String BASE_IP = "";

    public static boolean downloadPhotoToStream(String imageUrl,OutputStream outputStream){

        /*获取图片数据*/
        JSONObject result = null;
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(BASE_IP+imageUrl);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine())!=null)
                {
                    stringBuilder.append(line);
                }
            }
            else {
               return false;
            }
            result = new JSONObject(stringBuilder.toString());
        } catch(ClientProtocolException e){
            e.printStackTrace();
            return false;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            client.getConnectionManager().shutdown();
        }


        /*写入硬盘缓存*/
        BufferedOutputStream out = null;
        try {

            byte[] photoData = result.getString("data").getBytes("iso-8859-1");
            out = new BufferedOutputStream(outputStream, 8 * 1024);
            out.write(photoData);
        } catch (IOException e){
            e.printStackTrace();
            return false;
        } catch (JSONException e2){
            e2.printStackTrace();
            return false;
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
