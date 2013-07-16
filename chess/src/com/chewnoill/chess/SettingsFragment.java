package com.chewnoill.chess;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
	private static String TAG = "SettingsFragment";
	private ActivityCallback mCallback;
	String username,password,GCM_ID,registered_username;
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		final View ret = inflater.inflate(R.layout.settings, null);
		Button reg = (Button) ret.findViewById(R.id.register);
		EditText username_view = (EditText) ret.findViewById(R.id.username);
		username_view.setText(username);
		username_view.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				username = ((EditText)v).getText().toString();
				mCallback.setUsername(username);
				refreshIsRegistered(ret);
				return false;
			}
		});
		
		EditText password_view = (EditText) ret.findViewById(R.id.password);
		password_view.setText(password);
		password_view.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				password = ((EditText)v).getText().toString();
				mCallback.setPassword(password);
				refreshIsRegistered(ret);
				return false;
			}
		});
		refreshIsRegistered(ret);
		
		reg.setOnTouchListener(new OnTouchListener(){
	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getActionMasked()==MotionEvent.ACTION_DOWN){
					EditText username_view = (EditText)ret.findViewById(R.id.username);
					EditText password_view = (EditText)ret.findViewById(R.id.password);
					username = username_view.getText().toString();
					password = password_view.getText().toString();
					
					
					BackendInterface post = new BackendInterface(new BackendCallback(null){
						
						@Override
						public void onPostExecute(JSONObject value) {
							Log.d(TAG,"onPostExecute: "+value.toString());
							try {
								if(value.has("error")){
									Log.d(TAG,"error: "+value.toString());
								} else if (value.has("register_user") &&
										value.getBoolean("register_user")){
									TextView reg_view = (TextView) ret.findViewById(R.id.is_registered);
									reg_view.setText("registered");
									Log.d(TAG,"detail: "+value.getString("detail"));
									mCallback.setRegisteredName(username);
									
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
					return true;
				}
				return true;
			}});
		return ret;
	}
	private void refreshIsRegistered(View v){
		TextView reg_view = (TextView) v.findViewById(R.id.is_registered);
		if(registered_username.length()>0&&
				registered_username.equals(username)){
			
			reg_view.setText("registered");
		} else {
			reg_view.setText("not registered");
		}
	}
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ActivityCallback) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SettingsFragment.ActivityCallback");
        }
        username = mCallback.getUsername();
        registered_username = mCallback.getRegisteredName();
        password = mCallback.getPassword();
        GCM_ID = mCallback.getGCMID();
    }
	public interface ActivityCallback {
		abstract String getUsername();
		abstract String getRegisteredName();
		abstract void setUsername(String username);
		abstract void setRegisteredName(String username);
		abstract String getPassword();
		abstract void setPassword(String password);
		abstract String getGCMID();
		
	}
}
