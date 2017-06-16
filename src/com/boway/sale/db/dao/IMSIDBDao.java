package com.boway.sale.db.dao;

import com.boway.sale.db.IMSIDBHelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IMSIDBDao {
	private static final String TAG = "IMSIDBDao";
	private Context mContext;
	private IMSIDBHelper mHelper;
	
    public IMSIDBDao(Context context){
    	mHelper = new IMSIDBHelper(context);
    	mContext = context;
    }
    
    public void insertIMSI(String imsi,int isLegal){
    	SQLiteDatabase db = mHelper.getWritableDatabase();
    	db.execSQL("insert into IMSI(imsi,isLegal) values(?,?)", new String[]{
    			imsi,String.valueOf(isLegal)});
    	db.close();
    }
    
    public boolean imsiIsLegal(String imsi){
    	boolean isLegal = false;
    	SQLiteDatabase db = mHelper.getReadableDatabase();
    	Cursor cursor = db.rawQuery("select * from IMSI where imsi = ?",new String[]{imsi});
    	while(cursor != null && cursor.moveToNext()){
    		int status = cursor.getInt(cursor.getColumnIndex("isLegal"));
    		if(1 == status){
    			isLegal = true;
    		}
    	}
    	cursor.close();
    	db.close();
    	Log.i(TAG, imsi + ":" + isLegal);
    	return isLegal;
    }
}
