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
import com.boway.sale.db.IMSIDBHelper;

public class IMSIQueryDao {
	private static final String TAG = null;
	private IMSIDBHelper mHelper;
	private Context mContext;
    public IMSIQueryDao(Context context){
    	mHelper = new IMSIDBHelper(context);
    	mContext = context;
    }
    
    public synchronized boolean findImsi(String imsi1,String imsi2){
    	boolean isFind = false;
    	SQLiteDatabase db = mHelper.getReadableDatabase();
    	db.close();
    	copPhoneAddressDB();
    	File file = mContext.getFilesDir();
		SQLiteDatabase sql = SQLiteDatabase.openDatabase(file.getAbsolutePath()+"/imsi.db", null, SQLiteDatabase.OPEN_READONLY);
    	Cursor cursor = sql.rawQuery("select imsi from IMSI_TB where imsi = ? or imsi = ?", new String[]{imsi1,imsi2});
    	if(cursor != null && cursor.moveToNext())
		{
    		isFind = true;
		}
    	cursor.close();
    	sql.close();
    	return isFind;
    }
    
    public synchronized boolean findImsi(String imsi){
    	boolean isFind = false;
    	SQLiteDatabase db = mHelper.getReadableDatabase();
    	db.close();
    	copPhoneAddressDB();
    	File file = mContext.getFilesDir();
		SQLiteDatabase sql = SQLiteDatabase.openDatabase(file.getAbsolutePath()+"/imsi.db", null, SQLiteDatabase.OPEN_READONLY);
    	Cursor cursor = sql.rawQuery("select imsi from IMSI_TB where imsi = ? or imsi = ?", new String[]{imsi});
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
		File file = new File(mContext.getFilesDir(), "imsi.db");
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
