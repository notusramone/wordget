package com.hiorion.word.ever;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.db.DBschema;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("whatever");      
        //add button on action bar
        final Action shareAction = new IntentAction(this,createAddIntent(),R.drawable.content_new);
        actionBar.addAction(shareAction);
        //************Action Bar
        
       //testing code
        WordAccess dba=new WordAccess(this);
        dba.OpenConnectionForRead();
        Word word=new Word();
        word.category_id=1;
        word.library_id=1;
        word.meaning="no means always";
        word.ogsentence="whatever you want, I will give you.";
        word.ogsource="Up In The Air";
        word.word="ever";
        word.ogsourcephoto="";
        dba.Insert(word);
        //dba.Close();
        
        //fill the list
        Cursor cursor=dba.Query_WordList(0, 0,  0, 0);

        String[] froms={DBschema.COLUMN_Word_Word};
        int[] tos={R.id.rowTextView};
        @SuppressWarnings("deprecation")
		SimpleCursorAdapter ada=new SimpleCursorAdapter(this,R.layout.wordrow,cursor,froms,tos);
        
        ListView list=(ListView)findViewById(R.id.wordlist);
        list.setAdapter(ada);
        
        dba.Close();
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
