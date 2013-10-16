package com.hiorion.word.ever.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.hiorion.word.ever.Keys;
import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LibraryAccess {

	DBHelper dbh;
	SQLiteDatabase sd;

	public LibraryAccess(Context context) {
		dbh = new DBHelper(context);
	}

	public void OpenConnectionForWriting() {
		sd = dbh.getWritableDatabase();
	}

	public void OpenConnectionForRead() {
		sd = dbh.getReadableDatabase();
	}

	public void Close() {
		dbh.close();
	}

	public Library GetALibrary(long id) {
		Library lib = new Library();

		String selection = DBHelper.COLUMN_Library_ID + "= " + id + " and "
				+ DBHelper.COLUMN_Library_Tag + ">0";

		Cursor cursor = sd.query(DBHelper.TABLE_Library, null, selection, null,
				null, null, DBHelper.COLUMN_Library_ID, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			lib.id = cursor.getLong(0);
			lib.name = cursor.getString(1);
			lib.uuid = cursor.getString(4);
		} else {
			lib.id = 0;
			lib.name = "Not Set";
		}
		cursor.close();
		return lib;
	}

	public Library GetALibrary(String uuid) {
		Library lib = new Library();

		String selection = DBHelper.COLUMN_Library_UUID + "= '" + uuid
				+ "' and " + DBHelper.COLUMN_Library_Tag + ">0";

		Cursor cursor = sd.query(DBHelper.TABLE_Library, null, selection, null,
				null, null, DBHelper.COLUMN_Library_ID, null);
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			lib.id = cursor.getLong(0);
			lib.name = cursor.getString(1);
			lib.uuid = cursor.getString(4);
		} else {
			lib.id = 0;
			lib.name = "Not Set";
		}
		cursor.close();
		return lib;
	}

	public long Insert(Library lib) {
		ContentValues cvs = new ContentValues();
		cvs.put(DBHelper.COLUMN_Library_Name, lib.name);
		// timestamp
		Calendar now = Calendar.getInstance();
		cvs.put(DBHelper.COLUMN_Library_TimeStamp, now.getTimeInMillis());
		cvs.put(DBHelper.COLUMN_Library_Tag, Keys.DataTag_Regular);
		cvs.put(DBHelper.COLUMN_Library_UUID, java.util.UUID.randomUUID()
				.toString());
		long newid = sd.insert(DBHelper.TABLE_Library, null, cvs);
		return newid;
	}

	public Cursor Query_LibraryList() {
		String[] columns = { DBHelper.COLUMN_Library_Name,
				DBHelper.COLUMN_Library_ID, DBHelper.COLUMN_Library_UUID };
		String selection = DBHelper.COLUMN_Library_Tag + ">0 ";

		Cursor cursor = sd.query(DBHelper.TABLE_Library, columns, selection,
				null, null, null, DBHelper.COLUMN_Library_ID, null);
		cursor.moveToFirst();
		return cursor;
	}

	public boolean Delete(long id) {
		// String
		// sql="update "+DBHelper.TABLE_Word+" set "+DBHelper.COLUMN_Word_Library_ID+"=0 where "+DBHelper.COLUMN_Word_Library_ID+"="+id;
		// sd.execSQL(sql);
		String uuid = this.GetALibrary(id).uuid;
		String sql = "select * from word where tag>0 and library_uuid='" + uuid
				+ "'";
		Cursor cursor = sd.rawQuery(sql, null);
		if (cursor.getCount() == 0) {
			// sd.delete(DBHelper.TABLE_Library,
			// DBHelper.COLUMN_Library_ID+" = "+id,null);
			ContentValues cvs = new ContentValues();
			cvs.put(DBHelper.COLUMN_Library_Tag, Keys.DataTag_Deleted);
			Calendar now = Calendar.getInstance();
			cvs.put(DBHelper.COLUMN_Library_TimeStamp, now.getTimeInMillis());
			String condition = DBHelper.COLUMN_Library_ID + " = " + id;
			sd.update(DBHelper.TABLE_Library, cvs, condition, null);
			return true;
		}
		return false;

	}
}
