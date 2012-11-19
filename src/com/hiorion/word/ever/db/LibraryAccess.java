package com.hiorion.word.ever.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.hiorion.word.ever.model.Library;
import com.hiorion.word.ever.model.Word;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LibraryAccess {

	DBHelper dbh;
	SQLiteDatabase sd;
	public LibraryAccess(Context context){
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
	
	public Library GetALibrary(long id){
		Library lib=new Library();
		
		String selection=DBHelper.COLUMN_Library_ID+"= "+id;
		
		Cursor cursor= sd.query(DBHelper.TABLE_Library,null , selection,null, null, null, DBHelper.COLUMN_Library_ID, null);		
		cursor.moveToFirst();
		lib.id=cursor.getLong(0);
		lib.name=cursor.getString(1);
		lib.type=cursor.getInt(2);

		return lib;
	}
	public long Insert(Library lib){
		ContentValues cvs=new ContentValues();
		cvs.put(DBHelper.COLUMN_Library_Name,lib.name);
		cvs.put(DBHelper.COLUMN_Library_Type,lib.type);
		long newid=sd.insert(DBHelper.TABLE_Library, null, cvs);		
		return newid;
	}
	
	public Cursor Query_LibraryList(int id){
		String[] columns={DBHelper.COLUMN_Library_Name,DBHelper.COLUMN_Library_Type,DBHelper.COLUMN_Library_ID};
		String selection="";
		String[] args=new String[1];
		if(id>0){
			selection=" where "+DBHelper.COLUMN_Library_ID+"= "+id;
			args[0]=Integer.toString(id);
		}
		else
			args=null;	
		Cursor cursor= sd.query(DBHelper.TABLE_Library,columns , selection, args, null, null, DBHelper.COLUMN_Library_ID, null);
		cursor.moveToFirst();
		return cursor;
	}
}
