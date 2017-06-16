package com.boway.sale.broadcast;

import com.boway.sale.service.MSIMMODEService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.sax.StartElementListener;
import android.util.Log;

public class MSIMMODEReceiver extends BroadcastReceiver {

	private static final String TAG = "MSIMMODEReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
//		String action = intent.getAction();
//        Log.d(TAG, "jlzou mReceiver action = " + action);
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean needDisableSim1 = sp.getBoolean("needDisableSim1", false);
//        Log.i(TAG, "jlzou needDisableSim1:" + needDisableSim1);
//        boolean needDisableSim2 = sp.getBoolean("needDisableSim2", false);
//        Log.i(TAG, "jlzou needDisableSim2:" + needDisableSim2);
//        
//        if(needDisableSim1 || needDisableSim2){
//        	Intent service = new Intent(context,MSIMMODEService.class);
//        	context.startService(service);
//        }
        
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

}
