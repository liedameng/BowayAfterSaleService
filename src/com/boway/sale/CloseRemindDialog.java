package com.boway.sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

public class CloseRemindDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		TextView view = new TextView(this);
		setContentView(view);
		int simId = getIntent().getIntExtra("simId", 1);
		String message = null;
		if(simId == 1){
			message = getString(R.string.close_sim1_remind_message);
			createDialog(message);
		}else if(simId == 2){
			message = getString(R.string.close_sim2_remind_message);
			createDialog(message);
		}
	}
	
	private void createDialog(String message) {
		new AlertDialog.Builder(this)
				.setMessage(message)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
				        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        startActivity(intent);
						finish();
					}
				}).setCancelable(false)
				.show();
	}
}
