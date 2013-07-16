package com.chewnoill.chess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class BackendInterface extends AsyncTask<JSONObject, Integer, JSONObject> {
	private static final String TAG = "BackendInterface";
	private static final String USER_AGENT = "chess4chewy.android";
	private static final String PATH = "https://chess4chewy.appspot.com";
	private BackendCallback callback;
	public BackendInterface(BackendCallback callback){
		this.callback = callback;
	}
	@Override
	protected JSONObject doInBackground(JSONObject... arg0) {
		JSONObject ret = new JSONObject();
		
		try {
			StringEntity se = new StringEntity(arg0[0].toString());
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost(PATH);
			httpost.setEntity(se);
			httpost.setHeader("Content-type", "application/json");
			httpost.setHeader("User-Agent",USER_AGENT);
			
			HttpResponse response = httpclient.execute(httpost);
			
	
			String file = "";
			String line = "";
			
			BufferedReader in = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			while((line=in.readLine())!=null) {
				file += line;				
			}
			Log.d(TAG,file);
			response.getEntity().consumeContent();
			try{
				ret = new JSONObject(file);
			} catch (JSONException e) {
				e.printStackTrace();
				HashMap<String,String> t = new HashMap<String,String>();
				t.put("error","JSONException");
				t.put("file",file);
				ret = new JSONObject(t);
				
				
				return ret;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret;
	}
	@Override 
	public void onPostExecute(JSONObject value){
		callback.onPostExecute(value);
	}
	
}
