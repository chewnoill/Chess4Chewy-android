package com.chewnoill.chess;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

public class LoginActivity extends FragmentActivity 
	implements LoginFragment.ActivityCallback{
	private static final String TAG = "LoginActivity";
	private SharedPreferences spref;
	private ProgressDialog mDialog_loading;
	private String username;
	private String password;
	private String registered_username;
	private String GCM_ID;
	
	private static final int REG_REGISTERING = 0x1;
	private static final int REG_REGISTERED = 0x2;
	private static final int REG_UNSUPORTED = 0x0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG,"onCreate");
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		username = spref.getString("username", "");
    	password = spref.getString("password","");
    	registered_username = spref.getString("registered_username","");
    	GCM_ID = spref.getString("GCM_ID","");
		if(savedInstanceState==null){
			

			FragmentManager fragmentManager = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			LoginFragment fragment = new LoginFragment();
			fragmentTransaction.replace(R.id.main, fragment);
			fragmentTransaction.commit();
			 
		
		} 
	}
	@Override
	public String getUsername() {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		return spref.getString("username", "");
	}
	@Override
	public String getPassword() {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		return spref.getString("password", "");
	}
	private void showLoading(){
		mDialog_loading = new ProgressDialog(this);
		mDialog_loading.setMessage("Loading...");
		mDialog_loading.setCancelable(false);
		mDialog_loading.show();
	}
	private void dismissLoading(){
		if(mDialog_loading!=null){
			mDialog_loading.dismiss();
			mDialog_loading = null;
		}
	}
	public void setRegisteredName(String username) {
		Editor editor = spref.edit();
    	editor.putString("registered_username",username);
    	editor.commit();
		
	}
	@Override
	public void login(){
		showLoading();
		BackendInterface post = new BackendInterface(new BackendCallback(null){
			
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,"onPostExecute: "+value.toString());
				dismissLoading();
				try {
					if(value.has("error")){
						Log.d(TAG,"error: "+value.toString());
					} else if (value.has("register_user") &&
							value.getBoolean("register_user")){
						Log.d(TAG,"detail: "+value.getString("detail"));
						setRegisteredName(username);
						//load games list
						loadMainFragment();
						
					} 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}});
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","register_user");
		hash_data.put("username",username);
		hash_data.put("password",password);
		hash_data.put("GCM_ID",GCM_ID);
		
		JSONObject data = new JSONObject(hash_data);
		Log.d(TAG,"Sending message: "+data.toString());
		post.execute(data);
	}
	@Override
	public int registerDevice(){
		Log.d(TAG,"registering...");
		try{
			GCMRegistrar.checkDevice(this);
			GCMRegistrar.checkManifest(this);
			final String regId = GCMRegistrar.getRegistrationId(this);
			if (regId.equals("")) {
			  GCMRegistrar.register(this, Shared.Secret.GCM_PROJECT_ID);
			  showLoading();
			  return REG_REGISTERING;
			} else {
				GCM_ID = regId;
				Log.v(TAG, "Already registered: "+regId);
				return REG_REGISTERED;
			}
		} catch (UnsupportedOperationException e){
			Log.d(TAG,"UnsupportedOperationException: "+e.getMessage());
			//sucks to be you
			return REG_UNSUPORTED;
		}
	}
	@Override
	public String getRegisteredName() {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		return spref.getString("registered_username", "");
	}
	@Override
	public void loadMainFragment() {
		Intent intent = new Intent(this, MainActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(intent);
	}
	@Override
	public void setUsername(String username) {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = spref.edit();
		this.username = username;
        editor.putString("username",username);
    	editor.commit();
		
	}


	@Override
	public void setPassword(String password) {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = spref.edit();
		this.password = password;
    	editor.putString("password",password);
    	editor.commit();
		
	}
}
