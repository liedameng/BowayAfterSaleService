package com.boway.sale.service;

import com.android.internal.telephony.PhoneConstants;
import com.android.server.wm.WindowManagerService;
import com.boway.sale.CloseRemindDialog;
import com.boway.sale.LockRemindDialog;
import com.boway.sale.db.dao.IMSIQueryDao;
import com.boway.sale.option.FeatureOption;
import com.boway.sale.util.RemindDialogUtil;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.CalendarContract.Reminders;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

public class IsLockService extends Service {

	private static final String TAG = "IsLockService";
	private static final int SHUT_DOWN = 666;
	
	private TelephonyManager telephony = null;
	private RemindDialogUtil dialogUtil;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		telephony = TelephonyManager.from(this);
		dialogUtil = new RemindDialogUtil(this);
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
					
					imsiIsLegal(imsi1,imsi2);
//					if((imsi1 == null || "".equals(imsi1)) && (imsi2 == null || "".equals(imsi2)))
//					{
//						LockTelephone(false);
//					}else{
//						if(imsiIsLegal(imsi1,imsi2)){
//							LockTelephone(false);
//						}else{
//							LockTelephone(true);
//						}
//					}
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
		boolean hasSim1 = false;
		boolean hasSim2 = false;
		
		IMSIQueryDao dao = new IMSIQueryDao(this);
		if(imsi1 != null && imsi1.length() > 10)
		{
			imsi1 = imsi1.substring(0, 10);
			sim1IsLegal = dao.findImsi(imsi1);
			hasSim1 = true;
		}
		
		if(imsi2 != null && imsi2.length() > 10)
		{
			imsi2 = imsi2.substring(0, 10);
			sim2IsLegal = dao.findImsi(imsi2);
			hasSim2 = true;
		}
		
		if(hasSim1 && hasSim2){
			if(!sim1IsLegal && !sim2IsLegal){
//				LockTelephone(true);
				showRemindDialog(RemindDialogUtil.DOUBLESIM);
			}else if(!sim1IsLegal){
				showRemindDialog(RemindDialogUtil.ONLYSIM1);
			}else if(!sim2IsLegal){
				showRemindDialog(RemindDialogUtil.ONLYSIM2);
			}
		}else if(hasSim1){
			if(!sim1IsLegal){
				showRemindDialog(RemindDialogUtil.ONLYSIM1);
			}
		}else if(hasSim2){
			if(!sim2IsLegal){
				showRemindDialog(RemindDialogUtil.ONLYSIM2);
			}
		}
		
		Log.i(TAG,"jlzou isLegal:" + isLegal);
		return isLegal;
		
	}
	
	private void LockTelephone(boolean enabling){
		if(enabling){
			mHandler.sendEmptyMessageDelayed(SHUT_DOWN, 20 * 1000L);
			
//			Intent intent = new Intent(this,LockRemindDialog.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			startActivity(intent);
			
		}
		stopSelf();
	}
	
	private void showRemindDialog(int simId){
		mHandler.sendEmptyMessageDelayed(SHUT_DOWN, 20 * 1000L);
		
		dialogUtil.addView(this, simId);
		stopSelf();
//    	Intent intent = new Intent(this,CloseRemindDialog.class);
//    	intent.putExtra("simId", simId);
//    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//    	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//    	startActivity(intent);
    }

	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case SHUT_DOWN:
				shutdown();
				break;
			}
		};
	};
	
	private void shutdown() {
		dialogUtil.removeView(this);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			PowerManager.WakeLock mWakelock = pm.newWakeLock(
					PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");
			mWakelock.acquire(6000);
		}
		Intent intent = new Intent(
				"android.intent.action.ACTION_REQUEST_SHUTDOWN");
		intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
