package com.chewnoill.chess;

import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class GameFragment extends Fragment {
	private static String TAG = "GameFragment";
	private static int ORIENTATION = Configuration.ORIENTATION_PORTRAIT;
	ActivityCallback mCallback;
	private GameView mGameView;
	private View mMoveListView;
	private String username;
	private String password;
	private String game_id;
	private View mView;
	private ClockTask clTask;
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //setRetainInstance(true);
	    Log.d(TAG,"onCreate");
	}
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		//View mView;
		if(ORIENTATION == Configuration.ORIENTATION_PORTRAIT){
			mView = inflater.inflate(R.layout.game_portrait, null);

		} else {
			mView = inflater.inflate(R.layout.game_landscape, null);
		}
		
		
		TableLayout gameLayout = (TableLayout) mView.findViewById(R.id.game_view);
		
		if(mMoveListView == null){
			mMoveListView =  inflater.inflate(R.layout.game_move_list, null);
			mCallback.setMoveListView(mMoveListView);
		}
		if(mGameView == null){
			mGameView = new GameView(inflater.getContext(),this);
			mCallback.setGameView(mGameView);
		}
		ViewGroup parent = ((ViewGroup) mGameView.getParent());
		if(parent != null){
			parent.removeView(mGameView);
		}
		gameLayout.addView(mGameView);
		return mView;
		
    }
	
	public View getMoveListView(){
		return mCallback.getMoveListView();
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
                    + " must implement GameFragment.ActivityCallback");
        }
        ORIENTATION = mCallback.getOrientation();
        //mGameView = mCallback.getGameView();
        //mMoveListView = mCallback.getMoveListView();
    }
	public void loadGame(JSONObject game,String game_id){
		//TODO start up timer for clock view
		long move_start = 0;
		try {
			
			String game_start = game.getString("date_created");
			String last_move_time;
			JSONArray moves = game.getJSONArray("moveList");
			if(moves.length()>0){
				JSONObject last_move = moves.getJSONObject(moves.length()-1);
				if(last_move.has("black")){
					last_move_time = last_move.getJSONObject("black").
							getJSONObject("info").getString("time"); 
				} else if (last_move.has("white")){
					last_move_time = last_move.getJSONObject("white").
							getJSONObject("info").getString("time");
					
				} else if(moves.length()>1){
					last_move = moves.getJSONObject(moves.length()-2);
					last_move_time = last_move.getJSONObject("black").
							getJSONObject("info").getString("time");
					
				} else {
					last_move_time = game_start;
				}
				move_start = Long.parseLong(last_move_time);
			} 
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TextView clock = (TextView)mView.findViewById(R.id.clock);
		
		clTask = new ClockTask(clock,move_start);
		clTask.execute();
		mGameView.loadGame(game,game_id);
	}
	public void stopClock(){
		if(clTask!=null){
			clTask.cancel(true);
			clTask = null;
		}
	}
	@Override
    public void onDetach() {
		super.onDetach();
		if(clTask!=null){
			clTask.cancel(true);
			clTask = null;
		}
	}
	public void onBackPressed(){
		if(clTask!=null){
			clTask.cancel(true);
			clTask = null;
		}
		
		
	}
	public String getUsername(){
		return mCallback.getUsername();
	}
	public void SendMoveToServer(String move,String game_id){
		clTask.cancel(true);
		mCallback.SendMoveToServer(move,game_id,this);
	}
	public interface ActivityCallback {
		abstract int getOrientation();
		abstract View getMoveListView();
		abstract void setMoveListView(View view);
		abstract GameView getGameView();
		abstract void setGameView(GameView view);
		abstract void loadGame(GameFragment gameFragment, String game_id);
		
		abstract void SendMoveToServer(String move,String game_id,GameFragment gameFragment);
		//
		abstract String getUsername();
		
	}
	class ClockTask extends AsyncTask<Object, Object, Object> {
		private TextView clock_view;
		private long start_time;
		
		public ClockTask(TextView clock_view,long start_time){
			this.clock_view = clock_view;
			this.start_time = start_time;
		}
        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            long t = new Date().getTime();
            String text = elapesedTime(start_time,t);
            clock_view.setText(text);

        }
        private String elapesedTime(long t1, long t2){
        	//Log.d(TAG,"t1: "+t1);
        	//Log.d(TAG,"t2: "+t2);
        	long elapsed = t2-t1;
        	int seconds = (int) (elapsed/1000);
        	int minutes = seconds/60; 
        	seconds = seconds%60;
        	int hours = minutes/60;
        	minutes = minutes%60;
        	if(elapsed<0){
        		return "00:00:00";
        	}
        	String ret = "";
        	if(hours<10){
        		ret = "0"+hours+":";
        	} else {
        		ret = hours+":";
        	}
        	if(minutes<10){
        		ret += "0"+minutes + ":";
        	} else{
        		ret += minutes + ":";
        	} 
        	if(seconds<10){
        		ret += "0"+seconds;
        	} else{
        		ret += seconds;
        	} 
        	
			return ret;
        	
        }

        @Override
        protected Object doInBackground(Object... params) {
        	try {
	            while(true) {
	                //sleep for 1s in background...
	                Thread.sleep(1000);
	                //and update textview in ui thread
	                publishProgress();
	            }
        	} catch (InterruptedException e) {
                //stopped, do nothing
            }
            return null;
        }
    }
}
