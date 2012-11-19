package com.hiorion.word.ever;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;

import com.hiorion.word.ever.db.ExampleAccess;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class WordDisplayActivity extends Activity {
	
	long id;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_display);
        //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("loving niuniu");   
      //add button on action bar
        final Action shareAction = new IntentAction(this,createEditIntent(),R.drawable.content_edit);
        actionBar.addAction(shareAction);
        //************Action Bar


        id=getIntent().getLongExtra(Keys.IntentExtraKey_WordID,0);
        //filldata();

    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	filldata();
    }
    private void filldata(){
        WordAccess dba=new WordAccess(this);
        dba.OpenConnectionForRead();
        Word word=dba.GetAWord(id);
        dba.Close();
        TextView dis_tv_word=(TextView)findViewById(R.id.dis_tv_word);
        TextView dis_tv_library=(TextView)findViewById(R.id.dis_tv_library);
        TextView dis_tv_meaning=(TextView)findViewById(R.id.dis_tv_meaning);
        TextView dis_tv_ogsentence=(TextView)findViewById(R.id.dis_tv_ogsentence);
        TextView dis_tv_extrainfo=(TextView)findViewById(R.id.dis_tv_extrainfo);
        TextView dis_tv_example1=(TextView)findViewById(R.id.dis_tv_example1);
        TextView dis_tv_example2=(TextView)findViewById(R.id.dis_tv_example2);
        
        dis_tv_word.setText(word.word);
        dis_tv_meaning.setText(word.meaning);
        dis_tv_ogsentence.setText(word.ogsentence);
        dis_tv_extrainfo.setText(word.extrainfo);
        LibraryAccess la=new LibraryAccess(this);
        la.OpenConnectionForRead();
        Library lib=la.GetALibrary(word.library_id);
        la.Close();
        dis_tv_library.setText(lib.name);
        
        ExampleAccess ea=new ExampleAccess(this);
        ea.OpenConnectionForRead();
        ArrayList<Example> exlist= ea.Query_ExampleList(word.id);
        if(exlist.size()>0)
        	dis_tv_example1.setText(exlist.get(0).sentence);
        if(exlist.size()>1)
        	dis_tv_example2.setText(exlist.get(0).sentence);
        	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_word_display, menu);
        return true;
    }
    
    //************Action Bar Button_Edit
      private Intent createEditIntent() {
      	Intent intent=new Intent(this, WordEditActivity.class);
      	 id=getIntent().getLongExtra(Keys.IntentExtraKey_WordID,0);
      	intent.putExtra(Keys.IntentExtraKey_WordID, id);
      	return intent;
      }
}
