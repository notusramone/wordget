package com.hiorion.word.ever;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.Spinner;

public class CalendarActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_PROGRESS);  
		setContentView(R.layout.activity_calendar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_calendar, menu);
		return true;
	}
	
	public void calen_go_click(View view){
		Spinner calen_range=(Spinner)findViewById(R.id.calen_range);
		DatePicker calen_datepicker=(DatePicker)findViewById(R.id.calen_datepicker);
		
		int calrange=calen_range.getSelectedItemPosition();
		int day=calen_datepicker.getDayOfMonth();
		int year=calen_datepicker.getYear();
		int month=calen_datepicker.getMonth();
		int week=0;
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		try {
			
			Date curdate=sdf.parse(""+year+"-"+month+"-"+day);
			Calendar cal=Calendar.getInstance();
			cal.setTime(curdate);
			week=cal.get(Calendar.WEEK_OF_MONTH);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	public void calen_cancel_click(View view){
		finish();
	}

}
