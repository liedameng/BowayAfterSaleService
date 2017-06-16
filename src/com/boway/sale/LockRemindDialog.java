package com.boway.sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.widget.TextView;

public class LockRemindDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView view = new TextView(this);
		setContentView(view);
		
		createDialog();
	}
	
	private void createDialog() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.lock_remind_message)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).setCancelable(false)
				.show();
	}
}
