package com.chewnoill.chess;

import com.chewnoill.chess.GameFragment.ActivityCallback;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class MoveListFragment extends Fragment {
	private static String TAG = "MoveListFragment";
	private ActivityCallback mCallback;
	private View mMoveListView;
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState) {
		Log.d(TAG,"onCreateView");
		if(mMoveListView == null){
			mMoveListView =  inflater.inflate(R.layout.game_move_list, null);
			mCallback.setMoveListView(mMoveListView);
		}
		ViewGroup parent = (ViewGroup) mMoveListView.getParent();
		if(parent != null){
			parent.removeView(mMoveListView);
		}
		return mMoveListView;
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
                    + " must implement MoveListFragment.ActivityCallback");
        }
        mMoveListView = mCallback.getMoveListView();
    }
	
	public interface ActivityCallback {
		abstract View getMoveListView();
		abstract void setMoveListView(View view);
	}
}
