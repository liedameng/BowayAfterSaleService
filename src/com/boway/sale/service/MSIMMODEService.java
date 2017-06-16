package com.boway.sale.service;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.util.RemindDialogUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;

public class MSIMMODEService extends Service {

	private static final String TAG = "MSIMMODEService";
	private static final int MODE_PHONE1_ONLY = 1;
	private SharedPreferences sp;
	private RemindDialogUtil dialogUtil;
	
	private boolean needLockSim1;
	private boolean needLockSim2;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		dialogUtil = new RemindDialogUtil(this);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
        needLockSim1 = sp.getBoolean("needLockSim1", false);
        Log.i(TAG, "jlzou needDisableSim1:" + needLockSim1);
        
        needLockSim2 = sp.getBoolean("needLockSim2", false);
        Log.i(TAG, "jlzou needDisableSim2:" + needLockSim2);
        
        if(needLockSim1){
        	dialogUtil.addView(MSIMMODEService.this);
        	mHandler.sendEmptyMessage(1);
        }else if(needLockSim2){
        	dialogUtil.addView(MSIMMODEService.this);
        	mHandler.sendEmptyMessage(2);
        }
        
		return super.onStartCommand(intent, flags, startId);
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Log.i(TAG, "jlzou 1:");

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
							
						if (isRadioOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),
								MSIMMODEService.this)) {
							Log.i(TAG, "jlzou setRadionOn1:");
							setRadionOn(
									getSubIdBySlot(PhoneConstants.SIM_ID_1),
									false);
						}else{
							Log.i(TAG, "jlzou isRadioOn1 false ");
						}
						if(needLockSim2){
				        	mHandler.sendEmptyMessage(2);
				        }else{
				        	dialogUtil.removeView(MSIMMODEService.this);
				        	stopSelf();
				        }
					}
				}, 5 * 1000L);

				break;
			case 2:
				Log.i(TAG, "jlzou 2:");
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						if (isRadioOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),
								MSIMMODEService.this)) {
							Log.i(TAG, "jlzou setRadionOn2:");
							setRadionOn(
									getSubIdBySlot(PhoneConstants.SIM_ID_2),
									false);
						}
						else{
							Log.i(TAG, "jlzou isRadioOn2 false ");
						}
						
						dialogUtil.removeView(MSIMMODEService.this);
						stopSelf();
					}
				}, 5 * 1000L);
				break;
			}
		}
	};
    
//    private void showRemindDialog(int simId){
//    	Intent intent = new Intent(this,CloseRemindDialog.class);
//    	intent.putExtra("simId", simId);
//    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//    	startActivity(intent);
//    }

    public boolean setRadionOn(int subId, boolean turnOn) {
        Log.d(TAG, "setRadionOn, turnOn: " + turnOn + ", subId = " + subId);
        boolean isSuccessful = false;
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return isSuccessful;
        }   
        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(
                Context.TELEPHONY_SERVICE));
        try {
            if (telephony != null) {
                isSuccessful = telephony.setRadioForSubscriber(subId, turnOn);
                if (isSuccessful) {
                    updateRadioMsimDb(subId, turnOn);
                    /// M: for plug-in
//                    mExt.setRadioPowerState(subId, turnOn);
                }   
            } else {
                Log.d(TAG, "telephony is null");
            }   
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
        Log.d(TAG, "setRadionOn, isSuccessful: " + isSuccessful);
        return isSuccessful;
    }   


	private void updateRadioMsimDb(int subId, boolean turnOn) {
        int priviousSimMode = Settings.System.getInt(getContentResolver(),
                Settings.System.MSIM_MODE_SETTING, -1);
        Log.i(TAG, "updateRadioMsimDb, The current dual sim mode is " + priviousSimMode
                + ", with subId = " + subId);
        int currentSimMode;
        boolean isPriviousRadioOn = false;
        int slot = SubscriptionManager.getSlotId(subId);
        int modeSlot = MODE_PHONE1_ONLY << slot;
        if ((priviousSimMode & modeSlot) > 0) {
            currentSimMode = priviousSimMode & (~modeSlot);
            isPriviousRadioOn = true;
        } else {
            currentSimMode = priviousSimMode | modeSlot;
            isPriviousRadioOn = false;
        }

        Log.d(TAG, "currentSimMode=" + currentSimMode + " isPriviousRadioOn =" + isPriviousRadioOn
                + ", turnOn: " + turnOn);
        if (turnOn != isPriviousRadioOn) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MSIM_MODE_SETTING, currentSimMode);
        } else {
            Log.w(TAG, "quickly click don't allow.");
        }
    }
	
	private int getSubIdBySlot(int slot) {
        int [] subId = SubscriptionManager.getSubId(slot);
        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
    }
	
	public boolean isRadioOn(int subId, Context context) {
	    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
		           .getService(Context.TELEPHONY_SERVICE));
	    boolean isOn = false;
	    try {
		    if (phone != null) {
		        isOn = subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ? false :
		                 phone.isRadioOnForSubscriber(subId, null);
		    } else {
	            Log.d(TAG, "phone is null");
			}
		} catch (RemoteException e) {
		    e.printStackTrace();
		}
        Log.d(TAG, "isOn = " + isOn + ", subId: " + subId);
        return isOn;
    }
}
