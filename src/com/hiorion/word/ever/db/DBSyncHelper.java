package com.hiorion.word.ever.db;

import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hiorion.word.ever.Keys;

public class DBSyncHelper {

	private static final String COLUMN_Word_SyncTag = null;
	Context context;

	public DBSyncHelper(Context _context) {
		context = _context;
	}

	public boolean ValidateData() {
		DBHelper dbh = new DBHelper(context);
		SQLiteDatabase sd_sys = dbh.getWritableDatabase();
		Cursor cursor = null;
		try {
			String sql = "select * from " + DBHelper.TABLE_Word;
			cursor = sd_sys.rawQuery(sql, null);
			if (cursor.moveToFirst()) {
				/*
				 * cursor.close(); sd_sys.close();
				 */
				return true;
			}
		} catch (Exception e) {
		} finally {
			cursor.close();
			sd_sys.close();
		}
		return false;
	}

	public void CopyAllFromBox() {
		Log.e(Keys.LogTag, "start recovery");
		// local
		DBUtilForSync dbu = new DBUtilForSync(context);
		dbu.openReadableDataBase();
		SQLiteDatabase sd_local = dbu.getDb();
		// sys
		DBHelper dbh = new DBHelper(context);
		SQLiteDatabase sd_sys = dbh.getWritableDatabase();

		try {
			// word
			String sql = "select * from " + DBHelper.TABLE_Word;
			Cursor c_local = sd_local.rawQuery(sql, null);
			for (c_local.moveToFirst(); !c_local.isAfterLast(); c_local
					.moveToNext()) {
				ContentValues cvs = new ContentValues();
				cvs.put(DBHelper.COLUMN_Word_Word, c_local.getString(1));
				cvs.put(DBHelper.COLUMN_Word_Library_UUID, c_local.getString(2));
				cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto,
						c_local.getString(3));
				cvs.put(DBHelper.COLUMN_Word_Date, c_local.getString(4));
				cvs.put(DBHelper.COLUMN_Word_Year, c_local.getInt(5));
				cvs.put(DBHelper.COLUMN_Word_Month, c_local.getInt(6));
				cvs.put(DBHelper.COLUMN_Word_Week, c_local.getInt(7));
				cvs.put(DBHelper.COLUMN_Word_Day, c_local.getInt(8));
				cvs.put(DBHelper.COLUMN_Word_Part, c_local.getInt(9));
				cvs.put(DBHelper.COLUMN_Word_ExtraInfo, c_local.getString(10));
				cvs.put(DBHelper.COLUMN_Word_Meaning, c_local.getString(11));
				cvs.put(DBHelper.COLUMN_Word_TimeStamp, c_local.getLong(12));
				cvs.put(DBHelper.COLUMN_Word_Tag, c_local.getLong(13));
				cvs.put(DBHelper.COLUMN_Word_UUID, c_local.getString(14));
				//#TODO phonetic change
				cvs.put(DBHelper.COLUMN_Word_Phonetic, c_local.getString(15));

				sd_sys.insert(DBHelper.TABLE_Word, null, cvs);
			}
			c_local.close();
			// Example
			sql = "select * from " + DBHelper.TABLE_Example;
			Cursor c_local_ex = sd_local.rawQuery(sql, null);
			for (c_local_ex.moveToFirst(); !c_local_ex.isAfterLast(); c_local_ex
					.moveToNext()) {
				ContentValues cvs = new ContentValues();
				cvs.put(DBHelper.COLUMN_Example_Word_UUID,
						c_local_ex.getString(1));
				cvs.put(DBHelper.COLUMN_Example_Type, c_local_ex.getInt(2));
				cvs.put(DBHelper.COLUMN_Example_Sentence,
						c_local_ex.getString(3));
				cvs.put(DBHelper.COLUMN_Example_TimeStamp,
						c_local_ex.getLong(4));
				cvs.put(DBHelper.COLUMN_Example_Tag, c_local_ex.getLong(5));
				cvs.put(DBHelper.COLUMN_Example_UUID, c_local_ex.getString(6));

				sd_sys.insert(DBHelper.TABLE_Example, null, cvs);
			}
			c_local_ex.close();
			// Library

			sql = "select * from " + DBHelper.TABLE_Library;
			Cursor c_local_lib = sd_local.rawQuery(sql, null);
			for (c_local_lib.moveToFirst(); !c_local_lib.isAfterLast(); c_local_lib
					.moveToNext()) {
				ContentValues cvs = new ContentValues();
				cvs.put(DBHelper.COLUMN_Library_Name, c_local_lib.getString(1));
				cvs.put(DBHelper.COLUMN_Library_TimeStamp,
						c_local_lib.getLong(2));
				cvs.put(DBHelper.COLUMN_Library_Tag, c_local_lib.getLong(3));
				cvs.put(DBHelper.COLUMN_Example_UUID, c_local_lib.getString(4));

				sd_sys.insert(DBHelper.TABLE_Library, null, cvs);
			}
			c_local_lib.close();
			Log.e(Keys.LogTag, "recovery finished");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sd_sys.close();
			sd_local.close();
		}

	}

	public void ApplyChangesFromBoxSlow() {
		// local
		DBUtilForSync dbu = new DBUtilForSync(context);
		dbu.openReadableDataBase();
		SQLiteDatabase sd_local = dbu.getDb();
		// sys
		DBHelper dbh = new DBHelper(context);
		SQLiteDatabase sd_sys = dbh.getWritableDatabase();

		try {
			Log.e(Keys.LogTag, "words checking");
			// word
			// get all local rows
			String sql = "select * from " + DBHelper.TABLE_Word;
			Cursor c_local = sd_local.rawQuery(sql, null);
			// get all sys rows
			sql = "select " + DBHelper.COLUMN_Word_TimeStamp + ", "
					+ DBHelper.COLUMN_Word_UUID + " from "
					+ DBHelper.TABLE_Word;
			Cursor c_sys = sd_sys.rawQuery(sql, null);
			// put sys rows into Hashtable
			Hashtable<String, Long> sys_rows = new Hashtable<String, Long>();
			for (c_sys.moveToFirst(); !c_sys.isAfterLast(); c_sys.moveToNext()) {
				sys_rows.put(c_sys.getString(1), c_sys.getLong(0));
			}
			c_sys.close();
			// validate every row, compare timestamp with sys
			for (c_local.moveToFirst(); !c_local.isAfterLast(); c_local
					.moveToNext()) {
				long ts_local = c_local.getLong(12);
				String uuid_local = c_local.getString(14);
				// Object ts_sys_o=sys_rows.get(uuid_local);
				if (sys_rows.get(uuid_local) != null) {
					long ts_sys = sys_rows.get(uuid_local);
					ContentValues cvs = new ContentValues();
					if (ts_sys < ts_local) {
						// DropBox has latest version, update sys
						cvs.put(DBHelper.COLUMN_Word_Word, c_local.getString(1));
						cvs.put(DBHelper.COLUMN_Word_Library_UUID,
								c_local.getString(2));
						cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto,
								c_local.getString(3));
						cvs.put(DBHelper.COLUMN_Word_Part, c_local.getInt(9));
						cvs.put(DBHelper.COLUMN_Word_ExtraInfo,
								c_local.getString(10));
						cvs.put(DBHelper.COLUMN_Word_Meaning,
								c_local.getString(11));
						cvs.put(DBHelper.COLUMN_Word_TimeStamp, ts_local);
						cvs.put(DBHelper.COLUMN_Word_Tag, c_local.getInt(13));

					}
					else{
						//#TODO only copy phonetic
						cvs.put(DBHelper.COLUMN_Word_Phonetic, c_local.getString(15));
					}
					//update
					String condition = DBHelper.COLUMN_Word_UUID + " = '"
							+ uuid_local + "'";

					sd_sys.update(DBHelper.TABLE_Word, cvs, condition, null);
					
				} else {
					// this word from DropBox doesn't exsit in sys DB, insert it
					ContentValues cvs = new ContentValues();
					cvs.put(DBHelper.COLUMN_Word_Word, c_local.getString(1));
					cvs.put(DBHelper.COLUMN_Word_Library_UUID,
							c_local.getString(2));
					cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto,
							c_local.getString(3));
					cvs.put(DBHelper.COLUMN_Word_Date, c_local.getString(4));
					cvs.put(DBHelper.COLUMN_Word_Year, c_local.getInt(5));
					cvs.put(DBHelper.COLUMN_Word_Month, c_local.getInt(6));
					cvs.put(DBHelper.COLUMN_Word_Week, c_local.getInt(7));
					cvs.put(DBHelper.COLUMN_Word_Day, c_local.getInt(8));
					cvs.put(DBHelper.COLUMN_Word_Part, c_local.getInt(9));
					cvs.put(DBHelper.COLUMN_Word_ExtraInfo,
							c_local.getString(10));
					cvs.put(DBHelper.COLUMN_Word_Meaning, c_local.getString(11));
					cvs.put(DBHelper.COLUMN_Word_TimeStamp, ts_local);
					cvs.put(DBHelper.COLUMN_Word_Tag, c_local.getInt(13));
					cvs.put(DBHelper.COLUMN_Word_UUID, uuid_local);
					//#TODO phonetic change
					cvs.put(DBHelper.COLUMN_Word_Phonetic, c_local.getString(15));

					sd_sys.insert(DBHelper.TABLE_Word, null, cvs);
				}

			}
			c_local.close();
			Log.e(Keys.LogTag, "examples checking");
			// Example
			// get all changed rows
			sql = "select * from " + DBHelper.TABLE_Example;
			Cursor c_local_ex = sd_local.rawQuery(sql, null);
			// get all sys rows
			sql = "select " + DBHelper.COLUMN_Example_TimeStamp + ", "
					+ DBHelper.COLUMN_Example_UUID + " from "
					+ DBHelper.TABLE_Example;
			Cursor c_sys_ex = sd_sys.rawQuery(sql, null);
			// put sys rows into Hashtable
			Hashtable<String, Long> sys_rows_ex = new Hashtable<String, Long>();
			for (c_sys_ex.moveToFirst(); !c_sys_ex.isAfterLast(); c_sys_ex
					.moveToNext()) {
				sys_rows_ex.put(c_sys_ex.getString(1), c_sys_ex.getLong(0));
			}
			c_sys_ex.close();
			// validate every row, compare timestamp with sys
			for (c_local_ex.moveToFirst(); !c_local_ex.isAfterLast(); c_local_ex
					.moveToNext()) {
				long ts_local = c_local_ex.getLong(4);
				String uuid_local = c_local_ex.getString(6);
				if (sys_rows_ex.get(uuid_local) != null) {
					long ts_sys = sys_rows_ex.get(uuid_local);
					if (ts_sys < ts_local) {
						// DropBox has latest version, update sys
						ContentValues cvs = new ContentValues();
						cvs.put(DBHelper.COLUMN_Example_Word_UUID,
								c_local_ex.getString(1));
						cvs.put(DBHelper.COLUMN_Example_Type,
								c_local_ex.getInt(2));
						cvs.put(DBHelper.COLUMN_Example_Sentence,
								c_local_ex.getString(3));
						cvs.put(DBHelper.COLUMN_Example_TimeStamp, ts_local);
						cvs.put(DBHelper.COLUMN_Example_Tag,
								c_local_ex.getInt(5));

						String condition = DBHelper.COLUMN_Example_UUID
								+ " = '" + uuid_local + "'";

						sd_sys.update(DBHelper.TABLE_Example, cvs, condition,
								null);
					}
				} else {
					// this word from DropBox doesn't exsit in sys DB, insert it
					ContentValues cvs = new ContentValues();
					cvs.put(DBHelper.COLUMN_Example_Word_UUID,
							c_local_ex.getString(1));
					cvs.put(DBHelper.COLUMN_Example_Type, c_local_ex.getInt(2));
					cvs.put(DBHelper.COLUMN_Example_Sentence,
							c_local_ex.getString(3));
					cvs.put(DBHelper.COLUMN_Example_TimeStamp, ts_local);
					cvs.put(DBHelper.COLUMN_Example_Tag, c_local_ex.getInt(5));
					cvs.put(DBHelper.COLUMN_Example_UUID, uuid_local);

					sd_sys.insert(DBHelper.TABLE_Example, null, cvs);
				}

			}
			c_local_ex.close();
			Log.e(Keys.LogTag, "libraries checking");
			// Library
			// get all changed rows
			sql = "select * from " + DBHelper.TABLE_Library;
			Cursor c_local_lib = sd_local.rawQuery(sql, null);
			// get all sys rows
			sql = "select " + DBHelper.COLUMN_Library_TimeStamp + ", "
					+ DBHelper.COLUMN_Library_UUID + " from "
					+ DBHelper.TABLE_Library;
			Cursor c_sys_lib = sd_sys.rawQuery(sql, null);
			// put sys rows into Hashtable
			Hashtable<String, Long> sys_rows_lib = new Hashtable<String, Long>();
			for (c_sys_lib.moveToFirst(); !c_sys_lib.isAfterLast(); c_sys_lib
					.moveToNext()) {
				sys_rows_lib.put(c_sys_lib.getString(1), c_sys_lib.getLong(0));
			}
			c_sys_lib.close();
			// validate every row, compare timestamp with sys
			for (c_local_lib.moveToFirst(); !c_local_lib.isAfterLast(); c_local_lib
					.moveToNext()) {
				long ts_local = c_local_lib.getLong(2);
				String uuid_local = c_local_lib.getString(4);
				if (sys_rows_lib.get(uuid_local) != null) {
					long ts_sys = sys_rows_lib.get(uuid_local);
					if (ts_sys < ts_local) {
						// DropBox has latest version, update sys
						ContentValues cvs = new ContentValues();
						cvs.put(DBHelper.COLUMN_Library_Name,
								c_local_lib.getString(1));
						cvs.put(DBHelper.COLUMN_Library_TimeStamp, ts_local);
						cvs.put(DBHelper.COLUMN_Library_Tag,
								c_local_lib.getInt(3));

						String condition = DBHelper.COLUMN_Library_UUID
								+ " = '" + uuid_local + "'";

						sd_sys.update(DBHelper.TABLE_Library, cvs, condition,
								null);
					}
				} else {
					// this word from DropBox doesn't exsit in sys DB, insert it
					ContentValues cvs = new ContentValues();
					cvs.put(DBHelper.COLUMN_Library_Name,
							c_local_lib.getString(1));
					cvs.put(DBHelper.COLUMN_Library_TimeStamp, ts_local);
					cvs.put(DBHelper.COLUMN_Library_Tag, c_local_lib.getInt(3));
					cvs.put(DBHelper.COLUMN_Library_UUID, uuid_local);

					sd_sys.insert(DBHelper.TABLE_Library, null, cvs);
				}
			}
			c_local_lib.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sd_sys.close();
			sd_local.close();
		}
	}

}
