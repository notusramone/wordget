package com.hiorion.word.ever.model;

import java.util.ArrayList;
import java.util.Date;

public class Word {

	public long id = 0;
	public String word = "";
	public String library_uuid = "";
	public String ogsentence = "";
	public String ogsourcephoto = "";
	public Date date;
	public int week = 0;
	public int month = 0;
	public int year = 0;
	public int day = 0;
	public String meaning = "";
	public String extrainfo = "";
	public int part = 0;
	public int newdaymark;
	public String uuid = "";
	public String phonetic="";

	public ArrayList<Example> examples = new ArrayList<Example>();
}
