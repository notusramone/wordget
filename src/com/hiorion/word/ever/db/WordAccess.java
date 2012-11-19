package com.hiorion.word.ever.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Word;

public class WordAccess {

	DBHelper dbh;
	SQLiteDatabase sd;
	public WordAccess(Context context){
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
	
	public void Delete(long id){		
		sd.delete(DBHelper.TABLE_Word, DBHelper.COLUMN_Word_ID+" = "+id,null);
	}
	
	public void Update(Word word){
		ContentValues cvs=new ContentValues();
		cvs.put(DBHelper.COLUMN_Word_ID, word.id);
		cvs.put(DBHelper.COLUMN_Word_Word, word.word);
		cvs.put(DBHelper.COLUMN_Word_Library_ID,word.library_id);
		cvs.put(DBHelper.COLUMN_Word_Meaning,word.meaning);
		cvs.put(DBHelper.COLUMN_Word_ExtraInfo,word.extrainfo);
		cvs.put(DBHelper.COLUMN_Word_OgSentence,word.ogsentence);
		cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto,word.ogsourcephoto);
		
		sd.update(DBHelper.TABLE_Word, cvs,null,null);
		
		//delete examples
		/*String sql="delete  from "+DBHelper.TABLE_Word_Example+" where "+DBHelper.COLUMN_Word_Example_Word_ID+"="+word.id;
		sd.execSQL(sql);*/
		sd.delete(DBHelper.TABLE_Word_Example, DBHelper.COLUMN_Word_Example_Word_ID+"="+word.id, null);
		//insert examples
		ContentValues cvs_ex=new ContentValues();
		for(Example e:word.examples){
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Example_Sentence, e.sentence);
			long exid=sd.insert(DBHelper.TABLE_Example, null, cvs_ex);
			
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Word_Example_Example_ID, exid);
			cvs_ex.put(DBHelper.COLUMN_Word_Example_Word_ID, word.id);
			sd.insert(DBHelper.TABLE_Word_Example, null, cvs_ex);
		}
		
		
	}
	
	public long Insert(Word word){
		ContentValues cvs=new ContentValues();
		Calendar now=Calendar.getInstance();
		word.date=now.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		word.year=now.get(Calendar.YEAR);
		word.month=now.get(Calendar.MONTH);
		word.week=now.get(Calendar.WEEK_OF_MONTH);
		
		cvs.put(DBHelper.COLUMN_Word_Word, word.word);
		cvs.put(DBHelper.COLUMN_Word_Date,dateFormat.format(word.date));
		cvs.put(DBHelper.COLUMN_Word_Week,word.week);
		cvs.put(DBHelper.COLUMN_Word_Year,word.year);
		cvs.put(DBHelper.COLUMN_Word_Month,word.month);
		cvs.put(DBHelper.COLUMN_Word_Library_ID,word.library_id);
		cvs.put(DBHelper.COLUMN_Word_Meaning,word.meaning);
		cvs.put(DBHelper.COLUMN_Word_ExtraInfo,word.extrainfo);
		cvs.put(DBHelper.COLUMN_Word_OgSentence,word.ogsentence);
		cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto,word.ogsourcephoto);
		
		long wordid=sd.insert(DBHelper.TABLE_Word, null, cvs);
		
		//insert examples
		ContentValues cvs_ex=new ContentValues();
		for(Example e:word.examples){
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Example_Sentence, e.sentence);
			long exid=sd.insert(DBHelper.TABLE_Example, null, cvs_ex);
			
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Word_Example_Example_ID, exid);
			cvs_ex.put(DBHelper.COLUMN_Word_Example_Word_ID, wordid);
			sd.insert(DBHelper.TABLE_Word_Example, null, cvs_ex);
		}
		
		return wordid;
	}
	
	public Word GetAWord(long id){
		Word word=new Word();
		
		String selection=DBHelper.COLUMN_Word_ID+"= "+id;
/*		String sql=String.format("select * from %d,%d where %d=? and %d.%d=%d.%d",DBHelper.TABLE_Word,DBHelper.TABLE_Library,
				DBHelper.COLUMN_Word_ID,
				DBHelper.TABLE_Word,DBHelper.COLUMN_Word_Library_ID,DBHelper.TABLE_Library,DBHelper.COLUMN_Library_ID);
		Cursor cursor=sd.rawQuery(sql, new String[]{Integer.toString(id)});*/
		
		Cursor cursor= sd.query(DBHelper.TABLE_Word,null , selection,null, null, null,  null);		
		cursor.moveToFirst();		
		word.id=cursor.getLong(0);
		word.word=cursor.getString(1);
		word.library_id=cursor.getInt(2);
		word.ogsentence=cursor.getString(4);
		word.ogsourcephoto=cursor.getString(5);
		//word.date=cursor.getString(6);
		word.year=cursor.getInt(7);
		word.month=cursor.getInt(8);
		word.week=cursor.getInt(9);
		word.extrainfo=cursor.getString(10);
		word.meaning=cursor.getString(11);
		return word;
	}
	public Cursor Query_WordList(int year, int month, int week, int library_id){
		String[] columns={DBHelper.COLUMN_Word_Word,DBHelper.COLUMN_Word_ID};
		String selection=DBHelper.COLUMN_Word_ID+">? ";
		ArrayList<String> arglist=new ArrayList<String>();
		arglist.add("0");
		if(year>0){
			selection+="and "+DBHelper.COLUMN_Word_Year+"=? ";
			arglist.add(Integer.toString(year));
		}			
		if(month>0){
			selection+="and "+DBHelper.COLUMN_Word_Month+"=?";
			arglist.add(Integer.toString(month));
		}
			
		if(week>0){
			selection+="and "+DBHelper.COLUMN_Word_Week+"=? ";
			arglist.add(Integer.toString(week));
		}

		if(library_id>0){
			selection+="and "+DBHelper.COLUMN_Word_Library_ID+"=? ";
			arglist.add(Integer.toString(library_id));
		}
		String[] args=new String[arglist.size()];
		for(int i=0;i<arglist.size();i++)
			args[i]=arglist.get(i);
		Cursor cursor= sd.query(DBHelper.TABLE_Word,columns , selection, args, null, null, DBHelper.COLUMN_Word_ID, null);
		return cursor;
	}
	
}
