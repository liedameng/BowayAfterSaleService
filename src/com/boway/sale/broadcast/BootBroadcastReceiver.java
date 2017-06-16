package com.boway.sale.broadcast;

import com.boway.sale.db.MessageContentProvider;
import com.boway.sale.db.NetworkContentProvider;
import com.boway.sale.service.IsLockService;
import com.boway.sale.service.NetworkSendService;
import com.boway.sale.service.SendMessageService;
import com.boway.sale.util.ServiceUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "BootBroadcastReceiver";
	
//	static final long DELAY_TIME = 4 * 3600 * 1000;
	static final long DELAY_TIME = 1 * 60 * 1000L;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "BootBroadcastReceiver");
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean byMyself = sp.getBoolean("isAirplane", false);
		if(isAirPlaneModeOn(context) && byMyself){
			Editor editor = sp.edit();
			editor.putBoolean("needLock", false);
			editor.putBoolean("isAirplane", false);
			editor.commit();
			final ConnectivityManager mgr =
	                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        mgr.setAirplaneMode(false);
		}
		
		boolean isRunning = ServiceUtils.isServiceRunning(context, "com.boway.sale.service.IsLockService");
		if(!isRunning){
			Intent lockService = new Intent(context,IsLockService.class);
			context.startService(lockService);
		}
			
		Intent mIntent = new Intent(context,SendMessageService.class);
		
//		if(!isFirstSent(context)) {
//			startSendService(context, mIntent);
//		} else {
//			context.startService(mIntent);
//		}
		context.startService(mIntent);
		
		Intent netIntent = new Intent(context,NetworkSendService.class);
		Log.e(TAG, "-------------------!isNetworkFirstSent----------------------" + !isNetworkFirstSent(context));
//		if(!isNetworkFirstSent(context)) {
//			startSendService(context, netIntent);
//		} else {
//			context.startService(netIntent);
//		}
		context.startService(netIntent);
	}
	
	private void startSendService(Context context, Intent intent) {
		PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + DELAY_TIME, pendingIntent);
	}
	
	boolean isFirstSent(Context c) {
		Cursor cursor = null;
		try {
			cursor = c.getContentResolver().query(
					Uri.parse(MessageContentProvider.DATA_PROVIDER), null,
					null, null, null);
			if (cursor != null && cursor.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}
	
	boolean isNetworkFirstSent(Context c) {
		Cursor cursor = null;
		try {
			cursor = c.getContentResolver().query(
					Uri.parse(NetworkContentProvider.NETWORK_DATA_PROVIDER), null,
					null, null, null);
			if (cursor != null && cursor.moveToNext()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}
	
	private boolean isAirPlaneModeOn(Context context){
        int mode = 0;
        try {
            mode = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON);
        }catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
            return mode == 1;
    }

}
