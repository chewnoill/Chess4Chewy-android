package com.chewnoill.chess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameListFragment extends Fragment {
	private static final String TAG = "GameListFragment";
	private View view;
	private LayoutInflater inflater;
	private ActivityCallback mCallback;
	private int SDK;
	private String username;
	private boolean loaded_extra = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	    Log.d(TAG,"onCreate");
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		if(savedInstanceState==null){
			Log.d(TAG,"onCreateView");
			this.inflater = inflater;
			view = inflater.inflate(R.layout.game_list, null);
			reload();
			return view;
		}
		return view;
	}
	
	
	public void reload(){
		mCallback.getGameList(this);
	}
	public void refresh(){
		mCallback.requestMyGamesList(this);
	}
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void loadMyGamesList(JSONObject value){
		Log.d(TAG,"loadMyGamesList: "+value.toString());
		//--------------------------------------------------------------------------------
		//my games
		final LinearLayout my_games_list = (LinearLayout) view.findViewById(R.id.game_list);
		my_games_list.removeAllViews();
		try {
			if(value.has("list_my_games")){
				if(value.getBoolean("list_my_games")){
					JSONArray game_list = value.getJSONArray("game_list");
					for (int x = 0; x<game_list.length(); x++){
						JSONObject jo = (JSONObject) game_list.get(x);
						String move_num = jo.getInt("move_num")+"";
						String black_player = jo.getString("black_player");
						String white_player = jo.getString("white_player");
						
						JSONObject status = jo.getJSONObject("status");
						String game_id = jo.getString("game_id");
						String game_id_human = jo.getString("game_id_human");
						String draw_offered = jo.getString("draw_offered");
						Log.d(TAG,"is a draw? "+draw_offered);
						View game_item = inflater.inflate(R.layout.game_list_item, null);
						((TextView)game_item.findViewById(R.id.game_id_human)).setText(game_id_human);
						((TextView)game_item.findViewById(R.id.game_id)).setText(game_id);
						((TextView)game_item.findViewById(R.id.move_num)).setText(move_num);
						((TextView)game_item.findViewById(R.id.white_player)).setText(white_player);
						((TextView)game_item.findViewById(R.id.black_player)).setText(black_player);
						
						String whose_turn = jo.getString("whose_turn");
						String set_status = "";
						String game_status = status.getString("game");
						
						PaintDrawable mDrawable_background = new PaintDrawable();
						
				        
						if(game_status.equals("done")){
							set_status = "done";
							mDrawable_background.getPaint().setColor(Shared.Colors.GAME_OVER);
							String winner = status.getString("winner");
							
			
							View winner_view;
							String result = status.getString("result");
							if(result.equals("check mate")||result.equals("resign")){
								set_status = result;
								PaintDrawable winner_background = new PaintDrawable();
								winner_background.getPaint().setColor(Shared.Colors.MY_TURN);
								if(winner.equals("white")){
									winner_view = game_item.findViewById(R.id.white_player);
								}else{
									winner_view = game_item.findViewById(R.id.black_player);
								}
								if(SDK < android.os.Build.VERSION_CODES.JELLY_BEAN) {
									winner_view.setBackgroundDrawable(winner_background);
						    	} else {
						    		winner_view.setBackground(winner_background);
						    	}
								
							}
							//win:
							//  highlight winner/loser
							//draw: 
							//  
							//either way game shouldn't appear in my games list for very long
						} else if((whose_turn.equals("white")&&white_player.equals(username))||
								(whose_turn.equals("black")&&black_player.equals(username))){
							set_status = "your move";
							mDrawable_background.getPaint().setColor(Shared.Colors.MY_TURN);
						} else {
							set_status = "opponent's move";
							mDrawable_background.getPaint().setColor(Shared.Colors.OPP_TURN);
						}
						
						if(SDK < android.os.Build.VERSION_CODES.JELLY_BEAN) {
							game_item.setBackgroundDrawable(mDrawable_background);
				    	} else {
				    		game_item.setBackground(mDrawable_background);
				    	}
						
						
						((TextView)game_item.findViewById(R.id.status)).setText(set_status);
						addGameItemClickListener(game_item,game_id);
						my_games_list.addView(game_item);
						Log.d(TAG,"onPostExecute: game_id: "+jo.getString("game_id"));
					}
					view.invalidate();
				}else{
					//game not registered, some error
					Log.d(TAG,"onPostExecute: error "+value.getString("error"));
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void addGameItemClickListener(View view, final String game_id){
		
		view.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.d(TAG+":onClick","click");
				mCallback.loadGame(game_id);
				
			}
		});
		view.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				//TODO 
				//create game dialog
				//resign, offer/accept draw
				Log.d(TAG+":onLongClick","click "+game_id);
				if(loaded_extra){
					loaded_extra=false;
					reload();
				} else {
					loaded_extra = true;
				}
				View extra_view = inflater.inflate(R.layout.game_list_item_extra, null);
				
				Button resign = (Button) extra_view.findViewById(R.id.resign);
				resign.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						String move = "resign";
						Log.d(TAG+":"+move,"click "+game_id);
						mCallback.SendMoveToServer(move,game_id);
					}
					
				});
				Button draw = (Button) extra_view.findViewById(R.id.draw);
				draw.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View arg0) {
						String move = "draw";
						Log.d(TAG+":"+move,"click "+game_id);
						mCallback.SendMoveToServer(move,game_id);
					}
				});
				ViewGroup vg = (ViewGroup) v.findViewById(R.id.extra);
				vg.addView(extra_view);
				return true;
			}
			
		});
	}
	public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG,"onAttach");
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ActivityCallback) activity;
            
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GameListFragment.ActivityCallback");
        }
        SDK = mCallback.getSDK();
        username = mCallback.getUsername();
       
	}
	public interface ActivityCallback {
		abstract int getSDK();
		abstract void loadGame(String game);
		abstract String getUsername();
		abstract String getPassword();
		abstract void requestMyGamesList(GameListFragment caller);
		abstract void SendMoveToServer(String move,String game_id);
		abstract void getGameList(GameListFragment caller);
		abstract void setMainFragment(Fragment fragment);
		
	}
}
