package com.boway.sale;

import com.android.internal.telephony.PhoneConstants;
import com.boway.sale.broadcast.SimStateBroadcastReceiver;
import com.boway.sale.db.dao.PhoneNumberQueryDao;
import com.boway.sale.service.MessageVerifySevice;
import com.boway.sale.util.Filed;
import com.boway.sale.util.LockSimUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PhoneNumberCheckActivity extends Activity {

    private static final int PHONE_NUMBER_SEGMENG = 7;
    private static final String TAG = "PhoneNumberCheckActivity";
    public static final String ALWAY_FLY_MODE = "alway_fly_mode";

    private EditText mSim1_input;
    private EditText mSim2_input;
    private Button mCheckButton;

    private AlertDialog mDialog;
    private SharedPreferences mSPreferences;
    private View mView;
    private WindowManager mManager;
    private View mChecking;
    private View mCheckNotWeiFang;
    private AlphaAnimation mAnimation;

    private KeyguardManager mKeyguardManager;
    private KeyguardLock mKeyguardLock;

    BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
            int simState = tm.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                changeEditTextInputState();
            }
        }

    };

    BroadcastReceiver mUiChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isSuccess = intent.getBooleanExtra("isSuccess", false);
            int state = intent.getIntExtra("state", -1);
            // mDialog.cancel();
            if (mKeyguardLock != null)
                mKeyguardLock.reenableKeyguard();
            mManager.removeView(mView);
            if (!isSuccess) {
                /*
                 * Toast.makeText(PhoneNumberCheckActivity.this,
                 * R.string.check_number_not_phone_sim, Toast.LENGTH_LONG)
                 * .show();
                 */
                showEndDialog(R.string.check_number_not_phone_sim);
            } else if (state > 0 && state < 3) {
                int string = R.string.check_number_not_phone_sim;
                if (state == MessageVerifySevice.CHECK_SIM1_FAILE) {
                    string = R.string.check_number_not_phone_sim1;
                } else if (state == MessageVerifySevice.CHECK_SIM2_FAILE) {
                    string = R.string.check_number_not_phone_sim2;
                }
                /*
                 * Toast.makeText(PhoneNumberCheckActivity.this, string,
                 * Toast.LENGTH_LONG).show();
                 */
                showEndDialog(string);
            } else {
                PhoneNumberCheckActivity.this.finish();
            }
        }

    };

    private void showEndDialog(int string) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mDialog != null) {
                            mDialog.cancel();
                        }
                        PhoneNumberCheckActivity.this.finish();
                    }
                });
        mDialog = builder.create();
        mDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setContentView(R.layout.activity_location);
        mView = View.inflate(this, R.layout.activity_location, null);
        mSPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        lockSelf();
        mSim1_input = (EditText) mView.findViewById(R.id.check_input_1);
        mSim2_input = (EditText) mView.findViewById(R.id.check_input_2);
        mCheckButton = (Button) mView.findViewById(R.id.check_button);
        mChecking = mView.findViewById(R.id.check_checking);
        // mCheckNotWeiFang = mView.findViewById(R.id.check_not_weifang);
        mAnimation = new AlphaAnimation(1f, 0f);
        mAnimation.setDuration(2500L);
        addListener();
        changeEditTextInputState();
        registerSimStateAndSmsCheckReceiver();
        openSim();
        Log.i(TAG,"onCreat()");
    }

	private void openSim() {
        if (!LockSimUtils.isRadioOn(
                LockSimUtils.getSubIdBySlot(PhoneConstants.SIM_ID_1), this)) {
            Editor editor = mSPreferences.edit();
            editor.putBoolean("needLockSim1", false);
            editor.commit();
            LockSimUtils.setRadionOn(this,
                    LockSimUtils.getSubIdBySlot(PhoneConstants.SIM_ID_1), true);
        }

        if (!LockSimUtils.isRadioOn(
                LockSimUtils.getSubIdBySlot(PhoneConstants.SIM_ID_2), this)) {
            Editor editor = mSPreferences.edit();
            editor.putBoolean("needLockSim2", false);
            editor.commit();
            LockSimUtils.setRadionOn(this,
                    LockSimUtils.getSubIdBySlot(PhoneConstants.SIM_ID_2), true);
        }

    }

    private void addListener() {
        mCheckButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
            	
            	if (isAirPlaneModeOn(PhoneNumberCheckActivity.this)) {
            		mSPreferences.edit().putString("isStopByAirplaneMode", "yes").commit();
            		if (mKeyguardLock != null)
            			mKeyguardLock.reenableKeyguard();
            		mManager.removeView(mView);
            		showEndDialog(R.string.check_airplanemode);
            		return;
            	} else {
            		mSPreferences.edit().putString("isStopByAirplaneMode", "no").commit();
            	}

                if ((mSim1_input.getInputType() == EditorInfo.TYPE_CLASS_PHONE && mSim1_input
                        .getText().length() < 11)
                        || (mSim2_input.getInputType() == EditorInfo.TYPE_CLASS_PHONE && mSim2_input
                                .getText().length() < 11)
                        || mSim1_input.getText().toString()
                                .equals(mSim2_input.getText().toString())) {
                    return;
                }

                String sim1_num = mSim1_input.getText().toString().length() == 11 ? mSim1_input
                        .getText().toString() : "0000000";
                String sim2_num = mSim2_input.getText().toString().length() == 11 ? mSim2_input
                        .getText().toString() : "0000000";
                
                PhoneNumberQueryDao dao = new PhoneNumberQueryDao(
                        PhoneNumberCheckActivity.this);
                Editor editor = mSPreferences.edit();
                if ((!sim1_num.equals("0000000") || !sim2_num.equals("0000000"))
                        && checkNumberIsBelongWeiFang(
                                sim1_num.substring(0, PHONE_NUMBER_SEGMENG),
                                sim2_num.substring(0, PHONE_NUMBER_SEGMENG))) {
                    Intent intent = new Intent(PhoneNumberCheckActivity.this,
                            MessageVerifySevice.class);
                    if (dao.findNumber(sim1_num.substring(0,
                            PHONE_NUMBER_SEGMENG))) {
                        intent.putExtra("sim1Number", sim1_num);
                    } else {
                        int subId = LockSimUtils
                                .getSubIdBySlot(PhoneConstants.SIM_ID_1);
                        LockSimUtils.setRadionOn(PhoneNumberCheckActivity.this,
                                subId, false);
                        editor.putBoolean("needLockSim1", true);
                        Log.i(TAG,"jlzou needLockSim1 true");
                    }
                    if (dao.findNumber(sim2_num.substring(0,
                            PHONE_NUMBER_SEGMENG))) {
                        intent.putExtra("sim2Number", sim2_num);
                    } else {
                        int subId = LockSimUtils
                                .getSubIdBySlot(PhoneConstants.SIM_ID_2);
                        LockSimUtils.setRadionOn(PhoneNumberCheckActivity.this,
                                subId, false);
                        editor.putBoolean("needLockSim2", true);
                        Log.i(TAG,"jlzou needLockSim1 true");
                    }
                    editor.commit();
                    /*
                     * AlertDialog.Builder builder = new Builder(
                     * PhoneNumberCheckActivity.this);
                     * builder.setMessage(R.string.check_checking);
                     * builder.setCancelable(false); mDialog = builder.create();
                     * mDialog.show();
                     */
                    mChecking.setVisibility(View.VISIBLE);
                    mCheckButton.setEnabled(false);
                    mSim1_input.setEnabled(false);
                    mSim2_input.setEnabled(false);
                    startService(intent);
                } else if (!sim1_num.equals("0000000")
                        || !sim2_num.equals("0000000")) {
                    int subId = LockSimUtils
                            .getSubIdBySlot(PhoneConstants.SIM_ID_1);
                    editor.putBoolean("needLockSim1", true);
                    editor.commit();
                    LockSimUtils.setRadionOn(PhoneNumberCheckActivity.this,
                            subId, false);

                    subId = LockSimUtils
                            .getSubIdBySlot(PhoneConstants.SIM_ID_2);
                    editor.putBoolean("needLockSim2", true);
                    editor.commit();
                    Log.i(TAG,"jlzou needLock all sim true");
                    LockSimUtils.setRadionOn(PhoneNumberCheckActivity.this,
                            subId, false);
                    mManager.removeView(mView);
                    if (mKeyguardLock != null)
                        mKeyguardLock.reenableKeyguard();
                    showEndDialog(R.string.check_number_not_weifang);
                    /*
                     * Toast.makeText(PhoneNumberCheckActivity.this,
                     * getResources
                     * ().getString(R.string.check_number_not_weifang),
                     * Toast.LENGTH_LONG).show();
                     * PhoneNumberCheckActivity.this.finish();
                     */
                }
            }
        });

        mAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {
                if (mCheckNotWeiFang.getVisibility() != View.VISIBLE)
                    mCheckNotWeiFang.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                if (mCheckNotWeiFang.getVisibility() != View.GONE)
                    mCheckNotWeiFang.setVisibility(View.GONE);
            }
        });
    }

    private boolean isAirPlaneModeOn(Context context) {
        int mode = 0;
        try {
            mode = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return mode == 1;
    }

    private void lockSelf() {
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("");
        mKeyguardLock.disableKeyguard();
        mManager = getWindowManager();
        LayoutParams params = new LayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.type = LayoutParams.TYPE_SYSTEM_ERROR;
        params.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mManager.addView(mView, params);
    }

    protected void changeEditTextInputState() {
        Log.i(TAG,"changeEditTextInputState()");
        SubscriptionManager manager = SubscriptionManager.from(this);
        SubscriptionInfo sim1 = manager
                .getActiveSubscriptionInfoForSimSlotIndex(0);
        SubscriptionInfo sim2 = manager
                .getActiveSubscriptionInfoForSimSlotIndex(1);
        if (mSim1_input != null && sim1 != null) {
            mSim1_input.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            mSim1_input.setEnabled(true);
            mSim1_input.setHint(R.string.check_hint_please_input_number);
        } else {
            mSim1_input.setInputType(EditorInfo.TYPE_NULL);
            mSim1_input.setEnabled(false);
            mSim1_input.setHint(R.string.check_hint_no_sim);
        }
        if (mSim2_input != null && sim2 != null) {
            mSim2_input.setInputType(EditorInfo.TYPE_CLASS_PHONE);
            mSim2_input.setEnabled(true);
            mSim2_input.setHint(R.string.check_hint_please_input_number);
        } else {
            mSim2_input.setInputType(EditorInfo.TYPE_NULL);
            mSim2_input.setEnabled(false);
            mSim2_input.setHint(R.string.check_hint_no_sim);
        }
    }

    private boolean checkNumberIsBelongWeiFang(String sim1_num, String sim2_num) {
        PhoneNumberQueryDao dao = new PhoneNumberQueryDao(this);
        return dao.findImsi(sim1_num, sim2_num);
    }

    private void registerSimStateAndSmsCheckReceiver() {
        IntentFilter filter = new IntentFilter(
                SimStateBroadcastReceiver.SIM_STATE_CHANGED);
        registerReceiver(mSimStateReceiver, filter);
        filter = new IntentFilter(Filed.FLUSH_BROADCAST);
        registerReceiver(mUiChangeReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        return;
    };

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy()");
        if (mSimStateReceiver != null) {
            unregisterReceiver(mSimStateReceiver);
            mSimStateReceiver = null;
        }
        if (mUiChangeReceiver != null) {
            unregisterReceiver(mUiChangeReceiver);
            mUiChangeReceiver = null;
        }
        super.onDestroy();
    }
}
