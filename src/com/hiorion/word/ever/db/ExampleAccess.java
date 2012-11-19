package com.hiorion.word.ever.db;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiorion.word.ever.model.Example;

public class ExampleAccess {

	DBHelper dbh;
	SQLiteDatabase sd;
	public ExampleAccess(Context context){
		dbh=new DBHelper(context);
	}
	
	public void OpenConnectionForWriting(){
		sd=dbh.getWritableDatabase();
	}
	
	public void OpenConnectionForRead(){
		sd=dbh.getReadableDatabase();
	}
	
	public void Close(){
		dbh.close();
	}
	
	
	public ArrayList<Example> Query_ExampleList(long id){
		String[] args=new String[]{Long.toString(id)};
		String sql="select * from "+DBHelper.TABLE_Example+" where "+DBHelper.COLUMN_Example_ID+" in (select "
		+DBHelper.COLUMN_Word_Example_Example_ID+" from "+DBHelper.TABLE_Word_Example+" where "+DBHelper.COLUMN_Word_Example_Word_ID+"=?)";
		Cursor cursor = sd.rawQuery(sql, args);
		ArrayList<Example> exlist=new ArrayList<Example>();
		if(cursor.moveToFirst()){
			while(cursor.moveToNext()){
				Example ex=new Example();
				ex.id=cursor.getLong(0);
				ex.sentence=cursor.getString(1);
				exlist.add(ex);
			}
				
		}
		return exlist;
	}
}
