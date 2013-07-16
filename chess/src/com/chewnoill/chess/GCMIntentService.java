package com.chewnoill.chess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	private static final String TAG = "GCMIntentService";
	 
	
	@Override
	protected void onError(Context arg0, String arg1) {
		/*
		 * error recieved from GCM while trying to 
		 * register or unregister
		 */
		Log.d(TAG,"onError: "+arg1);
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		/*
		 * message received from the server 
		 * payload will be as arguments in intent
		 */
		Bundle bundle = arg1.getExtras();
		Log.d(TAG,"onMessage: ");
		for(String key : bundle.keySet()){
			Log.d(TAG,"\t"+key+": "+bundle.getString(key));
		}
		if(bundle.getString("game_id")!=null){
			Intent intent = new Intent(this, GameActivity.class);
	    	intent.putExtra("game_id", bundle.getString("game_id"));
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    	//intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
	    	//intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	    	//intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
	    	startActivity(intent);
		}
	}

	@Override
	protected void onRegistered(Context arg0, String regId) {
		/* regId should be sent to the server so 
		 * I can send messages to the device
		 */
		// TODO Auto-generated method stub
		Log.d(TAG,"onRegistered: "+regId);
		handleRegistration(regId);
	}

	@Override
	protected void onUnregistered(Context arg0, String regId) {
		/*
		 * regId needs to be sent to the server to unregister 
		 * the device
		 */
		// TODO Auto-generated method stub
		Log.d(TAG,"onUnregistered: "+regId);
		
	}

	private void handleRegistration(String registration_id) {
	    
	    Intent new_intent = new Intent(this, MainActivity.class);
	    new_intent.putExtra("registration_id", registration_id);
	    new_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    new_intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(new_intent);
	}

}
