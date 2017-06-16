package com.boway.sale.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.R;
import com.boway.sale.util.Filed;
import com.mediatek.telephony.SmsManagerEx;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SendMessageService extends Service {
	
	private static final String TAG = "SendMessageService";
	
	private StringBuffer rootSendMessage = new StringBuffer();
	private String [] imeiMapping = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
	private TelephonyManager telephony = null;
	private static final String TARGET_NUMBER = "106905501868";
    private static final String TARGET_OLD_NUMBER = "10690999095";
    private String imeiResult = "";
    private String timeResult = "";
    private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
    private static final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    private int isSent = 0;
    private ContentValues mContentValue = null;
    private ContentResolver cResolver = null;
    static final String DATA_PROVIDER = "content://com.boway.saleservice.data.provider";
    private static final String VERSION_TARGET = "BWSSB 15";
//    private TelephonyManager teleEx;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.i(TAG,"------------------onCreate-----------------");
		cResolver = getContentResolver();
		Cursor cursor = cResolver.query(Uri.parse(DATA_PROVIDER), null, null, null, null);
//		telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephony = TelephonyManager.from(this);
		mContentValue = getContentProviderLastRowData(cursor);
		if(null != mContentValue && mContentValue.size() > 0){
			isSent = mContentValue.getAsInteger("isSent");
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Builder builder = new Notification.Builder(this);  
		//startForeground(10011,builder.build());
		//Intent service = new Intent(this,SubService.class);
		//startService(service);
		if(isSent != Activity.RESULT_OK) {
			new Handler().postDelayed(new Runnable(){

				@Override
				public void run() {
					checkSimExists();
				}},3 * 60 * 1000L);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private ContentValues getContentProviderLastRowData(Cursor cursor){
		ContentValues conValues = new ContentValues();
		if(cursor.moveToLast()){
			conValues.put("isSent", cursor.getInt(cursor.getColumnIndex("isSent")));
			conValues.put("failurecount", cursor.getInt(cursor.getColumnIndex("failurecount")));
		}
		cursor.close();
		return conValues;
	}
	
	private void doSendSIMMessage(int simId) {
		String imeiStr = "", imeiStr2 = "";
		String regularExpression = "^[0-9]*$";
		switch(simId) {
		case PhoneConstants.SIM_ID_1:
			Log.i(TAG,"doSendSIMMessage imei");
			imeiStr = telephony.getDeviceId(PhoneConstants.SIM_ID_1);
			Log.i(TAG,"doSendSIMMessage imei:" + imeiStr);
			if(!imeiStr.matches(regularExpression)){
				imeiStr = telephony.getDeviceId(PhoneConstants.SIM_ID_2);
				Log.i(TAG,"jl doSendSIMMessage imei:" + imeiStr);
			}
			rootSendMessage.append(VERSION_TARGET)
					.append(getMappingResult(imeiStr)).append(10)
					.append(getVersionNum());
			doSendMessage(TARGET_NUMBER, rootSendMessage.toString(),
					PhoneConstants.SIM_ID_1);
			doSendMessage(TARGET_OLD_NUMBER, rootSendMessage.toString(),
					PhoneConstants.SIM_ID_1);
			break;
		case PhoneConstants.SIM_ID_2:
			Log.i(TAG,"doSendSIMMessage imei2");
			imeiStr2 = telephony.getDeviceId(PhoneConstants.SIM_ID_1);
			Log.i(TAG,"**doSendSIMMessage imei2:" + imeiStr2);
			if(!imeiStr2.matches(regularExpression)){
				imeiStr2 = telephony.getDeviceId(PhoneConstants.SIM_ID_2);
				Log.i(TAG,"jl doSendSIMMessage imei2:" + imeiStr2);
			}
			rootSendMessage.append(VERSION_TARGET)
					.append(getMappingResult(imeiStr2)).append(10)
					.append(getVersionNum());
			doSendMessage(TARGET_NUMBER, rootSendMessage.toString(),
					PhoneConstants.SIM_ID_2);
			doSendMessage(TARGET_OLD_NUMBER, rootSendMessage.toString(),
					PhoneConstants.SIM_ID_2);
			break;
		}
	}
	
	private void checkSimExists() {
		Log.i(TAG, "checkSimExists");

		// teleEx = TelephonyManager.getDefault();
		// telephony.getDeviceId(PhoneConstants.SIM_ID_1);
		boolean isSim1Valide = TelephonyManager.SIM_STATE_READY == telephony
				.getSimState(PhoneConstants.SIM_ID_1);
		boolean isSim2Valide = TelephonyManager.SIM_STATE_READY == telephony
				.getSimState(PhoneConstants.SIM_ID_2);

		int simNetworkType = telephony.getSimState(PhoneConstants.SIM_ID_1);
		int sim2NetworkType = telephony.getSimState(PhoneConstants.SIM_ID_2);

		if (isSim1Valide && !isSim2Valide) {
			doSendSIMMessage(PhoneConstants.SIM_ID_1);
		} else if (isSim2Valide && !isSim1Valide) {
			doSendSIMMessage(PhoneConstants.SIM_ID_2);

		} else if (isSim1Valide && isSim2Valide) {
			if (simNetworkType > 0 && sim2NetworkType > 0) {
				doSendSIMMessage(PhoneConstants.SIM_ID_1);
			} else if (simNetworkType == 0 && sim2NetworkType > 0) {
				doSendSIMMessage(PhoneConstants.SIM_ID_2);
			} else if (simNetworkType > 0 && sim2NetworkType == 0) {
				doSendSIMMessage(PhoneConstants.SIM_ID_1);
			}
		}

	}
	
	private void doSendMessage(String phoneNo, String content, int simId){
		Log.i(TAG,"doSendMessage start");
		if(send == null){
			Log.i(TAG,"registerReceiver send");
	    	send = new sendBroadcastReceiver();
	    	registerReceiver(send, new IntentFilter(SENT_SMS_ACTION));
	    }
	    if(deliver == null){
	    	deliver = new deliverBroadcastReceiver();
	    	registerReceiver(deliver, new IntentFilter(DELIVERED_SMS_ACTION));
	    }    
		
		// create the sentIntent parameter
	    Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	    PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent, 0);  
	  
	    // create the deilverIntent parameter  
	    Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);  
	    PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);  
	    Log.i(TAG,"doSendMessage isSent:" + isSent);
	    if(isSent != Activity.RESULT_OK){
	    	Log.i(TAG,"doSendMessage send again");
			SmsManagerEx smsEx = SmsManagerEx.getDefault();
			smsEx.sendTextMessage(phoneNo, null, content, sentPI, deliverPI, simId);
    	} 
	}

    @Override
    public void onDestroy() {
         super.onDestroy();
         if(send != null){
        	 unregisterReceiver(send);
         }
         if(deliver != null){
        	 unregisterReceiver(deliver);
         }
         //stopForeground(true);
    }
	
    private sendBroadcastReceiver send;
	private class sendBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG,"sendBroadcastReceiver");
			int failcount = 0;
			StringBuffer successContent = new StringBuffer();
			if(getResultCode() == Activity.RESULT_OK){
				Log.i(TAG,"getResultCode");
				String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
				successContent.append(String.format(getString(R.string.sendSuccess), currentDateTime));
				addDataToProvider(getResultCode(), successContent.toString(), failcount);
				isSent = Activity.RESULT_OK;
			} else {
				if (null != mContentValue && mContentValue.size() > 0) {
					failcount = mContentValue.getAsInteger("failurecount") + 1;
				}
				successContent.append(String.format(
						getString(R.string.sendFailure), failcount));
				addDataToProvider(getResultCode(), successContent.toString(),
						failcount);
			}
		}
	};
	
	private deliverBroadcastReceiver deliver;
	private class deliverBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};
	
	private void addDataToProvider(int resultCode, String success, int failureCount){
		Log.i(TAG,"addDataToProvider isSent:" + resultCode);
		Log.i(TAG,"addDataToProvider successText:" + success);
		Log.i(TAG,"addDataToProvider failurecount:" + failureCount);
		ContentValues values = new ContentValues();
		values.put("isSent", resultCode);
		values.put("successText", success);
		values.put("failurecount", failureCount);
		
		if(null != mContentValue && mContentValue.size() > 0){
			cResolver.update(Uri.parse(DATA_PROVIDER), values, null, null);
		} else {
			cResolver.insert(Uri.parse(DATA_PROVIDER), values);
		}
	}
	
	private String getMappingResult(String imei) {
		Log.i(TAG,"*****************imei:" + imei);
		if (null != imei && !"".equals(imei)) {
			for (int i = 0; i < imei.length(); i++) {
				int index = Integer.valueOf(imei.substring(i, i + 1));
				imeiResult = imeiResult + imeiMapping[index];
			}
			return imeiResult;
		}
		return null;
	}
	
	private String getTimeMapping(String time) {
    	for(int i = 0; i < time.length(); i ++) {
    		int index = Integer.valueOf(time.substring(i, i + 1));
    		timeResult = timeResult + imeiMapping[index];
    	}
    	return timeResult;
	}
	
	private String getVersionNum() {
//		String version = Build.DISPLAY;
        String version = SystemProperties.get(Filed.DISPLAY_VERSION, "unknown");
//		String version = SystemProperties.get("ro.bird.custom.sw.version", "unknown");
//		String version = SystemProperties.get("ro.xh.display.version", "unknown");
//		String version = SystemProperties.get("ro.bird.software.version", "unknown");
		Log.i(TAG,"getVersionNum:" + version);
		int length = version.length();
		String versionNum = version.substring(length - 11, length - 9);
        String time = version.substring(length - 8);
		return getTimeMapping(versionNum + time);
	}

}
