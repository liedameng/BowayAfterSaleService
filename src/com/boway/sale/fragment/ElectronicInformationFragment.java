package com.boway.sale.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

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
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.NetworkUtils;
import com.boway.sale.R;
import com.boway.sale.service.NetworkSendService;
import com.boway.sale.util.Filed;
import com.mediatek.telephony.SmsManagerEx;

public class ElectronicInformationFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "ElectronicInformationFragment";
	
	private View view;
	private TextView registerText;
	private EditText registerEidt;
	private Button registerBtn;
	private LinearLayout registerLayout;
	private LinearLayout inputRegisterLayout;
	private TextView registerSuccessText;
	private TextView elecInformationText;
	
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
    
    private NetworkUtils utils;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cResolver = getActivity().getContentResolver();
		Cursor cursor = cResolver.query(Uri.parse(DATA_PROVIDER), null, null, null, null);
//		telephony = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		telephony = TelephonyManager.from(getActivity());
		mContentValue = getContentProviderLastRowData(cursor);
		
		utils = new NetworkUtils(getActivity());
		if(null != mContentValue && mContentValue.size() > 0){
			isSent = mContentValue.getAsInteger("isSent");
		}
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.boway.sale.register");
		getActivity().registerReceiver(registerState, filter);
	}
	
	BroadcastReceiver registerState = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context content, Intent intent) {
			setRegisterStateLayout(isRegisterSuccess());
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.electronic_layout, container, false);
		registerText = (TextView) view.findViewById(R.id.elec_register_code);
		registerText.setText(getRegisterCode());
		registerEidt = (EditText) view.findViewById(R.id.register_code_edit);
		registerEidt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
		registerBtn = (Button) view.findViewById(R.id.register_btn);
		registerBtn.setOnClickListener(this);
		
		registerLayout = (LinearLayout) view.findViewById(R.id.register_code_layout);
		inputRegisterLayout = (LinearLayout) view.findViewById(R.id.input_register_code_layout);
		registerSuccessText = (TextView) view.findViewById(R.id.register_success_text);
		elecInformationText = (TextView) view.findViewById(R.id.elec_information_text);
		
		setRegisterStateLayout(isRegisterSuccess());
		return view;
	}
	
	private void setRegisterStateLayout(boolean isRegisterState) {
		registerLayout.setVisibility(isRegisterState ? View.GONE : View.VISIBLE);
		inputRegisterLayout.setVisibility(isRegisterState ? View.GONE : View.VISIBLE);
		registerBtn.setVisibility(isRegisterState ? View.GONE : View.VISIBLE);
		elecInformationText.setText(isRegisterState ? getResources().getString(R.string.electronic_register_success_information) : getResources().getString(R.string.electronic_information));
		registerSuccessText.setVisibility(isRegisterState ? View.VISIBLE : View.GONE);
	}
	
	private boolean isRegisterSuccess() {
		if(isSent == Activity.RESULT_OK || !utils.queryEelcIsSend()) {
			return true;
		} 
		return false;
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
	
	private String getRegisterCode() {
		int index;
		String code = null;
		Random dom;
		for (int i = 1; i < 5; i ++) {
			dom = new Random();
			index = dom.nextInt(10);
			if(code == null) {
				code = String.valueOf(index);
			} else {
				code = code + index;
			}
		}
		return code;
	}
	
	private boolean getSameREgisterCode() {
		return registerText.getText().toString().trim().endsWith(registerEidt.getText().toString().trim()) && !registerEidt.getText().toString().trim().isEmpty();
	}
	
	private void doSendSIMMessage(int simId) {
		String imeiStr = "", imeiStr2 = "";
		String regularExpression = "^[0-9]*$";
		switch(simId) {
		case PhoneConstants.SIM_ID_1:
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
		// telephony = TelephonyManager.getDefault();
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
		} else {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sim_check_error),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void doSendMessage(String phoneNo, String content, int simId){
		Log.i(TAG,"doSendMessage:" + content + "####" + "phoneNo:" + phoneNo);
		if(send == null)
	    {
	    	send = new sendBroadcastReceiver();
	    	getActivity().registerReceiver(send, new IntentFilter(SENT_SMS_ACTION));
	    }
	    if(deliver == null)
	    {
	    	deliver = new deliverBroadcastReceiver();
	    	getActivity().registerReceiver(deliver, new IntentFilter(DELIVERED_SMS_ACTION));
	    }
		
		// create the sentIntent parameter
	    Intent sentIntent = new Intent(SENT_SMS_ACTION);  
	    PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0, sentIntent, 0);  
	  
	    // create the deilverIntent parameter  
	    Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);  
	    PendingIntent deliverPI = PendingIntent.getBroadcast(getActivity(), 0, deliverIntent, 0);  
	  
	    if(isSent != Activity.RESULT_OK){
			SmsManagerEx smsEx = SmsManagerEx.getDefault();
			smsEx.sendTextMessage(phoneNo, null, content, sentPI, deliverPI, simId);
    	}
	}

	@Override
	public void onDestroy() {
		if(send != null){
       	 getActivity().unregisterReceiver(send);
        }
        if(deliver != null){
       	 getActivity().unregisterReceiver(deliver);
        }
        getActivity().unregisterReceiver(registerState);
		super.onDestroy();
	}
       
	private sendBroadcastReceiver send;
	private class sendBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			int failcount = 0;
			StringBuffer successContent = new StringBuffer();
			if(getResultCode() == Activity.RESULT_OK){
				String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
				successContent.append(String.format(getString(R.string.sendSuccess), currentDateTime));
				addDataToProvider(getResultCode(), successContent.toString(), failcount);
				isSent = Activity.RESULT_OK;
				setRegisterStateLayout(isRegisterSuccess());
			} else {
				if (null != mContentValue && mContentValue.size() > 0) {
					failcount = mContentValue.getAsInteger("failurecount") + 1;
				}
				successContent.append(String.format(
						getString(R.string.sendFailure), failcount));
				addDataToProvider(getResultCode(), successContent.toString(),
						failcount);
				registerFailtError();
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
	
	private void registerCodeError() {
		registerEidt.setText("");
		Toast.makeText(getActivity(), getResources().getString(R.string.register_code_error), Toast.LENGTH_SHORT).show();
	}
	
	private void registerFailtError() {
		registerEidt.setText("");
		registerBtn.setEnabled(true);
		Toast.makeText(getActivity(), getResources().getString(R.string.register_failt), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.register_btn:
			Log.e(TAG, "----------------------" + getSameREgisterCode());
			if(getSameREgisterCode()) {
				if(isSent != Activity.RESULT_OK) {
					checkSimExists();
					registerBtn.setEnabled(false);
					Toast.makeText(getActivity(), getResources().getString(R.string.registering), Toast.LENGTH_SHORT).show();
				} 
				if(utils.queryEelcIsSend() && utils.checkTelephonyState() && utils.isNetworkAvailable() && utils.isPositioning()) {
					Intent netIntent = new Intent(getActivity(),NetworkSendService.class);
					netIntent.putExtra("isRegister", true);
					getActivity().startService(netIntent);
					registerBtn.setEnabled(false);
					Toast.makeText(getActivity(), getResources().getString(R.string.registering), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.register_conditions), Toast.LENGTH_SHORT).show();
				}
				
			} else {
				registerCodeError();
			}
			break;
		}
	}

}
