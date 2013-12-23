package com.contactlensreminder;

import java.util.Calendar;



import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 
 * @author H101712
 *
 */
public class LensActivity extends Activity {


	private final static long divideDay=(1000*60*60*24);
	private final static long divideHour=(1000*60*60);
	private final static long divideMin=(1000*60);
	private final static long divideSec=(1000);

	public static enum ReminderType {LEFT,RIGHT,PRESCRIPTION};

	private Handler leftHandler;
	private Handler rightHandler;

	private TextView outputLeftNum;
	private TextView outputLeftString;
	private TextView outputRightNum;
	private TextView outputRightString;

	private Calendar finalLeftTime;
	private Calendar finalRightTime;
	private Calendar finalOrderTime;

	private DatabaseHandler db;

	private Runnable leftRunnable;
	private Runnable rightRunnable;

	private boolean setLeftNewAlarm=false;
	private boolean setRightNewAlarm=false;

	//change these to count for seconds vs numbers
	public static final int fieldCalendar=Calendar.DAY_OF_YEAR;
	//long divideBy=divideDay;
	long leftDivideBy=divideDay;
	long rightDivideBy=divideDay;
	//long divideBy=1000;

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		db=new DatabaseHandler(this);

		setContentView(R.layout.main_layout);

		//make it the only activity that is on top
		super.getIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		leftHandler=new Handler();
		rightHandler=new Handler();

		outputLeftNum=(TextView)findViewById(R.id.textViewLeftNum);
		outputLeftString=(TextView)findViewById(R.id.textViewLeftDays);

		outputRightNum=(TextView)findViewById(R.id.textViewRightNum);
		outputRightString=(TextView)findViewById(R.id.textViewRightDays);

		finalLeftTime=Calendar.getInstance();
		finalRightTime=Calendar.getInstance();
		finalOrderTime=Calendar.getInstance();

		finalLeftTime.setTimeInMillis(db.getLeftEyeDate());
		finalRightTime.setTimeInMillis(db.getRightEyeDate());
		finalOrderTime.setTimeInMillis(db.getOrderDate());

		leftRunnable=new Runnable(){       	
			public void run() {	
				double diff=((finalLeftTime.getTimeInMillis()-System.currentTimeMillis())/(1.0*leftDivideBy));				
				postText(diff,outputLeftNum,outputLeftString,leftDivideBy);
				leftDivideBy=changeDivideBy(diff,leftDivideBy);				
				leftHandler.postDelayed(this, divideSec);
			}
		};

		rightRunnable=new Runnable(){
			public void run() {
				double diff=((finalRightTime.getTimeInMillis()-System.currentTimeMillis())/(1.0*rightDivideBy));

				postText(diff,outputRightNum,outputRightString,rightDivideBy);
				rightDivideBy=changeDivideBy(diff,rightDivideBy);
				rightHandler.postDelayed(this, divideSec);
			}			
		}; 

		leftHandler.post(leftRunnable);
		rightHandler.post(rightRunnable);

		findViewById(R.id.buttonLeft).setOnClickListener(
				new OnClickListener(){

					public void onClick(View v) {						
						finalLeftTime.setTimeInMillis(System.currentTimeMillis());
						finalLeftTime.add(fieldCalendar, db.getLeftEyeDays());				
						setLeftNewAlarm=true;

						outputLeftString.setText("Days Left");
						outputLeftString.setTextColor(Color.BLACK);
						outputLeftNum.setTextColor(Color.BLACK);
						leftDivideBy=divideDay;
						leftHandler.post(leftRunnable);

						if (setLeftNewAlarm){
							db.addDate(finalLeftTime.getTimeInMillis(), finalRightTime.getTimeInMillis());
							mSetAlarm();	
							setLeftNewAlarm=false;
						}
					}
				});

		findViewById(R.id.buttonRight).setOnClickListener(
				new OnClickListener(){

					public void onClick(View v) {						
						finalRightTime.setTimeInMillis(System.currentTimeMillis());
						finalRightTime.add(fieldCalendar, db.getRightEyeDays());				
						setRightNewAlarm=true;

						outputRightString.setText("Days Left");
						outputRightString.setTextColor(Color.BLACK);
						outputRightNum.setTextColor(Color.BLACK);
						rightDivideBy=divideDay;
						rightHandler.post(rightRunnable);

						if (setRightNewAlarm){
							db.addDate(finalLeftTime.getTimeInMillis(), finalRightTime.getTimeInMillis());
							mSetAlarm();
							setRightNewAlarm=false;
						}
					}
				});
	}

	public void onPause(){		
		if (setLeftNewAlarm){
			db.addDate(finalLeftTime.getTimeInMillis(), finalRightTime.getTimeInMillis());
			mSetAlarm();	
			setLeftNewAlarm=false;
		}
		if (setRightNewAlarm){
			db.addDate(finalLeftTime.getTimeInMillis(), finalRightTime.getTimeInMillis());
			mSetAlarm();
			setRightNewAlarm=false;
		}
		super.onPause();
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_lens, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item){
		//handle item
		switch(item.getItemId()){
		case R.id.menu_settings:
			//Intent intent=new Intent(LensActivity.this,PrescriptionMenu.class);
			Intent intent=new Intent(LensActivity.this,SettingsLayout.class);
			startActivity(intent);
			return true;
		case R.id.menu_about:
			Intent aboutIntent=new Intent(LensActivity.this,AboutLayout.class);
			startActivity(aboutIntent);
			return true;
		default:
			return false;
		}
	}

	private void mSetAlarm(){
		Intent intentRight=new Intent(LensActivity.this,AlarmRightReceiver.class);
		Intent intentLeft=new Intent(LensActivity.this,AlarmLeftReceiver.class);

		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);

		//find one which ever is going to go first set it up		
		if (setRightNewAlarm){
			intentRight.putExtra("TYPE", ReminderType.RIGHT);
			PendingIntent sender=PendingIntent.getBroadcast(LensActivity.this, 0, intentRight, PendingIntent.FLAG_CANCEL_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP,finalRightTime.getTimeInMillis(), sender);		
		}

		if (setLeftNewAlarm){
			intentLeft.putExtra("TYPE", ReminderType.LEFT);			

			PendingIntent sender=PendingIntent.getBroadcast(LensActivity.this, 0, intentLeft, PendingIntent.FLAG_CANCEL_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP,finalLeftTime.getTimeInMillis(), sender);	
		}											
	}

	private void postText(Double diff,TextView numView,TextView strView,long divideBy){
		int displayNum=diff.intValue();
		//such that it displays and makes sense
		displayNum+=1;
		if (diff>0.0){
			strView.setTextColor(Color.BLACK);
			numView.setTextColor(Color.BLACK);

			if (divideBy==divideDay){
                strView.setText(displayNum != 1 ? "Days Left" : "Day Left");

			}
			else if (divideBy==divideHour){
                strView.setText(displayNum != 1 ? "Hours Left" : "Hour Left");
			}
			else if (divideBy==divideMin){
                strView.setText(displayNum != 1 ? "Mins Left" : "Min Left");

			}
			else if (divideBy==divideSec){
                strView.setText(displayNum != 1 ? "Secs Left" : "Sec Left");
			}
			if (displayNum!=0){
				numView.setText(Integer.toString(displayNum));
			}
		}
		else {
			
			numView.setTextColor(Color.RED);
			strView.setTextColor(Color.RED);
			if (divideBy==divideDay){

                strView.setText(displayNum != 1 ? "Days Past" : "Day Past");
			}
			else if (divideBy==divideHour){
                strView.setText(displayNum != 1 ? "Hours Past" : "Hour Past");

			}
			else if (divideBy==divideMin){
                strView.setText(displayNum != 1 ? "Mins Past" : "Min Past");

			}
			else if (divideBy==divideSec){
                strView.setText(displayNum != 1 ? "Secs Past" : "Sec Past");

			}
			if (displayNum!=0){
				numView.setText(Integer.toString(Math.abs(displayNum)));
			}
		}	
	}

	/*depending upon which way it is counting
		we might have to display higher resolution
		ppl might think the app is broken if it does not change anything
	 */	
	private long changeDivideBy(final double diff,final long divideBy){
		//if the num is positive and less than 0
		//means some time is left
		if (diff<1.0 && diff>0){
			if (divideBy==divideDay){
				return divideHour;
			}
			else if (divideBy==divideHour){
				return divideMin;
			}
			else if (divideBy==divideMin){
				return divideSec;
			}					
		}
		//if the num is negative and greater than -1
		else if (diff<-0.1){			
			if (divideBy==divideSec && diff<-60){
				return divideMin;
			}
			else if (divideBy==divideMin && diff<-60){
				return divideHour;
			}
			else if (divideBy==divideHour && diff<-24){
				return divideDay;
			}
		}

		//in most cases we just want to remain in the same resolution
		return divideBy;
	}
}
