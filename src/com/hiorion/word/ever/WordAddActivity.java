package com.hiorion.word.ever;

import model.Word;

import com.hiorion.word.ever.db.WordAccess;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class WordAddActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_add);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_word_add, menu);
        return true;
    }
    
    public void add_act_insert(View view){    	
    	
    	  EditText et_word=(EditText)findViewById(R.id.et_word);
    	  EditText et_ogsentence=(EditText)findViewById(R.id.et_ogsentence);
    	  EditText et_meaning=(EditText)findViewById(R.id.et_meaning);
    	  EditText et_example1=(EditText)findViewById(R.id.et_example1);
    	  EditText et_example2=(EditText)findViewById(R.id.et_example2);
    	  
    	  WordAccess dba=new WordAccess(this);
          dba.OpenConnectionForWriting();
          Word word=new Word();
          word.category_id=1;
          word.library_id=1;
          word.meaning=et_meaning.getText().toString();
          word.ogsentence=et_ogsentence.getText().toString();
          word.ogsource="";
          word.word=et_word.getText().toString();
          word.ogsourcephoto="";
          dba.Insert(word);
          dba.Close();
          Intent intent=new Intent(this, MainActivity.class);
          startActivity(intent);
    }
}
