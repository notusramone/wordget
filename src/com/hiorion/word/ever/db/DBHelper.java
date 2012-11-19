package com.hiorion.word.ever.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {
	
	public static final String TABLE_Word = "word";
	  public static final String TABLE_Category="category";
	  public static final String TABLE_Library="library";
	  public static final String TABLE_Example="example";
	  public static final String TABLE_Word_Example="word_example";

	  public static final String COLUMN_Word_ID = "_id";
	  public static final String COLUMN_Word_Word="word";
	  public static final String COLUMN_Word_Library_ID="library_id";
	  //public static final String COLUMN_Word_Category_ID="category_id";
	  public static final String COLUMN_Word_OgSource="ogsource";
	  public static final String COLUMN_Word_OgSentence="ogsentence";
	  public static final String COLUMN_Word_OgSourcePhoto="ogsourcephoto";
	  public static final String COLUMN_Word_Date="date";
	  public static final String COLUMN_Word_Year="year";
	  public static final String COLUMN_Word_Month="month";
	  public static final String COLUMN_Word_Week="week";
	  public static final String COLUMN_Word_Meaning="meaning";
	  public static final String COLUMN_Word_ExtraInfo="extrainfo";
	  
/*	  public static final String COLUMN_Category_ID="_id";
	  public static final String COLUMN_Category_Name="name";
	  public static final String COLUMN_Category_Desc="desc";*/
	  
	  public static final String COLUMN_Library_ID="_id";
	  public static final String COLUMN_Library_Name="name";
	  public static final String COLUMN_Library_Type="type";
	  
	  public static final String COLUMN_Example_ID="_id";
	  public static final String COLUMN_Example_Sentence="sentence";
	  
	  public static final String COLUMN_Word_Example_ID="_id";
	  public static final String COLUMN_Word_Example_Word_ID="word_id";
	  public static final String COLUMN_Word_Example_Example_ID="example_id";

	  private static final String DATABASE_NAME = "wordever.db";
	  private static final int DATABASE_VERSION = 1;

	  private static final String TAG_log = "EverProvider";
	  
	  private static final String CREATE_Table_Word = "create table "
	      + TABLE_Word + "(" 
	      + COLUMN_Word_ID + " integer primary key autoincrement, " 
	      + COLUMN_Word_Word + "  text not null, " 
	      + COLUMN_Word_Library_ID + "  integer, "
	      + COLUMN_Word_OgSource + "  text, "
	      + COLUMN_Word_OgSentence + "  text, "
	      + COLUMN_Word_OgSourcePhoto + "  text, "
	      + COLUMN_Word_Date + "  datetime not null, "
	      + COLUMN_Word_Year + "  integer not null, "
	      + COLUMN_Word_Month + "  integer not null, "
	      + COLUMN_Word_Week + "  integer not null, "
	      + COLUMN_Word_ExtraInfo + "  text , "
	      + COLUMN_Word_Meaning + " text not null);";
	  
	/*  private static final String CREATE_Table_Category = "create table "
	      + TABLE_Category + "(" 
	      + COLUMN_Category_ID + " integer primary key autoincrement, " 
	      + COLUMN_Category_Name + "  text not null, "
	      + COLUMN_Category_Desc + " text );";*/
	 
	  
	  private static final String CREATE_Table_Example = "create table "
	      + TABLE_Example + "(" 
	      + COLUMN_Example_ID + " integer primary key autoincrement, " 
	      + COLUMN_Example_Sentence + " text not null);";

	  private static final String CREATE_Table_Library = "create table "
	      + TABLE_Library + "(" 
	      + COLUMN_Library_ID + " integer primary key autoincrement, " 
	      + COLUMN_Library_Name + "  text not null, "
	      + COLUMN_Library_Type + " integer  not null);";
	  
	  private static final String CREATE_Table_Word_Example = "create table "
	      + TABLE_Word_Example + "(" 
	      + COLUMN_Word_Example_ID + " integer primary key autoincrement, " 
	      + COLUMN_Word_Example_Word_ID + "  text not null, "
	      + COLUMN_Word_Example_Example_ID + " integer  not null);";
	  
	  
	  public DBHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(CREATE_Table_Word);
	   // database.execSQL(CREATE_Table_Category);
	    database.execSQL(CREATE_Table_Example);
	    database.execSQL(CREATE_Table_Library);
	    database.execSQL(CREATE_Table_Word_Example);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		  //alter the DB
		  
	   /* Log.w(EverHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");*/
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_Word);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_Category);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_Library);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_Example);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_Word_Example);
	    onCreate(db);
	  }

	}