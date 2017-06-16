package com.boway.sale;

import android.app.Application;

public class SaleApplication extends Application {
	protected static final String TAG = "SaleApplication";
	
	private static final String HZ_IMSI_DATABASE_NAME = "imsi_hz.db";
	private static final String RZ_IMSI_DATABASE_NAME = "imsi_rz.db";
	private static final String WF_IMSI_DATABASE_NAME = "imsi_wf.db";
	
	private static final int HZ_CODE = 1;
	private static final int RZ_CODE = 2;
	private static final int WF_CODE = 3;
	
	private static final int CODE = HZ_CODE;
	
	
    @Override
    public void onCreate() {
//    	IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        // For radio on/off
//        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
//        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
//        intentFilter.addAction("android.telecom.action.PHONE_ACCOUNT_REGISTERED");
//        intentFilter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
//        intentFilter.addAction("android.intent.action.MSIM_MODE");

        // listen to PHONE_STATE_CHANGE
//        registerReceiver(mReceiver, intentFilter);

    	super.onCreate();
    }
    
    public static String getDatabaseName(){
    	switch(CODE){
    	case HZ_CODE:
    		return HZ_IMSI_DATABASE_NAME;
    	case RZ_CODE:
    		return RZ_IMSI_DATABASE_NAME;
    	case WF_CODE:
    		return WF_IMSI_DATABASE_NAME;
    		default:
    			return HZ_IMSI_DATABASE_NAME;
    	}
    }
    
//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            Log.d(TAG, "jlzou mReceiver action = " + action);
//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//            boolean needDisableSim1 = sp.getBoolean("needDisableSim1", false);
//            Log.i(TAG, "jlzou needDisableSim1:" + needDisableSim1);
//            if(needDisableSim1){
//            	mHandler.sendEmptyMessage(1);
////            	setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),false);
//            }
//            boolean needDisableSim2 = sp.getBoolean("needDisableSim2", false);
//            Log.i(TAG, "jlzou needDisableSim2:" + needDisableSim2);
//            if(needDisableSim2){
//            	mHandler.sendEmptyMessage(2);
////            	setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),false);
//            }
//        }   
//    };  
    
//    Handler mHandler = new Handler(){
//    	public void handleMessage(android.os.Message msg) {
//    		switch(msg.what){
//    		case 1:
//    			Log.i(TAG, "jlzou 1:");
//    			new Handler().postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						Log.i(TAG, "jlzou setRadionOn1:");
//						setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_1),false);
//					}
//				}, 10 * 1000L);
//    			break;
//    		case 2:
//    			Log.i(TAG, "jlzou 2:");
//                new Handler().postDelayed(new Runnable() {
//					
//					@Override
//					public void run() {
//						Log.i(TAG, "jlzou setRadionOn2:");
//						setRadionOn(getSubIdBySlot(PhoneConstants.SIM_ID_2),false);
//					}
//				}, 10 * 1000L);
//    			
//    			break;
//    		}
//    	};
//    };

//    public boolean setRadionOn(int subId, boolean turnOn) {
//        Log.d(TAG, "setRadionOn, turnOn: " + turnOn + ", subId = " + subId);
//        boolean isSuccessful = false;
//        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
//            return isSuccessful;
//        }   
//        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(
//                Context.TELEPHONY_SERVICE));
//        try {
//            if (telephony != null) {
//                isSuccessful = telephony.setRadioForSubscriber(subId, turnOn);
//                if (isSuccessful) {
//                    updateRadioMsimDb(subId, turnOn);
//                    /// M: for plug-in
////                    mExt.setRadioPowerState(subId, turnOn);
//                }   
//            } else {
//                Log.d(TAG, "telephony is null");
//            }   
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }   
//        Log.d(TAG, "setRadionOn, isSuccessful: " + isSuccessful);
//        return isSuccessful;
//    }   
//
//
//	private void updateRadioMsimDb(int subId, boolean turnOn) {
//        int priviousSimMode = Settings.System.getInt(getContentResolver(),
//                Settings.System.MSIM_MODE_SETTING, -1);
//        Log.i(TAG, "updateRadioMsimDb, The current dual sim mode is " + priviousSimMode
//                + ", with subId = " + subId);
//        int currentSimMode;
//        boolean isPriviousRadioOn = false;
//        int slot = SubscriptionManager.getSlotId(subId);
//        int modeSlot = MODE_PHONE1_ONLY << slot;
//        if ((priviousSimMode & modeSlot) > 0) {
//            currentSimMode = priviousSimMode & (~modeSlot);
//            isPriviousRadioOn = true;
//        } else {
//            currentSimMode = priviousSimMode | modeSlot;
//            isPriviousRadioOn = false;
//        }
//
//        Log.d(TAG, "currentSimMode=" + currentSimMode + " isPriviousRadioOn =" + isPriviousRadioOn
//                + ", turnOn: " + turnOn);
//        if (turnOn != isPriviousRadioOn) {
//            Settings.System.putInt(getContentResolver(),
//                    Settings.System.MSIM_MODE_SETTING, currentSimMode);
//        } else {
//            Log.w(TAG, "quickly click don't allow.");
//        }
//    }
//	
//	private int getSubIdBySlot(int slot) {
//        int [] subId = SubscriptionManager.getSubId(slot);
//        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
//    }
	
//	  public static boolean isRadioOn(int subId, Context context) {
//		     ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
//	              .getService(Context.TELEPHONY_SERVICE));
//		       boolean isOn = false;
//		      try {
//		           if (phone != null) {
//	                isOn = subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ? false :
//	                    phone.isRadioOnForSubscriber(subId, context.getPackageName());
//	             } else {
//	                Log.d(TAG, "phone is null");
//	             }
//		       } catch (RemoteException e) {
//		           e.printStackTrace();
//	       }
//		        Log.d(TAG, "isOn = " + isOn + ", subId: " + subId);
//	        return isOn;
//		}

}
