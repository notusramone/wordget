package com.hiorion.word.ever;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemLongClickListener;

import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.model.Library;
import com.markupartist.android.widget.ActionBar;

public class LibraryMgnActivity extends Activity {

	ListView list;
	LibraryAccess la;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_library_mgn);
		
		 //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle(R.string.actionbar_title_libmgn);
        //****
        
		list=(ListView)findViewById(R.id.libmgn_list);
		
		list.setOnItemLongClickListener (new OnItemLongClickListener() {
      	  public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {      		  
      		  showDeleteDialog(id,position);
      		  return true;
      		  }
      		});
	}

	
	//Popup delete confimation
    private void showDeleteDialog(final long id, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.content_discard);
        dialog.setMessage(R.string.delete_lib_alert_title);        
        dialog.setPositiveButton(R.string.delete_alert_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            		la.OpenConnectionForWriting();
            		la.Delete(id);
            		la.Close();
            		fillList(list);
            }
        });
        dialog.setNegativeButton(R.string.delete_alert_cancel, null);   
        dialog.show();      
    }
    
	@Override
    public void onResume(){
    	super.onResume();
    	if(list==null)
    		 list=(ListView)findViewById(R.id.libmgn_list);
    	fillList(list);
    }
   
    
    private void fillList(ListView list){
    	//open db
        la=new LibraryAccess(this);
    try{
        la.OpenConnectionForRead();
        Cursor cursor=la.Query_LibraryList();
        startManagingCursor(cursor);
        //fill the listView

        String[] froms={DBHelper.COLUMN_Library_Name};
        int[] tos={R.id.row_lib_name};
        @SuppressWarnings("deprecation")
		SimpleCursorAdapter ada=new SimpleCursorAdapter(this,R.layout.librow,cursor,froms,tos);

        list.setAdapter(ada);
    }
    finally{
    	  la.Close();
    }
        
      
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_library_mgn, menu);
		return true;
	}
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
           
            case R.id.op_setting:
            	Intent it_setting=new Intent(this, SettingActivity.class);
            	startActivity(it_setting);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	public void libmgn_btn_click(View view){
		EditText libmgn_et_name=(EditText)findViewById(R.id.libmgn_et_name);
		if(libmgn_et_name.getText().toString().trim().length()==0)
			  return;
		Library lib=new Library();
		lib.name=libmgn_et_name.getText().toString();
		LibraryAccess la=new LibraryAccess(this);
		try{
			la.OpenConnectionForWriting();
			la.Insert(lib);
		}
		finally{
			la.Close();
		}
		
		libmgn_et_name.setText("");
		fillList(list);
	}
}
