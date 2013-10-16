package com.hiorion.word.ever;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.R.attr;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiorion.word.ever.db.ExampleAccess;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import android.widget.LinearLayout.LayoutParams;

public class WordDisplayActivity extends Activity   implements OnClickListener{

	long cur_id;
	String word_str;
	boolean isPrev=true;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;
	SoundPool sp=null;
	int spid=0;
	

	@Override
	public void onResume() {
		super.onResume();
		if(cur_id==0){
			cur_id = getIntent().getLongExtra(Keys.IntentExtraKey_WordID, 0);
		}
		filldata();
	}
	    
	Intent editIntent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(cur_id==0){
			cur_id = getIntent().getLongExtra(Keys.IntentExtraKey_WordID, 0);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_word_display);

		// *************Action Bar
		// set action bar title
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.actionbar_title_displayword);
		// add edit button on action bar
		initEditIntent();
		final Action editAction = new IntentAction(this, this.editIntent,
				R.drawable.content_edit);
		actionBar.addAction(editAction);
		actionBar.addAction(new InsertWordAction());
		actionBar.addAction(new PlayAction());
		
		// ************Action Bar
		
		// Gesture detection
        gestureDetector = new GestureDetector(new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };
	}

	private class PlayAction implements Action {

		@Override
		public int getDrawable() {
			return R.drawable.av_play_over_video;
		}

		@Override
		public void performAction(View view) {
		
			sp=new SoundPool(1,AudioManager.STREAM_MUSIC,0);
			String prefix=word_str.substring(0, 1);
			sp.setOnLoadCompleteListener(new soundready());
			String path=Keys.FolderLocalAppSpeech+"/"+prefix+"/"+word_str.trim()+".mp3";
			try{
				spid=sp.load(path, 1);				
				
			}
			catch(Exception e){
				Log.e(Keys.LogTag,"no such audio file");
			}
		}

	}
	
	// insert a new word
	private class InsertWordAction implements Action {

			@Override
			public int getDrawable() {
				return R.drawable.content_new;
			}

			@Override
			public void performAction(View view) {
				// check library avaliable
				LibraryAccess la = new LibraryAccess(view.getContext());
				Intent intent = null;
				try {
					la.OpenConnectionForRead();
					Cursor cursor = la.Query_LibraryList();

					// if has library
					if (cursor.getCount() > 0) {
						intent = new Intent(view.getContext(),
								WordAddActivity.class);

					} else {
						intent = new Intent(view.getContext(),
								LibraryMgnActivity.class);

					}
					cursor.close();
				} finally {
					la.Close();
					startActivity(intent);
				}

			}

		}

	
	private class soundready implements SoundPool.OnLoadCompleteListener{

		@Override
		public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
			sp.play(spid, 1, 1, 0, 0, 1);
			
		}
		
	}
	
	private int pix(int dp) {
		Resources res = getResources();
		float dp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				res.getDisplayMetrics());
		return (int) dp1;
	}

	private void generate(LinearLayout ll, Word xword) {

		String normalbg = "#d4d4d4";
		String fontcolor="#000000";
		word_str=xword.word;
		// meaning
		if (xword.meaning.length() > 0) {
			TextView tv1 = new TextView(this);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params.setMargins(0, pix(20), 0, pix(20));
			tv1.setLayoutParams(params);
			tv1.setTextSize(16);
			tv1.setPadding(pix(10), pix(15), pix(10), pix(15));
			tv1.setGravity(Gravity.LEFT);
			tv1.setBackgroundColor(Color.parseColor("#bfd678"));
			tv1.setText("(" + Keys.GetPartOfSpeech(xword.part) + ") "
					+ xword.meaning);
			// add longclick evernt
			tv1.setClickable(true);
			tv1.setTextColor(Color.parseColor(fontcolor));
			tv1.setOnLongClickListener(new oglongclicklistener(this
					.getString(R.string.display_copy_meaning), xword.meaning));

			ll.addView(tv1);
		}
		// library
		LibraryAccess la = new LibraryAccess(this);
		Library lib = null;
		try {
			la.OpenConnectionForRead();
			lib = la.GetALibrary(xword.library_uuid);
		} finally {
			la.Close();
		}

		String fromlib = "from <" + lib.name + ">";
		TextView tv2 = new TextView(this);
		LayoutParams params2 = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params2.setMargins(0, pix(20), 0, 0);
		tv2.setLayoutParams(params2);
		tv2.setTextSize(16);
		tv2.setPadding(pix(10), pix(15), pix(10), pix(5));
		tv2.setGravity(Gravity.LEFT);
		tv2.setBackgroundColor(Color.parseColor(normalbg));
		tv2.setText(fromlib);
		tv2.setTextColor(Color.parseColor(fontcolor));
		ll.addView(tv2);

		ExampleAccess ea = new ExampleAccess(this);
		ArrayList<Example> exlist = new ArrayList<Example>();
		try {
			ea.OpenConnectionForRead();
			exlist = ea.Query_ExampleList(xword.uuid);
		} finally {
			ea.Close();
		}

		for (Example ex : exlist) {
			if (ex.type == Example.Type_OGSentence) {
				xword.ogsentence = ex.sentence;
				exlist.remove(ex);
				break;
			}
		}

		// og sentence
		if (xword.ogsentence.length() > 0) {
			TextView tv3 = new TextView(this);
			LayoutParams params3 = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params3.setMargins(0, 0, 0, pix(10));
			tv3.setLayoutParams(params3);
			tv3.setTextSize(16);
			tv3.setPadding(pix(10), 0, pix(10), pix(15));
			tv3.setGravity(Gravity.LEFT);
			tv3.setBackgroundColor(Color.parseColor(normalbg));
			String ogsen = "     ''" + xword.ogsentence + "'';";
			tv3.setText(ogsen);
			tv3.setClickable(true);
			tv3.setOnLongClickListener(new oglongclicklistener(this
					.getString(R.string.display_copy_og), xword.ogsentence));
			tv3.setTextColor(Color.parseColor(fontcolor));
			ll.addView(tv3);
		}
		// examples

		if (exlist.get(1).sentence.length() > 0) {
			TextView tv4 = new TextView(this);
			LayoutParams params3 = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params3.setMargins(0, pix(10), 0, 0);
			tv4.setLayoutParams(params3);
			tv4.setTextSize(16);
			tv4.setPadding(pix(10), pix(15), pix(10), pix(15));
			tv4.setGravity(Gravity.LEFT);
			tv4.setBackgroundColor(Color.parseColor(normalbg));
			tv4.setText(exlist.get(1).sentence);
			tv4.setClickable(true);
			tv4.setOnLongClickListener(new oglongclicklistener(this
					.getString(R.string.display_copy_example),
					exlist.get(1).sentence));
			tv4.setTextColor(Color.parseColor(fontcolor));
			ll.addView(tv4);
		}

		if (exlist.get(0).sentence.length() > 0) {
			TextView tv5 = new TextView(this);
			LayoutParams params3 = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			// params3.setMargins(0, 0, 0,0);
			tv5.setLayoutParams(params3);
			tv5.setTextSize(16);
			tv5.setPadding(pix(10), 0, pix(10), pix(15));
			tv5.setGravity(Gravity.LEFT);
			tv5.setBackgroundColor(Color.parseColor(normalbg));
			tv5.setText(exlist.get(0).sentence);
			tv5.setClickable(true);
			tv5.setOnLongClickListener(new oglongclicklistener("this example ",
					exlist.get(1).sentence));
			tv5.setTextColor(Color.parseColor(fontcolor));
			ll.addView(tv5);
		}

		// extra info
		if (xword.extrainfo.length() > 0) {
			TextView tv6 = new TextView(this);
			LayoutParams params3 = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			params3.setMargins(0, pix(20), 0, 0);
			tv6.setLayoutParams(params3);
			tv6.setTextSize(16);
			tv6.setPadding(pix(10), pix(15), pix(10), pix(15));
			tv6.setGravity(Gravity.LEFT);
			tv6.setBackgroundColor(Color.parseColor(normalbg));
			tv6.setText(xword.extrainfo);
			tv6.setTextColor(Color.parseColor(fontcolor));
			ll.addView(tv6);
		}

	}

	
	//gesture detector
	class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(WordDisplayActivity.this, "previous word", Toast.LENGTH_SHORT).show();
                    isPrev=true;
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //Toast.makeText(WordDisplayActivity.this, "next word", Toast.LENGTH_SHORT).show();
                    isPrev=false;
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

    }

	private void filldata() {

		LinearLayout ll = (LinearLayout) findViewById(R.id.layout_display);

		WordAccess dba = new WordAccess(this);
		ArrayList<Word> wlist = new ArrayList<Word>();
		try {
			dba.OpenConnectionForRead();
			// current_word=dba.GetAWord(id);
			wlist = dba.GetAWordWithAllMeaning(cur_id);
		} finally {
			dba.Close();
		}

		Word current_word = wlist.get(0);

		ll.removeAllViews();
		TextView tv = new TextView(this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		params.setMargins(0, pix(20), 0, pix(20));
		tv.setLayoutParams(params);
		tv.setTextSize(24);
		tv.setHeight(pix(60));
		// tv.setPadding(0, pix(15), 0, pix(15));
		tv.setGravity(Gravity.CENTER);
		tv.setBackgroundColor(Color.parseColor("#88b131"));
		tv.setText(current_word.word);
		tv.setTextColor(Color.parseColor("#000000"));
		//gesture detective
		tv.setOnClickListener(WordDisplayActivity.this); 
		tv.setOnTouchListener(gestureListener);
		ll.addView(tv);
		
	if(current_word.phonetic!=null && current_word.phonetic.length()>0){
		TextView tv_p = new TextView(this);
		LayoutParams params_p = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		//params_p.setMargins(0, pix(20), 0, pix(20));
		tv_p.setLayoutParams(params_p);
		tv_p.setTextSize(15);
		tv_p.setHeight(pix(25));
		//tv_p.setPadding(0, pix(15), 0, pix(15));
		tv_p.setGravity(Gravity.CENTER);
		//tv_p.setBackgroundColor(Color.parseColor("#88b131"));
		tv_p.setText("/"+current_word.phonetic+"/");
		tv_p.setTextColor(Color.parseColor("#000000"));
		ll.addView(tv_p);
	}

		for (Word w : wlist) {
			generate(ll, w);
		}

	}

	class oglongclicklistener implements OnLongClickListener {

		String contouse = "";
		String keytouse = "";

		public oglongclicklistener(String key, String cont) {
			contouse = cont;
			keytouse = key;
		}

		@Override
		public boolean onLongClick(View v) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(contouse);
			// ClipData clip = ClipData.newPlainText("word_cp",contouse);
			// clipboard.setPrimaryClip(clip);
			showCopiedDialog(keytouse);
			return true;
		}

	}

	// Popup copy confimation
	private void showCopiedDialog(String key) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(R.drawable.content_discard);
		dialog.setMessage(key + this.getString(R.string.display_copy));

		dialog.setNegativeButton(R.string.copied_alert_confirm, null);
		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_word_display, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.op_libraryedit:
			Intent it_libmgn = new Intent(this, LibraryMgnActivity.class);
			startActivity(it_libmgn);
			return true;
		case R.id.op_setting:
			Intent it_setting = new Intent(this, SettingActivity.class);
			startActivity(it_setting);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ************Action Bar Button_Edit
	private Intent initEditIntent() {
		editIntent = new Intent(this, WordEditActivity.class);
		//cur_id = getIntent().getLongExtra(Keys.IntentExtraKey_WordID, 0);
		editIntent.putExtra(Keys.IntentExtraKey_WordID_e, cur_id);
		Log.e("test", cur_id+" pass to edit");
		return editIntent;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub for OnClickListener interface
		WordAccess dba = new WordAccess(this);
		dba.OpenConnectionForRead();
		long new_id=0;
        if(isPrev){
        	new_id=dba.GetPrevWord(cur_id);
        }
        else{
        	new_id=dba.GetNextWord(cur_id);
        }
        dba.Close();
        if(new_id>0){
        	cur_id=new_id;
        	Log.e("test", cur_id+" after click");
        	editIntent.putExtra(Keys.IntentExtraKey_WordID_e, cur_id);
        	filldata();
        }
        
	}
	
	
}