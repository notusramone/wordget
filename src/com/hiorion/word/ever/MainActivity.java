package com.hiorion.word.ever;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.DBSyncHelper;
import com.hiorion.word.ever.db.LibraryAccess;

import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class MainActivity extends ListActivity {

	WordAccess dba;
	ListView list;
	int updateflag;
	int year, month, day, week = 0;
	String key = "";
	ActionBar actionBar;
	Activity main;
	int position=0;
	int top=0;
	String lib_uuid_user="";
	String titlewords="";
	int limit=5000;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);


		DBSyncHelper dbsh = new DBSyncHelper(this);
		main = this;

		// *************Action Bar
		// set action bar title
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		// actionBar.setTitle("Word List");

		// actionBar.addAction(new BackAction());
		actionBar.addAction(new LibraryAction());
		actionBar.addAction(new SearchAction());
		actionBar.addAction(new CalenAction());
		actionBar.addAction(new InsertWordAction());
		// sync action
		//actionBar.setHomeAction(new BackAction());
		actionBar.setOnTitleClickListener(new OnClickListener() {
			public void onClick(View v) {
				//dialog
				showClearDialog();
			}
		});

		// ************Action Bar

		
		if(Keys.Lib_uuid_global.length()>0){
			LibraryAccess la = new LibraryAccess(this);
			try{				
				la.OpenConnectionForRead();
				Library lib = la.GetALibrary(Keys.Lib_uuid_global);				
				lib_uuid_user = lib.uuid;				
				titlewords=lib.name;
				lib_uuid_user=Keys.Lib_uuid_global;
			}
			finally{
				la.Close();
			}
			
		}
		list = getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView parent, View view,
					int position, long id) {
				showDeleteDialog(id, position);
				return true;
			}
		});

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				showWordDisplay(arg3);
			}
		});

	}
	
	@Override
	public void onPause() {
		super.onPause();
		position=this.getListView().getFirstVisiblePosition();
		top=this.getListView().getTop();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if(year>0 || month>0 ||day>0||week>0||key.length()>0||lib_uuid_user.length()>0)
			limit=0;
		fillList(limit);
		this.getListView().setSelectionFromTop(position, top);
	}

	public void fillList_Home() {
		year = month = day = week = 0;
		key = "";
		lib_uuid_user="";
		titlewords="";
		Keys.Lib_uuid_global="";
		fillList(50);
	}

	@SuppressWarnings("deprecation")
	private void fillList(int limit) {		
		
		if (year != 0)
			titlewords = "" + year;
		if (month != 0)
			titlewords += "-" + month;
		if (week != 0)
			titlewords += "-" + week + "w";
		if (day != 0)
			titlewords += "-" + day;
		if (key.length() > 0)
			titlewords = "Search: " + key;
		if (titlewords.length() == 0)
			actionBar.setTitle(R.string.actionbar_title_main);
		else
			actionBar.setTitle(titlewords);
		if (year == 0 && key == "")
			limit = 5000;
		dba = new WordAccess(this);
		try {
			dba.OpenConnectionForRead();
			// fill the listView
			Cursor cursor = dba.Query_WordList(year, month, day, week,
					lib_uuid_user, limit, key);
			startManagingCursor(cursor);
			String[] froms = { DBHelper.COLUMN_Word_Word };
			EverAdapter evadapter = new EverAdapter(this, cursor);
			setListAdapter(evadapter);
		} finally {
			dba.Close();
		}
		//if(titlewords.length()>0)
			//showToast(titlewords);

	}

	// Display a WordgetActivity
	private void showWordDisplay(long id) {
		Intent intent = new Intent(this, WordDisplayActivity.class);
		intent.putExtra(Keys.IntentExtraKey_WordID, id);
		startActivity(intent);
	}

	// Popup delete confimation
	private void showDeleteDialog(final long id, final int position) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setIcon(R.drawable.content_discard);
		dialog.setMessage(R.string.delete_alert_title);
		dialog.setPositiveButton(R.string.delete_alert_confirm,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dba.OpenConnectionForWriting();
						dba.Delete(id);
						dba.Close();
						fillList(0);
					}
				});
		dialog.setNegativeButton(R.string.delete_alert_cancel, null);
		dialog.show();
	}

	// Popup delete confimation
		private void showClearDialog() {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setIcon(R.drawable.content_discard);
			dialog.setMessage(R.string.clear_alert_title);
			dialog.setPositiveButton(R.string.delete_alert_confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							fillList_Home();
						}
					});
			dialog.setNegativeButton(R.string.delete_alert_cancel, null);
			dialog.show();
		}	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_main, menu);
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

	private class BackAction implements Action{
		public int getDrawable() {
			// if dropbox enabled
			return R.drawable.ic_menu_home;
		}

		@Override
		public void performAction(View view) {
			 onBackPressed();
		}
	}
	
	// sync action
	private class HomeAction implements Action {
		@Override
		public int getDrawable() {
			// if dropbox enabled
			return R.drawable.navigation_refresh;
		}

		@Override
		public void performAction(View view) {
			Activity host = (Activity) view.getContext();
			AlertDialog.Builder dialog = new AlertDialog.Builder(host);
			dialog.setIcon(R.drawable.content_discard);
			dialog.setMessage(R.string.dropbox_alert_sync);
			dialog.setPositiveButton(R.string.delete_alert_confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Dialog d = (Dialog) dialog;
							Log.e(Keys.LogTag, "start sync");
							((MainActivity) main).actionBar
									.setTitle(R.string.actionbar_title_syncstart);
							Sync(main);
						}
					});
			dialog.setNegativeButton(R.string.delete_alert_cancel, null);
			dialog.show();
		}

		private void Sync(Context host) {
			actionBar.syncMode(true);

			AndroidAuthSession session = buildSession();

			DropboxAPI<AndroidAuthSession> mApi = new DropboxAPI<AndroidAuthSession>(
					session);
			DropboxSync ds = new DropboxSync(host, mApi);
			showToast(main.getString(R.string.popup_sync_start));
			ds.execute();
		}

	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	private String[] getKeys() {
		SharedPreferences prefs = getSharedPreferences(Keys.ACCOUNT_PREFS_NAME,
				0);
		String key = prefs.getString(Keys.ACCESS_KEY_NAME, null);
		String secret = prefs.getString(Keys.ACCESS_SECRET_NAME, null);
		if (key != null && secret != null) {
			String[] ret = new String[2];
			ret[0] = key;
			ret[1] = secret;
			return ret;
		} else {
			return null;
		}
	}

	private AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(Keys.APP_KEY, Keys.APP_SECRET);
		AndroidAuthSession session;

		String[] stored = getKeys();
		if (stored != null) {
			AccessTokenPair accessToken = new AccessTokenPair(stored[0],
					stored[1]);
			session = new AndroidAuthSession(appKeyPair, Keys.ACCESS_TYPE,
					accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair, Keys.ACCESS_TYPE);
		}

		return session;
	}

	// library dialog
	private class LibraryAction implements Action {

		@Override
		public int getDrawable() {
			return R.drawable.collections_labels;
		}

		@Override
		public void performAction(View view) {
			Activity host = (Activity) view.getContext();
			AlertDialog.Builder builder = new AlertDialog.Builder(host);
			LayoutInflater inflater = host.getLayoutInflater();
			View diaview = inflater.inflate(R.layout.dialog_library, null);
			// set library
			Spinner dialog_library = (Spinner) diaview
					.findViewById(R.id.dialog_library);
			LibraryAccess la = new LibraryAccess(host);
			try {
				la.OpenConnectionForRead();
				Cursor cursor = la.Query_LibraryList();
				// if has library
				if (cursor.getCount() > 0) {
					startManagingCursor(cursor);
					String[] froms = { DBHelper.COLUMN_Library_Name };
					int[] tos = { android.R.id.text1 };
					@SuppressWarnings("deprecation")
					SimpleCursorAdapter ada = new SimpleCursorAdapter(host,
							android.R.layout.simple_spinner_dropdown_item,
							cursor, froms, tos);
					ada.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					dialog_library.setAdapter(ada);

					builder.setView(diaview)
							.setPositiveButton(R.string.dialog_calendar_btn,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialogface,
												int arg1) {
											Dialog dia = (Dialog) dialogface;
											Spinner dialog_library = (Spinner) dia
													.findViewById(R.id.dialog_library);
											long libid = dialog_library
													.getSelectedItemId();
											LibraryAccess la = new LibraryAccess(
													dia.getContext());
											la.OpenConnectionForRead();
											Library lib = la.GetALibrary(libid);
											lib_uuid_user = lib.uuid;
											Keys.Lib_uuid_global = lib.uuid;
											la.Close();
											fillList(0);
											titlewords=lib.name;
											actionBar.setTitle(titlewords);
										}
									})
							.setNegativeButton(R.string.delete_alert_cancel,
									null);
					builder.show();
				}
				/*
				 * else{ cursor.close(); la.Close(); //go to library manage
				 * Intent it_libmgn=new Intent(host, LibraryMgnActivity.class);
				 * startActivity(it_libmgn); }
				 */
			} finally {
				la.Close();
			}

		}

	}

	// search dialog
	private class SearchAction implements Action {

		@Override
		public int getDrawable() {
			return R.drawable.action_search;
		}

		@Override
		public void performAction(View view) {
			Activity host = (Activity) view.getContext();
			AlertDialog.Builder builder = new AlertDialog.Builder(host);
			LayoutInflater inflater = host.getLayoutInflater();

			builder.setView(inflater.inflate(R.layout.dialog_search, null))
					.setPositiveButton(R.string.dialog_calendar_btn,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialogface,
										int arg1) {
									Dialog dia = (Dialog) dialogface;
									EditText dialog_search_keyword = (EditText) dia
											.findViewById(R.id.dialog_search_keyword);
									key = dialog_search_keyword.getText()
											.toString();
									if (key.length() > 0) {
										fillList(0);
									}

								}
							})
					.setNegativeButton(R.string.delete_alert_cancel, null);
			builder.show();
		}

	}

	// caneldar dialog
	private class CalenAction implements Action {

		@Override
		public int getDrawable() {
			return R.drawable.collections_go_to_today;
		}

		@Override
		public void performAction(View view) {
			Activity host = (Activity) view.getContext();
			AlertDialog.Builder builder = new AlertDialog.Builder(host);
			// Get the layout inflater
			LayoutInflater inflater = host.getLayoutInflater();
			builder.setView(inflater.inflate(R.layout.dialog_calen, null))
					// Add action buttons
					.setPositiveButton(R.string.dialog_calendar_btn,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									Dialog maind = (Dialog) dialog;
									Spinner calen_range = (Spinner) maind
											.findViewById(R.id.dialog_calen_range);
									DatePicker calen_datepicker = (DatePicker) maind
											.findViewById(R.id.dialog_calen_datepicker);

									int calrange = calen_range
											.getSelectedItemPosition();
									day = calen_datepicker.getDayOfMonth();
									year = calen_datepicker.getYear();
									month = calen_datepicker.getMonth() + 1;
									week = 0;

									SimpleDateFormat sdf = new SimpleDateFormat(
											"yyyy-MM-dd");
									try {

										Date curdate = sdf.parse("" + year
												+ "-" + month + "-" + day);
										Calendar cal = Calendar.getInstance();
										cal.setTime(curdate);
										week = cal.get(Calendar.WEEK_OF_MONTH);
									} catch (ParseException e) {
										e.printStackTrace();
									}
									// refill the list
									switch (calrange) {
									case 0:
										week = 0;
										break;
									case 1:
										day = 0;
										break;
									case 2:
										day = 0;
										week = 0;
										break;
									}

									fillList(0);

								}
							})
					.setNegativeButton(R.string.delete_alert_cancel, null);
			builder.show();

		}

	}

	private static class EverViewHolder {
		public TextView separator;
		public TextView wordView;
		public TextView ogsenView;
		public CharArrayBuffer wordbuff=new CharArrayBuffer(128);
		public CharArrayBuffer ogsenbuff=new CharArrayBuffer(256) ;

	}

	// ************a custom adapter
	private static class EverAdapter extends CursorAdapter {

		int[] stateArray;
		int[] dayArray;
		int[] monthArray;
		int[] yearArray;
		static final int state_unknown = 0;
		static final int state_regular = 1;
		static final int state_section = 2;

		public EverAdapter(Context context, Cursor cursor) {
			super(context, cursor);
			stateArray = cursor == null ? null : new int[cursor.getCount()];
			dayArray = cursor == null ? null : new int[cursor.getCount()];
			monthArray = cursor == null ? null : new int[cursor.getCount()];
			yearArray = cursor == null ? null : new int[cursor.getCount()];
		}

		@Override
		public void changeCursor(Cursor cursor) {
			stateArray = cursor == null ? null : new int[cursor.getCount()];
			dayArray = cursor == null ? null : new int[cursor.getCount()];
			monthArray = cursor == null ? null : new int[cursor.getCount()];
			yearArray = cursor == null ? null : new int[cursor.getCount()];
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			final int position = cursor.getPosition();
			boolean needSp = false;
			final EverViewHolder holder = (EverViewHolder) view.getTag();

			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String datestr = cursor.getString(4);
			String part = Keys.GetPartOfSpeech(cursor.getInt(5));
			String wordstr=cursor.getString(1);
			String ogstr=cursor.getString(3);
			//holder.ogsenbuff=new CharArrayBuffer(60);
			//holder.wordbuff=new CharArrayBuffer(128);
			cursor.copyStringToBuffer(1, holder.wordbuff);
			cursor.copyStringToBuffer(3, holder.ogsenbuff);
			Date wdate = null;
			try {
				wdate = dateFormat.parse(datestr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			int day = wdate.getDate();
			int month = wdate.getMonth();
			int year = wdate.getYear();

			// spefify the state
			switch (stateArray[position]) {

			case state_regular:
				needSp = false;
				break;
			case state_section:
				needSp = true;
				break;
			case state_unknown:
			default:
				if (position == 0) {
					needSp = true;
					dayArray[0] = day;
					monthArray[0] = month;
					yearArray[0] = year;
					stateArray[position] = state_section;
				} else {
					dayArray[position] = day;
					monthArray[position] = month;
					yearArray[position] = year;
					if (day != dayArray[position - 1]) {
						needSp = true;
						stateArray[position] = state_section;

					} else if (month != monthArray[position - 1]) {
						needSp = true;
						stateArray[position] = state_section;
					} else if (year != yearArray[position - 1]) {
						needSp = true;
						stateArray[position] = state_section;
					} else {
						needSp = false;
						stateArray[position] = state_regular;
					}
				}
			}

			holder.wordView.setText(wordstr);
			//String ogs=new String(holder.ogsenbuff.data);
			//Log.e(Keys.LogTag,ogs);
			holder.wordView.setText(holder.wordbuff.data, 0, holder.wordbuff.sizeCopied);
			holder.ogsenView.setText(holder.ogsenbuff.data, 0, holder.ogsenbuff.sizeCopied);
			/*holder.ogsenView.setText("(" + part + ") "
					+ ogstr);*/

			SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
			if (needSp) {
				holder.separator.setText(dateFormat2.format(wdate));
				holder.separator.setVisibility(View.VISIBLE);
			} else {
				holder.separator.setText("");
				holder.separator.setVisibility(View.GONE);
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View v = LayoutInflater.from(context).inflate(R.layout.wordrow,
					parent, false);

			EverViewHolder holder = new EverViewHolder();
			holder.separator = (TextView) v
					.findViewById(R.id.row_word_separator);
			holder.wordView = (TextView) v.findViewById(R.id.row_word_word);
			holder.ogsenView = (TextView) v.findViewById(R.id.row_word_meaning);

			v.setTag(holder);

			return v;
		}

	}
}
