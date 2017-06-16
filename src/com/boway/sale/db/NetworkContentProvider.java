package com.boway.sale.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class NetworkContentProvider extends ContentProvider {
	
	static final String TAG = "MessageContentProvider";

	public static final String NETWORK_DATA_PROVIDER = "content://com.boway.saleservice.network.data.provider";
	
	private SQLiteDatabase sdb = null;
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "imei_sent.db3";
	
	private static final String EXECUTE_CREATE_SQL = "create table if not exists network_imei " +
			"(_id integer primary key autoincrement, isSent integer default 0, " +
			"failurecount integer default 0, isRegister integer default 0, " + 
			"successText text)";

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		sdb.beginTransaction();
        try{
	        long id = sdb.insert("network_imei", null, contentValues);
	        if(id > 0){
	        	Uri mUri = ContentUris.withAppendedId(uri, id);
	        	getContext().getContentResolver().notifyChange(mUri, null);
	        	sdb.setTransactionSuccessful();
	        	return mUri;
	        }
        } catch (Exception e){
        	Log.e(TAG, "insert data happend a exception!", e.getCause());
        } finally {
        	sdb.endTransaction();
        }
		return null;
	}

	@Override
	public boolean onCreate() {
		sdb = getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
		executeSQL(sdb, EXECUTE_CREATE_SQL);
		return false;
	}
	
	private void executeSQL(SQLiteDatabase db, String sql) {
        db.beginTransaction();
        try {
        	db.execSQL(sql);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.e(TAG, "happened a exception when create table", e.getCause());
        }finally {
            db.endTransaction();
        }
    }

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		return sdb.query("network_imei", null, null, null, null, null,null);
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
		return sdb.update("network_imei", contentValues, selection, selectionArgs);
	}

}
