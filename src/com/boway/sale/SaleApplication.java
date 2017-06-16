package com.boway.sale;

import android.app.Application;
import android.util.Log;

public class SaleApplication extends Application {
	protected static final String TAG = "SaleApplication";
	
	private static final String WF_NUMBER_DATABASE_NAME = "phone_number_wf.db";
	
	private static final int WF_CODE = 3;
	
	private static final int CODE = WF_CODE;
	
	
    @Override
    public void onCreate() {
    	super.onCreate();
    }
    
    public static String getDatabaseName(){
    	switch(CODE){
    	case WF_CODE:
    		return WF_NUMBER_DATABASE_NAME;
    		default:
    			return WF_NUMBER_DATABASE_NAME;
    	}
    }
}
