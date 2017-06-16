package com.boway.sale.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.option.FeatureOption;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class ServiceUtils {
	private static final String TAG = "ServiceUtils";
	
	public static boolean isServiceRunning(Context context,
			String serviceClassName){
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> infos = am.getRunningServices(Integer.MAX_VALUE);
		for(RunningServiceInfo info : infos)
		{
			String name = info.service.getClassName();
			if(name.equals(serviceClassName))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean needCheckAgain(Context ctx) {
		TelephonyManager telephony = TelephonyManager.from(ctx);
		Log.i(TAG,"checkSimExists");
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			Log.i(TAG,"FeatureOption.MTK_GEMINI_SUPPORT");
			boolean isSim1Valide = TelephonyManager.SIM_STATE_READY == telephony
					.getSimState(PhoneConstants.SIM_ID_1);
			boolean isSim2Valide = TelephonyManager.SIM_STATE_READY == telephony
					.getSimState(PhoneConstants.SIM_ID_2);
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
			
			if(!isSim1Valide && !isSim2Valide){
				Log.d(TAG, "two SIM not insert");
				Editor editor = sp.edit();
	    		editor.putStringSet("iccId", null);
	    		editor.putBoolean("needLock", false);
	    		editor.commit();
		    	return false;
			}
			
//			String phoneNum1 = telephony.getLine1NumberForSubscriber(0);
//		    String phoneNum2 = telephony.getLine1NumberForSubscriber(1);
//		    
//		    String imsi1 = telephony.getSubscriberId(PhoneConstants.SIM_ID_1);
//			String imsi2 = telephony.getSubscriberId(PhoneConstants.SIM_ID_2);
//			String imsi3 = telephony.getSubscriberId(PhoneConstants.SIM_ID_3);
//			String imsi4 = telephony.getSubscriberId(PhoneConstants.SIM_ID_4);
//			Log.i(TAG,"jlzou imsi1:" + imsi1);
//			Log.i(TAG,"jlzou imsi2:" + imsi2);
//			Log.i(TAG,"jlzou imsi3:" + imsi3);
//			Log.i(TAG,"jlzou imsi4:" + imsi4);
			
//		    Log.i(TAG,"jlzou phoneNum1:" + phoneNum1);
//		    Log.i(TAG,"jlzou phoneNum2:" + phoneNum2);

		    List<SubscriptionInfo> subInfoList;
		    subInfoList = SubscriptionManager.from(ctx).getActiveSubscriptionInfoList();
		    Set<String> iccId = new HashSet<String>();
		    if (subInfoList == null || subInfoList.size() == 0) {
		        Log.d(TAG, "isLocalNumber SIM not insert");
		    } else {
		        
		        for (SubscriptionInfo subInfoRecord : subInfoList) {
		             String iccIdTmp = subInfoRecord.getIccId();
		             iccId.add(iccIdTmp);
		        } 
		    }
		    Log.i(TAG,"#jlzou iccId#:" + iccId);
		    
		    if(iccId.size() <= 0){
		    	Log.i(TAG,"jlzou iccId is null");
		    	Editor editor = sp.edit();
	    		editor.putStringSet("iccId", iccId);
	    		editor.putBoolean("needLock", false);
	    		editor.commit();
		    	return false;
		    }
		    
		    Set<String> oldIccId;
		    oldIccId = sp.getStringSet("iccId", null);
		    if(oldIccId == null || oldIccId.size() <= 0){
		    	Log.i(TAG,"jlzou oldIccId is null");
		    	Editor editor = sp.edit();
	    		editor.putStringSet("iccId", iccId);
	    		editor.commit();
	    		return true;
		    }
		    for(String id : oldIccId){
		    	Log.i(TAG,"jlzou id:" + id);
		    	if(!iccId.contains(id)){
		    		Log.i(TAG,"jlzou iccId dont contains " + id);
		    		Editor editor = sp.edit();
		    		editor.putStringSet("iccId", iccId);
		    		editor.commit();
		    		return true;
		    	}
		    }
			
//			if(isSim1Valide || isSim2Valide){
//				return true;
//			}
		} else {
			Log.i(TAG,"no double");
			
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
			int simState = telephony.getSimState();
			
			if(simState != TelephonyManager.SIM_STATE_READY){
				Editor editor = sp.edit();
	    		editor.putStringSet("iccId", null);
	    		editor.putBoolean("needLock", false);
	    		editor.commit();
		    	return false;
			}
			
			List<SubscriptionInfo> subInfoList;
		    subInfoList = SubscriptionManager.from(ctx).getActiveSubscriptionInfoList();
		    Set<String> iccId = new HashSet<String>();
		    if (subInfoList == null || subInfoList.size() == 0) {
		        Log.d(TAG, "isLocalNumber SIM not insert");
		    } else {
		        
		        for (SubscriptionInfo subInfoRecord : subInfoList) {
		             String iccIdTmp = subInfoRecord.getIccId();
		             iccId.add(iccIdTmp);
		        } 
		    }
		    Log.i(TAG,"iccId:" + iccId);
		    
		    if(iccId.size() <= 0){
		    	Editor editor = sp.edit();
	    		editor.putStringSet("iccId", iccId);
	    		editor.putBoolean("needLock", false);
	    		editor.commit();
		    	return false;
		    }
		    
		    Set<String> oldIccId;
		    oldIccId = sp.getStringSet("iccId", null);
		    if(oldIccId == null || oldIccId.size() <= 0){
		    	Editor editor = sp.edit();
	    		editor.putStringSet("iccId", iccId);
	    		editor.commit();
	    		return true;
		    }
		    for(String id : oldIccId){
		    	Log.i(TAG,"jlzou id:" + id);
		    	if(!iccId.contains(id)){
		    		Log.i(TAG,"jlzou iccId dont contains " + id);
		    		Editor editor = sp.edit();
		    		editor.putStringSet("iccId", iccId);
		    		editor.commit();
		    		return true;
		    	}
		    }
		}
		return false;
	}
}
