package com.boway.sale;

import java.util.ArrayList;

import com.boway.sale.R;
import com.boway.sale.fragment.ElectronicInformationFragment;
import com.boway.sale.fragment.ServiceModeFragment;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SaleServiceActivity extends FragmentActivity implements OnClickListener {
	
	private final static String TAG = "SaleServiceActivity";
	
	private TextView serviceMode;
	private TextView electronicInformation;
	
	private ArrayList<Fragment> mFragments;
	private ViewPager mViewpager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sale_service);
		
		initView();
	}

	private void initView() {
		serviceMode = (TextView) findViewById(R.id.service_mode);
		serviceMode.setOnClickListener(this);
		electronicInformation = (TextView) findViewById(R.id.electronic_information);
		electronicInformation.setOnClickListener(this);
		
		mFragments = new ArrayList<Fragment>();
		Fragment serviceModeFragment = new ServiceModeFragment();
		Fragment electronicInformationFragment = new ElectronicInformationFragment();
		mFragments.add(serviceModeFragment);
		mFragments.add(electronicInformationFragment);
		
		mViewpager = (ViewPager) this.findViewById(R.id.viewpager);
		mViewpager.setAdapter(new SaleServicePagerAdapter(getSupportFragmentManager(),mFragments));
		mViewpager.setOnPageChangeListener(new SaleServicePageChangeListener());
		mViewpager.setOffscreenPageLimit(4);
	}
	
	private class SaleServicePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
			switch(position) {
			case 0:
				serviceMode.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
				electronicInformation.setTextColor(Color.argb(0x7F, 0xFF, 0xFF, 0xFF));
				break;
			case 1:
				electronicInformation.setTextColor(Color.argb(0xFF, 0xFF, 0xFF, 0xFF));
				serviceMode.setTextColor(Color.argb(0x7F, 0xFF, 0xFF, 0xFF));
				break;
			}
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
	}
	
	class SaleServicePagerAdapter extends FragmentPagerAdapter {
		private ArrayList<Fragment> mFragments;
		public SaleServicePagerAdapter(FragmentManager fm,ArrayList<Fragment> fragments) {
			super(fm);
			this.mFragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
	} 

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.service_mode:
			mViewpager.setCurrentItem(0);
			break;
		case R.id.electronic_information:
			mViewpager.setCurrentItem(1);
			break;
		}
	}
}
