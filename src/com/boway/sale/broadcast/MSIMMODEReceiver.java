package com.boway.sale.broadcast;

import com.boway.sale.PhoneNumberCheckActivity;
import com.boway.sale.service.MSIMMODEService;
import com.boway.sale.util.ServiceUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MSIMMODEReceiver extends BroadcastReceiver {

	private static final String TAG = "MSIMMODEReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
        Log.d(TAG, "jlzou mReceiver action = " + action);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean needLockSim1 = sp.getBoolean("needLockSim1", false);
        Log.i(TAG, "jlzou needDisableSim1:" + needLockSim1);
        boolean needLockSim2 = sp.getBoolean("needLockSim2", false);
        Log.i(TAG, "jlzou needDisableSim2:" + needLockSim2);
        
        if(needLockSim1 || needLockSim2){
//        	Intent service = new Intent(context,MSIMMODEService.class);
//        	context.startService(service);
        	startMSIMMODEService(context);
        }
        
//        String imsi1 = sp.getString("imsi1", "none");
//        String imsi2 = sp.getString("imsi2", "none");
//        if("none".equals(imsi1) && "none".equals(imsi2)){
//        	Intent intent2 = new Intent(context,
//                    PhoneNumberCheckActivity.class);
//            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent2);
//        }
        
        if(!sp.contains("needLockSim1") && !sp.contains("needLockSim2")){
        	Intent intent2 = new Intent(context,
                  PhoneNumberCheckActivity.class);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
        }
        
//        if(needDisableSim1){
//        	mHandler.sendEmptyMessage(1);
////        	setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),false);
//        }
//        
//        
//        if(needDisableSim2){
//        	mHandler.sendEmptyMessage(2);
////        	setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),false);
//        }

	}

	private void startMSIMMODEService(Context context) {
		boolean isRunning = ServiceUtils.isServiceRunning(context, "com.boway.sale.service.MSIMMODEService");
		if(!isRunning){
			Intent service = new Intent(context,MSIMMODEService.class);
        	context.startService(service);
		}
	}

}
