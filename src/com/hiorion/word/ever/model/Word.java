package com.hiorion.word.ever.model;

import java.util.ArrayList;
import java.util.Date;

public class Word {

	public long id;
	public String word;
	public long library_id;

	public String ogsentence;
	public String ogsourcephoto;
	public Date date;
	public int week;
	public int month;
	public int year;
	public String meaning;
	public String extrainfo;
	
	public ArrayList<Example> examples=new ArrayList<Example>();
}
