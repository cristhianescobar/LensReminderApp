package com.contactlensreminder;



import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

public class LensReminderApplication extends Activity{

	private DatabaseHandler db;
	protected Dialog mSplashDialog;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//delete the database to see how it starts working
		//deleteDatabase("CONTACT_LENS_MANAGER");

		db=new DatabaseHandler(this);

		startActivity(chooseActivity());
	}

	//choose between which activity to start
	private Intent chooseActivity(){
		Intent intent=null;
		if (db.getPrescriptionCount()<1){			
			intent=new Intent(this,SettingsLayout.class);
		}
		else {
			intent=new Intent(this,LensActivity.class);	
		}
		return intent;
	}
	
}
