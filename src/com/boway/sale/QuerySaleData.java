package com.boway.sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.TextView;
import com.boway.sale.R;

public class QuerySaleData extends Activity {
	
	static final String SMS_DATA_PROVIDER = "content://com.boway.saleservice.data.provider";
	static final String NETWORK_DATA_PROVIDER = "content://com.boway.saleservice.network.data.provider";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView view = new TextView(this);
		setContentView(view);
		
		createDialog();
	}
	
	private void createDialog() {
		StringBuffer smsRegBuffer = new StringBuffer();
        ContentResolver resolver = this.getContentResolver();
        Cursor c = null;
		    try{
	          c = resolver.query(Uri.parse(SMS_DATA_PROVIDER), null, null, null, null);
	          smsRegBuffer.append(this.getString(R.string.smsregMms));
	          if(c != null && c.getCount() > 0){
		            if(c.moveToLast()){
		        	      smsRegBuffer.append(c.getString(c.getColumnIndex("successText")));
		        	      smsRegBuffer.append("\r\n");
		            }
	          } else {
	        	    smsRegBuffer.append(this.getString(R.string.smsregTips));
	        	    smsRegBuffer.append("\r\n");
	          }
		    }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(c != null){
        		    c.close();
        	  }
        }
        
        Cursor cursor = null;
        try{
	          cursor = resolver.query(Uri.parse(NETWORK_DATA_PROVIDER), null, null, null, null);
	          smsRegBuffer.append(this.getString(R.string.smsregNetwork));
	          if(cursor != null && cursor.getCount() > 0){
		            if(cursor.moveToLast()){
		        	      smsRegBuffer.append(cursor.getString(cursor.getColumnIndex("successText")));
		            }
	          } else {
	        	    smsRegBuffer.append(this.getString(R.string.smsregTips));
	          }
		    }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(cursor != null){
        		    cursor.close();
        	  }
        }
        Log.e("Query", "---------------smsRegBuffer.toString()---------" + smsRegBuffer.toString());
		new AlertDialog.Builder(this)
				.setTitle(R.string.smsreg).setMessage(smsRegBuffer.toString())
				.setPositiveButton(R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).setCancelable(false)
				.show();
		
	}
	
}
