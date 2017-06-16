package com.boway.sale.broadcast;

import com.boway.sale.service.NetworkSendService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkBroadcastReceiver extends BroadcastReceiver {
	
	private static final String TAG = "NetworkBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "---------------intent---------------" + intent);
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeInfo = manager.getActiveNetworkInfo();
		Log.e(TAG, "-----------------activeInfo----------------" + activeInfo);
		Log.e(TAG, "-----------------getServiceState----------------" + getServiceState(context));
		if(null != activeInfo && getServiceState(context)) {
			Intent netIntent = new Intent(context,NetworkSendService.class);
			context.startService(netIntent);
		}
	}
	
	private boolean getServiceState(Context context) {
		SharedPreferences preferences = context.getSharedPreferences("service_state", 0);
		boolean serviceState = preferences.getBoolean("isServiceStart", false);
		return serviceState;
	}

}
