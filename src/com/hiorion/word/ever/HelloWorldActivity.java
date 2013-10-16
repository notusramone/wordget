package com.hiorion.word.ever;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.LibraryAccess;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class HelloWorldActivity extends Activity {

	ActionBar actionBar;
	ListView list;
	LibraryAccess la;
	Activity main;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_hello_world);
		
		main=this;
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setHomeAction(new HomeAction());
		actionBar.setTitle(R.string.hello_world);
		//actionBar.setHomeLogo(R.drawable.navigation_refresh);
		list=(ListView)findViewById(R.id.hello_list);
		
		list.setOnItemLongClickListener (new OnItemLongClickListener() {
      	  public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {      		  
      		  return false;
      		  }

		
      		});

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				enterLib(arg3);
			}
		});
	}

	 
	@Override
	public void onResume(){
	   super.onResume();
	   
	   FillList();
	}
		
	public void FillList() {
		// TODO Auto-generated method stub
		//open db
		if(list==null)
	    	list=(ListView)findViewById(R.id.hello_list);
        la=new LibraryAccess(this);
        Button btn_lib=(Button)findViewById(R.id.hello_btn_addlib);
        TextView tv_into=(TextView) findViewById(R.id.hello_intr);
		
    try{
        la.OpenConnectionForRead();
        Cursor cursor=la.Query_LibraryList();
        startManagingCursor(cursor);
        //fill the listView
        if(cursor.getCount()>0){     
        	btn_lib.setVisibility(View.GONE);
        	tv_into.setVisibility(View.GONE);
        	 String[] froms={DBHelper.COLUMN_Library_Name};
             int[] tos={R.id.row_lib_name};
             @SuppressWarnings("deprecation")
     		SimpleCursorAdapter ada=new SimpleCursorAdapter(this,R.layout.librow,cursor,froms,tos);

             list.setAdapter(ada);
        }
        else{
        	btn_lib.setVisibility(View.VISIBLE);
        	tv_into.setText(Html.fromHtml("<p>To sync, go to setting and enable your Dropbox account.</p>" +
        			"<p>To add a new library, click the button above.<br/>After got your first library, you can add words in it.</p>"));
        }
        	
       
    }
    finally{
    	  la.Close();
    }
        
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_hello_world, menu);
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
	
	public void gotolib(View view){
		Intent it_libmgn = new Intent(this, LibraryMgnActivity.class);
		startActivity(it_libmgn);
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
							((HelloWorldActivity) main).actionBar
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
	
	
	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}
	
	private void enterLib(long id) {
		// TODO Auto-generated method stub
		try{
			la.OpenConnectionForRead();
			Keys.Lib_uuid_global = la.GetALibrary(id).uuid;
			
		}
		  finally{
	    	  la.Close();
	    	  Intent intent = new Intent(this, MainActivity.class);
			  startActivity(intent);
	    }
	}

}
