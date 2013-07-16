package com.chewnoill.chess;
import org.json.JSONObject;

import android.view.LayoutInflater;
import android.widget.LinearLayout;

public abstract class BackendCallback {
	private LayoutInflater inflater;
	private LinearLayout list_view;
	public BackendCallback() {
	}
	public BackendCallback(LayoutInflater inflater){
		this.inflater = inflater;
	}
	public BackendCallback(LayoutInflater inflater, LinearLayout list_view) {
		this(inflater);
		this.list_view = list_view;
	}
	public abstract void onPostExecute(JSONObject value);
}
