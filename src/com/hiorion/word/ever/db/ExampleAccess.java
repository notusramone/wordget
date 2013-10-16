package com.hiorion.word.ever.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiorion.word.ever.model.Example;

public class ExampleAccess {

	DBHelper dbh;
	SQLiteDatabase sd;

	public ExampleAccess(Context context) {
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

	public ArrayList<Example> Query_ExampleList(String worduuid) {
		/*
		 * String[] args=new String[]{Long.toString(id)}; String
		 * sql="select * from "
		 * +DBHelper.TABLE_Example+" where "+DBHelper.COLUMN_Example_ID
		 * +" in (select "
		 * +DBHelper.COLUMN_Word_Example_Example_ID+" from "+DBHelper
		 * .TABLE_Word_Example
		 * +" where "+DBHelper.COLUMN_Word_Example_Word_ID+"=?)"; Cursor cursor
		 * = sd.rawQuery(sql, args);
		 */
		String[] columns = { DBHelper.COLUMN_Example_ID,
				DBHelper.COLUMN_Example_Word_UUID,
				DBHelper.COLUMN_Example_Sentence, DBHelper.COLUMN_Example_Type };
		String selection = DBHelper.COLUMN_Example_Tag + ">0 ";
		if (worduuid.length() > 0) {
			selection += " and " + DBHelper.COLUMN_Example_Word_UUID + "= '"
					+ worduuid + "'";
		}

		Cursor cursor = sd.query(DBHelper.TABLE_Example, columns, selection,
				null, null, null, DBHelper.COLUMN_Example_ID, null);
		ArrayList<Example> exlist = new ArrayList<Example>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Example ex = new Example();
			ex.id = cursor.getLong(0);
			ex.word_uuid = cursor.getString(1);
			ex.sentence = cursor.getString(2);
			ex.type = cursor.getInt(3);
			exlist.add(ex);

		}
		cursor.close();
		return exlist;
	}
}
