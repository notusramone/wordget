package com.hiorion.word.ever;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class WordAddActivity extends Activity {

	int lib_selected = 0;
	Spinner spinner_lib = null;
	Spinner spinner_part = null;
	int part_selected = 0;
	String meaning = "";
	String ogsen = "";
	String word = "";
	String ex1 = "";
	String ex2 = "";
	String extra = "";

	EditText et_word = null;
	EditText et_ogsentence = null;
	EditText et_meaning = null;
	EditText et_example1 = null;
	EditText et_example2 = null;
	EditText et_extrainfo = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_word_add);

		// *************Action Bar
		// set action bar title
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.actionbar_title_addword);

		actionBar.addAction(new Action() {

			public int getDrawable() {
				return R.drawable.navigation_accept;
			}

			public void performAction(View view) {
				add_act_insert(view);

			}

		});
		// *************Action Bar
	}

	@Override
	public void onResume() {
		super.onResume();
		fillData();
	}

	private void fillData() {
		// fill the library list
		LibraryAccess la = new LibraryAccess(this);
		try {
			la.OpenConnectionForRead();
			Cursor cursor = la.Query_LibraryList();
			startManagingCursor(cursor);
			String[] froms = { DBHelper.COLUMN_Library_Name };
			int[] tos = { android.R.id.text1 };
			@SuppressWarnings("deprecation")
			SimpleCursorAdapter ada = new SimpleCursorAdapter(this,
					android.R.layout.simple_spinner_dropdown_item, cursor,
					froms, tos);
			ada.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner_lib = (Spinner) findViewById(R.id.spin_library);
			spinner_lib.setAdapter(ada);
			if (lib_selected > 0)
				spinner_lib.setSelection(lib_selected);
			else if(Keys.Lib_uuid_global.length()>0){
				int libcount=cursor.getCount();
				int i=0;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					
					String uuid=cursor.getString(2);
					if(uuid.equals(Keys.Lib_uuid_global))
						break;
					i++;
				}
				spinner_lib.setSelection(i);
			}
		}

		finally {
			la.Close();
		}

		// resume other data
		if (ex1.length() > 0)
			et_extrainfo.setText(ex1);
		if (ex2.length() > 0)
			et_extrainfo.setText(ex2);

		if (part_selected > 0)
			spinner_part.setSelection(part_selected);
		if (word.length() > 0)
			et_extrainfo.setText(word);
		if (meaning.length() > 0)
			et_extrainfo.setText(meaning);
		if (ogsen.length() > 0)
			et_extrainfo.setText(ogsen);
		if (extra.length() > 0)
			et_extrainfo.setText(extra);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (spinner_lib != null) {
			lib_selected = spinner_lib.getSelectedItemPosition();
		}
		if (spinner_part != null) {
			part_selected = spinner_part.getSelectedItemPosition();
		}
		if (et_word != null) {
			word = et_word.getText().toString().trim();
		}
		if (et_ogsentence != null) {
			ogsen = et_ogsentence.getText().toString().trim();
		}
		if (et_meaning != null) {
			meaning = et_meaning.getText().toString().trim();
		}
		if (et_example1 != null) {
			ex1 = et_example1.getText().toString().trim();
		}
		if (et_example2 != null) {
			ex2 = et_example2.getText().toString().trim();
		}
		if (et_extrainfo != null) {
			extra = et_extrainfo.getText().toString().trim();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_word_add, menu);
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

	@Override
	public void onBackPressed() {
		// do something on back.
		Keys.WordAddFlag = -1;
		super.onBackPressed();
		return;
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	public void add_act_insert(View view) {

		EditText et_word = (EditText) findViewById(R.id.et_word);
		if (et_word.getText().toString().trim().length() == 0)
			return;
		et_ogsentence = (EditText) findViewById(R.id.et_ogsentence);
		et_meaning = (EditText) findViewById(R.id.et_meaning);
		et_example1 = (EditText) findViewById(R.id.et_example1);
		et_example2 = (EditText) findViewById(R.id.et_example2);
		et_extrainfo = (EditText) findViewById(R.id.et_extrainfo);
		spinner_lib = (Spinner) findViewById(R.id.spin_library);
		spinner_part = (Spinner) findViewById(R.id.spin_part);

		if (et_word.getText().toString().trim().length() == 0) {
			showToast(this.getString(R.string.popup_add_fillvalues));
		} else {
			WordAccess dba = new WordAccess(this);
			try {
				dba.OpenConnectionForWriting();
				Word word = new Word();
				word.meaning = et_meaning.getText().toString();
				word.ogsentence = et_ogsentence.getText().toString();
				word.word = et_word.getText().toString();
				word.ogsourcephoto = "";
				word.extrainfo = et_extrainfo.getText().toString();
				word.part = spinner_part.getSelectedItemPosition() + 1;
				// librar
				long libid = spinner_lib.getSelectedItemId();
				LibraryAccess la = new LibraryAccess(this);
				la.OpenConnectionForRead();
				String lib_uuid = la.GetALibrary(libid).uuid;
				la.Close();
				word.library_uuid = lib_uuid;
				Example ex1 = new Example();
				ex1.sentence = et_example1.getText().toString();
				Example ex2 = new Example();
				ex2.sentence = et_example2.getText().toString();
				word.examples.add(ex1);
				word.examples.add(ex2);
				dba.Insert(word);
			} finally {
				dba.Close();
			}

			finish();
			
			startActivity(new Intent(this,
					MainActivity.class));
		}
	}
}
