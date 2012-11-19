package com.hiorion.word.ever;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.view.*;
import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Library;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class MainActivity extends Activity {

	WordAccess dba;
	ListView list;
	int updateflag ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("loving niuniu");      
        //add button on action bar
        final Action shareAction = new IntentAction(this,createAddIntent(),R.drawable.content_new);
        actionBar.addAction(shareAction);
        //************Action Bar
        
/*        LibraryAccess la=new LibraryAccess(this);
        la.OpenConnectionForWriting();
        Library lib=new Library();
        
        lib.name="Daily Reading";
        lib.type=0;
        la.Insert(lib);
       
        
        lib.name="The Economist";
        lib.type=1;
        la.Insert(lib);
        
        lib.name="Life Of Pi";
        lib.type=2;
        la.Insert(lib);
        
        lib.name="World Without End";
        lib.type=2;
        la.Insert(lib);
        
        lib.name="The Art of Procrastination";
        lib.type=2;
        la.Insert(lib);

       la.Close();*/
       
        list=(ListView)findViewById(R.id.wordlist);
       // fillList(list);
        
        list.setOnItemLongClickListener (new OnItemLongClickListener() {
        	  public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {      		  
        		  showDeleteDialog(id,position);
        		  return true;
        		  }
        		});
        list.setOnItemClickListener (new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
        		showWordDisplay(arg3);
		}
      		});
     
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(list==null)
    		 list=(ListView)findViewById(R.id.wordlist);
    	if(Keys.WordAddFlag>0)
    		fillList(list);
    	Keys.WordAddFlag=1;
    }
   
    
    private void fillList(ListView list){
        dba=new WordAccess(this);
        dba.OpenConnectionForRead();
   
        //fill the listView
        Cursor cursor=dba.Query_WordList(0, 0,  0, 0);

        String[] froms={DBHelper.COLUMN_Word_Word};
        int[] tos={R.id.rowTextView};
        @SuppressWarnings("deprecation")
		SimpleCursorAdapter ada=new SimpleCursorAdapter(this,R.layout.wordrow,cursor,froms,tos);

        list.setAdapter(ada);
        
        dba.Close();
    }
    //Display a Word
    private void showWordDisplay(long id){
    	Intent intent=new Intent(this, WordDisplayActivity.class);
    	intent.putExtra(Keys.IntentExtraKey_WordID, id);
    	startActivity(intent);
    }
    
    //Popup delete confimation
    private void showDeleteDialog(final long id, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.content_discard);
        dialog.setMessage(R.string.delete_alert_title);        
        dialog.setPositiveButton(R.string.delete_alert_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            		dba.OpenConnectionForWriting();
            		dba.Delete(id);
            		if(list==null)
               		 list=(ListView)findViewById(R.id.wordlist);
            		fillList(list);
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
    
  //************Action Bar Button_Add
    private Intent createAddIntent() {
    	Intent intent=new Intent(this, WordAddActivity.class);
    	return intent;
    }
}
