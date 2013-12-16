package com.contactlensreminder;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmService extends BroadcastReceiver{


	@Override
	public void onReceive(Context context, Intent intent) {

		//need to query the database and reset the alarm
		DatabaseHandler db=new DatabaseHandler(context);
		Calendar finalRightEye=Calendar.getInstance();
		Calendar finalLeftEye=Calendar.getInstance();
		Calendar finalOrder=Calendar.getInstance();
		
		if (!db.getLeftNotified()){
			finalLeftEye.setTimeInMillis(db.getLeftEyeDate());
			mSetAlarm(finalLeftEye,context);
		}
		
		if (!db.getRightNotified()){
			finalRightEye.setTimeInMillis(db.getRightEyeDate());
			mSetAlarm(finalRightEye,context);
		}

		if (!db.getOrderNotified()){
			finalOrder.setTimeInMillis(db.getOrderDate());
			mSetAlarm(finalOrder,context);
		}
		
	}

	public void mSetAlarm(Calendar finalTime,Context cntxt){
		Intent intent=new Intent(cntxt,AlarmRightReceiver.class);

		PendingIntent sender=PendingIntent.getBroadcast(cntxt, 0, intent, 0);

		//schedule the alarm
		AlarmManager am=(AlarmManager)cntxt.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, finalTime.getTimeInMillis(), sender);			
	}
}
