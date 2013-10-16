package com.hiorion.word.ever;

import com.markupartist.android.widget.ActionBar;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setTitle(R.string.app_name);  
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_about, menu);
		return true;
	}
	 
		@Override
		public void onResume(){
			super.onResume();
			fillText();
		}
		
		void fillText(){
			TextView tv=(TextView) findViewById(R.id.about_tv);
			tv.setText(Html.fromHtml("<p><b>Are you learning a foreign language?</b> " +
					"I have read a book about fast reading. In this book," +
					" the author points out that a poor personal dictionary is the main cause of slow reading speed. " +
					"To resolve this, he suggests that people should rememeber as many new words as possible in their daily reading." +
					" To do so, I code this App to help me remember words.</p><p><b>The challenge.</b>When typing this paragraph on my laptop, " +
					"I have added approximately 300 words in my wordget.I found that reviewing those words became a really hard work. " +
					"So currently I am working on finding a efficient way to manage and review words, " +
					"especailly when the pool's size is larger than 500.</p><p>Please tell me your ideas about words study.<b>Let Us Cling Together</b></p>"));
			tv.setMovementMethod(LinkMovementMethod.getInstance());
		}
					

}
