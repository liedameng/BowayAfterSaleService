package com.boway.sale;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.db.NetworkContentProvider;
import com.boway.sale.util.Filed;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class NetworkUtils {
	
	private static final String TAG = "NetworkUtils";
	
	private Context mContext;
	private TelephonyUtils utils;
	
	private static final int REQUEST_TIMEOUT = 3000;
	private static final int SO_TIMEOUT = 3000;
	
	private TelephonyManager telephony;
//	private TelephonyManager teleEx;
	
	private static final Uri CONTENT_URI = Uri.parse("content://com.android.bowayweather.db/contacts");
	
	public NetworkUtils(Context context) {
		this.mContext = context;
		utils = new TelephonyUtils(mContext);
		
		telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//		teleEx = TelephonyManager.getDefault();
	}
	
	public boolean checkTelephonyState() {
		int tpState = utils.checkSimExists();
		if(tpState == utils.TELEPHONY_SIM1_VALIDE || tpState == utils.TELEPHONY_SIM2_VALIDE 
				|| tpState == utils.TELEPHONY_SIM_VALIDE) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * check network（WLAN、3G/2G）state
	 * @param context Context
	 * @return true available network
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}
	
	public int queryElecRepeatNum() {
		ContentValues values = queryNetworkElec();
		if(null != values && values.size() > 0) {
			int failureCount = values.getAsInteger("failureCount");
			return failureCount;
		} 
		return 0;
	}
	
	private ContentValues queryNetworkElec() {
		ContentValues values = new ContentValues();
		Cursor cursor = null;
		try {
			cursor = mContext.getContentResolver().query(Uri.parse(NetworkContentProvider.NETWORK_DATA_PROVIDER), null, null, null, null);
			if(null != cursor && cursor.getCount() > 0 && cursor.moveToLast()) {
				values.put("isSent", cursor.getInt(cursor.getColumnIndex("isSent")));
				values.put("failureCount", cursor.getColumnIndex("failurecount"));
				return values;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return null;
	}
	
	public boolean queryEelcIsSend() {
		ContentValues values = queryNetworkElec();
		Log.e(TAG, "------------queryEelcIsSend values------------" + values);
		if(null != values && values.size() > 0) {
			int isSent = values.getAsInteger("isSent");
			Log.e(TAG, "------------isSent------------" + isSent);
			if(isSent != Activity.RESULT_OK) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public void storeNetworkElecState(ContentValues values) {
		Log.e(TAG, "-------------storeNetworkElecState values--------------" + values);
		ContentValues mContentValues = queryNetworkElec();
		if(null != mContentValues && mContentValues.size() > 0) {
			mContext.getContentResolver().update(Uri.parse(NetworkContentProvider.NETWORK_DATA_PROVIDER), values, null, null);
		} else {
			mContext.getContentResolver().insert(Uri.parse(NetworkContentProvider.NETWORK_DATA_PROVIDER), values);
		}
	}
	
	public String getImei1Num() {
		String regularExpression = "^[0-9]*$";
		String imei1 = telephony.getDeviceId(PhoneConstants.SIM_ID_1);
		if(!imei1.matches(regularExpression)){
			imei1 = telephony.getDeviceId(PhoneConstants.SIM_ID_2);
		}
		return imei1;
	}
	
	public String getImei2Num() {
		return telephony.getDeviceId(PhoneConstants.SIM_ID_2);
	}
	
	public String getImsiNum() {
		return telephony.getSubscriberId();
	}
	
	public String getSNNum() {
		return SystemProperties.get("gsm.serial").trim();
	}
	
	public String getSoftVersion() {
//		return Build.DISPLAY;
		return SystemProperties.get(Filed.DISPLAY_VERSION, "unknown");
//		return SystemProperties.get("ro.bird.custom.sw.version", "unknown");
//		return SystemProperties.get("ro.xh.display.version", "unknown");
//		return SystemProperties.get("ro.bird.software.version", "unknown");
	}
	
	public String getPlmnNum() {
		if(null != getImsiNum() && !getImsiNum().isEmpty()) {
			return getImsiNum().substring(0, 4);
		}
		return "";
	}
	
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return "";
	}
	
	public String getLocalMacAddress() {
		try{
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  
	        WifiInfo wifiInfo = wifiManager.getConnectionInfo();  
	        String ipAdress = intToIp(wifiInfo.getIpAddress()).trim();
	        if(ipAdress.substring(0, 7).equals("192.168")) {
	        	return "";
	        }
	        return ipAdress;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + (i >> 24 & 0xFF);
	}
	
	public String getModelNum() {
		return Build.MODEL.replace("_", " ");
	}
	
	public String getSubModelNum() {
		return "";
	}
	
	public String getCurrentTime() {
		Date curDate = new Date(System.currentTimeMillis());
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curDate);
	}
	
	public long getCurrentUnixTime() {
		return System.currentTimeMillis();
	}
	
	public String getAndroidVersion() {
		return Build.VERSION.RELEASE;
	}
	
	public int getTelephonyDetail() {
		TelephonyManager tm = TelephonyManager.getDefault();
		CellLocation cl = tm.getCellLocation();
		
		GsmCellLocation gcl = null;
		CdmaCellLocation ccl = null;
		
		if(cl instanceof GsmCellLocation){
			gcl = (GsmCellLocation) cl;
		}else if(cl instanceof CdmaCellLocation){
			ccl = (CdmaCellLocation) cl;
		}
        
		if(gcl != null) {
			int id = gcl.getCid();
			Log.i(TAG,"gcl id :" + id);
			return id;
		}
		
		if(ccl != null){
			int id = ccl.getBaseStationId();
			Log.i(TAG,"ccl id :" + id);
			return id;
		}
		return 0;
	}
	
	public boolean isPositioning() {
//		Cursor cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
//		if(cursor != null) {
//			while(cursor.moveToNext()) {
//				String locationCity = cursor.getString(cursor.getColumnIndex("islocation"));
//				if(locationCity.equals("T")) {
//					return true;
//				}
//			}
//		}
//		return false;
		return true;
	}
	
	public String getCityCode(String name) {
		Cursor cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
		if(cursor != null) {
			return queryCityDate(name, cursor);
		} else {
			return "";
		}
	}
	
	private String queryCityDate(String name, Cursor cursor) {
		while(cursor.moveToNext()) {
			String locationCity = cursor.getString(cursor.getColumnIndex("islocation"));
			if(locationCity.equals("T")) {
				String city = cursor.getString(cursor.getColumnIndex(name));
				cursor.close();
				return city;
			} else {
				cursor.close();
				return "";
			}
		}
		cursor.close();
		return "";
	}
	
	public String postRequestHttp(String httpPath, JSONObject params) {
		String result = null;
		try {
			HttpPost httpPost = new HttpPost(httpPath);
			Log.e(TAG, "----------params.toString()-----------" + params.toString());
			StringEntity entity = new StringEntity(params.toString(), HTTP.UTF_8);
			entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(entity);
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
			int code = httpResponse.getStatusLine().getStatusCode();
			Log.e(TAG, "--------------------code---------------------" + code);
			if(code == HttpStatus.SC_OK) {
				InputStream input = httpResponse.getEntity().getContent();
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				int len = 0;
				byte[] by = new byte[1024];
				while ((len = input.read(by)) != -1) {
					output.write(by, 0, len);
				}
				result = new String(output.toByteArray());
				Log.e(TAG, "--------------result-----------------" + result);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	public JSONObject parseJson(String data) {
		if(null != data) {
			JSONObject jObject = null;
			try {
				jObject = new JSONObject(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return jObject;
		}
		return null;
	}
	
}
