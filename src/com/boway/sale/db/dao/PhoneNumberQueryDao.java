package com.boway.sale.db.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.boway.sale.SaleApplication;
import com.boway.sale.db.PhoneNumberDBHelper;

public class PhoneNumberQueryDao {
	private static final String TAG = null;
	private PhoneNumberDBHelper mHelper;
	private Context mContext;
    public PhoneNumberQueryDao(Context context){
    	mHelper = new PhoneNumberDBHelper(context);
    	mContext = context;
    }
    
    public synchronized boolean findImsi(String number1,String number2){
    	boolean isFind = false;
    	SQLiteDatabase db = mHelper.getReadableDatabase();
    	db.close();
    	copPhoneAddressDB();
    	File file = mContext.getFilesDir();
		SQLiteDatabase sql = SQLiteDatabase.openDatabase(file.getAbsolutePath()+"/phone_number.db", null, SQLiteDatabase.OPEN_READONLY);
    	Cursor cursor = sql.rawQuery("select number from NUMBER_TB where number = ? or number = ?", new String[]{number1,number2});
    	if(cursor != null && cursor.moveToNext())
		{
    		isFind = true;
		}
    	cursor.close();
    	sql.close();
    	return isFind;
    }
    
    public synchronized boolean findNumber(String number){
    	boolean isFind = false;
    	SQLiteDatabase db = mHelper.getReadableDatabase();
    	db.close();
    	copPhoneAddressDB();
    	File file = mContext.getFilesDir();
		SQLiteDatabase sql = SQLiteDatabase.openDatabase(file.getAbsolutePath()+"/phone_number.db", null, SQLiteDatabase.OPEN_READONLY);
    	Cursor cursor = sql.rawQuery("select number from NUMBER_TB where number = ?", new String[]{number});
    	if(cursor != null && cursor.moveToNext())
		{
    		isFind = true;
		}
    	cursor.close();
    	sql.close();
    	return isFind;
    } 
    
    private void copPhoneAddressDB() {
		Log.i(TAG,"jlzou copPhoneAddressDB");
		File file = new File(mContext.getFilesDir(), "phone_number.db");
		if(file.exists() && file.length() > 0)
		{
			Log.i(TAG,"no need copy DB");
			return;
		}
		else
		{
			try {
				InputStream in = mContext.getAssets().open(SaleApplication.getDatabaseName());
				byte[] buff = new byte[1024]; 
				FileOutputStream out = new FileOutputStream(file);
				int len = 0;
				while((len = in.read(buff, 0, 1024)) != -1)
				{
					out.write(buff, 0, len);
				}
				in.close();
				out.close();
			} catch (IOException e) {
				Log.i(TAG,"jlzou copPhoneAddressDB failed");
				e.printStackTrace();
			}
		}
	}
}
