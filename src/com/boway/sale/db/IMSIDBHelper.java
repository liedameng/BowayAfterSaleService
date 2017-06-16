package com.boway.sale.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class IMSIDBHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "imsi_info.db";
	public IMSIDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table IMSI (_id integer primary key autoincrement," +
				"imsi varchar(20),isLegal integer)");
		
		int isLegal = 1;
		db.execSQL("insert into IMSI(imsi,isLegal) values(?,?)", new String[]{
    			"460003742128328",String.valueOf(isLegal)});
		
		db.execSQL("insert into IMSI(imsi,isLegal) values(?,?)", new String[]{
    			"460004970560881",String.valueOf(isLegal)});
		
		db.execSQL("insert into IMSI(imsi,isLegal) values(?,?)", new String[]{
    			"460078136815066",String.valueOf(isLegal)});

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
