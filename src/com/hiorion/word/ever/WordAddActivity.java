package com.hiorion.word.ever;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;

public class WordAddActivity extends Activity {
 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_add);
        //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("loving niuniu");   
        
        //fill the library list
        Spinner spinner=(Spinner)findViewById(R.id.spin_library);
        //open db
        LibraryAccess la=new LibraryAccess(this);
        la.OpenConnectionForRead();
        Cursor cursor=la.Query_LibraryList(0);
        
        String[] froms={DBHelper.COLUMN_Library_Name};
        int[] tos={android.R.id.text1};
        @SuppressWarnings("deprecation")
		SimpleCursorAdapter ada=new SimpleCursorAdapter(this,android.R.layout.simple_spinner_dropdown_item,cursor,froms,tos);
        ada.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ada);
        la.Close();
    }
    
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_word_add, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
    	// do something on back.
    	Keys.WordAddFlag=-1;
    	super.onBackPressed();
    	return;
    }
    
    
    public void add_act_insert(View view){    	
    	
    	  EditText et_word=(EditText)findViewById(R.id.et_word);
    	  EditText et_ogsentence=(EditText)findViewById(R.id.et_ogsentence);
    	  EditText et_meaning=(EditText)findViewById(R.id.et_meaning);
    	  EditText et_example1=(EditText)findViewById(R.id.et_example1);
    	  EditText et_example2=(EditText)findViewById(R.id.et_example2);
    	  EditText et_extrainfo=(EditText)findViewById(R.id.et_extrainfo);
    	  Spinner spinner=(Spinner)findViewById(R.id.spin_library);
    	  
    	  WordAccess dba=new WordAccess(this);
          dba.OpenConnectionForWriting();
          Word word=new Word();
          word.meaning=et_meaning.getText().toString();
          word.ogsentence=et_ogsentence.getText().toString();
          word.word=et_word.getText().toString();
          word.ogsourcephoto="";
          word.extrainfo=et_extrainfo.getText().toString();
          //librar
          long libid=spinner.getSelectedItemId();
          word.library_id=libid;
          Example ex1=new Example();
          ex1.sentence=et_example1.getText().toString();
          Example ex2=new Example();
          ex2.sentence=et_example2.getText().toString();
          if(ex1.sentence.length()>0)
        	  word.examples.add(ex1);
          if(ex2.sentence.length()>0)
        	  word.examples.add(ex2);
          dba.Insert(word);
          dba.Close();
          
          finish();
    }
}
