package com.chewnoill.chess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/*
 * GameView
 * 	TODO:
 * 		required
 * 			game over screen
 * 			castle moves
 * 			passant moves
 * 		optional
 * 			implement game clock
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {
	public class Piece {
		public int color;
		public int piece;
		public int row;
		public char column;
		public boolean can_passant;
		public boolean can_castle;
		Piece(
				int color,
				int piece,
				int row,
				char column,
				boolean can_passant,
				boolean can_castle){
			this.color=color;
			this.piece=piece;
			this.row=row;
			this.column=column;
			this.can_passant=can_passant;
			this.can_castle=can_castle;
		}
	}
	public class Cell {
		private int color;
		private int piece;
		private Rect rect;
		private int piece_color;
		public int row;
		public int col;
		public boolean can_passant;
		public boolean can_castle;
		
		Cell(
				int color,
				int piece,
				int piece_color,
				Rect rect){

			this.color=color;
			this.piece=piece;
			this.rect=rect;
			this.piece_color = piece_color;
			
		}
		public void render(Canvas canvas){
			canvas.drawBitmap(bitmaps.get(color),null,rect,null);
			if(piece != NO_PIECE){
				
				canvas.drawBitmap(bitmaps.get(piece_color|piece),null,rect,null);
			}
		}
		
		public void setPiece(int piece,int piece_color){
			this.piece = piece;
			this.piece_color = piece_color;
		}
		public void setPiece(int piece, int color, boolean can_passant,
				boolean can_castle) {
			this.setPiece(piece,color);
			this.can_passant=can_passant;
			this.can_castle=can_castle;
		}
		public Rect getRect(){
			return rect;
		}
		public int getPiece(){
			return piece;
		}
		public int getPieceColor(){
			return piece_color;
		}
		public void setPos(int row, int col) {
			this.row=row;
			this.col=col;
		}
		
	}

	private static final String TAG = "GameView";
	//black stuff
	private static final int BLACK_SQUARE 	= 0x100;
	private static final int BLACK_BISHOP 	= 0x101;
	private static final int BLACK_KING 	= 0x102;
	private static final int BLACK_KNIGHT 	= 0x103;
	private static final int BLACK_PAWN 	= 0x104;
	private static final int BLACK_QUEEN 	= 0x105;
	private static final int BLACK_ROOK 	= 0x106;
	//white stuff
	private static final int WHITE_SQUARE	= 0x200;
	private static final int WHITE_BISHOP 	= 0x201;
	private static final int WHITE_KING 	= 0x202;
	private static final int WHITE_KNIGHT 	= 0x203;
	private static final int WHITE_PAWN 	= 0x204;
	private static final int WHITE_QUEEN 	= 0x205;
	private static final int WHITE_ROOK 	= 0x206;
	//general stuff
	private static final int BISHOP 		= 0x001;
	private static final int KING 			= 0x002;
	private static final int KNIGHT 		= 0x003;
	private static final int PAWN 			= 0x004;
	private static final int QUEEN 			= 0x005;
	private static final int ROOK 			= 0x006;
	//null piece
	private static final int NO_PIECE 		= 0xFFF;
	private static final int PIECE_MASK		= 0x00F;
	private static final int COLOR_MASK		= 0xF00;
	//labels
	private static final int LABEL_NONE_ROW	= 0x300;
	private static final int LABEL_1 		= 0x301;
	private static final int LABEL_2 		= 0x302;
	private static final int LABEL_3 		= 0x303;
	private static final int LABEL_4 		= 0x304;
	private static final int LABEL_5 		= 0x305;
	private static final int LABEL_6 		= 0x306;
	private static final int LABEL_7 		= 0x307;
	private static final int LABEL_8 		= 0x308;
	private static final int LABEL_NONE_COL	= 0x310;
	private static final int LABEL_A 		= 0x311;
	private static final int LABEL_B 		= 0x312;
	private static final int LABEL_C 		= 0x313;
	private static final int LABEL_D 		= 0x314;
	private static final int LABEL_E 		= 0x315;
	private static final int LABEL_F 		= 0x316;
	private static final int LABEL_G 		= 0x317;
	private static final int LABEL_H 		= 0x318;
	
	private static int[] ROWS = {1,2,3,4,5,6,7,8};
	private static int[] ROWS_REV = {8,7,6,5,4,3,2,1};
	private static char[] COLUMNS = {'a','b','c','d','e','f','g','h'};
	private static char[] COLUMNS_REV = {'h','g','f','e','d','c','b','a'};
	
	private HashMap<Integer, HashMap<Character, Cell>> board;
	private HashMap<Integer, Rect> labels;
	private HashMap<String, Integer> label_map;
	private SparseArray<Bitmap> bitmaps;
	private Rect clipBounds_board;
	
	private int cell_size;
	
	//paints
	private Paint selected_paint;
	private Paint txt_paint;
	
	private Cell selected;
	private int player_color = WHITE_SQUARE;
	private ArrayList<Cell> next_moves;
	private HashMap<String, Integer> piece_map;
	private HashMap<Integer, HashMap<Character, Piece>> pieces;
	private String username;
	private String password;
	private String game_id;
	private int[] rows;
	private char[] columns;
	private View move_list_view;
	private NotationBuilder note_builder;
	private GameFragment mGameFragment;
	
	public GameView(Context context,GameFragment gameFragment){
		this(context);
		this.mGameFragment = gameFragment;
		this.move_list_view = gameFragment.getMoveListView();
		this.username = gameFragment.getUsername();
	}
	public GameView(Context context,View move_list_view) {
		this(context);
		this.move_list_view = move_list_view;
	}
	public GameView(Context context) {
		super(context);
		
		Log.d(TAG,"New GameView");
		getHolder().addCallback(this);

		txt_paint = new Paint(); 
		txt_paint.setColor(Color.WHITE); 
		txt_paint.setStyle(Style.FILL); 
		
		selected_paint = new Paint();
		selected_paint.setColor(Color.RED);
		selected_paint.setStyle(Style.STROKE); 
		selected_paint.setStrokeWidth(2);
		
		label_map = new HashMap<String,Integer>();
		label_map.put("a",LABEL_A);
		label_map.put("b",LABEL_B);
		label_map.put("c",LABEL_C);
		label_map.put("d",LABEL_D);
		label_map.put("e",LABEL_E);
		label_map.put("f",LABEL_F);
		label_map.put("g",LABEL_G);
		label_map.put("h",LABEL_H);
		
		label_map.put("1",LABEL_1);
		label_map.put("2",LABEL_2);
		label_map.put("3",LABEL_3);
		label_map.put("4",LABEL_4);
		label_map.put("5",LABEL_5);
		label_map.put("6",LABEL_6);
		label_map.put("7",LABEL_7);
		label_map.put("8",LABEL_8);
		
		
		//store only one copy of each bitmap in memory
		bitmaps = new SparseArray<Bitmap>();
		bitmaps.put(BLACK_SQUARE,BitmapFactory.decodeResource(getResources(), R.drawable.black_square));
		bitmaps.put(BLACK_BISHOP,BitmapFactory.decodeResource(getResources(), R.drawable.black_bishop));
		bitmaps.put(BLACK_KING,BitmapFactory.decodeResource(getResources(), R.drawable.black_king));
		bitmaps.put(BLACK_KNIGHT,BitmapFactory.decodeResource(getResources(), R.drawable.black_knight));
		bitmaps.put(BLACK_PAWN,BitmapFactory.decodeResource(getResources(), R.drawable.black_pawn));
		bitmaps.put(BLACK_QUEEN,BitmapFactory.decodeResource(getResources(), R.drawable.black_queen));
		bitmaps.put(BLACK_ROOK,BitmapFactory.decodeResource(getResources(), R.drawable.black_rook));
		
		bitmaps.put(WHITE_SQUARE,BitmapFactory.decodeResource(getResources(), R.drawable.white_square));
		bitmaps.put(WHITE_BISHOP,BitmapFactory.decodeResource(getResources(), R.drawable.white_bishop));
		bitmaps.put(WHITE_KING,BitmapFactory.decodeResource(getResources(), R.drawable.white_king));
		bitmaps.put(WHITE_KNIGHT,BitmapFactory.decodeResource(getResources(), R.drawable.white_knight));
		bitmaps.put(WHITE_PAWN,BitmapFactory.decodeResource(getResources(), R.drawable.white_pawn));
		bitmaps.put(WHITE_QUEEN,BitmapFactory.decodeResource(getResources(), R.drawable.white_queen));
		bitmaps.put(WHITE_ROOK,BitmapFactory.decodeResource(getResources(), R.drawable.white_rook));
		
		//labels
		bitmaps.put(LABEL_1,BitmapFactory.decodeResource(getResources(), R.drawable.label1));
		bitmaps.put(LABEL_2,BitmapFactory.decodeResource(getResources(), R.drawable.label2));
		bitmaps.put(LABEL_3,BitmapFactory.decodeResource(getResources(), R.drawable.label3));
		bitmaps.put(LABEL_4,BitmapFactory.decodeResource(getResources(), R.drawable.label4));
		bitmaps.put(LABEL_5,BitmapFactory.decodeResource(getResources(), R.drawable.label5));
		bitmaps.put(LABEL_6,BitmapFactory.decodeResource(getResources(), R.drawable.label6));
		bitmaps.put(LABEL_7,BitmapFactory.decodeResource(getResources(), R.drawable.label7));
		bitmaps.put(LABEL_8,BitmapFactory.decodeResource(getResources(), R.drawable.label8));
		
		bitmaps.put(LABEL_A,BitmapFactory.decodeResource(getResources(), R.drawable.labela));
		bitmaps.put(LABEL_B,BitmapFactory.decodeResource(getResources(), R.drawable.labelb));
		bitmaps.put(LABEL_C,BitmapFactory.decodeResource(getResources(), R.drawable.labelc));
		bitmaps.put(LABEL_D,BitmapFactory.decodeResource(getResources(), R.drawable.labeld));
		bitmaps.put(LABEL_E,BitmapFactory.decodeResource(getResources(), R.drawable.labele));
		bitmaps.put(LABEL_F,BitmapFactory.decodeResource(getResources(), R.drawable.labelf));
		bitmaps.put(LABEL_G,BitmapFactory.decodeResource(getResources(), R.drawable.labelg));
		bitmaps.put(LABEL_H,BitmapFactory.decodeResource(getResources(), R.drawable.labelh));
		
		piece_map = new HashMap<String,Integer>();
		piece_map.put("bishop",BISHOP);
		piece_map.put("king",KING);
		piece_map.put("knight",KNIGHT);
		piece_map.put("pawn",PAWN);
		piece_map.put("queen",QUEEN);
		piece_map.put("rook",ROOK);
		
		piece_map.put("white",WHITE_SQUARE);
		piece_map.put("black",BLACK_SQUARE);
		note_builder = new NotationBuilder();
		
		initPieces();
		
	}
	private void initPieces(){
		pieces = new HashMap<Integer,HashMap<Character,Piece>>();

		//build empty board
		for(int row : ROWS){
			HashMap<Character, Piece> r = new HashMap<Character,Piece>();
			pieces.put(row,r);
		}
	}
	
	public void loadGame(JSONObject game,String game_id){
		this.game_id = game_id;
		
		
		try{
			Log.d(TAG+":loadGame","move_list"+game.getString("moveList"));
			Log.d(TAG+":loadGame","date_created"+game.getInt("date_created"));
			JSONObject pieces = game.getJSONObject("gamePieces");
			JSONArray moves = game.getJSONArray("moveList");
			loadGamePieces(pieces);
			loadMoveList(moves);
			if(game.getString("white_player").equals(GameView.this.username)){
				player_color = WHITE_SQUARE;
			}else if(game.getString("black_player").equals(GameView.this.username)){
				player_color = BLACK_SQUARE;
			}
			GameView.this.initBoard(clipBounds_board);
			GameView.this.invalidate();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void loadMoveList(JSONArray moves) throws JSONException{
		LayoutInflater inflater = LayoutInflater.from(getContext());
		LinearLayout move_list_layout = (LinearLayout) move_list_view.findViewById(R.id.move_list_layout);
		move_list_layout.removeAllViews();
		for(int x=0;x<moves.length();x++){
			JSONObject move_item = moves.getJSONObject(x);
			View move_item_view = inflater.inflate(R.layout.move_item, null);
			int turn_num = move_item.getInt("turn_num")+1;
			TextView move_num_view = (TextView) move_item_view.findViewById(R.id.move_num);
			move_num_view.setText(turn_num+"");
			
			if(move_item.has("white")){
				TextView white_move_view = (TextView) move_item_view.findViewById(R.id.white_move_view);
				String white_move_note = note_builder.builtMoveNote(move_item.getJSONObject("white"));
				white_move_view.setText(white_move_note);
				
			}
			
			if(move_item.has("black")){
				TextView black_move_view = (TextView) move_item_view.findViewById(R.id.black_move_view);
				String black_move_note = note_builder.builtMoveNote(move_item.getJSONObject("black"));
				black_move_view.setText(black_move_note);
			}
			move_list_layout.addView(move_item_view);
			
		}
	}
	private String builtMoveNote(JSONObject move) throws JSONException{
		
		JSONObject info = move.getJSONObject("info");
		String ret="";
		String color = info.getString("color");
		//TODO check for other pieces on this row or column
		
		return ret;
	}
	public void loadGamePieces(JSONObject pieces) throws JSONException{
		Log.d(TAG,"loadGamePieces");
		JSONArray names = pieces.names();
		initPieces();
		for(int x=0;x<names.length();x++){

			String name = names.getString(x);
			int row = Integer.parseInt(name.charAt(1)+"");
			char col = name.charAt(0);
			JSONObject obj = pieces.getJSONObject(name);
			int color = piece_map.get(obj.getString("color"));
			int piece = piece_map.get(obj.getString("piece"));
			boolean can_passant = obj.getBoolean("can_passant");
			boolean can_castle = obj.getBoolean("can_castle");
			//Log.d(TAG+":loadGame","get piece @ "+row+", "+col+": "+obj.getString("piece")+","+obj.getString("color"));
			this.pieces.get(row).put(col,new Piece(color,piece, row, col, can_passant, can_castle));
	
			
		}
		//setUpState();
	}
	@SuppressLint("UseSparseArrays")
	private void initBoard(Rect board_space){
		Log.d(TAG,"initBoard");
		boolean playerIsWhite = player_color == WHITE_SQUARE?true:false;
		Log.d(TAG,"playerIsWhite:"+playerIsWhite);
		//reverse order if playing as black
		
		if(playerIsWhite){
			rows = ROWS;
			columns = COLUMNS;
		} else {
			rows = ROWS_REV;
			columns = COLUMNS_REV;
		}
		
		boolean isBlack = true;
		int color;
		
		board = new HashMap<Integer,HashMap<Character,Cell>>();
		labels = new HashMap<Integer,Rect>();
		int y = 7;
		int x;
		Rect rect;
		cell_size = (int) (board_space.width()/8.5);
		int label_size = cell_size/2;
		int offset_x = board_space.left+label_size;
		int offset_y = board_space.top+label_size;
		
		//build empty board
		for(int row : rows){
			HashMap<Character, Cell> r = new HashMap<Character,Cell>();
			
			x = 0;
			rect = new Rect(board_space.left,
					y*cell_size+label_size,
					x*cell_size+label_size,
					y*cell_size+cell_size+label_size);
			labels.put(label_map.get(row+""),rect);
			
			for(char col : columns){
				rect = new Rect(x*cell_size+offset_x,
						y*cell_size+offset_y,
						x*cell_size+offset_x+cell_size,
						y*cell_size+offset_y+cell_size);
				color = isBlack?BLACK_SQUARE:WHITE_SQUARE;
				r.put(col,new Cell(color,NO_PIECE,NO_PIECE,rect));
				//flip color
				isBlack = isBlack?false:true;
				
				rect = new Rect(x*cell_size+label_size,
						board_space.top,
						x*cell_size+cell_size+label_size,
						y*cell_size+label_size);
				
				labels.put(label_map.get(col+""),rect);
				x++;
			}
			isBlack = isBlack?false:true;
			board.put(row,r);
			y--;
		}
		setUpState();
	}
	public void setUpState(){
		
		for(int row : ROWS){ 
			for(char col : COLUMNS){
				Piece piece = this.pieces.get(row).get(col); 
				//Integer piece = this.pieces.get(row).get(col);
				if(piece != null){
					board.get(row).get(col).setPiece(piece.piece,piece.color,piece.can_passant,piece.can_castle);
					
				}
			}
		}
	}
	private void setUpInitialState(){
		//add pieces to starting positions
		board.get(1).get('a').setPiece(WHITE_ROOK,WHITE_SQUARE);
		board.get(1).get('b').setPiece(WHITE_KNIGHT,WHITE_SQUARE);
		board.get(1).get('c').setPiece(WHITE_BISHOP,WHITE_SQUARE);
		board.get(1).get('d').setPiece(WHITE_QUEEN,WHITE_SQUARE);
		board.get(1).get('e').setPiece(WHITE_KING,WHITE_SQUARE);
		board.get(1).get('f').setPiece(WHITE_BISHOP,WHITE_SQUARE);
		board.get(1).get('g').setPiece(WHITE_KNIGHT,WHITE_SQUARE);
		board.get(1).get('h').setPiece(WHITE_ROOK,WHITE_SQUARE);
		
		board.get(2).get('a').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('b').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('c').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('d').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('e').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('f').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('g').setPiece(WHITE_PAWN,WHITE_SQUARE);
		board.get(2).get('h').setPiece(WHITE_PAWN,WHITE_SQUARE);
		
		board.get(8).get('a').setPiece(BLACK_ROOK,BLACK_SQUARE);
		board.get(8).get('b').setPiece(BLACK_KNIGHT,BLACK_SQUARE);
		board.get(8).get('c').setPiece(BLACK_BISHOP,BLACK_SQUARE);
		board.get(8).get('d').setPiece(BLACK_QUEEN,BLACK_SQUARE);
		board.get(8).get('e').setPiece(BLACK_KING,BLACK_SQUARE);
		board.get(8).get('f').setPiece(BLACK_BISHOP,BLACK_SQUARE);
		board.get(8).get('g').setPiece(BLACK_KNIGHT,BLACK_SQUARE);
		board.get(8).get('h').setPiece(BLACK_ROOK,BLACK_SQUARE);
		
		board.get(7).get('a').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('b').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('c').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('d').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('e').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('f').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('g').setPiece(BLACK_PAWN,BLACK_SQUARE);
		board.get(7).get('h').setPiece(BLACK_PAWN,BLACK_SQUARE);
		
	}
	private void renderBoard(Canvas canvas){
		for(Integer key: labels.keySet()){
			canvas.drawBitmap(bitmaps.get(key),null,labels.get(key),null);
			
		}
		for(HashMap<Character,Cell> row: board.values()){
			for(Cell cell: row.values()){
				cell.render(canvas);
			}
			
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG,"surfaceChanged");
		int min = Math.min(width,height);
		clipBounds_board = new Rect(0,0,min,min);
		initBoard(clipBounds_board);
		
	}
	@Override
	public void onDraw(Canvas canvas){
		Log.d(TAG,"drawing");
		//canvas.drawBitmap(board, null,clipBounds_board, null);
		renderBoard(canvas);
		
		//maybe select something
		if(selected!=null){
			canvas.drawRect(
					selected.getRect(),
					selected_paint);
		}
		if(next_moves!=null){
			for(Cell c : next_moves){
				Log.d(TAG,"nxt mv");
				canvas.drawRect(
						c.getRect(),
						selected_paint);
			}
		}
	}
	
	public boolean onTouchEvent(final MotionEvent event) {
		Log.d(TAG,"time: "+event.getEventTime());
		switch(event.getActionMasked()){
		case MotionEvent.ACTION_DOWN:
			float x = event.getX();
			float y = event.getY();
			if(clipBounds_board.contains(
					Math.round(x),
					Math.round(y))){
				Cell cell = hitCell(Math.round(x),Math.round(y));
				if(cell.getPiece()!=NO_PIECE && 
						cell.getPieceColor()==player_color){
					
					selected = cell;
					next_moves = posibleNextPositions(cell);
				} else if(selected!=null&&
						next_moves!=null&&
						next_moves.contains(cell)){
					//move piece to new square
					cell.setPiece(
							selected.getPiece(),
							player_color);
					//remove piece from previous square
					selected.setPiece(
							NO_PIECE,
							NO_PIECE);
					//send move to server
					
					String move = ""+columns[selected.col]+rows[selected.row]+
							columns[cell.col]+rows[cell.row];
					mGameFragment.SendMoveToServer(move,game_id);
					selected = null;
					next_moves = null;
				} else {
					selected = null;
					next_moves = null;
				}
				invalidate();
				
			}
		}
		return true;
	}
	
	private ArrayList<Cell> posibleNextPositions(Cell cell){
		ArrayList<Cell> ret = new ArrayList<Cell>();
		
		switch(PIECE_MASK & cell.getPiece()){
		case BISHOP:
			Log.d(TAG,"bishop");
			//diagonals
			ret.addAll(bishopMoves(cell));
			break;
		case KING:
			Log.d(TAG,"king");
			//Adjacent
			//maybe castle
			ret.addAll(kingMoves(cell));
			break;
		case PAWN:
			Log.d(TAG,"pawn");
			//forward, maybe two
			//maybe attack
			ret.addAll(pawnMoves(cell));
			break;
		case KNIGHT:
			Log.d(TAG,"knight");
			//the Ls
			ret.addAll(knightMoves(cell));
			break;
		case ROOK:
			Log.d(TAG,"rook");
			//Horizontals
			//verticals 
			ret.addAll(rookMoves(cell));
			break;
		case QUEEN:
			//rook&bishop
			Log.d(TAG,"queen");
			ret.addAll(bishopMoves(cell));
			ret.addAll(rookMoves(cell));
			break;
		}
		return ret;
	}
	private ArrayList<Cell> bishopMoves(Cell cell) {
		ArrayList<Cell> ret = new ArrayList<Cell>();
		int row,col;
		int row_inc = 0;
		int col_inc = 0;
		
		for(int x = 0;x<4;x++){
			switch(x){
			case 0:
				//top right quadrant
				row_inc = 1;
				col_inc = 1;
				break;
			case 1:
				//top left quadrant
				row_inc = 1;
				col_inc = -1;
				break;
			case 2:
				//bottom left quadrant
				row_inc = -1;
				col_inc = -1;
				break;
			case 3:
				//bottom right quadrant
				row_inc = -1;
				col_inc = 1;
				break;
			}
			col = cell.col+col_inc;
			row = cell.row+row_inc;

			Cell c;
			while(
					col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPiece()==NO_PIECE){
				ret.add(c);
				row+=row_inc;
				col+=col_inc;
			}
			if(
					col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPieceColor()!=player_color){
				ret.add(c);
			}
			
		}

		return ret;
	}
	private ArrayList<Cell> pawnMoves(Cell cell) {
		ArrayList<Cell> ret = new ArrayList<Cell>();
		int col = cell.col;
		int row = cell.row;
		Cell c,d;
		//move 2
		if(row==1){
			c = board.get(rows[row+2]).get(columns[col]);
			if(c.getPiece()==NO_PIECE){
				ret.add(c);
			}
		}
		//move 1
		if(row+1<rows.length){
			c = board.get(rows[row+1]).get(columns[col]);
			if(c.getPiece()==NO_PIECE){
				ret.add(c);
			}
			//attack left
			if(col-1>=0){
				c = board.get(rows[row+1]).get(columns[col-1]);
				if(c.getPiece()!=NO_PIECE&&
						c.getPieceColor()!=player_color){
					ret.add(c);
				}
			}
			//attack right
			if(col+1<columns.length){
				c = board.get(rows[row+1]).get(columns[col+1]);
				if(c.getPiece()!=NO_PIECE&&
						c.getPieceColor()!=player_color){
					ret.add(c);
				}
			}
		}
		//passant left
		if(col-1>=0){
			c = board.get(rows[row]).get(columns[col-1]);
			d = board.get(rows[row+1]).get(columns[col-1]);
			if(c.getPiece()!=NO_PIECE&&
					c.getPieceColor()!=player_color&&
					c.can_passant &&
					d.getPiece()==NO_PIECE){
				ret.add(d);
			}
		}
		//passant right
		if(col+1<columns.length){
			c = board.get(rows[row]).get(columns[col+1]);
			d = board.get(rows[row+1]).get(columns[col+1]);
			if(c.getPiece()!=NO_PIECE&&
					c.getPieceColor()!=player_color&&
					c.can_passant &&
					d.getPiece()==NO_PIECE){
				ret.add(d);
			}
		}
		Log.d(TAG,"nxt mvs: "+ret.size()+","+row);
		return ret;
	}
	private ArrayList<Cell> kingMoves(Cell cell) {
		ArrayList<Cell> ret = new ArrayList<Cell>();
		int row = cell.row;
		int col = cell.col;
		int row_inc = 0;
		int col_inc = 0;
		Cell c;
		for(int x = 0;x<8;x++){
			switch(x){
			case 0:
				//up right
				row_inc = 1;
				col_inc = 1;
				break;
			case 1:
				//up
				row_inc = 1;
				col_inc = 0;
				break;
			case 2:
				//up left
				row_inc = 1;
				col_inc = -1;
				break;
			case 3:
				//left
				row_inc = 0;
				col_inc = -1;
				break;
			case 4:
				//down left
				row_inc = -1;
				col_inc = -1;
				break;
			case 5:
				//down
				row_inc = -1;
				col_inc = 0;
				break;
			case 6:
				//down right
				row_inc = -1;
				col_inc = 1;
				break;
			case 7:
				//right
				row_inc = 0;
				col_inc = 1;
				break;
			}
			row = cell.row+row_inc;
			col = cell.col+col_inc;
			
			if(
					col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPieceColor()!=player_color){
				ret.add(c);
			}
		}
		if(cell.can_castle){
			Cell d;
			//check left
			d = board.get(rows[cell.row]).get(columns[0]);
			if(d.can_castle){
				//so far so good, now just make sure everything is empty
				boolean fail=false;
				for(int x = 1; x<cell.col-1; x++){
					if(board.get(rows[cell.row]).get(columns[x]).getPiece()!=NO_PIECE){
						fail=true;
						break;
					}
				}
				if(!fail){
					c = board.get(rows[cell.row]).get(columns[cell.col-2]);
					ret.add(c);
				}
			}
			//check right
			d = board.get(rows[cell.row]).get(columns[columns.length-1]);
			if(d.can_castle){
				//so far so good, now just make sure everything is empty
				boolean fail=false;
				for(int x = cell.col+1; x<columns.length-1; x++){
					if(board.get(rows[cell.row]).get(columns[x]).getPiece()!=NO_PIECE){
						fail=true;
						break;
					}
				}
				if(!fail){
					c = board.get(rows[cell.row]).get(columns[cell.col+2]);
					ret.add(c);
				}
			}
		}
		return ret;
	}
	private ArrayList<Cell> knightMoves(Cell cell){
		ArrayList<Cell> ret = new ArrayList<Cell>();
		int row = cell.row;
		int col = cell.col;
		int row_inc = 0;
		int col_inc = 0;
		Cell c;
		for(int x = 0;x<8;x++){
			switch(x){
			case 0:
				//up right
				row_inc = 2;
				col_inc = 1;
				break;
			case 1:
				//up left
				row_inc = 2;
				col_inc = -1;
				break;
			case 2:
				//left up
				row_inc = 1;
				col_inc = -2;
				break;
			case 3:
				//left down
				row_inc = -1;
				col_inc = -2;
				break;
			case 4:
				//down left
				row_inc = -2;
				col_inc = -1;
				break;
			case 5:
				//down right
				row_inc = -2;
				col_inc = 1;
				break;
			case 6:
				//right down
				row_inc = -1;
				col_inc = 2;
				break;
			case 7:
				//right up
				row_inc = 1;
				col_inc = 2;
				break;
			}
			row = cell.row+row_inc;
			col = cell.col+col_inc;
			
			if(
					col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPieceColor()!=player_color){
				ret.add(c);
			}
		}
		return ret;
	}
	private ArrayList<Cell> rookMoves(Cell cell) {
		ArrayList<Cell> ret = new ArrayList<Cell>();
		int row,col;
		int row_inc = 0;
		int col_inc = 0;
		
		for(int x = 0;x<4;x++){
			switch(x){
			case 0:
				//up
				row_inc = 1;
				col_inc = 0;
				break;
			case 1:
				//left
				row_inc = 0;
				col_inc = -1;
				break;
			case 2:
				//down
				row_inc = -1;
				col_inc = 0;
				break;
			case 3:
				//right
				row_inc = 0;
				col_inc = 1;
				break;
			}
			col = cell.col+col_inc;
			row = cell.row+row_inc;

			Cell c;
			while(
					col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPiece()==NO_PIECE){
				ret.add(c);
				row+=row_inc;
				col+=col_inc;
			}
			if(		col<columns.length&&
					col>=0&&
					row<rows.length&&
					row>=0&&
					(c = board.get(rows[row]).get(columns[col]))
						.getPieceColor()!=player_color){
				ret.add(c);
			}
		
		}

		return ret;
	}

	private Cell hitCell(int x, int y){
		int label_size = cell_size/2;
		int row = (y-clipBounds_board.top-label_size)/cell_size;
		row = 7-row;
		int col = (x-clipBounds_board.left-label_size)/cell_size;
		Cell ret =board.get(rows[row]).get(columns[col]);
		ret.setPos(row,col);
		Log.d(TAG,"hit on: "+ret.row+", "+ret.col+": "+ret.getRect().contains(x,y));
		
		return ret;
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}

}

class PanelThread extends Thread {
    private SurfaceHolder _surfaceHolder;
    private GameView _panel;
    private boolean _run = false;


    public PanelThread(SurfaceHolder surfaceHolder, GameView panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }


    public void setRunning(boolean run) { //Allow us to stop the thread
        _run = run;
    }


    @Override
    public void run() {
        Canvas c;
        while (_run) {     //When setRunning(false) occurs, _run is 
            c = null;      //set to false and loop ends, stopping thread


            try {


                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {


                 //Insert methods to modify positions of items in onDraw()
                _panel.postInvalidate();


                }
            } finally {
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}