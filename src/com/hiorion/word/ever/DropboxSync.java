/*
 * Copyright (c) 2011 Dropbox, Inc.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.hiorion.word.ever;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.hiorion.word.ever.db.DBSyncHelper;
import com.markupartist.android.widget.ActionBar;

/**
 * Here we show uploading a file in a background thread, trying to show typical
 * exception handling and flow of control for an app that uploads a file from
 * Dropbox.
 */
public class DropboxSync extends AsyncTask<Void, Long, Boolean> {

	private DropboxAPI<?> mApi;

	private long mFileLen;
	private UploadRequest mRequest;
	private Context mContext;

	private String mErrorMsg;

	/*
	 * public DropboxSync(Context context, DropboxAPI<?> api, String path,File
	 * file) { // We set the context this way so we don't accidentally leak
	 * activities mContext = context.getApplicationContext();
	 * 
	 * mFileLen = file.length(); mApi = api;
	 * 
	 * mDialog = new ProgressDialog(context); mDialog.setMax(100);
	 * mDialog.setMessage("Uploading " + file.getName());
	 * mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	 * mDialog.setProgress(0); mDialog.setButton("Cancel", new OnClickListener()
	 * { public void onClick(DialogInterface dialog, int which) { // This will
	 * cancel the putFile operation mRequest.abort(); } }); mDialog.show(); }
	 */
	public DropboxSync(Context context, DropboxAPI<?> api) {
		// We set the context this way so we don't accidentally leak activities
		mContext = context;
		mApi = api;

	}

	private boolean validateLock(Date CSTmod) {
		Calendar caldrop = Calendar.getInstance();
		caldrop.setTime(CSTmod);

		Calendar calnow = Calendar.getInstance();
		TimeZone fromTimeZone = calnow.getTimeZone();
		TimeZone toTimeZone = TimeZone.getTimeZone("GMT");

		calnow.setTimeZone(fromTimeZone);
		calnow.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);
		if (fromTimeZone.inDaylightTime(calnow.getTime())) {
			calnow.add(Calendar.MILLISECOND, calnow.getTimeZone()
					.getDSTSavings() * -1);
		}

		calnow.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());
		if (toTimeZone.inDaylightTime(calnow.getTime())) {
			calnow.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
		}

		// check if the lock file is still in its life time
		if ((calnow.getTimeInMillis() - caldrop.getTimeInMillis()) > 15 * 60 * 1000) {
			return false;
		}
		return true;
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean firsttime = false;
		DBSyncHelper dbsh = new DBSyncHelper(mContext);
		boolean islock = false;
		try {

			List<Entry> lockfiles = mApi.search("/data", "lock", 1, false);
			if (lockfiles.size() > 0) {
				// there is another user is syncing the data.
				Log.e(Keys.LogTag, "lock folder is detected.");
				// Wed, 05 Dec 2012 09:07:03 +0000
				Log.e(Keys.LogTag, lockfiles.get(0).modified);
				SimpleDateFormat sdf = new SimpleDateFormat(
						"E, dd MMM yyyy hh:mm:ss");
				Date CSTmod = sdf.parse(lockfiles.get(0).modified);

				// check if the lock file is still in its life time
				if (validateLock(CSTmod)) {
					Log.e(Keys.LogTag, "lock is validated");
					islock = true;

				} else {
					Log.e(Keys.LogTag, "remove out-of-time lock");
					unlock();
					// recreate lock
					Log.e(Keys.LogTag, "recreate lock");
					Entry lock = mApi.createFolder(Keys.FolderDropboxLock);
				}
			} else {
				Log.e(Keys.LogTag, "lock DropBox...");
				// create lock file
				Entry lock = mApi.createFolder(Keys.FolderDropboxLock);
				Log.e(Keys.LogTag, lock.rev);
			}
		} catch (DropboxException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (!islock) {

			try {
				// **check if this is the first time

				// check local folder
				File local = new File(Keys.FolderLocalApp);
				if (!local.exists())
					local.mkdir();
				local = new File(Keys.FolderLocalAppData);
				if (!local.exists())
					local.mkdir();
				local = new File(Keys.DBPath_Local);
				if (!local.exists())
					local.createNewFile();
				Log.e(Keys.LogTag, "check Dropbox file");
				// check dropbox folder
				File file = new File(Keys.DBPath_Local);
				FileOutputStream outputStream = new FileOutputStream(file);
				mApi.getFile(Keys.DBPath_DropBox, null, outputStream, null);
			} catch (FileNotFoundException e) {
				firsttime = true;
				e.printStackTrace();
				Log.e(Keys.LogTag,
						"that means this is the first time sync, or the file on dropbox has been deleted.");
			} catch (IOException e) {
				firsttime = true;
				e.printStackTrace();
				Log.e(Keys.LogTag,
						"that means this is the first time sync, or the file on dropbox has been deleted.");
			} catch (DropboxException e) {
				firsttime = true;
				e.printStackTrace();
				Log.e(Keys.LogTag,
						"that means this is the first time sync, or the file on dropbox has been deleted.");
			}

			if (firsttime) {
				Log.e(Keys.LogTag, "first time");
				// **2.copy sys to local
				Log.e(Keys.LogTag, "copy sys to local");
				File DBlocal = new File(Keys.DBPath_Local);
				File DBsys = new File(Keys.DBPath_Sys);
				if (copyDB(DBsys, DBlocal)) {
					// **3.upload local to DropBox
					if (upload()) {
						unlock();
						return true;
					}
				} else {
					Log.e(Keys.LogTag, "copy db failed");
				}
			} else {
				// not the first time
				if (dbsh.ValidateData()) {
					Log.e(Keys.LogTag, "not the first time");
					// **1.download db from Dropbox-skipped, already got the
					// latest db when doing firsttime-check
					// **2.sync changed rows with sys db.
					Log.e(Keys.LogTag, "accept changes");
					dbsh.ApplyChangesFromBoxSlow();
					// **3.copy sys db to local db
					Log.e(Keys.LogTag, "copy sys db to local db");
					File DBlocal = new File(Keys.DBPath_Local);
					File DBsys = new File(Keys.DBPath_Sys);
					if (copyDB(DBsys, DBlocal)) {
						Log.e(Keys.LogTag, "upload");
						// **4.upload local to DropBox
						if (upload()) {
							unlock();
							return true;
						}

					} else {
						Log.e(Keys.LogTag, "copy db failed");
					}

				} else {
					Log.e(Keys.LogTag, "Data lost, recoverd from Box");
					dbsh.CopyAllFromBox();
					unlock();
					return true;
				}
			}
			unlock();
		} else {
			mErrorMsg = mContext.getString(R.string.popup_sync_inuse);
		}

		return false;
	}

	private boolean copyDB(File source, File target) {
		try {
			Log.e(Keys.LogTag, "db copy start");
			FileInputStream inStream = new FileInputStream(source);
			FileOutputStream outStream = new FileOutputStream(target);
			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			Log.e(Keys.LogTag, "db copy finished");
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void unlock() {
		Log.e(Keys.LogTag, "unlock DropBox...");
		try {
			mApi.delete(Keys.FolderDropboxLock);
		} catch (DropboxException e) {
			e.printStackTrace();
		}
	}

	private boolean upload() {
		Log.e(Keys.LogTag, "upload start");
		try {
			// By creating a request, we get a handle to the putFile operation,
			// so we can cancel it later if we want to
			File DBlocal = new File(Keys.DBPath_Local);
			FileInputStream fis = new FileInputStream(DBlocal);
			mRequest = mApi.putFileOverwriteRequest(Keys.DBPath_DropBox, fis,
					DBlocal.length(), new ProgressListener() {
						@Override
						public long progressInterval() {
							// Update the progress bar every half-second or so
							return 500;
						}

						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes);
						}
					});

			if (mRequest != null) {
				Entry entry = mRequest.upload();
				Log.e(Keys.LogTag, "upload finished");
				return true;
			}

		} catch (DropboxUnlinkedException e) {
			// This session wasn't authenticated properly or user unlinked
			mErrorMsg = mContext.getString(R.string.popup_sync_authfailed);
		} catch (DropboxFileSizeException e) {
			// File size too big to upload via the API
			mErrorMsg = mContext.getString(R.string.popup_sync_toobig);
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = mContext.getString(R.string.popup_sync_cancel);
		} catch (DropboxServerException e) {
			// Server-side exception. These are examples of what could happen,
			// but we don't do anything special with them here.
			if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				// Unauthorized, so we should unlink them. You may want to
				// automatically log the user out in this case.
				mApi.getSession().unlink();
				SharedPreferences prefs = mContext.getSharedPreferences(
						Keys.ACCOUNT_PREFS_NAME, 0);
				Editor edit = prefs.edit();
				edit.clear();
				edit.commit();
				mErrorMsg = mContext
						.getString(R.string.popup_sync_userauthfailed);
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				// Not allowed to access this
				mErrorMsg = mContext.getString(R.string.popup_sync_forbidden);
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				// path not found (or if it was the thumbnail, can't be
				// thumbnailed)
				mErrorMsg = mContext.getString(R.string.popup_sync_forbidden);
			} else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
				// user is over quota
				mErrorMsg = mContext.getString(R.string.popup_sync_forbidden);
			} else {
				// Something else
				mErrorMsg = mContext.getString(R.string.popup_sync_unknown);
			}
			// This gets the Dropbox error, translated into the user's language
			mErrorMsg = e.body.userError;
			if (mErrorMsg == null) {
				mErrorMsg = e.body.error;
			}
		} catch (DropboxIOException e) {
			// Happens all the time, probably want to retry automatically.
			mErrorMsg = mContext.getString(R.string.popup_sync_networkerror);
		} catch (DropboxParseException e) {
			// Probably due to Dropbox server restarting, should retry
			mErrorMsg = mContext.getString(R.string.popup_sync_dropboxerror);
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = mContext.getString(R.string.popup_sync_unknown);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * @Override protected void onProgressUpdate(Long... progress) { int percent
	 * = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
	 * mDialog.setProgress(percent); }
	 */

	@Override
	protected void onPostExecute(Boolean result) {

		HelloWorldActivity act = (HelloWorldActivity) mContext;
		act.FillList();
		ActionBar actionBar = (ActionBar) act.findViewById(R.id.actionbar);
		actionBar.syncMode(false);
		// mDialog.dismiss();
		if (result) {
			showToast(mContext.getString(R.string.popup_sync_ok));						
			actionBar.setTitle(R.string.actionbar_title_synccomplete);
			
		} else {
			showToast(mErrorMsg);						
			actionBar.setTitle(mContext
					.getString(R.string.actionbar_title_syncstop));
			actionBar.syncMode(false);
		}
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		error.show();
	}
}
