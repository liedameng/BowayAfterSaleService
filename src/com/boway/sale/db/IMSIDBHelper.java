package com.boway.sale.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.boway.sale.SaleApplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IMSIDBHelper extends SQLiteOpenHelper {
    private Context mContext; 
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "version.db";
	private static final String TAG = "IMSIDBHelper";
	
	public IMSIDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		Log.i(TAG,mContext.getDatabasePath(DATABASE_NAME).getAbsolutePath());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG,"onCreate");
		copPhoneAddressDB();
        
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG,"onUpgrade");
		copPhoneAddressDB();

	}
	
	private void copPhoneAddressDB() {
		Log.i(TAG,"jlzou copPhoneAddressDB");
		File file = new File(mContext.getFilesDir(), "imsi.db");
		if(file.exists() && file.length() > 0)
		{
			Log.i(TAG,"jlzou copPhoneAddressDB is exsit");
			boolean isDel = file.delete();
			Log.i(TAG,"jlzou isDel:" + isDel);
		}
		
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
