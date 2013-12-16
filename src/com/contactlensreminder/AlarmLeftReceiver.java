package com.contactlensreminder;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.contactlensreminder.LensActivity.ReminderType;



public class AlarmLeftReceiver extends BroadcastReceiver{

	private static DatabaseHandler db;

	public void onReceive(Context context, Intent intent) {		
		Bundle b=intent.getExtras();

		ReminderType type=(ReminderType)b.get("TYPE");	
		db=new DatabaseHandler(context);

		sSetNotification(context,type);		
	}

	public static void sSetNotification(Context context,ReminderType value){
		NotificationManager notificationManager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent=new Intent(context,LensActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent=PendingIntent.getActivity(context,0, intent, PendingIntent.FLAG_UPDATE_CURRENT	);

		//the alarm to go off in
		Calendar calendar=Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 0);

		String notifyString="Time to change your contact lens";
		if (value==ReminderType.LEFT){
			notifyString="Time to change your left contact lens";
		}
		else if (value==ReminderType.RIGHT){
			notifyString="Time to change your right contact lens";
		}
		else if (value==ReminderType.PRESCRIPTION){
			notifyString="Time to order your prescription";
		}

		Notification notification=new Notification(R.drawable.ic_launcher,"Change your contact lens",calendar.getTimeInMillis());
		notification.setLatestEventInfo(context, "Contact Lens Reminder", notifyString, contentIntent);
		notification.flags |=Notification.FLAG_AUTO_CANCEL;
		notification.defaults|=Notification.DEFAULT_ALL;

		if (value==ReminderType.LEFT){
			notificationManager.notify(5556666,notification);
			db.setleftNotified();
		}
		else if (value==ReminderType.RIGHT){
			notificationManager.notify(5556667,notification);
			db.setRightNotified();
		}
		else if (value==ReminderType.PRESCRIPTION){
			notificationManager.notify(5556668,notification);
		}
	}
}
