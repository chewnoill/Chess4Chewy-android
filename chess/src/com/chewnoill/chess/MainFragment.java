package com.chewnoill.chess;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chewnoill.chess.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;


public class MainFragment extends Fragment {
	private static final String TAG = "MainFragment";
	private static int SDK=11; 
	private String password;
	private String username;
	private ActivityCallback mCallback;
	private LayoutInflater inflater;
	private View view;
	

	private GameListFragment mGameListFragment;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	    Log.d(TAG,"onCreate");
	}
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		//if(savedInstanceState==null){
			
			this.inflater = inflater;
			view = inflater.inflate(R.layout.main_view, null);
			mGameListFragment = new GameListFragment();
			loadGameListFragment();
			
			//--------------------------------------------------------------------------------
			//register new game button
			Button new_game_button = (Button) view.findViewById(R.id.new_game_button);
			new_game_button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					inflate_new_game_view(inflater);
				}});
			//--------------------------------------------------------------------------------
			//refresh  button
			Button refresh_button = (Button) view.findViewById(R.id.refresh_button);
			refresh_button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mGameListFragment.refresh();
				}
			});
			//--------------------------------------------------------------------------------
			//logout  button
			Button logout_button = (Button) view.findViewById(R.id.logout_button);
			logout_button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					mCallback.logout();
				}
			});	
		//}
		
		return view;
	}
	private View inflate_new_game_view(final LayoutInflater inflater){
		final View ret = inflater.inflate(R.layout.new_game, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(inflater.getContext());
        builder.setView(ret);
        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {


			public void onClick(DialogInterface dialog, int which) {
        		String choose_color = ((Spinner) ret.findViewById(R.id.color_spinner)).getSelectedItem().toString();
        		String privacy = ((Spinner) ret.findViewById(R.id.privacy_spinner)).getSelectedItem().toString();
        		String invited_user = ((EditText) ret.findViewById(R.id.invited_user)).getText().toString();
        		username = mCallback.getUsername();
        	    password = mCallback.getPassword();
        	    
        		
        		HashMap<String,String> hash_data = new HashMap<String,String>();
				hash_data.put("type","register_game");
				hash_data.put("username",username);
				hash_data.put("password",password);
				hash_data.put("choose_color",choose_color);
				hash_data.put("invite_user",invited_user);
				
				JSONObject data = new JSONObject(hash_data);
				Log.d(TAG,"Sending message: "+data.toString());
				BackendInterface post = new BackendInterface(new BackendCallback(null){

					@Override
					public void onPostExecute(JSONObject value) {
						Log.d(TAG,""+value.toString());
						/* TODO:
						 * tell the user about what happened
						 */
					}
					
				});
				post.execute(data);
				dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		dialog.cancel();
            }
        }).show();
		return ret;
	}
	
	private void loadGameListFragment(){
		Log.d(TAG+":loadGameListFragment","loading game list frag");
		FragmentManager fm = mCallback.getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.game_list, mGameListFragment,"game_list");
		fragmentTransaction.commit();
		//fragmentTransaction.addToBackStack( "game_list" ).commit();
	}
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG,"onAttach");
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ActivityCallback) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainFragment.ActivityCallback");
        }
        SDK = mCallback.getSDK();
        username = mCallback.getUsername();
        password = mCallback.getPassword();
    }
	public interface ActivityCallback {
		abstract int getSDK();
		abstract void loadGame(String game);
		abstract String getUsername();
		abstract String getPassword();
		abstract void logout();
		abstract FragmentManager getSupportFragmentManager();
		
	}
}
