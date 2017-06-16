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
//	private final static int SIM_VALID = 0;
//    private final static int SIM_INVALID = 1;
//	private int simState = SIM_INVALID;
//	private boolean mIsChange = false;
//	private int mSim1State = TelephonyManager.SIM_STATE_UNKNOWN;
//	private int mSim2State = TelephonyManager.SIM_STATE_UNKNOWN;
	private SharedPreferences sp;
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"jlzou SimStateReceiver");
		sp = PreferenceManager.getDefaultSharedPreferences(context);
		int mSim1State = sp.getInt("sim1State", 0);
		int mSim2State = sp.getInt("sim2State", 0);
		Log.i(TAG,"jlzou mSim1State:" + mSim1State);
		Log.i(TAG,"jlzou mSim2State:" + mSim2State);
//		boolean isRunning = ServiceUtils.isServiceRunning(context, "com.boway.sale.service.IsLockService");
//		if(!isRunning){
//			Intent service = new Intent(context,IsLockService.class);
//        	context.startService(service);
//		}
		
//		mIsChange = false;
//		simState = SIM_INVALID;
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE); 
//        int state = tm.getSimState();
//        switch (state) {
//            case TelephonyManager.SIM_STATE_READY :
//            	Log.i(TAG,"jlzou SimStateReceiver SIM_STATE_READY");
//                break;
//            case TelephonyManager.SIM_STATE_ABSENT :
//            	Log.i(TAG,"jlzou SimStateReceiver SIM_STATE_ABSENT");
//            	break;
//            case TelephonyManager.SIM_STATE_UNKNOWN :
//            case TelephonyManager.SIM_STATE_PIN_REQUIRED :
//            case TelephonyManager.SIM_STATE_PUK_REQUIRED :
//            case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
//            default:
//            	Log.i(TAG,"jlzou SimStateReceiver default");
//                break;
//        }
        
        int sim1State = tm.getSimState(0);
        switch (sim1State) {
        case TelephonyManager.SIM_STATE_READY :
        	Log.i(TAG,"jlzou sim1State SIM_STATE_READY");
        	if(mSim1State != sim1State){
        		Editor editor = sp.edit();
        		editor.putInt("sim1State", TelephonyManager.SIM_STATE_READY);
        		editor.commit();
        		startLockService(context);
        	}
            break;
        case TelephonyManager.SIM_STATE_ABSENT :
        	Log.i(TAG,"jlzou sim1State SIM_STATE_ABSENT");
        	if(mSim1State != sim1State){
        		Editor editor = sp.edit();
        		editor.putInt("sim1State", TelephonyManager.SIM_STATE_ABSENT);
        		editor.commit();
        		startLockService(context);
        	}
        	break;
        case TelephonyManager.SIM_STATE_UNKNOWN :
        case TelephonyManager.SIM_STATE_PIN_REQUIRED :
        case TelephonyManager.SIM_STATE_PUK_REQUIRED :
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
        default:
        	Log.i(TAG,"jlzou sim1State default");
            break;
        }
        
        int sim2State = tm.getSimState(1);
        switch (sim2State) {
        case TelephonyManager.SIM_STATE_READY :
        	Log.i(TAG,"jlzou sim2State SIM_STATE_READY");
        	if(mSim2State != sim2State){
        		Editor editor = sp.edit();
        		editor.putInt("sim2State", TelephonyManager.SIM_STATE_READY);
        		editor.commit();
        		startLockService(context);
        	}
            break;
        case TelephonyManager.SIM_STATE_ABSENT :
        	Log.i(TAG,"jlzou sim2State SIM_STATE_ABSENT");
        	if(mSim2State != sim2State){
        		Editor editor = sp.edit();
        		editor.putInt("sim2State", TelephonyManager.SIM_STATE_ABSENT);
        		editor.commit();
        		startLockService(context);
        	}
        	break;
        case TelephonyManager.SIM_STATE_UNKNOWN :
        case TelephonyManager.SIM_STATE_PIN_REQUIRED :
        case TelephonyManager.SIM_STATE_PUK_REQUIRED :
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
        default:
        	Log.i(TAG,"jlzou sim2State default");
            break;
        }
            
//        
//        if(simState == SIM_VALID){
//        	Log.i(TAG,"jlzou SimStateReceiver SIM_VALID");
//        	List<SubscriptionInfo> subInfoList;
//    	    subInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
//    	    List<String> iccId = new ArrayList<String>();
//    	    if (subInfoList == null || subInfoList.size() == 0) {
//    	        Log.d(TAG, "isLocalNumber SIM not insert");
//    	    } else {
//    	        
//    	        for (SubscriptionInfo subInfoRecord : subInfoList) {
//    	             String iccIdTmp = subInfoRecord.getIccId();
//    	             iccId.add(iccIdTmp);
//    	             Log.i(TAG,"jlzou iccIdTmp:" + iccIdTmp);
//    	        } 
//    	    }
//
//    	    if(iccId.size() == 0){
//            	return;
//            }
//    	    
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//            String old = sp.getString("iccId", "");
//            
//            String[] oldIccId = old.split(";");
//            for(String id : oldIccId){
//            	Log.i(TAG,"jlzou id:" + id);
//            	if(!iccId.contains(id)){
//            		mIsChange = true;
//            		break;
//            	}
//            }
//            if(mIsChange){
//            	Intent service = new Intent(context,IsLockService.class);
//            	context.startService(service);
//            }
//        }
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
