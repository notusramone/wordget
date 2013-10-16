package com.hiorion.word.ever;

import com.markupartist.android.widget.ActionBar;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.Window;
import android.widget.TextView;

public class SpeechActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_speech);
		
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
		TextView tv=(TextView) findViewById(R.id.speech_text);
		tv.setText(Html.fromHtml("<p> <b> Word Navigation</b><br/>When you are on the word viewing page," +
				" <b>click</b> the word on the top will go to the next word.<br/>" +
				"<b>swipe</b> on the word from left to right will also present you the next word,<br/>" +
				"to check the previous one, just <b>swipe </b>on the word from right to left</p>" +
				"<p><b>How to prepare the speech engine:</b> <br/>"
				+"<a href=\"http://hiorion.com/?p=30\">click me to read detailed article</a><br/><br/>"
+"1. Download speech resource \"Longman 2005 Voice Package - American English\" from  <a href=\"http://www.lingoes.net/en/translator/speech.htm\">http://www.lingoes.net/en/translator/speech.htm</a><br/>"
+"2. Unzip the zip file to your SD card.<br/>"
+"3. The path of the speech resource should look like that: <br/><br/> SDcard/wordget/speech/A/about.mp3<br/><br/>"
+"Tip: <br/> How to copy the speech files to your SD card."
+"Because there are thousands of mp3 files, directly copying the files to your SD card could be a pailful process. " 
+"I did this by uploading the files to my phone through wifi. " 
+" To do this, first you have to install a FTP App on your phone, then enable the FTP function and upload the mp3 files to the correct folder" 
+" of your SD card.</p>"
    	
    	+"<p> <b>How to update phonetic:</b><br/><br/>"
    	+"<a href=\"http://hiorion.com/?p=32\">click me to read detailed article</a><br/><br/>"
    	+"(Brfore we start: I should develop a Phonetic keyboard so that we can edit phonetic on our lovely androids. But before that, please use this FFX add-on)<br/>"
    	+"1. <a href=\"http://hiorion.com/?p=32\">Download the add-on from my website </a><br/>"
    	+"2. Go to your Dropbox folder, copy the word data file to Desktop. The path should be: <br/><br/>"
    	+"Dropbox Folder/Apps/Wordget/data/do_not_move_or_delete_or_edit/wordget.db <br/> <br/>"
    	+"3. Launch Firefox, you should find wordget button on the menu bar.<br/>"
    	+"4. Click Load libraries button in the addon window, all the libraries will filled in the window.<br/>"
    	+"5. Happy edit each word phonetic <br/>"
    	+"6. (Important!!)After editing, click \"Update Phonetic\" <br/>"
    	+"7. Copy the data file back to the Dropbox folder.<br/>"
    	+"8. Sync the word on your phone. </p>"
    	
    	+"<p> <b> Typing on Androids.</b> <br/><br/>"
    	+"<a href=\"http://hiorion.com/?p=34\">click me to read detailed article</a><br/><br/>"
    	+"Typing could be painful, especially when your phones screen is not big. If you feel bad about this, I personal recommend SwiftKey keyboard. <br/>"
    	+"It is just awesome :) I have purchased it, but now I am usng Swiftkey Flow, a free beta app. By using Flow, typing becomes much faster! <br/>"
    	+"Well this is not an advertisement. Try the free beta or trial version, you will fall in love with it."
    	+"</p>"));
		tv.setMovementMethod(LinkMovementMethod.getInstance());
	}
				

}
