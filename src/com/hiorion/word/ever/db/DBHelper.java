package com.hiorion.word.ever.db;

import java.util.Calendar;

import com.hiorion.word.ever.Keys;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	public static final String TABLE_Word = "word";
	public static final String TABLE_Library = "library";
	public static final String TABLE_Example = "example";
	// public static final String TABLE_Whatever="whatever";

	public static final String COLUMN_Word_ID = "_id";
	public static final String COLUMN_Word_Word = "word";
	public static final String COLUMN_Word_Library_UUID = "library_uuid";
	public static final String COLUMN_Word_OgSource = "ogsource";
	public static final String COLUMN_Word_OgSourcePhoto = "ogsourcephoto";
	public static final String COLUMN_Word_Date = "date";
	public static final String COLUMN_Word_Year = "year";
	public static final String COLUMN_Word_Month = "month";
	public static final String COLUMN_Word_Week = "week";
	public static final String COLUMN_Word_Day = "day";
	public static final String COLUMN_Word_Meaning = "meaning";
	public static final String COLUMN_Word_ExtraInfo = "extrainfo";
	public static final String COLUMN_Word_Part = "part";
	public static final String COLUMN_Word_TimeStamp = "timestamp";
	public static final String COLUMN_Word_Tag = "tag";
	public static final String COLUMN_Word_UUID = "uuid";
	public static final String COLUMN_Word_Phonetic = "phonetic";

	public static final String COLUMN_Library_ID = "_id";
	public static final String COLUMN_Library_Name = "name";
	public static final String COLUMN_Library_TimeStamp = "timestamp";
	public static final String COLUMN_Library_Tag = "tag";
	public static final String COLUMN_Library_UUID = "uuid";

	public static final String COLUMN_Example_ID = "_id";
	public static final String COLUMN_Example_Sentence = "sentence";
	public static final String COLUMN_Example_Type = "type";
	public static final String COLUMN_Example_Word_UUID = "word_uuid";
	public static final String COLUMN_Example_TimeStamp = "timestamp";
	public static final String COLUMN_Example_Tag = "tag";
	public static final String COLUMN_Example_UUID = "uuid";

	private static final int DATABASE_VERSION = 2;

	private static final String CREATE_Table_Word = "create table "
			+ TABLE_Word + "(" + COLUMN_Word_ID
			+ " integer primary key autoincrement, " + COLUMN_Word_Word
			+ "  text not null, " + COLUMN_Word_Library_UUID
			+ "  text not null, " + COLUMN_Word_OgSourcePhoto + "  text, "
			+ COLUMN_Word_Date + "  datetime not null, " + COLUMN_Word_Year
			+ "  integer not null, " + COLUMN_Word_Month
			+ "  integer not null, " + COLUMN_Word_Week
			+ "  integer not null, " + COLUMN_Word_Day + "  integer not null, "
			+ COLUMN_Word_Part + "  integer not null, " + COLUMN_Word_ExtraInfo
			+ "  text , " + COLUMN_Word_Meaning + " text not null,"
			+ COLUMN_Word_TimeStamp + "  integer not null, " + COLUMN_Word_Tag
			+ " integer not null," + COLUMN_Word_UUID + " text not null,"
			+ COLUMN_Word_Phonetic+" text );";

	private static final String CREATE_Table_Example = "create table "
			+ TABLE_Example + "(" + COLUMN_Example_ID
			+ " integer primary key autoincrement, " + COLUMN_Example_Word_UUID
			+ "  text not null, " + COLUMN_Example_Type
			+ "  integer not null, " + COLUMN_Example_Sentence
			+ " text not null, " + COLUMN_Example_TimeStamp
			+ "  integer not null, " + COLUMN_Example_Tag
			+ " integer not null," + COLUMN_Example_UUID + " text not null);";

	private static final String CREATE_Table_Library = "create table "
			+ TABLE_Library + "(" + COLUMN_Library_ID
			+ " integer primary key autoincrement, " + COLUMN_Library_Name
			+ "  text not null, " + COLUMN_Library_TimeStamp
			+ "  integer not null, " + COLUMN_Library_Tag
			+ " integer not null," + COLUMN_Library_UUID + " text not null);";

	public DBHelper(Context context) {
		super(context, Keys.DBName, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_Table_Word);
		database.execSQL(CREATE_Table_Example);
		database.execSQL(CREATE_Table_Library);

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		//version 2, add phonetic
		String table_temp="word_temp";
		String sql_rename="alter table "+TABLE_Word+" rename to "+table_temp;
		database.execSQL(sql_rename);
		database.execSQL(CREATE_Table_Word);
		String sql_copydata="insert into "+TABLE_Word+" ("
		+COLUMN_Word_ID+ ", "
		+COLUMN_Word_Word+ ", "
		+COLUMN_Word_Library_UUID+ ", "
		+COLUMN_Word_OgSourcePhoto+ ", "
		+COLUMN_Word_Date+ ", "
		+COLUMN_Word_Year+ ", "
		+COLUMN_Word_Month+ ", "
		+COLUMN_Word_Week+ ", "
		+COLUMN_Word_Day+ ", "
		+COLUMN_Word_Part+ ", "
		+COLUMN_Word_ExtraInfo+ ", "
		+COLUMN_Word_Meaning+ ", "
		+COLUMN_Word_TimeStamp+ ", "
		+COLUMN_Word_Tag+ ", "
		+COLUMN_Word_UUID+ " "
		+") select "
		+COLUMN_Word_ID+ ", "
		+COLUMN_Word_Word+ ", "
		+COLUMN_Word_Library_UUID+ ", "
		+COLUMN_Word_OgSourcePhoto+ ", "
		+COLUMN_Word_Date+ ", "
		+COLUMN_Word_Year+ ", "
		+COLUMN_Word_Month+ ", "
		+COLUMN_Word_Week+ ", "
		+COLUMN_Word_Day+ ", "
		+COLUMN_Word_Part+ ", "
		+COLUMN_Word_ExtraInfo+ ", "
		+COLUMN_Word_Meaning+ ", "
		+COLUMN_Word_TimeStamp+ ", "
		+COLUMN_Word_Tag+ ", "
		+COLUMN_Word_UUID+ ""
		+" from "+table_temp;
		database.execSQL(sql_copydata);
		String sql_drop="drop table "+table_temp;
		database.execSQL(sql_drop);
		
		
	}

}