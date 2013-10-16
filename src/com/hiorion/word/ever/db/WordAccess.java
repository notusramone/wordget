package com.hiorion.word.ever.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiorion.word.ever.Keys;
import com.hiorion.word.ever.model.Example;
import com.hiorion.word.ever.model.Word;

public class WordAccess {

	DBHelper dbh;
	SQLiteDatabase sd;

	public WordAccess(Context context) {
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

	public void Delete(long id) {
		String uuid = this.GetAWord(id).uuid;
		Calendar now = Calendar.getInstance();
		long timestamp = now.getTimeInMillis();
		// delete the word
		ContentValues cvs = new ContentValues();
		cvs.put(DBHelper.COLUMN_Word_Tag, Keys.DataTag_Deleted);

		cvs.put(DBHelper.COLUMN_Word_TimeStamp, timestamp);
		String condition = DBHelper.COLUMN_Word_ID + " = " + id;
		sd.update(DBHelper.TABLE_Word, cvs, condition, null);
		// delete all examples
		ContentValues cvs_ex = new ContentValues();
		// delete examples
		cvs_ex.put(DBHelper.COLUMN_Example_Tag, Keys.DataTag_Deleted);
		cvs_ex.put(DBHelper.COLUMN_Example_TimeStamp, timestamp);

		condition = DBHelper.COLUMN_Example_Word_UUID + " = '" + uuid + "'";

		sd.update(DBHelper.TABLE_Example, cvs_ex, condition, null);
	}

	public void Update(Word word, Word ogword) {
		Calendar now = Calendar.getInstance();
		long timestamp = now.getTimeInMillis();
		ContentValues cvs = new ContentValues();
		cvs.put(DBHelper.COLUMN_Word_Word, word.word);
		cvs.put(DBHelper.COLUMN_Word_Library_UUID, word.library_uuid);
		cvs.put(DBHelper.COLUMN_Word_Meaning, word.meaning);
		cvs.put(DBHelper.COLUMN_Word_ExtraInfo, word.extrainfo);
		cvs.put(DBHelper.COLUMN_Word_Part, word.part);
		cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto, word.ogsourcephoto);
		cvs.put(DBHelper.COLUMN_Word_TimeStamp, timestamp);
		// cvs.put(DBHelper.COLUMN_Word_Tag,Keys.SyncTag_Updated);

		String condition = DBHelper.COLUMN_Word_ID + " = " + word.id;

		sd.update(DBHelper.TABLE_Word, cvs, condition, null);

		String uuid = this.GetAWord(word.id).uuid;
		ContentValues cvs_ex = new ContentValues();
		// update examples
		for (int i = 0; i < 2; i++) {
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Example_TimeStamp, timestamp);
			cvs_ex.put(DBHelper.COLUMN_Example_Sentence,
					word.examples.get(i).sentence);
			condition = DBHelper.COLUMN_Example_ID + " = "
					+ ogword.examples.get(i).id;
			sd.update(DBHelper.TABLE_Example, cvs_ex, condition, null);
		}

		// update ogsentence

		cvs_ex.clear();
		cvs_ex.put(DBHelper.COLUMN_Example_Sentence, word.ogsentence);
		cvs_ex.put(DBHelper.COLUMN_Example_TimeStamp, timestamp);
		condition = DBHelper.COLUMN_Example_Word_UUID + " = '" + uuid
				+ "' and type=" + Example.Type_OGSentence;
		sd.update(DBHelper.TABLE_Example, cvs_ex, condition, null);

	}

	@SuppressWarnings("deprecation")
	public long Insert(Word word) {
		ContentValues cvs = new ContentValues();
		Calendar now = Calendar.getInstance();
		long timestamp = now.getTimeInMillis();
		word.date = now.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		word.year = now.get(Calendar.YEAR);
		word.month = now.get(Calendar.MONTH) + 1;
		word.day = now.get(Calendar.DATE);
		word.week = now.get(Calendar.WEEK_OF_MONTH);
		word.uuid = java.util.UUID.randomUUID().toString();

		cvs.put(DBHelper.COLUMN_Word_Word, word.word);
		cvs.put(DBHelper.COLUMN_Word_Date, dateFormat.format(word.date));
		cvs.put(DBHelper.COLUMN_Word_Week, word.week);
		cvs.put(DBHelper.COLUMN_Word_Year, word.year);
		cvs.put(DBHelper.COLUMN_Word_Month, word.month);
		cvs.put(DBHelper.COLUMN_Word_Day, word.day);
		cvs.put(DBHelper.COLUMN_Word_Library_UUID, word.library_uuid);
		cvs.put(DBHelper.COLUMN_Word_Meaning, word.meaning);
		cvs.put(DBHelper.COLUMN_Word_ExtraInfo, word.extrainfo);
		cvs.put(DBHelper.COLUMN_Word_Part, word.part);
		cvs.put(DBHelper.COLUMN_Word_OgSourcePhoto, word.ogsourcephoto);
		cvs.put(DBHelper.COLUMN_Word_TimeStamp, timestamp);
		cvs.put(DBHelper.COLUMN_Word_Tag, Keys.DataTag_Regular);
		cvs.put(DBHelper.COLUMN_Word_UUID, word.uuid);

		long wordid = sd.insert(DBHelper.TABLE_Word, null, cvs);

		// insert ogsentence
		ContentValues cvs_ex = new ContentValues();
		cvs_ex.put(DBHelper.COLUMN_Example_Sentence, word.ogsentence);
		cvs_ex.put(DBHelper.COLUMN_Example_Type, Example.Type_OGSentence);
		cvs_ex.put(DBHelper.COLUMN_Example_Word_UUID, word.uuid);
		cvs_ex.put(DBHelper.COLUMN_Example_TimeStamp, timestamp);
		cvs_ex.put(DBHelper.COLUMN_Example_Tag, Keys.DataTag_Regular);
		cvs_ex.put(DBHelper.COLUMN_Example_UUID, java.util.UUID.randomUUID()
				.toString());
		sd.insert(DBHelper.TABLE_Example, null, cvs_ex);

		// insert examples, there will be always two examples ragardless user
		// inputs
		for (Example e : word.examples) {
			cvs_ex.clear();
			cvs_ex.put(DBHelper.COLUMN_Example_Sentence, e.sentence);
			cvs_ex.put(DBHelper.COLUMN_Example_Type, Example.Type_Example);
			cvs_ex.put(DBHelper.COLUMN_Example_Word_UUID, word.uuid);
			cvs_ex.put(DBHelper.COLUMN_Example_TimeStamp, timestamp);
			cvs_ex.put(DBHelper.COLUMN_Example_Tag, Keys.DataTag_Regular);
			cvs_ex.put(DBHelper.COLUMN_Example_UUID, java.util.UUID
					.randomUUID().toString());
			sd.insert(DBHelper.TABLE_Example, null, cvs_ex);
		}

		return wordid;
	}

	private void formatWordCursor(Word word, Cursor cursor) {
		word.id = cursor.getLong(0);
		word.word = cursor.getString(1);
		word.library_uuid = cursor.getString(2);
		word.ogsourcephoto = cursor.getString(3);
		// 2012-11-21 18:06:17
		String datestr = cursor.getString(4);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		try {
			word.date = dateFormat.parse(datestr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		word.year = cursor.getInt(5);
		word.month = cursor.getInt(6);
		word.week = cursor.getInt(7);
		word.day = cursor.getInt(8);
		word.part = cursor.getInt(9);
		word.extrainfo = cursor.getString(10);
		word.meaning = cursor.getString(11);
		word.uuid = cursor.getString(14);
		word.phonetic = cursor.getString(15);
	}

	private Word getAWordWithExamples(long id) {

		Word word = new Word();
		String selection = DBHelper.COLUMN_Word_ID + "= " + id + " and "
				+ DBHelper.COLUMN_Word_Tag + ">0";

		Cursor cursor = sd.query(DBHelper.TABLE_Word, null, selection, null,
				null, null, null);
		cursor.moveToFirst();
		formatWordCursor(word, cursor);
		cursor.close();
		fillExamples(word);

		return word;

	}

	public void fillExamples(Word word) {
		String selection = "select * from " + DBHelper.TABLE_Example
				+ " where " + DBHelper.COLUMN_Example_Word_UUID + " = '"
				+ word.uuid + "' and " + DBHelper.COLUMN_Example_Tag + " >0";
		Cursor cursorex = sd.rawQuery(selection, null);
		for (cursorex.moveToFirst(); !cursorex.isAfterLast(); cursorex
				.moveToNext()) {

			switch (cursorex.getInt(2)) {
			case 1:
				word.ogsentence = cursorex.getString(3);
				break;

			case 0:
				Example ex = new Example();
				ex.id = cursorex.getLong(0);
				ex.word_uuid = cursorex.getString(1);
				ex.type = cursorex.getInt(2);
				ex.sentence = cursorex.getString(3);
				ex.uuid = cursorex.getString(6);
				word.examples.add(ex);
				break;
			}

		}
		cursorex.close();
	}

	public ArrayList<Word> GetAWordWithAllMeaning(long id) {
		ArrayList<Word> list = new ArrayList<Word>();
		Word word1 = getAWordWithExamples(id);
		list.add(word1);
		String sql = "select * from " + DBHelper.TABLE_Word + " where "
				+ DBHelper.COLUMN_Word_Word + " = '" + word1.word + "' and "
				+ DBHelper.COLUMN_Word_ID + " <> " + id + " and "
				+ DBHelper.COLUMN_Word_Tag + " >0";
		Cursor cursor = sd.rawQuery(sql, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			Word wordx = new Word();
			formatWordCursor(wordx, cursor);
			fillExamples(wordx);
			list.add(wordx);
		}
		cursor.close();
		return list;
	}

	public Word GetAWord(long id) {
		Word word = new Word();

		String selection = DBHelper.COLUMN_Word_ID + "= " + id + " and "
				+ DBHelper.COLUMN_Word_Tag + ">0";

		Cursor cursor = sd.query(DBHelper.TABLE_Word, null, selection, null,
				null, null, null);
		cursor.moveToFirst();
		formatWordCursor(word, cursor);
		cursor.close();
		return word;
	}
	
	public long GetPrevWord(long wid){
		long pid=0;
		String sql="select * from "+DBHelper.TABLE_Word +" where "+DBHelper.COLUMN_Word_ID+"<"+wid+" and "+DBHelper.COLUMN_Word_Tag+">0 order by  "+DBHelper.COLUMN_Word_ID+" desc limit 1";
		Cursor cursor=sd.rawQuery(sql, null);
		if(cursor.moveToFirst()){
			pid= cursor.getInt(0);
			cursor.close();
		}
			
		return pid;
	}
	
	public long GetNextWord(long wid){
		long nid=0;
		String sql="select * from "+DBHelper.TABLE_Word +" where "+DBHelper.COLUMN_Word_ID+">"+wid+" and "+DBHelper.COLUMN_Word_Tag+">0 order by  "+DBHelper.COLUMN_Word_ID+" limit 1";
		Cursor cursor=sd.rawQuery(sql, null);
		if(cursor.moveToFirst()){
			nid= cursor.getInt(0);
			cursor.close();
		}
		return nid;
	}

	public Cursor Query_WordList(int year, int month, int day, int week,
			String library_uuid, int limit, String key) {

		// select word._id,word.word,word.meaning,example.sentence, word.date
		// from word,example
		// where word._id=example.word_id and example.type=1 and word.synctag>0
		// and example.synctag>0 order by word.date desc limit 50
		String sql = "select word._id,"
				+ "word.word,"
				+ "word.meaning,"
				+ "example.sentence, "
				+ "word.date, "
				+ "word.part "
				+ "from word,example where word.uuid=example.word_uuid and example.type=1 and word.tag>0 and example.tag>0";
		if (year > 0)
			sql += " and " + DBHelper.COLUMN_Word_Year + "=" + year;
		if (month > 0)
			sql += " and " + DBHelper.COLUMN_Word_Month + "=" + month;
		if (week > 0)
			sql += " and " + DBHelper.COLUMN_Word_Week + "=" + week;
		if (day > 0)
			sql += " and " + DBHelper.COLUMN_Word_Day + "=" + day;
		if (library_uuid.length() > 0)
			sql += " and " + DBHelper.COLUMN_Word_Library_UUID + "='"
					+ library_uuid + "'";
		if (key.length() > 0)
			sql += " and word.word like '%" + key + "%'";
		sql += " order by word.date desc ";
		if (limit > 0)
			sql += " limit " + limit;
		Cursor cursor = sd.rawQuery(sql, null);
		return cursor;
	}

}
