package com.contactlensreminder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.contactlensreminder.LensActivity.ReminderType;



import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsLayout extends Activity{

	private int mainSettingsOption;

	private String[] standardUnits;
	private String[] presUnits;
	private int rightPrescribedValue;
	private int leftPrescribedValue;
	private int orderValue;

	private DatabaseHandler db;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//set the database
		db=new DatabaseHandler(this);		

		setContentView(R.layout.settings_layout);
		ListView lv=(ListView)findViewById(R.id.listview);

		standardUnits=getResources().getStringArray(R.array.standardTime);
		presUnits=getResources().getStringArray(R.array.presUnits);
		String [] settingsNames=getResources().getStringArray(R.array.settings);
		List<String> listNames=new ArrayList<String>(Arrays.asList(settingsNames));

		//initialize the list
		leftPrescribedValue=db.getLeftEyeDays();
		rightPrescribedValue=db.getRightEyeDays();
		orderValue=db.getOrderDays();

		//display the list view		
		CustomListAdapter nameAdapter=new CustomListAdapter(this,R.layout.custom_layout,listNames);
		lv.setAdapter(nameAdapter);


		//set the eyelisteners
		DialogInterface.OnClickListener eyeListener=new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int item){				
				
				dialog.dismiss();
				doEverything(item,mainSettingsOption);
			}
		};

		DialogInterface.OnClickListener orderListener=new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {				
				
				dialog.dismiss();
				doEverything(which+4,mainSettingsOption);
			}
		};

		final AlertDialog.Builder leftBuilder=new AlertDialog.Builder(this);
		leftBuilder.setTitle("I change my left lens");
		leftBuilder.setSingleChoiceItems(standardUnits, getSelected(0), eyeListener);	
		final AlertDialog leftAlert=leftBuilder.create();

		final AlertDialog.Builder rightBuilder=new AlertDialog.Builder(this);
		rightBuilder.setTitle("I change my right lens");
		rightBuilder.setSingleChoiceItems(standardUnits, getSelected(1), eyeListener);	
		final AlertDialog rightAlert=rightBuilder.create();

		final AlertDialog.Builder orderBuilder=new AlertDialog.Builder(this);
		orderBuilder.setTitle("I order my lenses");
		orderBuilder.setSingleChoiceItems(presUnits, getSelected(2), orderListener);	
		final AlertDialog orderAlert=orderBuilder.create();


		lv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				
				mainSettingsOption=pos;
		                
				if (pos==0){ 
					ListView lvs=leftAlert.getListView();
					int checkPos=getSelected(pos);
					lvs.setItemChecked(checkPos, true);
					leftAlert.show();
				}
				else if (pos==1){
					ListView lvs=rightAlert.getListView();
					int checkPos=getSelected(pos);
					lvs.setItemChecked(checkPos, true);
					rightAlert.show();
				}
				else if (pos==2){
					ListView lvs=orderAlert.getListView();
					int checkPos=getSelected(pos);
					lvs.setItemChecked(checkPos, true);
					orderAlert.show();
				}
			}			
		});

				findViewById(R.id.buttonSettingsLayout).setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				//set the order time when the program starts
				Calendar finalOrderTime=Calendar.getInstance();
				finalOrderTime.setTimeInMillis(System.currentTimeMillis());
				finalOrderTime.add(LensActivity.fieldCalendar, orderValue);

				db.addOrderDate(finalOrderTime.getTimeInMillis());
				mSetAlarm(finalOrderTime);
				
				
				Intent intent=new Intent(SettingsLayout.this,LensActivity.class);	
				startActivity(intent);
			}

		});
	}

	private void doEverything (int timePos,int listPos){
		int days=getDays(timePos);

		if (listPos==0){
			leftPrescribedValue=days;
		}
		else if (listPos==1){
			rightPrescribedValue=days;
		}
		else if (listPos==2){
			orderValue=days;
		}
		db.addPrescribedDays(leftPrescribedValue, rightPrescribedValue, orderValue);
	}

	private int getDays(int timePos){
		String item=standardUnits[timePos];

		if (item.equals("Weekly")) return 7;
		else if (item.equals("Biweekly"))return 14;
		else if (item.equals("Monthly")) return 30;
		else if (item.equals("Biannually")) return 180;
		else if (item.equals("Annually")) return 365;

		return 1;
	}

	private int getSelected(int listPos){
		int days=1;
		if (listPos==0){
			days=db.getLeftEyeDays();			
			return getUnitPos(days);
		}
		else if (listPos==1){
			days=db.getRightEyeDays();			
			return getUnitPos(days);
		}
		else if (listPos==2){
			days=db.getOrderDays();			
			return getUnitPos(days)-4;
		}		

		return 0;
	};

	private int getUnitPos(int days){
		if (days<7) return 0;//days
		else if (days<14) return 1;//weekly
		else if (days<30) return 2;//Biweekly;
		else if (days<180) return 3;//monthly;
		else if (days<365) return 4;
		else return 5;//yearly
	}

	public class CustomListAdapter extends ArrayAdapter<String> {

		private Context mContext;
		private int id;
		private List <String>items ;



		public CustomListAdapter(Context context, int textViewResourceId , List<String> list ) 
		{
			super(context, textViewResourceId, list);           
			mContext = context;
			id = textViewResourceId;
			items = list ;


		}

		@Override
		public View getView(int position, View v, ViewGroup parent)
		{
			View mView = v ;
			if(mView == null){
				LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mView = vi.inflate(id, null);
			}

			TextView text = (TextView) mView.findViewById(R.id.textView);
			

			if(items.get(position) != null )
			{
				text.setTextColor(Color.WHITE);
				text.setText(items.get(position));


				int color=Color.rgb(35,107,142);			
				text.setBackgroundColor( color );
			}

			return mView;
		}

	}

	private void mSetAlarm(Calendar finalTime){
		Intent intentOrder=new Intent(this,OrderReceiver.class);

		AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);

		//find one which ever is going to go first set it up		
		intentOrder.putExtra("TYPE", ReminderType.PRESCRIPTION);
		PendingIntent sender=PendingIntent.getBroadcast(this, 0, intentOrder, PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP,finalTime.getTimeInMillis(), sender);	
	}



}
