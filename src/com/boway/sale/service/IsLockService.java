package com.boway.sale.service;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.CloseRemindDialog;
import com.boway.sale.LockRemindDialog;
import com.boway.sale.db.dao.IMSIQueryDao;
import com.boway.sale.option.FeatureOption;

import android.app.Notification;
import android.app.Service;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class IsLockService extends Service {

	private static final String TAG = "IsLockService";
	private static final int MODE_PHONE1_ONLY = 1;
	
	private TelephonyManager telephony = null;
	private SharedPreferences sp ;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		telephony = TelephonyManager.from(this);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"IsLockService onStartCommand");
		Builder builder = new Notification.Builder(this);  
		startForeground(10011,builder.build());
		Intent service = new Intent(this,SubService.class);
		startService(service);
		
//		copPhoneAddressDB();
		LockTelephone(false);
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if (FeatureOption.MTK_GEMINI_SUPPORT) {
					Log.i(TAG,"jlzou FeatureOption.MTK_GEMINI_SUPPORT");
					
					String imsi1 = telephony.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
					String imsi2 = telephony.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
					int sim1State = telephony.getSimState(0);
					int sim2State = telephony.getSimState(1);
					int count = 1;
					while(!(( imsi1 != null || sim1State == TelephonyManager.SIM_STATE_ABSENT)
							&& ( imsi2 != null || sim2State == TelephonyManager.SIM_STATE_ABSENT)
							)){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sim1State = telephony.getSimState(0);
						sim2State = telephony.getSimState(1);
						imsi1 = telephony.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
						imsi2 = telephony.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
						count ++;
						Log.i(TAG,"count:" + count);
						Log.i(TAG,"sim1State:" + sim1State);
						Log.i(TAG,"sim2State:" + sim2State);
						Log.i(TAG,"imsi1:" + imsi1);
						Log.i(TAG,"imsi2:" + imsi2);
					}
					
					Log.i(TAG,"jlzou imsi1:" + imsi1);
					Log.i(TAG,"jlzou imsi2:" + imsi2);
					if((imsi1 == null || "".equals(imsi1)) && (imsi2 == null || "".equals(imsi2)))
					{
						LockTelephone(false);
					}else{
						if(imsiIsLegal(imsi1,imsi2)){
							LockTelephone(false);
						}else{
							LockTelephone(true);
						}
					}
				}else{
					Log.i(TAG,"jlzou not supper two sim card");
					String imsi1 = telephony.getSubscriberId();
					int sim1State = telephony.getSimState();
					int count = 1;
					while(!(((sim1State == TelephonyManager.SIM_STATE_READY && imsi1 != null) || sim1State == TelephonyManager.SIM_STATE_ABSENT)
							|| count > 50)){
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						sim1State = telephony.getSimState();
						imsi1 = telephony.getSubscriberId();;
						count ++;
						Log.i(TAG,"count:" + count);
						Log.i(TAG,"simState:" + sim1State);
						Log.i(TAG,"imsi:" + imsi1);
					}
					
					Log.i(TAG,"jlzou imsi:" + imsi1);
					if(imsi1 == null || "".equals(imsi1)){
						LockTelephone(false);
					}else{
						if(imsiIsLegal(imsi1,null)){
							LockTelephone(false);
						}else{
							LockTelephone(true);
						}
					}
				}
				
			}
		},0);
		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private int getSubIdBySlot(int slot) {
        int [] subId = SubscriptionManager.getSubId(slot);
        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
    }
	
	private boolean imsiIsLegal(String imsi1,String imsi2){
		boolean isLegal = false;
		boolean sim1IsLegal = false;
		boolean sim2IsLegal = false;
		boolean isOnlyOneSim = false;
		IMSIQueryDao dao = new IMSIQueryDao(this);
		
		if(imsi1 != null && imsi1.length() > 10)
		{
			String imsi1_new = imsi1.substring(0, 10);
			sim1IsLegal = dao.findImsi(imsi1_new);
		}else{
			isOnlyOneSim = true;
		}
		
		if(imsi2 != null && imsi2.length() > 10)
		{
			String imsi2_new = imsi2.substring(0, 10);
			sim2IsLegal = dao.findImsi(imsi2_new);
		}else{
			isOnlyOneSim = true;
		}
		
		Log.i(TAG,"jlzou imsi1 ten:" + imsi1);
		Log.i(TAG,"jlzou imsi2 ten:" + imsi2);
		
		if(sim1IsLegal || sim2IsLegal)
			isLegal = true;
		
		if((sim1IsLegal || sim2IsLegal) && !isOnlyOneSim){
			
			if(sim1IsLegal){
				boolean result = setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),true);
				Editor editor = sp.edit();
				editor.putBoolean("needDisableSim1", false);
				editor.commit();
				Log.i(TAG,"jlzou enable sim1:" + result);
			}else {
				Editor editor = sp.edit();
				editor.putBoolean("needDisableSim1", true);
				editor.commit();
//				if(isRadioOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),this)){
					boolean result =  setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),false);
					showRemindDialog(1);
					Log.i(TAG,"jlzou disable sim1:" + result);
//				}
			}
			if(sim2IsLegal){
				boolean result =  setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),true);
				Editor editor = sp.edit();
				editor.putBoolean("needDisableSim2", false);
				editor.commit();
				Log.i(TAG,"jlzou enable sim2:" + result);
			}else {
				Editor editor = sp.edit();
				editor.putBoolean("needDisableSim2", true);
				editor.commit();
//				if(isRadioOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),this)){
					boolean result =  setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),false);
					showRemindDialog(2);
					Log.i(TAG,"jlzou disable sim2:" + result);
//				}
				
			}
		}
		
		Log.i(TAG,"jlzou isLegal:" + isLegal);
		return isLegal;
		
	}
	
	private void LockTelephone(boolean enabling){
		if(enabling){
			Intent intent = new Intent(this,LockRemindDialog.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			        
			final ConnectivityManager mgr =
			       (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			mgr.setAirplaneMode(enabling);
			
			Editor editor = sp.edit();
			editor.putBoolean("needLock", true);
			editor.putBoolean("isAirplane", true);
			editor.commit();
		}else{
			Editor editor = sp.edit();
			editor.putBoolean("needLock", false);
			editor.putBoolean("isAirplane", false);
			editor.commit();
		}
		stopSelf();
	}
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

	private void showRemindDialog(int simId){
    	Intent intent = new Intent(this,CloseRemindDialog.class);
    	intent.putExtra("simId", simId);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    	startActivity(intent);
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
