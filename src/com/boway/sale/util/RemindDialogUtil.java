package com.boway.sale.util;

import com.boway.sale.R;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class RemindDialogUtil {
    
	private static final String TAG = "RemindDialogUtil";
	
	public static final int DOUBLESIM = 0;
	public static final int ONLYSIM1 = 1;
	public static final int ONLYSIM2 = 2;
	
	private WindowManager mWindowManager;
	private LayoutParams mParams;
	private View mView;
	private KeyguardManager mKeyguardManager;
	private KeyguardLock mKeyguardLock;
	private Context mCtx;
	
	private boolean isAdd = false;
	
	public RemindDialogUtil(Context ctx){
		mCtx = ctx;
		mWindowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		mParams = new WindowManager.LayoutParams();
		mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mParams.format = PixelFormat.RGBA_8888;
		mParams.gravity = Gravity.CENTER;
		mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		mParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		mKeyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = mKeyguardManager.newKeyguardLock("LOCK");
	}
	
	public void addView(Context ctx){
    	    
		mView = View.inflate(ctx, R.layout.remind_dialog, null);
		TextView messageTxt = (TextView) mView.findViewById(R.id.message);
		TextView btn = (TextView) mView.findViewById(R.id.ok);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mKeyguardLock != null)
				{
					Log.i(TAG,"disableKeyguard");
					mKeyguardLock.reenableKeyguard();
				}
				shutdown();
			}
		});
		
		messageTxt.setText(ctx.getString(R.string.close_sim_remind));
		if(!isAdd){
			isAdd = true;
			if(mKeyguardLock != null){
				mKeyguardLock.disableKeyguard();
				Log.i(TAG,"disableKeyguard");
			}
			mWindowManager.addView(mView, mParams);
		}
	}
	
	public void removeView(Context ctx){
		if(isAdd){
			if(mKeyguardLock != null)
			{
				Log.i(TAG,"disableKeyguard");
				mKeyguardLock.reenableKeyguard();
			}
			mWindowManager.removeView(mView);
			isAdd = false;
		}
	}
	
	private void shutdown() {
		mWindowManager.removeView(mView);
		PowerManager pm = (PowerManager) mCtx.getSystemService(Context.POWER_SERVICE);
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
		mCtx.startActivity(intent);
	}
}
