package com.boway.sale.util;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

public class LockSimUtils {
	private static final String TAG = "LockSimUtils";
	private static final int MODE_PHONE1_ONLY = 1;

	public static boolean setRadionOn(Context ctx,int subId, boolean turnOn) {
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
                    updateRadioMsimDb(ctx,subId, turnOn);
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


	private static void updateRadioMsimDb(Context ctx,int subId, boolean turnOn) {
        int priviousSimMode = Settings.System.getInt(ctx.getContentResolver(),
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
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.MSIM_MODE_SETTING, currentSimMode);
        } else {
            Log.w(TAG, "quickly click don't allow.");
        }
    }
	
	public static int getSubIdBySlot(int slot) {
        int [] subId = SubscriptionManager.getSubId(slot);
        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
    }
	
	public static boolean isRadioOn(int subId, Context context) {
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
