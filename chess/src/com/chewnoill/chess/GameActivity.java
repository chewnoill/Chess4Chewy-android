package com.chewnoill.chess;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class GameActivity extends FragmentActivity implements GameFragment.ActivityCallback{
	private final static String TAG = "GameActivity";
	private SharedPreferences spref;
	private String username;
	private String password;
	private String registered_username;
	private String GCM_ID;
	private View mGameListView;
	private GameView mGameView;
	private String game_id;
	private static GameFragment mGameFragment;
	@SuppressLint("NewApi")
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
    	
    	Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
    	if(bundle!=null){
			int SDK = android.os.Build.VERSION.SDK_INT;
			if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
				game_id = intent.getStringExtra("game_id");
			}else{
				game_id = bundle.getString("game_id", "");
			}
			
		}
    	
		Log.d(TAG,"new mGameFragment");
		mGameFragment = new GameFragment();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main, mGameFragment);
		fragmentTransaction.commit();
	
    	
		
		Log.d(TAG,"game_id: "+game_id);
		if(game_id!=null &&
				game_id.length()>0){
    		loadGame(mGameFragment, game_id);
		}
	}
	@SuppressLint("NewApi")
	@Override
	protected void onNewIntent(Intent intent){
		Log.d(TAG,"onNewIntent");
		Bundle bundle = intent.getExtras();
    	if(bundle!=null){
			int SDK = android.os.Build.VERSION.SDK_INT;
			if(SDK < android.os.Build.VERSION_CODES.HONEYCOMB) {
				game_id = intent.getStringExtra("game_id");
			}else{
				game_id = bundle.getString("game_id", "");
			}
			
		}

		if(game_id!=null &&
				game_id.length()>0){
    		loadGame(mGameFragment, game_id);
		}
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
	public void loadGame(final GameFragment gameFragment,final String game_id){
		gameFragment.stopClock();
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
	@Override
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
		Log.d(TAG,"Sending message: "+data.toString());
		BackendInterface post = new BackendInterface(new BackendCallback(null){
			@Override
			public void onPostExecute(JSONObject value) {
				Log.d(TAG,value.toString());
				try {
					if(value.has("submit_move")){
						if(value.getBoolean("submit_move")){
							Toast toast = Toast.makeText(GameActivity.this, 
									"move accepted",Toast.LENGTH_SHORT);
							toast.show();
						}else{
							Toast toast = Toast.makeText(GameActivity.this, 
									value.getString("error"),Toast.LENGTH_SHORT);
							toast.show();
							
						}
						loadGame(gameFragment,game_id);
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
	public String getUsername() {
		spref = PreferenceManager.getDefaultSharedPreferences(this);
		return spref.getString("username", "");
	}
}
