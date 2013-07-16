package com.chewnoill.chess;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends FragmentActivity
		implements 
		GameFragment.ActivityCallback,
		SettingsFragment.ActivityCallback,
		MainFragment.ActivityCallback,
		MoveListFragment.ActivityCallback,
		LoginFragment.ActivityCallback,
		GameListFragment.ActivityCallback{
	
	private static final String TAG = "MainActivity";
	private static final int REG_REGISTERING = 0x1;
	private static final int REG_REGISTERED = 0x2;
	private static final int REG_UNSUPORTED = 0x0;
	
	protected static String GCM_ID = "";
	private static String username;
	private static String password;
	private static String registered_username;
	private static int SDK = android.os.Build.VERSION.SDK_INT;
	private SharedPreferences spref;
	
	private String game_id = "";
	private ProgressDialog mDialog_loading;
	private Fragment fragment;
	
	private MainFragment mainFragment;
	private static JSONObject mGameList=null;
	

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	static ViewPager mViewPager;
	public static GameView mGameView;
	public static View mMoveListView;
	private static View mGameListView;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG,"onCreate");
		if(savedInstanceState==null){
			Log.d(TAG,"new activity");
			init(savedInstanceState);
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			String game_id = "";
			
			if(bundle!=null){
				int SDK = android.os.Build.VERSION.SDK_INT;
				if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
					game_id = intent.getStringExtra("game_id");
				}else{
					game_id = bundle.getString("game_id", "");
				}
				if(game_id!=null &&
						game_id.length()>0){
					Log.d(TAG,"SIS: "+savedInstanceState);
		    		loadGame(game_id);
		    		return;
				}
			} 
		
		} 
	}
	@Override
	public void onBackPressed(){
		finish();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        Log.d(TAG,"Landscape");
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	Log.d(TAG,"Portrait");
	    }
	}
	private void init(Bundle savedInstanceState){
		
		int reg = registerDevice();
		switch(reg){
		case REG_REGISTERING:
			//wait for registration
		case REG_REGISTERED:
			//proceed with login
		case REG_UNSUPORTED:
			//no gcm_id available, login anyway
		}
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		username = spref.getString("username", "");
    	password = spref.getString("password","");
    	GCM_ID = spref.getString("GCM_ID","");
    	registered_username = spref.getString("registered_username","");
    	
    	
		if(savedInstanceState==null){
			registerDevice();
			if(username.equals("")||
					!username.equals(registered_username)){
				loadLoginActivity();
			}else{
				loadMainFragment();
			}
		}else{
			
		}
	}
	private void loadLoginActivity(){
		Intent intent = new Intent(this, LoginActivity.class);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(intent);
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
	public void loadMainFragment(){
		Log.d(TAG,"Loading MainFragment...");
		if(mainFragment == null){
			Log.d(TAG,"new MainFragment...");
			mainFragment = new MainFragment();
		}
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main, mainFragment);
		fragmentTransaction.addToBackStack( "main_fragment" ).commit();
	}
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
	@SuppressLint("NewApi")
	@Override
	protected void onNewIntent(Intent intent){
		Log.d(TAG,"onNewIntent");
		Bundle bundle = intent.getExtras();
		String game_id = "";
		if(bundle!=null){
			int SDK = android.os.Build.VERSION.SDK_INT;
			if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
				game_id = intent.getStringExtra("game_id");
			}else{
				game_id = bundle.getString("game_id", null);
			}
			
			if(game_id != null && game_id.length()>0){
	    		loadGame(game_id);
	    		return;
			}
			String registration_id;
			if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
				registration_id = intent.getStringExtra("registration_id");
			}else{
				registration_id = bundle.getString("registration_id", null);
			}
			if(registration_id!=null){
				dismissLoading();
				GCM_ID = registration_id;
				login();
			}
			
		}
	}
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
	public void setMainFragment(Fragment fragment){
		this.fragment = fragment;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main, fragment);
		fragmentTransaction.commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
	@Override
	public String getGCMID() {
		return GCM_ID;
	}
	@Override
	public int getOrientation() {
		return this.getResources().getConfiguration().orientation;	
	}
	@Override
	public GameView getGameView() {
		return mGameView;
	}
	@Override
	public void setGameView(GameView mGameView) {
		this.mGameView = mGameView;
	}
	@Override
	public View getMoveListView() {
		return mGameListView;
	}
	@Override
	public void setMoveListView(View mGameListView) {
		this.mGameListView = mGameListView;
	}

	@Override
	public int getSDK(){
		return SDK;
	}


	@Override
	public void setUsername(String username) {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = spref.edit();
        editor.putString("username",username);
    	editor.commit();
		
	}


	@Override
	public void setPassword(String password) {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = spref.edit();
    	editor.putString("password",password);
    	editor.commit();
		
	}
	@Override
	public void loadGame(final GameFragment gameFragment,final String game_id){

		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","get_game_state");
		hash_data.put("username",username);
		hash_data.put("password",password);
		hash_data.put("game_id",game_id);
		JSONObject data = new JSONObject(hash_data);
		BackendInterface post = new BackendInterface(new BackendCallback(null){
			@Override
			public void onPostExecute(JSONObject value) {
				if(value.optBoolean("get_game_state",false)){
					gameFragment.loadGame(value.optJSONObject("game"),game_id);
				}
			}
		});
		post.execute(data);
	}
	public void SendMoveToServer(String move,
			final String game_id,
			final GameFragment gameFragment){
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","submit_move");
		hash_data.put("username",username);
		hash_data.put("password",password);
		hash_data.put("game_id",game_id);
		hash_data.put("move",move);
		
		JSONObject data = new JSONObject(hash_data);
		Log.d(TAG+":SendMoveToServer1","Sending message: "+data.toString());
		BackendInterface post = new BackendInterface(new BackendCallback(null){
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,value.toString());
				try {
					if(value.has("submit_move")){
						if(value.getBoolean("submit_move")){
							Toast toast = Toast.makeText(MainActivity.this, 
									"move accepted",Toast.LENGTH_SHORT);
							toast.show();
						}else{
							Toast toast = Toast.makeText(MainActivity.this, 
									value.getString("error"),Toast.LENGTH_SHORT);
							toast.show();
							loadGame(gameFragment,game_id);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		post.execute(data);
	}
	public void SendMoveToServer(String move,
			final String game_id){
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","submit_move");
		hash_data.put("username",username);
		hash_data.put("password",password);
		hash_data.put("game_id",game_id);
		hash_data.put("move",move);
		
		JSONObject data = new JSONObject(hash_data);
		Log.d(TAG+":SendMoveToServer2","Sending message: "+data.toString());
		BackendInterface post = new BackendInterface(new BackendCallback(null){
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,value.toString());
				try {
					if(value.has("submit_move")){
						if(value.getBoolean("submit_move")){
							Toast toast = Toast.makeText(MainActivity.this, 
									"move accepted",Toast.LENGTH_SHORT);
							toast.show();
						}else{
							Toast toast = Toast.makeText(MainActivity.this, 
									value.getString("error"),Toast.LENGTH_SHORT);
							toast.show();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		post.execute(data);
	}

	@Override
	public String getRegisteredName() {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		return spref.getString("registered_username", "");
	}


	@Override
	public void setRegisteredName(String username) {
		Editor editor = spref.edit();
    	editor.putString("registered_username",username);
    	editor.commit();
		
	}


	@Override
	public void requestMyGamesList(final GameListFragment caller) {
		Log.d(TAG,"requestMyGamesList");
		HashMap<String,String> hash_data = new HashMap<String,String>();
		hash_data.put("type","list_my_games");
		hash_data.put("username",username);
		hash_data.put("password",password);
		
		JSONObject data = new JSONObject(hash_data);
		Log.d(TAG,"Sending message: "+data.toString());
		BackendInterface my_games_post= new BackendInterface(new BackendCallback(){
			@Override
			public void onPostExecute(JSONObject value) {
				dismissLoading();
				mGameList = value;
				caller.loadMyGamesList(value);
			}
		});
		my_games_post.execute(data);
		showLoading();
	}
	@Override
	public void getGameList(GameListFragment caller){
		Log.d(TAG,"getGameList");
		if(mGameList==null){
			requestMyGamesList(caller);
		}else{
			caller.loadMyGamesList(mGameList);
		}
	}
	@Override
	public void loadGame(String game) {
		
		Intent intent = new Intent(this, GameActivity.class);
		intent.putExtra("game_id", game);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    	startActivity(intent);
	}

	@Override
	public void logout() {
		setUsername("");
		setPassword("");
		loadLoginActivity();
		
	}

}
