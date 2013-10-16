package com.hiorion.word.ever;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.hiorion.word.ever.db.ExampleAccess;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class WordEditActivity extends Activity {

	long id;
	Library[] libarray = new Library[1];
	String[] libnamearray = new String[1];
	Word ogword;
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

	EditText edit_et_word = null;
	EditText edit_et_ogsentence = null;
	EditText edit_et_meaning = null;
	EditText edit_et_example1 = null;
	EditText edit_et_example2 = null;
	EditText edit_et_extrainfo = null;

	@Override
	public void onPause() {
		super.onPause();
		if (spinner_lib != null) {
			lib_selected = spinner_lib.getSelectedItemPosition();
		}
		if (spinner_part != null) {
			part_selected = spinner_part.getSelectedItemPosition();
		}
		if (edit_et_word != null) {
			word = edit_et_word.getText().toString().trim();
		}
		if (edit_et_ogsentence != null) {
			ogsen = edit_et_ogsentence.getText().toString().trim();
		}
		if (edit_et_meaning != null) {
			meaning = edit_et_meaning.getText().toString().trim();
		}
		if (edit_et_example1 != null) {
			ex1 = edit_et_example1.getText().toString().trim();
		}
		if (edit_et_example2 != null) {
			ex2 = edit_et_example2.getText().toString().trim();
		}
		if (edit_et_extrainfo != null) {
			extra = edit_et_extrainfo.getText().toString().trim();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_word_edit);

		// *************Action Bar
		// set action bar title
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.actionbar_title_editword);

		actionBar.addAction(new Action() {

			public int getDrawable() {
				return R.drawable.navigation_accept;
			}

			public void performAction(View view) {
				edit_act_update(view);

			}

		});
		// ************Action Bar

	}

	@Override
	public void onResume() {
		super.onResume();
		id = getIntent().getLongExtra(Keys.IntentExtraKey_WordID_e, 0);
		Log.e("test", id+"");
		filldata();
	}

	private void fillLibrary(String library_uuid) {
		// fill the library list
		spinner_lib = (Spinner) findViewById(R.id.edit_spin_library);
		// open db
		LibraryAccess la = new LibraryAccess(this);
		try {
			la.OpenConnectionForRead();
			Cursor cursor = la.Query_LibraryList();
			startManagingCursor(cursor);
			int count = cursor.getCount();
			Library libdef = new Library();
			libdef.name = "Not Set";
			libdef.id = 0;
			libarray[0] = libdef;
			libnamearray[0] = libdef.name;
			int selectedidx = 0;
			if (count > 0) {
				libarray = new Library[cursor.getCount()];
				libnamearray = new String[cursor.getCount()];
				int i = 0;
				// put the element to array
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					Library libdb = new Library();
					libdb.name = cursor.getString(0);
					libdb.uuid = cursor.getString(2);
					libarray[i] = libdb;
					libnamearray[i] = libdb.name;
					if (libdb.uuid.equals(library_uuid))
						selectedidx = i;
					i++;
				}
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, libnamearray);

			spinner_lib.setAdapter(adapter);

			if (lib_selected == 0)
				spinner_lib.setSelection(selectedidx);
			else
				spinner_lib.setSelection(lib_selected);

		} finally {
			la.Close();
		}
	}

	private void filldata() {
		WordAccess dba = new WordAccess(this);
		try {
			dba.OpenConnectionForRead();
			ogword = dba.GetAWord(id);
			dba.fillExamples(ogword);
		} finally {
			dba.Close();
		}

		edit_et_word = (EditText) findViewById(R.id.edit_et_word);
		edit_et_ogsentence = (EditText) findViewById(R.id.edit_et_ogsentence);
		edit_et_meaning = (EditText) findViewById(R.id.edit_et_meaning);
		edit_et_example1 = (EditText) findViewById(R.id.edit_et_example1);
		edit_et_example2 = (EditText) findViewById(R.id.edit_et_example2);
		edit_et_extrainfo = (EditText) findViewById(R.id.edit_et_extrainfo);
		spinner_part = (Spinner) findViewById(R.id.edit_spin_part);

		ExampleAccess ea = new ExampleAccess(this);
		ArrayList<Example> exlist = new ArrayList<Example>();
		try {
			ea.OpenConnectionForRead();
			exlist = ea.Query_ExampleList(ogword.uuid);
		} finally {
			ea.Close();
		}

		// og sentence
		for (Example ex : exlist) {
			if (ex.type == Example.Type_OGSentence) {
				ogword.ogsentence = ex.sentence;
				exlist.remove(ex);
				break;
			}
		}
		// examples
		if (ex1.length() == 0)
			edit_et_example1.setText(exlist.get(0).sentence);
		else
			edit_et_extrainfo.setText(ex1);
		if (ex2.length() == 0)
			edit_et_example2.setText(exlist.get(1).sentence);
		else
			edit_et_extrainfo.setText(ex2);

		if (part_selected == 0)
			spinner_part.setSelection(ogword.part - 1);
		else
			spinner_part.setSelection(part_selected);
		if (word.length() == 0)
			edit_et_word.setText(ogword.word);
		else
			edit_et_extrainfo.setText(word);
		if (meaning.length() == 0)
			edit_et_meaning.setText(ogword.meaning);
		else
			edit_et_extrainfo.setText(meaning);
		if (ogsen.length() == 0)
			edit_et_ogsentence.setText(ogword.ogsentence);
		else
			edit_et_extrainfo.setText(ogsen);
		if (extra.length() == 0)
			edit_et_extrainfo.setText(ogword.extrainfo);
		else
			edit_et_extrainfo.setText(extra);

		fillLibrary(ogword.library_uuid);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_word_edit, menu);
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

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	public void edit_act_update(View view) {

		EditText edit_et_word = (EditText) findViewById(R.id.edit_et_word);
		if (edit_et_word.getText().toString().trim().length() == 0)
			return;
		edit_et_ogsentence = (EditText) findViewById(R.id.edit_et_ogsentence);
		edit_et_meaning = (EditText) findViewById(R.id.edit_et_meaning);
		edit_et_example1 = (EditText) findViewById(R.id.edit_et_example1);
		edit_et_example2 = (EditText) findViewById(R.id.edit_et_example2);
		edit_et_extrainfo = (EditText) findViewById(R.id.edit_et_extrainfo);
		spinner_part = (Spinner) findViewById(R.id.edit_spin_part);

		if (edit_et_word.getText().toString().trim().length() == 0) {
			showToast(this.getString(R.string.popup_add_fillvalues));
		} else {
			WordAccess dba = new WordAccess(this);
			try {
				dba.OpenConnectionForWriting();
				Word word = new Word();
				word.meaning = edit_et_meaning.getText().toString();
				word.ogsentence = edit_et_ogsentence.getText().toString();
				word.word = edit_et_word.getText().toString();
				word.ogsourcephoto = "";
				word.extrainfo = edit_et_extrainfo.getText().toString();
				word.part = spinner_part.getSelectedItemPosition() + 1;
				// librar
				// long libid=spinner.getSelectedItemId();
				Library libsel = (Library) libarray[spinner_lib
						.getSelectedItemPosition()];
				word.library_uuid = libsel.uuid;
				Example ex1 = new Example();
				ex1.sentence = edit_et_example1.getText().toString();
				Example ex2 = new Example();
				ex2.sentence = edit_et_example2.getText().toString();
				word.examples.add(ex1);
				word.examples.add(ex2);
				word.id = id;
				dba.Update(word, ogword);
			} finally {
				dba.Close();
			}

			finish();
		}
	}
}
