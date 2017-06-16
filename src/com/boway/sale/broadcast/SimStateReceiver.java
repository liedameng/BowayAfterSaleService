package com.boway.sale.broadcast;

import com.boway.sale.service.IsLockService;
import com.boway.sale.util.ServiceUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimStateReceiver extends BroadcastReceiver {

	private static final String TAG = "SimStateReceiver";
	private SharedPreferences sp;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"jlzou SimStateReceiver");
//		sp = PreferenceManager.getDefaultSharedPreferences(context);
//		int mSim1State = sp.getInt("sim1State", 0);
//		int mSim2State = sp.getInt("sim2State", 0);
//		Log.i(TAG,"jlzou mSim1State:" + mSim1State);
//		Log.i(TAG,"jlzou mSim2State:" + mSim2State);
//		TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE); 
//        
//        int sim1State = tm.getSimState(0);
//        switch (sim1State) {
//        case TelephonyManager.SIM_STATE_READY :
//        	Log.i(TAG,"jlzou sim1State SIM_STATE_READY");
//        	if(mSim1State != sim1State){
//        		Editor editor = sp.edit();
//        		editor.putInt("sim1State", TelephonyManager.SIM_STATE_READY);
//        		editor.commit();
//        		startLockService(context);
//        	}
//            break;
//        case TelephonyManager.SIM_STATE_ABSENT :
//        	Log.i(TAG,"jlzou sim1State SIM_STATE_ABSENT");
//        	if(mSim1State != sim1State){
//        		Editor editor = sp.edit();
//        		editor.putInt("sim1State", TelephonyManager.SIM_STATE_ABSENT);
//        		editor.commit();
//        		startLockService(context);
//        	}
//        	break;
//        case TelephonyManager.SIM_STATE_UNKNOWN :
//        case TelephonyManager.SIM_STATE_PIN_REQUIRED :
//        case TelephonyManager.SIM_STATE_PUK_REQUIRED :
//        case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
//        default:
//        	Log.i(TAG,"jlzou sim1State default");
//            break;
//        }
//        
//        int sim2State = tm.getSimState(1);
//        switch (sim2State) {
//        case TelephonyManager.SIM_STATE_READY :
//        	Log.i(TAG,"jlzou sim2State SIM_STATE_READY");
//        	if(mSim2State != sim2State){
//        		Editor editor = sp.edit();
//        		editor.putInt("sim2State", TelephonyManager.SIM_STATE_READY);
//        		editor.commit();
//        		startLockService(context);
//        	}
//            break;
//        case TelephonyManager.SIM_STATE_ABSENT :
//        	Log.i(TAG,"jlzou sim2State SIM_STATE_ABSENT");
//        	if(mSim2State != sim2State){
//        		Editor editor = sp.edit();
//        		editor.putInt("sim2State", TelephonyManager.SIM_STATE_ABSENT);
//        		editor.commit();
//        		startLockService(context);
//        	}
//        	break;
//        case TelephonyManager.SIM_STATE_UNKNOWN :
//        case TelephonyManager.SIM_STATE_PIN_REQUIRED :
//        case TelephonyManager.SIM_STATE_PUK_REQUIRED :
//        case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
//        default:
//        	Log.i(TAG,"jlzou sim2State default");
//            break;
//        }
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
