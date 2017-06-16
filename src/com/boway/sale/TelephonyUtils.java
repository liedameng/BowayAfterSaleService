package com.boway.sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.R;
import com.boway.sale.db.MessageContentProvider;
import com.boway.sale.option.FeatureOption;
import com.boway.sale.util.Filed;
import com.mediatek.telephony.SmsManagerEx;

public class TelephonyUtils {
	
	private Context mContext;
	
	public static final int TELEPHONY_SIM1_VALIDE = 101;
	public static final int TELEPHONY_SIM2_VALIDE = 102;
	public static final int TELEPHONY_SIM_VALIDE = 103;
	public static final int TELEPHONY_NO_SIM_VALIDE = 1001;
	public static final int TELEPHNOY_SIM_LOCKED =  1002;
	public static final int TELEPHONY_SIM_ABSENT = 1003;
	public static final int TELEPHNOY_SIM_ERROR = 1004;
	
	private TelephonyManager telephony;
//	private TelephonyManager teleEx;
	
	private static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";  
    private static final String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    
    private int isSent = 0;
    private ContentValues mContentValue = null;
    private ContentResolver cResolver = null;
    
    private StringBuffer rootSendMessage = new StringBuffer();
	private String [] imeiMapping = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O"};
	private static final String TARGET_NUMBER = "106905501868";
    private static final String TARGET_OLD_NUMBER = "10690999095";
    private String imeiResult = "";
    private String timeResult = "";
    private static final String VERSION_TARGET = "BWSSB 15";
	
	public TelephonyUtils(Context context) {
		this.mContext = context;
		
		telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//		teleEx = TelephonyManager.getDefault();
		
		cResolver = mContext.getContentResolver();
		Cursor cursor = cResolver.query(Uri.parse(MessageContentProvider.DATA_PROVIDER), null, null, null, null);
		mContentValue = getContentProviderLastRowData(cursor);
		if(null != mContentValue && mContentValue.size() > 0){
			isSent = mContentValue.getAsInteger("isSent");
		}
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
	
	public void checkTPStateAndSendMsg() {
		if(isSent != Activity.RESULT_OK) {
			checkAndSendMsg();
		}
	}
	
	private void checkAndSendMsg() {
		switch(checkSimExists()) {
		case TELEPHONY_SIM1_VALIDE:
			sendMsgSim1();
			break;
		case TELEPHONY_SIM2_VALIDE:
			sendMsgSim2();
			break;
		case TELEPHONY_SIM_VALIDE:
			sendMsgSim();
			break;
		case TELEPHONY_NO_SIM_VALIDE:
		case TELEPHNOY_SIM_LOCKED:
		case TELEPHONY_SIM_ABSENT:
		case TELEPHNOY_SIM_ERROR:
			break;
		}
	}
	
	private void sendMsgSim1() {
		String imeiStr = telephony.getDeviceId(PhoneConstants.SIM_ID_1);
		rootSendMessage.append(VERSION_TARGET)
				.append(getMappingResult(imeiStr)).append(10)
				.append(getVersionNum());
		doSendMessage(TARGET_NUMBER, rootSendMessage.toString(),
				PhoneConstants.SIM_ID_1);
		doSendMessage(TARGET_OLD_NUMBER, rootSendMessage.toString(),
				PhoneConstants.SIM_ID_1);
	}
	
	private void sendMsgSim2() {
		String imeiStr2 = telephony.getDeviceId(PhoneConstants.SIM_ID_2);
		rootSendMessage.append(VERSION_TARGET)
				.append(getMappingResult(imeiStr2)).append(10)
				.append(getVersionNum());
		doSendMessage(TARGET_NUMBER, rootSendMessage.toString(),
				PhoneConstants.SIM_ID_2);
		doSendMessage(TARGET_OLD_NUMBER, rootSendMessage.toString(),
				PhoneConstants.SIM_ID_2);
	}
	
	private void sendMsgSim() {
		rootSendMessage.append(VERSION_TARGET)
				.append(getMappingResult(telephony.getDeviceId())).append(10)
				.append(getVersionNum());
		doSendMessage(TARGET_NUMBER, rootSendMessage.toString(),
				telephony.getSimState());
		doSendMessage(TARGET_OLD_NUMBER, rootSendMessage.toString(),
				telephony.getSimState());
	}
	
	private String getMappingResult(String imei) {
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
		int length = version.length();
		String versionNum = version.substring(length - 11, length - 9);
        String time = version.substring(length - 8);
		return getTimeMapping(versionNum + time);
	}
	
	public int checkSimExists() {
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			boolean isSim1Valide = TelephonyManager.SIM_STATE_READY == telephony
					.getSimState(PhoneConstants.SIM_ID_1);
			boolean isSim2Valide = TelephonyManager.SIM_STATE_READY == telephony
					.getSimState(PhoneConstants.SIM_ID_2);

			int simNetworkType = telephony.getSimState(PhoneConstants.SIM_ID_1);
			int sim2NetworkType = telephony.getSimState(PhoneConstants.SIM_ID_2);

			if (isSim1Valide && !isSim2Valide) {
				return TELEPHONY_SIM1_VALIDE;
			} else if (isSim2Valide && !isSim1Valide) {
				return TELEPHONY_SIM2_VALIDE;
			} else if (isSim1Valide && isSim2Valide) {
				if (simNetworkType > 0 && sim2NetworkType > 0) {
					return TELEPHONY_SIM1_VALIDE;
				} else if (simNetworkType == 0 && sim2NetworkType > 0) {
					return TELEPHONY_SIM2_VALIDE;
				} else if (simNetworkType > 0 && sim2NetworkType == 0) {
					return TELEPHONY_SIM1_VALIDE;
				}
			}
		} else {
			int simState = telephony.getSimState();
			if(simState == TelephonyManager.SIM_STATE_ABSENT) {
				return TELEPHONY_SIM_ABSENT;
			} else if(simState == TelephonyManager.SIM_STATE_NETWORK_LOCKED
					|| simState == TelephonyManager.SIM_STATE_PIN_REQUIRED
					|| simState == TelephonyManager.SIM_STATE_PUK_REQUIRED) {
				return TELEPHNOY_SIM_LOCKED;
			} else if(simState == TelephonyManager.SIM_STATE_READY) {
				return TELEPHONY_SIM_VALIDE;
			} else if(simState == TelephonyManager.SIM_STATE_UNKNOWN) {
				return TELEPHONY_NO_SIM_VALIDE;
			}
			
		}
		return TELEPHNOY_SIM_ERROR;
	}
	
	private void doSendMessage(String phoneNo, String content, int simId){
		// create the sentIntent parameter
	    Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	    PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0, sentIntent, 0);  
	  
	    // create the deilverIntent parameter  
	    Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);  
	    PendingIntent deliverPI = PendingIntent.getBroadcast(mContext, 0, deliverIntent, 0);  
	  
	    if(FeatureOption.MTK_GEMINI_SUPPORT && isSent != Activity.RESULT_OK){
			SmsManagerEx smsEx = SmsManagerEx.getDefault();
			smsEx.sendTextMessage(phoneNo, null, content, sentPI, deliverPI, simId);
    	} else {  
    		SmsManager smsManager = SmsManager.getDefault();
    		if (content.length() > 70) {  
    	        ArrayList<String> msgs = smsManager.divideMessage(content);  
    	        for (String msg : msgs) {  
    	        	if(isSent != Activity.RESULT_OK){ 
    	        		smsManager.sendTextMessage(phoneNo, null, msg, sentPI, deliverPI);  
    	        	}
    	        }  
    	    } else {  
    	    	if(isSent != Activity.RESULT_OK){ 
    	    		smsManager.sendTextMessage(phoneNo, null, content, sentPI, deliverPI); 
    	    	} 
    	    }  
	    }
	    mContext.registerReceiver(send, new IntentFilter(SENT_SMS_ACTION));
	    mContext.registerReceiver(deliver, new IntentFilter(DELIVERED_SMS_ACTION));
	}
	
	private void addDataToProvider(int resultCode, String success, int failureCount){
		ContentValues values = new ContentValues();
		values.put("isSent", resultCode);
		values.put("successText", success);
		values.put("failurecount", failureCount);
		
		if(null != mContentValue && mContentValue.size() > 0){
			cResolver.update(Uri.parse(MessageContentProvider.DATA_PROVIDER), values, null, null);
		} else {
			cResolver.insert(Uri.parse(MessageContentProvider.DATA_PROVIDER), values);
		}
	}
	
	private BroadcastReceiver send = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int failcount = 0;
			StringBuffer successContent = new StringBuffer();
			if(getResultCode() == Activity.RESULT_OK){
				String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
				successContent.append(String.format(mContext.getString(R.string.sendSuccess), currentDateTime));
				addDataToProvider(getResultCode(), successContent.toString(), failcount);
				isSent = Activity.RESULT_OK;
			} else {
				if (null != mContentValue && mContentValue.size() > 0) {
					failcount = mContentValue.getAsInteger("failurecount") + 1;
				}
				successContent.append(String.format(
						mContext.getString(R.string.sendFailure), failcount));
				addDataToProvider(getResultCode(), successContent.toString(),
						failcount);
			}
		}
	};
	
	private BroadcastReceiver deliver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
		}
	};

}
