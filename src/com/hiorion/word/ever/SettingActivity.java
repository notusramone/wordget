package com.hiorion.word.ever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;
import com.markupartist.android.widget.ActionBar;

public class SettingActivity extends Activity {

	// Dropbox settings
	/*
	 * private static final String TAG = "WordFavour"; final static private
	 * String APP_KEY = "4k7y212qbbo5epi"; final static private String
	 * APP_SECRET = "wdx1yahm9u7dhi8"; final static private AccessType
	 * ACCESS_TYPE = AccessType.APP_FOLDER; final static private String
	 * ACCOUNT_PREFS_NAME = "wordever_dropbox_prefs"; final static private
	 * String ACCESS_KEY_NAME = "ACCESS_KEY"; final static private String
	 * ACCESS_SECRET_NAME = "ACCESS_SECRET";
	 */
	DropboxAPI<AndroidAuthSession> mApi;
	private boolean mLoggedIn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// We create a new AuthSession so that we can use the Dropbox API.
		AndroidAuthSession session = buildSession();
		mApi = new DropboxAPI<AndroidAuthSession>(session);

		setContentView(R.layout.activity_setting);
		setLoggedIn(mApi.getSession().isLinked());

		final ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.actionbar_title_setting);

	}

	@Override
	protected void onResume() {
		super.onResume();
		AndroidAuthSession session = mApi.getSession();

		// The next part must be inserted in the onResume() method of the
		// activity from which session.startAuthentication() was called, so
		// that Dropbox authentication completes properly.
		if (session.authenticationSuccessful()) {
			try {
				// Mandatory call to complete the auth
				session.finishAuthentication();

				// Store it locally in our app for later use
				TokenPair tokens = session.getAccessTokenPair();
				storeKeys(tokens.key, tokens.secret);
				setLoggedIn(true);
			} catch (IllegalStateException e) {
				showToast(e.getLocalizedMessage());
				Log.i(Keys.LogTag, "Error authenticating", e);
			}
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
		error.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_setting, menu);
		return true;
	}

	public void Setting_Dropbox_click(View view) {
		// This logs you out if you're logged in, or vice versa
		if (mLoggedIn) {
			logOut();
		} else {
			// Start the remote authentication
			mApi.getSession().startAuthentication(this);
		}
	}

	private void logOut() {
		// Remove credentials from the session
		mApi.getSession().unlink();
		// Clear our stored keys
		clearKeys();
		// Change UI state to display logged out version
		setLoggedIn(false);
	}

	private void setLoggedIn(boolean loggedIn) {
		mLoggedIn = loggedIn;
		CheckBox cb = (CheckBox) findViewById(R.id.setting_cb_dropbox);
		if (loggedIn) {
			cb.setChecked(true);

		} else {
			cb.setChecked(false);

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

	private void storeKeys(String key, String secret) {
		// Save the access key for later
		SharedPreferences prefs = getSharedPreferences(Keys.ACCOUNT_PREFS_NAME,
				0);
		Editor edit = prefs.edit();
		edit.putString(Keys.ACCESS_KEY_NAME, key);
		edit.putString(Keys.ACCESS_SECRET_NAME, secret);
		edit.commit();
	}

	private void clearKeys() {
		SharedPreferences prefs = getSharedPreferences(Keys.ACCOUNT_PREFS_NAME,
				0);
		Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}

	public void sendEmail(View view) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "notusramone@gmail.com" });
		i.putExtra(Intent.EXTRA_SUBJECT, "Bug Report & Suggestion");
		i.putExtra(Intent.EXTRA_TEXT, "Hey... ");
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, this.getString(R.string.popup_email_noclient),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void about(View view) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	
	public void website(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://hiorion.com"));
		startActivity(browserIntent);
	}
	
	public void preparespeech(View view) {
		Intent intent = new Intent(this, SpeechActivity.class);
		startActivity(intent);
	}

}
