package com.boway.sale.service;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.MD5Utils;
import com.boway.sale.db.dao.IMSIDBDao;
import com.boway.sale.option.FeatureOption;
import com.boway.sale.util.Filed;
import com.boway.sale.util.LockSimUtils;
import com.mediatek.telephony.SmsManagerEx;

import android.R.integer;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MessageVerifySevice extends Service {

    public static final int CHECK_SIM1_FAILE = 1;
    public static final int CHECK_SIM2_FAILE = 2;
    public static final int CHECK_ALL_SIM_FAILE = 3;

	private static final String TAG = "MessageVerifySevice";
	private TelephonyManager teManager;
	private SmsManagerEx smsEx;
	private SharedPreferences sp;
	private static final int CHECK_SIM1 = 100;
	private static final int CHECK_SIM2 = 101;

	private static final long INTERVAL_TIME = 10 * 1000L;
	
	private SMSBroadcastReceiver receiver;
	
	private boolean mSim2Send = false;
	private boolean mIsReceiver = false;
	private String imsi1;
	private String imsi2;
	private boolean isSim1Valide;
	private boolean isSim2Valide;
	
	private String sim1Number;
	private String sim2Number;
	private String content;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.i(TAG,"jlzou MessageVerifySevice onCreate");
		teManager = TelephonyManager.from(this);
		sp = PreferenceManager.getDefaultSharedPreferences(this);
		receiver = new SMSBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(receiver, filter);
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		if(receiver != null){
			unregisterReceiver(receiver);
			receiver = null;
		}
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG,"jlzou MessageVerifySevice onStartCommand");
		sim1Number = intent.getStringExtra("sim1Number");
		sim2Number = intent.getStringExtra("sim2Number");
		isSim1Valide = (sim1Number != null);
		isSim2Valide = (sim2Number != null);
		Log.i(TAG,"jlzou sim1Number:" + sim1Number);
		imsi1 = teManager.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
		imsi2 = teManager.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
//		int sim1State = teManager.getSimState(0);
//		int sim2State = teManager.getSimState(1);
//		int count = 1;
//		while (!(((sim1State == TelephonyManager.SIM_STATE_READY && imsi1 != null) || sim1State == TelephonyManager.SIM_STATE_ABSENT) 
//				&& ((sim2State == TelephonyManager.SIM_STATE_READY && imsi2 != null) || sim2State == TelephonyManager.SIM_STATE_ABSENT))) 
//		{
//			try {
//				Thread.sleep(2000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			sim1State = teManager.getSimState(0);
//			sim2State = teManager.getSimState(1);
//			imsi1 = teManager
//					.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
//			imsi2 = teManager
//					.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
//			count++;
//			Log.i(TAG, "count:" + count);
//			Log.i(TAG, "sim1State:" + sim1State);
//			Log.i(TAG, "sim2State:" + sim2State);
//			Log.i(TAG, "imsi1:" + imsi1);
//			Log.i(TAG, "imsi2:" + imsi2);
//		}
		Log.i(TAG,"jlzou MessageVerifySevice sim ready");
		if (FeatureOption.MTK_GEMINI_SUPPORT) {
			Log.i(TAG, "doSendMessage send MTK_GEMINI_SUPPORT");
			mSim2Send = false;
			smsEx = SmsManagerEx.getDefault();
			
//			if(isSim1Valide && isSim2Valide){
//				doubleSim = true;
//			}else if(isSim1Valide){
//				isOnlySim1 = true;
//			}else if(isSim2Valide){
//				isOnlySim1 = true;
//			}
			Log.i(TAG, "jlzou isSim1Valide:" + isSim1Valide);
			Log.i(TAG, "jlzou isSim2Valide:" + isSim2Valide);
			if (isSim1Valide) {
				Log.i(TAG, "jlzou isSim1Valide");
				content = produceSmsContent();
				smsEx.sendTextMessage(sim1Number, null, content, null,
						null, PhoneConstants.SIM_ID_1);
				 mHandler.sendEmptyMessageDelayed(CHECK_SIM1,
				 INTERVAL_TIME);
			} else if (isSim2Valide) {
				Log.i(TAG, "jlzou isSim2Valide");
				mSim2Send = true;
				content = produceSmsContent();
				smsEx.sendTextMessage(sim2Number, null, content, null,
						null, PhoneConstants.SIM_ID_2);
				 mHandler.sendEmptyMessageDelayed(CHECK_SIM2,
				 INTERVAL_TIME);
			} else {
				sendBroadCastToUI(false);
			}

		} else {
			Log.i(TAG, "doSendMessage send no double");
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(sim1Number, null, content, null,
					null);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private class SMSBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG,"jlzou SmsBroadcastReceiver");
			Bundle bundle = intent.getExtras();
			Object[] pdus = (Object[])bundle.get("pdus");
			SmsMessage[] msg = new SmsMessage[pdus.length];
			for(int i = 0;i < pdus.length;i ++)
			{
				msg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			StringBuilder sb = new StringBuilder(); 
			String phone = null;
			phone = msg[0].getOriginatingAddress();
			if(phone.length() > 11){
				phone = phone.substring(phone.length() - 11);
			}
			Log.i(TAG,"jlzou phone:" + phone);
			
			for(SmsMessage sms : msg)
			{
				String subbody = sms.getDisplayMessageBody();
				if(subbody != null)
				    sb.append(subbody);
			}
			String body = sb.toString();
			
			if(mSim2Send){
				Log.i(TAG,"jlzou remove message sim2");
				if(body.equals(content) && phone.equals(sim2Number)){
					mIsReceiver = true;
					mHandler.removeMessages(CHECK_SIM2);
					mHandler.sendEmptyMessage(CHECK_SIM2);
				}
			}else{
				Log.i(TAG,"jlzou remove message sim1");
				if(body.equals(content) && phone.equals(sim1Number)){
					mIsReceiver = true;
					mHandler.removeMessages(CHECK_SIM1);
					mHandler.sendEmptyMessage(CHECK_SIM1);
				}
			}
			
		}
		
	};
	
	private Handler mHandler = new Handler(){
	    private int state = 0;
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case CHECK_SIM1:
				if(mIsReceiver){
					mIsReceiver = false;
					LockSim1(false);
					if(isSim2Valide){
						Log.i(TAG,"jlzou isSim2Valide");
						mSim2Send = true;
						content = produceSmsContent();
						smsEx.sendTextMessage(sim2Number, null, content, null, null, PhoneConstants.SIM_ID_2);
						mHandler.sendEmptyMessageDelayed(CHECK_SIM2, INTERVAL_TIME);
					}else{
						sendBroadCastToUI(true);
					}
				}else{
					LockSim1(true);
					if(isSim2Valide){
					    state = state + CHECK_SIM1_FAILE;
					    Log.i(TAG,"jlzou state: " +state);
						Log.i(TAG,"jlzou isSim2Valide");
						mSim2Send = true;
						content = produceSmsContent();
						smsEx.sendTextMessage(sim2Number, null, content, null, null, PhoneConstants.SIM_ID_2);
						mHandler.sendEmptyMessageDelayed(CHECK_SIM2, INTERVAL_TIME);
					}else{
						sendBroadCastToUI(false);
					}
				}
				break;
			case CHECK_SIM2:
				if(mIsReceiver){
				    mIsReceiver = false;
                    LockSim2(false);
                    if (state == CHECK_SIM1_FAILE)
                        sendBroadCastToUI(true, state);
                    else if (state == 0)
                        sendBroadCastToUI(true);
				}else{
				    LockSim2(true);
				    if (!isSim1Valide)
				        state += CHECK_SIM1_FAILE;
                    state += CHECK_SIM2_FAILE;
                    Log.i(TAG,"jlzou state: " +state);
                    if (state == CHECK_ALL_SIM_FAILE)
                        sendBroadCastToUI(false);
                    else if (state == CHECK_SIM2_FAILE)
                        sendBroadCastToUI(true, state);
				}
				break;
			}
		}
	};

	private void LockSim1(boolean needLock) {
		Log.i(TAG,"jlzou LockSim1: " +needLock);
	    Editor editor = sp.edit();
	    editor.putBoolean("needLockSim1", needLock);
	    editor.commit();
	    
		if (needLock) {
			int subId = LockSimUtils
                    .getSubIdBySlot(PhoneConstants.SIM_ID_1);
            if (LockSimUtils.isRadioOn(subId, this)) 
                LockSimUtils.setRadionOn(this, subId, false);
		}else{
			IMSIDBDao dao = new IMSIDBDao(this);
			dao.insertIMSI(imsi1, 1);
		}
	}

	private void LockSim2(boolean needLock) {
		Log.i(TAG,"jlzou LockSim2: " +needLock);
	    Editor editor = sp.edit();
	    editor.putBoolean("needLockSim2", needLock);
	    editor.commit();
		if (needLock) {
			int subId = LockSimUtils
                    .getSubIdBySlot(PhoneConstants.SIM_ID_2);
            if (LockSimUtils.isRadioOn(subId, this)) 
                LockSimUtils.setRadionOn(this, subId, false);
		}else{
			IMSIDBDao dao = new IMSIDBDao(this);
			dao.insertIMSI(imsi2, 1);
		}
	};
	
	protected void sendBroadCastToUI(boolean isSuccess) {
		Intent broadcast = new Intent(Filed.FLUSH_BROADCAST);
		broadcast.putExtra("isSuccess", isSuccess);
		sendBroadcast(broadcast);
		stopSelf();
	}

    protected void sendBroadCastToUI(boolean isSuccess ,int state) {
        Intent broadcast = new Intent(Filed.FLUSH_BROADCAST);
        broadcast.putExtra("isSuccess", isSuccess);
        broadcast.putExtra("state", state);
        sendBroadcast(broadcast);
        stopSelf();
    }
	
	private int getSubIdBySlot(int slot) {
        int [] subId = SubscriptionManager.getSubId(slot);
        return (subId != null) ? subId[0] : SubscriptionManager.getDefaultSubId();
    }
	
	private String produceSmsContent() {
		String origin = String.valueOf(System.currentTimeMillis());
		String desStr = MD5Utils.encode(origin);
		if(desStr == null){
			desStr = origin;
		}
		return desStr;
	}
}
