package com.hiorion.word.ever.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;

import com.hiorion.word.ever.Keys;

/**
 * Standard database utility class.
 * 
 * TODO: Refactor.
 */
public class DBUtilForSync extends SQLiteOpenHelper {

	/**
	 * Database directory.
	 * 
	 * <p>
	 * Example: "/sdcard/myapp/db/"
	 * </p>
	 */
	// public static String DB_DIRECTORY = null;

	/**
	 * Name of the database file.
	 * 
	 * <p>
	 * Example: "mydatabase.db"
	 * </p>
	 * 
	 */
	// public static String DB_NAME = null;

	/**
	 * Full absolute path of the database.
	 * 
	 * <p>
	 * Example: "/sdcard/myapp/db/mydatabase.db"
	 * </p>
	 */
	// public static String DB_FULL_PATH = null;
	// static {
	// DB_DIRECTORY = Keys.DBPath_Local;
	// DB_NAME = "mydatabase.db";
	// DB_FULL_PATH = Keys.DBPath_Local;
	// }

	private SQLiteDatabase myDataBase;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */
	public DBUtilForSync(Context context) {
		super(context, Keys.DBName, null, 1);
		/*
		 * try { this.createDataBase(); } catch (IOException ioe) { throw new
		 * Error("Unable to create database"); }
		 */
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {
		if (!checkDataBase())
			this.getWritableDatabase();
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			checkDB = SQLiteDatabase.openDatabase(Keys.DBPath_Local, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	public void openDataBase(int mode) throws SQLException {
		try {
			myDataBase = SQLiteDatabase.openDatabase(Keys.DBPath_Local, null,
					mode);
		} catch (IllegalStateException e) {
			// Sometimes, esp. after application upgrade, the database will be
			// non-closed, raising a IllegalStateException
			// below. Try to avoid by simply opening it again.
			// Log.d(MyApp.APP, "Database non-closed. Reopening.");
			myDataBase = SQLiteDatabase.openDatabase(Keys.DBPath_Local, null,
					mode);
		}
	}

	public void openReadableDataBase() throws SQLException {
		openDataBase(SQLiteDatabase.OPEN_READONLY);
	}

	public SQLiteDatabase getDb() {
		return myDataBase;
	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}