package com.hiorion.word.ever.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.hiorion.word.ever.model.Word;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class WordAccess {

	DBschema dbh;
	SQLiteDatabase sd;
	public WordAccess(Context context){
		dbh=new DBschema(context);
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
	
	public long Insert(Word word){
		ContentValues cvs=new ContentValues();
		Calendar now=Calendar.getInstance();
		word.date=now.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		word.year=now.get(Calendar.YEAR);
		word.month=now.get(Calendar.MONTH);
		word.week=now.get(Calendar.WEEK_OF_YEAR);
		
		cvs.put(DBschema.COLUMN_Word_Word, word.word);
		//cvs.put(DBschema.COLUMN_Word_Category_ID, word.category_id);
		cvs.put(DBschema.COLUMN_Word_Date,dateFormat.format(word.date));
		cvs.put(DBschema.COLUMN_Word_Week,word.week);
		cvs.put(DBschema.COLUMN_Word_Year,word.year);
		cvs.put(DBschema.COLUMN_Word_Month,word.month);
		cvs.put(DBschema.COLUMN_Word_Library_ID,word.library_id);
		cvs.put(DBschema.COLUMN_Word_Meaning,word.meaning);
		cvs.put(DBschema.COLUMN_Word_ExtraInfo,word.extrainfo);
		cvs.put(DBschema.COLUMN_Word_OgSentence,word.ogsentence);
		cvs.put(DBschema.COLUMN_Word_OgSource,word.ogsource);
		cvs.put(DBschema.COLUMN_Word_OgSourcePhoto,word.ogsourcephoto);
		
		long newid=sd.insert(DBschema.TABLE_Word, null, cvs);
		
		return newid;
	}
	
	public Cursor Query_WordList(int year, int month, int week, int library_id){
		String[] columns={DBschema.COLUMN_Word_Word,DBschema.COLUMN_Word_ID};
		String selection=DBschema.COLUMN_Word_ID+">? ";
		ArrayList<String> arglist=new ArrayList<String>();
		arglist.add("0");
		if(year>0){
			selection+="and "+DBschema.COLUMN_Word_Year+"=? ";
			arglist.add(Integer.toString(year));
		}			
		if(month>0){
			selection+="and "+DBschema.COLUMN_Word_Month+"=?";
			arglist.add(Integer.toString(month));
		}
			
		if(week>0){
			selection+="and "+DBschema.COLUMN_Word_Week+"=? ";
			arglist.add(Integer.toString(week));
		}
			
		/*if(category_id>0){
			selection+="and "+DBschema.COLUMN_Word_Category_ID+"=? ";
			arglist.add(Integer.toString(category_id));
		}*/
			
		if(library_id>0){
			selection+="and "+DBschema.COLUMN_Word_Library_ID+"=? ";
			arglist.add(Integer.toString(library_id));
		}
		String[] args=new String[arglist.size()];
		for(int i=0;i<arglist.size();i++)
			args[i]=arglist.get(i);
		Cursor cursor= sd.query(DBschema.TABLE_Word,columns , selection, args, null, null, DBschema.COLUMN_Word_ID, null);
		return cursor;
	}
	
}
