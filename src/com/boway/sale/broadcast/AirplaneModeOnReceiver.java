package com.boway.sale.broadcast;

import com.boway.sale.LockRemindDialog;
import com.boway.sale.service.IsLockService;
import com.boway.sale.util.ServiceUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telecom.Log;

public class AirplaneModeOnReceiver extends BroadcastReceiver {

	private static final String TAG = "AirplaneModeOnReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"jlzou AirplaneModeOnReceiver");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isNeedLock = sp.getBoolean("needLock", false);
		if(isNeedLock){
			if(!isAirPlaneModeOn(context))
			    LockTelephone(context,true);
    	}else{
    		if(!isAirPlaneModeOn(context)){
    			startLockService(context);
    		}
    	}
		
//		if(!isAirPlaneModeOn(context)){
//			startLockService(context);
//		}
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
	
//	private void LockTelephone(final Context context,final boolean enabling){
//		new Handler().postDelayed(new Runnable(){
//
//			@Override
//			public void run() {
//				Log.i(TAG,"LockTelephone");
//				Intent intent = new Intent(context,LockRemindDialog.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//				context.startActivity(intent);
//		        Settings.Global.putInt(context.getContentResolver(),  
//		                Settings.Global.AIRPLANE_MODE_ON,enabling ? 1 : 0);  
//		        Intent broadcast = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);  
//		        broadcast.putExtra("state", enabling);  
//		        context.sendBroadcast(intent);
//			}
//			
//		}, 30 * 1000L);
//	}
	
	private void LockTelephone(Context context,boolean enabling){
		Log.i(TAG,"LockTelephone");
		Intent intent = new Intent(context,LockRemindDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
		final ConnectivityManager mgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mgr.setAirplaneMode(enabling);
	}

	private void startLockService(Context context){
		boolean isRunning = ServiceUtils.isServiceRunning(context, "com.boway.sale.service.IsLockService");
		if(!isRunning){
			Intent service = new Intent(context,IsLockService.class);
        	context.startService(service);
		}else{
			Log.i(TAG,"jlzou startLockService is running");
		}
	}
	
}
