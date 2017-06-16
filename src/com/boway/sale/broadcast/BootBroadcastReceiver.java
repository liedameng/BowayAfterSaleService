package com.boway.sale.broadcast;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.PhoneNumberCheckActivity;
import com.boway.sale.db.dao.IMSIDBDao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "BootBroadcastReceiver";

    // static final long DELAY_TIME = 4 * 3600 * 1000;
    static final long DELAY_TIME = 1 * 60 * 1000L;
    private TelephonyManager tm;
    private Context mContext;
    private SharedPreferences sp;
    
    private Handler mHandle = new Handler() {
        public void handleMessage(android.os.Message msg) {
            
            if (!checkNumber(mContext)) {
                Log.i(TAG, "Start phone num check");
                Intent intent2 = new Intent(mContext,
                        PhoneNumberCheckActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent2);
            }else{
            	Editor editor = sp.edit();
        	    editor.putBoolean("needLockSim1", false);
        	    editor.putBoolean("needLockSim2", false);
        	    editor.commit();
            }
        };
    };
    
    private boolean isAirPlaneModeOn(Context context) {
        int mode = 0;
        try {
            mode = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode == 1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "BootBroadcastReceiver");
        mContext = context;
        tm = TelephonyManager.from(mContext);
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        mHandle.sendEmptyMessageDelayed(0, 0);
    }

    private boolean checkNumber(Context context) {
    	
    	if(isAirPlaneModeOn(context)){
    		return false;
    	}
        
        String imsi1 = tm
                .getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
        String imsi2 = tm
                .getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
        int sim1State = tm.getSimState(0);
		int sim2State = tm.getSimState(1);
		int count = 0;
		while(!((( imsi1 != null || sim1State == TelephonyManager.SIM_STATE_ABSENT)
				&& ( imsi2 != null || sim2State == TelephonyManager.SIM_STATE_ABSENT))
				|| count > 50)){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sim1State = tm.getSimState(0);
			sim2State = tm.getSimState(1);
			imsi1 = tm.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_1));
			imsi2 = tm.getSubscriberId(getSubIdBySlot(PhoneConstants.SIM_ID_2));
			count ++;
			Log.i(TAG,"jlzou count:" + count);
			Log.i(TAG,"jlzou sim1State:" + sim1State);
			Log.i(TAG,"jlzou sim2State:" + sim2State);
			Log.i(TAG,"jlzou imsi1:" + imsi1);
			Log.i(TAG,"jlzou imsi2:" + imsi2);
		}
		IMSIDBDao dao = new IMSIDBDao(context);
        if(imsi1 == null && imsi2 == null){
        	Log.i(TAG,"jlzou imsi2 and imsi1 is null");
        	return true;
        }else if(imsi1 == null){
        	Log.i(TAG,"jlzou imsi1 is null");
        	return dao.imsiIsLegal(imsi2);
        }else if(imsi2 == null){
        	Log.i(TAG,"jlzou imsi2 is null");
        	return dao.imsiIsLegal(imsi1);
        }else{
        	Log.i(TAG,"jlzou all not null");
        	return dao.imsiIsLegal(imsi1) && dao.imsiIsLegal(imsi2);
        }
    }

    private int getSubIdBySlot(int slot) {
        int[] subId = SubscriptionManager.getSubId(slot);
        return (subId != null) ? subId[0] : SubscriptionManager
                .getDefaultSubId();
    }

    private void startSendService(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + DELAY_TIME, pendingIntent);
    }

}
