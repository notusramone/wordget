package com.hiorion.word.ever;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import com.hiorion.word.ever.db.DBHelper;
import com.hiorion.word.ever.db.ExampleAccess;
import com.hiorion.word.ever.db.LibraryAccess;
import com.hiorion.word.ever.db.WordAccess;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Word;
import com.markupartist.android.widget.ActionBar;

public class WordEditActivity extends Activity {

	long id;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_edit);
        
        //*************Action Bar
        //set action bar title
        final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("loving niuniu");   

        //************Action Bar
        
        id=getIntent().getLongExtra(Keys.IntentExtraKey_WordID,0);
        
        fillLibrary();
        filldata();
    }
    
    private void fillLibrary(){
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
    
    private void filldata(){
    	   WordAccess dba=new WordAccess(this);
           dba.OpenConnectionForRead();
           Word word=dba.GetAWord(id);
           dba.Close();
           
           EditText edit_et_word=(EditText)findViewById(R.id.edit_et_word);
     	  	EditText edit_et_ogsentence=(EditText)findViewById(R.id.edit_et_ogsentence);
     	  	EditText edit_et_meaning=(EditText)findViewById(R.id.edit_et_meaning);
     	  	EditText edit_et_example1=(EditText)findViewById(R.id.edit_et_example1);
     	  	EditText edit_et_example2=(EditText)findViewById(R.id.edit_et_example2);
     	     EditText edit_et_extrainfo=(EditText)findViewById(R.id.edit_et_extrainfo);
     	  	Spinner spinner=(Spinner)findViewById(R.id.spin_library);
           //spinner.
     	  	edit_et_word.setText(word.word);
     	  	edit_et_meaning.setText(word.meaning);
     	  	edit_et_ogsentence.setText(word.ogsentence);
     	  	edit_et_extrainfo.setText(word.extrainfo);
          
           
           ExampleAccess ea=new ExampleAccess(this);
           ea.OpenConnectionForRead();
           ArrayList<Example> exlist= ea.Query_ExampleList(word.id);
           if(exlist.size()>0)
           	edit_et_example1.setText(exlist.get(0).sentence);
           if(exlist.size()>1)
           	edit_et_example2.setText(exlist.get(0).sentence);
        	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_word_edit, menu);
        return true;
    }
    
    public void edit_act_update(View view){
    	
    	
  	  EditText edit_et_word=(EditText)findViewById(R.id.edit_et_word);
  	  EditText edit_et_ogsentence=(EditText)findViewById(R.id.edit_et_ogsentence);
  	  EditText edit_et_meaning=(EditText)findViewById(R.id.edit_et_meaning);
  	  EditText edit_et_example1=(EditText)findViewById(R.id.edit_et_example1);
  	  EditText edit_et_example2=(EditText)findViewById(R.id.edit_et_example2);
  	  EditText edit_et_extrainfo=(EditText)findViewById(R.id.edit_et_extrainfo);
  	  Spinner spinner=(Spinner)findViewById(R.id.spin_library);
  	  
  	   WordAccess dba=new WordAccess(this);
        dba.OpenConnectionForWriting();
        Word word=new Word();
        word.meaning=edit_et_meaning.getText().toString();
        word.ogsentence=edit_et_ogsentence.getText().toString();
        word.word=edit_et_word.getText().toString();
        word.ogsourcephoto="";
        word.extrainfo=edit_et_extrainfo.getText().toString();
        //librar
        long libid=spinner.getSelectedItemId();
        word.library_id=libid;
        Example ex1=new Example();
        ex1.sentence=edit_et_example1.getText().toString();
        Example ex2=new Example();
        ex2.sentence=edit_et_example2.getText().toString();
        if(ex1.sentence.length()>0)
      	  word.examples.add(ex1);
        if(ex2.sentence.length()>0)
      	  word.examples.add(ex2);
        word.id=id;
        dba.Update(word);
        dba.Close();
        
        finish();
    }
}
