package com.contactlensreminder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHandler extends SQLiteOpenHelper{

	private static int _version=1;
	private static final String DATABASE="CONTACT_LENS_MANAGER";
	private static final String DATE_TABLE="DATE_STORE";
	private static final String PRESCRIBED_TABLE="PRESCRIBED_TABLE";
	private static final String ORDER_TABLE="ORDER_TABLE";

	private static final String ID="id";
	private static final String LEFT_EYE_DATE="left_eye";
	private static final String RIGHT_EYE_DATE="right_eye";
	private static final String RIGHT_NOTIFIED="right_notified";
	private static final String LEFT_NOTIFIED="left_notified";
	private static final String PRESCRIPTION="prescription";
	private static final String ORDER_LENS="order_lens";
	private static final String ORDER_NOTIFIED="order_notified";

	private static final int LEFT_EYE_COLUMN=1;
	private static final int RIGHT_EYE_COLUMN=2;
	private static final int LEFT_NOTIFIED_COLUMN=3;
	private static final int RIGHT_NOTIFIED_COLUMN=4;
	private static final int ORDER_COLUMN=1;
	private static final int ORDER_NOTIFIED_COLUMN=2;

	private static final int PRESCRIPTION_COLUMN=3;

	public DatabaseHandler(Context context){
		super(context, DATABASE, null, _version);
	} 

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + DATE_TABLE + "("
				+ ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LEFT_EYE_DATE + " INTEGER,"
				+ RIGHT_EYE_DATE + " INTEGER," + LEFT_NOTIFIED +" INTEGER, " + RIGHT_NOTIFIED + " INTEGER)";

		String CREATE_PRESCRIBED_TABLE="CREATE TABLE "+ PRESCRIBED_TABLE +"("				
				+ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+ LEFT_EYE_DATE + " INTEGER, "+ RIGHT_EYE_DATE + " INTEGER," +
				PRESCRIPTION +" INTEGER "+ ")";

		String CREATE_ORDER_TABLE="CREATE TABLE "+ ORDER_TABLE +"(" + ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+ ORDER_LENS +" INTEGER, "+ ORDER_NOTIFIED 
				+ "INTEGER "+")";
		db.execSQL(CREATE_CONTACTS_TABLE);
		db.execSQL(CREATE_PRESCRIBED_TABLE);
		db.execSQL(CREATE_ORDER_TABLE);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + DATE_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + PRESCRIBED_TABLE);
		// Create tables again
		onCreate(db);
	}

	/**
	 * set the date for both the eyes when the alarm has to go off
	 * @param dateLeftEye
	 * @param dateRightEye
	 */
	public void addDate(long dateLeftEye,long dateRightEye){
		SQLiteDatabase db=this.getWritableDatabase();

		ContentValues values=new ContentValues();
		values.put(LEFT_EYE_DATE, dateLeftEye);
		values.put(RIGHT_EYE_DATE, dateRightEye);
		//when first created none of them are notified
		values.put(LEFT_NOTIFIED, 0);
		values.put(RIGHT_NOTIFIED,0);

		db.insert(DATE_TABLE,null , values);
		db.close();
	}

	/**
	 * get the date when the alarm has to go off for left eye
	 * @return long which is the time 
	 */
	public long getLeftEyeDate(){
		String selectQuery="SELECT * FROM "+DATE_TABLE;
		long retVal=System.currentTimeMillis();
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(selectQuery, null);

		if (cursor.moveToLast()){
			retVal=cursor.getLong(LEFT_EYE_COLUMN);
		}
		db.close();
		return retVal;	
	}

	/**
	 * get the date when the alarm has to go off for right eye
	 * @return long which is the time 
	 */
	public long getRightEyeDate(){
		String selectQuery="SELECT * FROM "+DATE_TABLE;
		long retVal=System.currentTimeMillis();
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(selectQuery, null);

		if (cursor.moveToLast()){
			retVal=cursor.getLong(RIGHT_EYE_COLUMN);
		}
		db.close();
		return retVal;	
	}

	/**
	 * set the order data once its been received
	 * 
	 */
	public void addOrderDate(long orderDate){
		SQLiteDatabase db=this.getWritableDatabase();

		ContentValues values=new ContentValues();
		values.put(ORDER_LENS, orderDate);

		db.insert(ORDER_TABLE,null,values);
		db.close();
	}

	/**
	 * get the date when the alarm has to go off for the next prescription
	 * @return long which is the time 
	 */
	public long getOrderDate(){
		String selectQuery="SELECT * FROM "+ORDER_TABLE;
		long retVal=System.currentTimeMillis();
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(selectQuery, null);

		if (cursor.moveToLast()){
			retVal=cursor.getLong(ORDER_COLUMN);
		}

		db.close();
		return retVal;
	}

	/**
	 * get the id for ORDER_TABLE
	 * @return
	 */
	private int getOrderId(){
		String idQuery="SELECT "+ID+" FROM "+ORDER_TABLE;
		int retValue=-1;
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(idQuery, null);

		if (cursor.moveToLast()){
			retValue=cursor.getInt(0);
		}

		return retValue;
	}

	/**
	 * set the order be notified
	 * 
	 */	
	public void setOrderNotified(){
		SQLiteDatabase db=this.getWritableDatabase();

		String strFilter=ID + "=" +getOrderId();
		ContentValues values=new ContentValues();
		values.put(ORDER_NOTIFIED, 1);


		try{
			db.update(ORDER_TABLE,values,strFilter,null);
		}
		catch (Throwable e){
			Log.e("lens update",e.toString());
		}

		db.close();
	}

	/**
	 * get whether the order has been notified
	 * @return
	 */
	public boolean getOrderNotified(){
		String selectQuery="SELECT * FROM "+ORDER_TABLE;
		int retValue=-1;

		SQLiteDatabase db=this.getWritableDatabase();

		Cursor cursor=db.rawQuery(selectQuery, null);
		//close it so no need to worry about it later		

		if (cursor.moveToLast()){
			retValue=cursor.getInt(ORDER_NOTIFIED_COLUMN);
		}	

		db.close();
		if (retValue==1){
			return true;
		}
		return false;
	}

	/**
	 * add the prescription days to change the lens for both eyes
	 * @param leftDays
	 * @param rightDays
	 */
	public void addPrescribedDays(Integer leftDays,Integer rightDays,Integer order){
		SQLiteDatabase db=this.getWritableDatabase();

		ContentValues values=new ContentValues();
		values.put(LEFT_EYE_DATE, leftDays);
		values.put(RIGHT_EYE_DATE, rightDays);
		values.put(PRESCRIPTION, order);

		db.insert(PRESCRIBED_TABLE,null , values);
		db.close();
	}

	/**
	 * Number of days to change the right lens in
	 * @return int or 1 if no date prescribed
	 */
	public int getRightEyeDays(){
		String selectQuery="SELECT * FROM "+PRESCRIBED_TABLE;
		int retVal=1;

		SQLiteDatabase db=this.getWritableDatabase();
		try{
			Cursor cursor=db.rawQuery(selectQuery, null);

			if (cursor.moveToLast()){
				retVal=cursor.getInt(RIGHT_EYE_COLUMN);
			}	
		}
		catch(SQLiteException e){
			Log.e("getRighteye query","could not find prescribed table");
		}

		//close it so no need to worry about it later		
		db.close();
		return retVal;
	}



	/**
	 * Number of days to change the left lens in
	 * @return int or 1 if no date prescribed
	 */
	public int getLeftEyeDays(){
		String selectQuery="SELECT * FROM "+PRESCRIBED_TABLE;
		int retValue=1;

		SQLiteDatabase db=this.getWritableDatabase();

		Cursor cursor=db.rawQuery(selectQuery, null);
		//close it so no need to worry about it later		

		if (cursor.moveToLast()){
			retValue=cursor.getInt(LEFT_EYE_COLUMN);
		}	

		db.close();
		return retValue;
	}

	/**
	 * get the days for the prescription 
	 * @return at least one day or what has been set
	 */
	public int getOrderDays(){
		String selectQuery="SELECT * FROM "+PRESCRIBED_TABLE;
		int retValue=365; //make the prescription alarm yearly

		SQLiteDatabase db=this.getWritableDatabase();

		Cursor cursor=db.rawQuery(selectQuery, null);
		//close it so no need to worry about it later		

		if (cursor.moveToLast()){
			retValue=cursor.getInt(PRESCRIPTION_COLUMN);
		}	

		db.close();
		return retValue;
	}

	/**
	 * keeps track of prescription entered
	 * @return -1 if something goes wrong or the number of prescriptions stored in the table 
	 */
	public int getPrescriptionCount(){
		String countQuery="SELECT COUNT(*) FROM "+PRESCRIBED_TABLE;
		int retValue=1;
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(countQuery, null);

		if (cursor.moveToLast()){
			retValue=cursor.getInt(0);
		}
		db.close();
		return retValue;
	}

	/**
	 * get total numbers of dates entered
	 * @return -1 if no dates available or the num of rows 
	 */
	public int getDateCount(){
		String countQuery="SELECT COUNT(*) FROM "+DATE_TABLE;

		int retValue=1;
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(countQuery, null);

		if (cursor.moveToLast()){
			retValue=cursor.getInt(0);
		}		

		db.close();
		return retValue;
	}

	/**
	 * get the last id from the date table
	 * return -1 if none exists
	 */
	private int getLastId(){
		String idQuery="SELECT "+ID+" FROM "+DATE_TABLE;
		int retValue=-1;
		SQLiteDatabase db=this.getWritableDatabase();
		Cursor cursor=db.rawQuery(idQuery, null);

		if (cursor.moveToLast()){
			retValue=cursor.getInt(0);
		}

		return retValue;
	}

	/**
	 * 
	 */
	public void setleftNotified(){
		SQLiteDatabase db=this.getWritableDatabase();

		String strFilter=ID + "=" +getLastId();
		ContentValues values=new ContentValues();
		values.put(LEFT_NOTIFIED, 1);

		db.update(DATE_TABLE,values,strFilter,null);
		db.close();
	}

	/**
	 * 
	 */
	public void setRightNotified(){
		SQLiteDatabase db=this.getWritableDatabase();

		String strFilter=ID + "=" +getLastId();
		ContentValues values=new ContentValues();
		values.put(RIGHT_NOTIFIED, 1);


		try{
			db.update(DATE_TABLE,values,strFilter,null);
		}
		catch (Throwable e){
			Log.e("lens update",e.toString());
		}

		db.close();
	}

	/**
	 * 
	 * @return
	 */
	public boolean getLeftNotified(){
		String selectQuery="SELECT * FROM "+DATE_TABLE;
		int retValue=-1;

		SQLiteDatabase db=this.getWritableDatabase();

		Cursor cursor=db.rawQuery(selectQuery, null);
		//close it so no need to worry about it later		

		if (cursor.moveToLast()){
			retValue=cursor.getInt(LEFT_NOTIFIED_COLUMN);
		}	

		db.close();
		if (retValue==1){
			return true;
		}
		return false;
	}

	public boolean getRightNotified(){
		String selectQuery="SELECT * FROM "+DATE_TABLE;
		int retValue=-1;

		SQLiteDatabase db=this.getWritableDatabase();

		Cursor cursor=db.rawQuery(selectQuery, null);
		//close it so no need to worry about it later		

		if (cursor.moveToLast()){
			retValue=cursor.getInt(RIGHT_NOTIFIED_COLUMN);
		}	

		db.close();
		if (retValue==1){
			return true;
		}
		return false;
	}
}
