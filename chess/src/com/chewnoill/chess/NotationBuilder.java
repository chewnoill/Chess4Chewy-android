package com.chewnoill.chess;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotationBuilder {
	private HashMap<String,String> piece_map;
	public NotationBuilder(){
		piece_map = new HashMap<String,String>();
		piece_map.put("king","K");
		piece_map.put("bishop","B");
		piece_map.put("rook","R");
		piece_map.put("knight","N");
		piece_map.put("queen","Q");
		piece_map.put("pawn","");
	}

	public String builtMoveNote(JSONObject move) throws JSONException{
		
		JSONObject info = move.getJSONObject("info");
		String ret="";
		String color = info.getString("color");
		//TODO check for other pieces on this row or column
		String piece = info.getString("piece");
		String piece_note = piece_map.get(piece);
		JSONArray src = info.getJSONArray("src");
		
		JSONArray dest = info.getJSONArray("dest");
		
		String takes = info.getBoolean("takes")?"x":"-";
		String check = info.getBoolean("check")?"+":"";
		String mate = info.getBoolean("mate")?" mate":"";
		ret = piece_note + src.getString(0) + src.getString(1) + 
				takes + 
				dest.getString(0) + dest.getString(1)+
				check+mate;
		return ret;
	}
}
