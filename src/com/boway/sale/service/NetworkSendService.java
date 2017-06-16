package com.boway.sale.service;

import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import com.boway.sale.MD5Utils;
import com.boway.sale.NetworkUtils;
import com.boway.sale.R;
//import com.example.mycryptactivity.MCrypt;


import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

public class NetworkSendService extends Service {
	
	private static final String TAG = "NetworkSendService";
	
	private NetworkUtils utils;
	
	private static final String QUERY_ELEC_HTTP_URL = "http://s1.bwssglobal.com:8181/MobileNetAPI/query";
	private static final String QUERY_SEND_STATE_HTTP_URL = "http://s1.bwssglobal.com:8181/MobileNetAPI/regist";
	
	private JSONObject elecJsonData;
	
	private static final int QUERY_ELEC_STATE_SUCCESS = 1;
	private static final int QUERY_ELEC_STATE_FAILURE = 0;
	
	private static final int ELEC_FAILURE_MAX_NUM = 3;
	
//	private MCrypt crypt;
	
	private Handler Handler;
	private HandlerThread thread;
	
	private SharedPreferences preferences;
	
	private static final String PROVINCE = "province";
	private static final String CITY = "name";
	private static final String DISTRICT = "district";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.e(TAG, "-------onCreate-------");
		utils = new NetworkUtils(this);
//		crypt= new MCrypt();
		preferences = getSharedPreferences("service_state", 0);
		setServiceState(true);
		queryElecState();
	}
	
	private void sendRegisterStateReceiver() {
		Intent intent = new Intent("com.boway.sale.register");
		sendBroadcast(intent);
	}
	
	private void setServiceState(boolean isServiceSate) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean("isServiceStart", isServiceSate);
		editor.commit();
	}
	
	private void queryElecState() {
		Log.e(TAG, "-------utils.queryEelcIsSend()-------" + utils.queryEelcIsSend());
		Log.e(TAG, "-------utils.checkTelephonyState()-------" + utils.checkTelephonyState());
		Log.e(TAG, "-------utils.isNetworkAvailable()-------" + utils.isNetworkAvailable());
		Log.e(TAG, "-------utils.isPositioning()-------" + utils.isPositioning());
		if(utils.queryEelcIsSend() && utils.checkTelephonyState() && utils.isNetworkAvailable() && utils.isPositioning()) {
			thread = new HandlerThread("queryElecState");
			thread.start();
			Handler = new Handler(thread.getLooper());
			Handler.post(queryElecStateRun);
		} else {
			stopSelf();
		}
	}
	
	Runnable queryElecStateRun = new Runnable() {
		
		@Override
		public void run() {
			Log.e(TAG, "----------queryJSONObject1------------" + queryJSONObject1());
			elecJsonData = utils.parseJson(utils.postRequestHttp(QUERY_ELEC_HTTP_URL, queryJSONObject1()));
			Log.e(TAG, "-----------queryElecState-----------" + elecJsonData);
			boolean isElecSuccess = isElecSuccess(elecJsonData);
			dealElecResult(isElecSuccess);
		}
	};
	
	Runnable dealElecResultRun = new Runnable() {
		
		@Override
		public void run() {
			Log.e(TAG, "-----------querySendState1----------------" + querySendState1());
			elecJsonData = utils.parseJson(utils.postRequestHttp(QUERY_SEND_STATE_HTTP_URL, querySendState1()));
			StringBuffer successContent = new StringBuffer();
			ContentValues values = null;
			int failure = 0;
			Log.e(TAG, "-----------dealElecResult----------" + elecJsonData);
			boolean isElecSuccess = isElecSuccess(elecJsonData);
			if(!isElecSuccess && failure <= ELEC_FAILURE_MAX_NUM) {
				failure = utils.queryElecRepeatNum() + 1;
				successContent.append(String.format(getString(R.string.sendFailure), failure));
				Log.e(TAG, "---------!isElecSuccess failure----------" + failure);
				values = getStoreElecDetail(Activity.RESULT_CANCELED, successContent.toString(), failure);
				utils.storeNetworkElecState(values);
				queryElecState();
			} else if(isElecSuccess){
				String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
				successContent.append(String.format(getString(R.string.sendSuccess), currentDateTime));
				Log.e(TAG, "---------isElecSuccess failure----------" + failure);
				values = getStoreElecDetail(Activity.RESULT_OK, successContent.toString(), failure);
				utils.storeNetworkElecState(values);
				setServiceState(false);
				sendRegisterStateReceiver();
				stopSelf();
			}
		}
	};
	
	private void dealElecResult(boolean isSuccess) {
		if(!isSuccess) {
			thread = new HandlerThread("dealElecResult");
			thread.start();
			Handler = new Handler(thread.getLooper());
			Handler.post(dealElecResultRun);
		} else {
			ContentValues values = null;
			StringBuffer successContent = new StringBuffer();
			successContent.append(String.format(getString(R.string.sendSuccess), getSendSuccessTime(elecJsonData)));
			values = getStoreElecDetail(Activity.RESULT_OK, successContent.toString(), 0);
			utils.storeNetworkElecState(values);
			setServiceState(false);
			sendRegisterStateReceiver();
			stopSelf();
		}
	}
	
	private String getSendSuccessTime(JSONObject data) {
		if(null != data) {
			try {
				Log.e(TAG, "-----------data.getJSONObject----------" + data.getJSONObject("data"));
				JSONObject reData = data.getJSONObject("data");
				Log.e(TAG, "-----------localtime----------" + reData.getString("localtime"));
				return reData.getString("localtime");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private ContentValues getStoreElecDetail(int isSent, String successText, int failureCont) {
		ContentValues values = new ContentValues();
		values.put("isSent", isSent);
		values.put("successText", successText);
		values.put("failurecount", failureCont);
		return values;
	}
	
	private boolean isElecSuccess(JSONObject data) {
		if(null != data) {
			try {
				int state = data.getInt("status");
				Log.e(TAG, "----------------state-----------------" + state);
				if(state == QUERY_ELEC_STATE_SUCCESS) {
					return true;
				} else if(state == QUERY_ELEC_STATE_FAILURE) {
					return false;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	private String encrypt(String str) {
		if(null != str && !str.isEmpty()) {
			try {
//				return MCrypt.bytesToHex(crypt.encrypt(str));
				return new String(Base64.encode(str.getBytes(), Base64.DEFAULT));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private JSONObject querySendState() {
		JSONObject sendData = new JSONObject();
		try {
			sendData.put(encrypt("phone"), "");
			sendData.put(encrypt("ip"), encrypt(utils.getLocalMacAddress()));
			sendData.put(encrypt("imei1"), encrypt(utils.getImei1Num()));
			sendData.put(encrypt("imei2"), encrypt(utils.getImei2Num()));
			sendData.put(encrypt("imsi"), encrypt(utils.getImsiNum()));
			Log.e(TAG, "----------utils.getSNNum()-----------" + utils.getSNNum());
			sendData.put(encrypt("sn"), encrypt(utils.getSNNum()));
			sendData.put(encrypt("model"), encrypt(utils.getModelNum()));
			Log.e(TAG, "----------utils.getSubModelNum()-----------" + utils.getSubModelNum());
			sendData.put(encrypt("submodel"), encrypt(utils.getSubModelNum()));
			Log.e(TAG, "----------utils.getSoftVersion()-----------" + utils.getSoftVersion());
			sendData.put(encrypt("version"), encrypt(utils.getSoftVersion()));
			sendData.put(encrypt("localtime"), encrypt(utils.getCurrentTime()));
			Log.e(TAG, "----------String.valueOf(utils.getCurrentUnixTime())-----------" + String.valueOf(utils.getCurrentUnixTime()));
			sendData.put(encrypt("timestamp"), encrypt(String.valueOf(utils.getCurrentUnixTime())));
			sendData.put(encrypt("longitude"), "");
			sendData.put(encrypt("latitude"), "");
			sendData.put(encrypt("stationid"), encrypt(String.valueOf(utils.getTelephonyDetail())));
			sendData.put(encrypt("country"), getResources().getString(R.string.contry));
			sendData.put(encrypt("province"), utils.getCityCode(PROVINCE));
			Log.e(TAG, "----------String.valueOf(utils.getCityCode())-----------" + utils.getCityCode(CITY));
			sendData.put(encrypt("city"), encrypt(utils.getCityCode(CITY)));
			sendData.put(encrypt("town"), utils.getCityCode(DISTRICT));
			sendData.put(encrypt("plmn"), encrypt(utils.getPlmnNum()));
			sendData.put(encrypt("androidver"), encrypt(utils.getAndroidVersion()));
			sendData.put(encrypt("secret"), "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sendData;
	}
	
	private JSONObject querySendState1() {
		JSONObject sendData = new JSONObject();
		try {
			sendData.put("phone", "");
			sendData.put("ip", utils.getLocalMacAddress());
			sendData.put("imei1", utils.getImei1Num());
			sendData.put("imei2", utils.getImei2Num());
			sendData.put("imsi", utils.getImsiNum());
			sendData.put("sn", utils.getSNNum());
			sendData.put("model", utils.getModelNum());
			sendData.put("submodel", utils.getSubModelNum());
			sendData.put("version", utils.getSoftVersion());
			sendData.put("localtime", utils.getCurrentTime());
			sendData.put("timestamp", String.valueOf(utils.getCurrentUnixTime()));
			sendData.put("longitude", "");
			sendData.put("latitude", "");
			sendData.put("stationid", String.valueOf(utils.getTelephonyDetail()));
			sendData.put("country", getResources().getString(R.string.contry));
			sendData.put("province", utils.getCityCode(PROVINCE));
			sendData.put("city", utils.getCityCode(CITY));
			sendData.put("town", utils.getCityCode(DISTRICT));
			sendData.put("plmn", utils.getPlmnNum());
			sendData.put("androidver", utils.getAndroidVersion());
			sendData.put("secret", getRegisterSecretMD5());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sendData;
	}
	
	private JSONObject queryJSONObject() {
		JSONObject queryData = new JSONObject();
		try {
			queryData.put(encrypt("imei"), encrypt(utils.getImei1Num()));
			queryData.put(encrypt("secret"), encrypt("0123456789abcdef"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return queryData;
	}
	
	private JSONObject queryJSONObject1() {
		JSONObject queryData = new JSONObject();
		try {
			queryData.put("imei", utils.getImei1Num());
			queryData.put("secret", getQuerySecretMD5());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return queryData;
	}
	
	private String getRegisterSecretMD5() {
		String md5Str = utils.getLocalIpAddress() + utils.getImei1Num()
				+ utils.getImei2Num() + utils.getModelNum()
				+ utils.getSubModelNum() + utils.getSoftVersion()
				+ getResources().getString(R.string.contry)
				+ utils.getCityCode(PROVINCE) + utils.getCityCode(CITY)
				+ utils.getCityCode(DISTRICT);
		return MD5Utils.encode(md5Str);
	}
	
	private String getQuerySecretMD5() {
		return MD5Utils.encode(utils.getImei1Num());
	}
	
}
