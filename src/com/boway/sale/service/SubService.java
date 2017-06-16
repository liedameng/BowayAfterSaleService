package com.boway.sale.service;

import android.app.Notification;
import android.app.Service;
import android.app.Notification.Builder;
import android.content.Intent;
import android.os.IBinder;

public class SubService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Builder builder = new Notification.Builder(this);  
		startForeground(10011,builder.build());
		stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
		super.onDestroy();
	}

}
