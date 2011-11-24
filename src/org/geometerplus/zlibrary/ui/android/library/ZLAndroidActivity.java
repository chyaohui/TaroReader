/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.library;

import java.lang.reflect.*;

import android.app.Activity;
import android.os.Bundle;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager;

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.library.Bookmark;
import org.geometerplus.fbreader.library.Library;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;

import com.mt.airad.AirAD;
import com.mt.airad.AirAD.AirADListener;

public abstract class ZLAndroidActivity extends Activity implements View.OnClickListener{
	protected abstract ZLApplication createApplication(ZLFile file);
	protected RelativeLayout rl_title;
	protected RelativeLayout rl_bottom;
	protected AirAD ad;
	private ImageView  bt_INCREASE_FONT;
	private ImageView  bt_DECREASE_FONT;
    private TextView  top_book_title;
	private ImageView menu_toc;
	private ImageView menu_bookmark;
	private ImageView menu_day;
	
	private boolean isDayMode = false;
	
	private static final String REQUESTED_ORIENTATION_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.RequestedOrientation";
	private static final String ORIENTATION_CHANGE_COUNTER_KEY = "org.geometerplus.zlibrary.ui.android.library.androidActiviy.ChangeCounter";

	static {
        AirAD.setGlobalParameter("39493759-b9d7-4cdb-a31a-b85b097c0188", false);
    }
	
	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(REQUESTED_ORIENTATION_KEY, myOrientation);
		state.putInt(ORIENTATION_CHANGE_COUNTER_KEY, myChangeCounter);
	}

	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	final void setScreenBrightness(int percent) {
		if (percent < 1) {
			percent = 1;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		((ZLAndroidApplication)getApplication()).ScreenBrightnessLevelOption.setValue(percent);
	}

	final int getScreenBrightness() {
		final int level = (int)(100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private void disableButtonLight() {
		try {
			final WindowManager.LayoutParams attrs = getWindow().getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, 0);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}

	protected abstract ZLFile fileFromIntent(Intent intent);

	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

		if (state != null) {
			myOrientation = state.getInt(REQUESTED_ORIENTATION_KEY, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			myChangeCounter = state.getInt(ORIENTATION_CHANGE_COUNTER_KEY);
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (ZLAndroidApplication.Instance().DisableButtonLightsOption.getValue()) {
			disableButtonLight();
		}
		setContentView(R.layout.main);
	    rl_title = (RelativeLayout)this.findViewById(R.id.main_top);
	    rl_bottom = (RelativeLayout)this.findViewById(R.id.main_bottom);
	    bt_DECREASE_FONT = (ImageView)this.findViewById(R.id.top_bt_reduce);
	    bt_INCREASE_FONT = (ImageView)this.findViewById(R.id.top_bt_add);
	    top_book_title = (TextView)this.findViewById(R.id.top_bar_text);
	    menu_toc = (ImageView)this.findViewById(R.id.img_booktoc);
	    menu_bookmark = (ImageView)this.findViewById(R.id.img_bookmark);
	    menu_day = (ImageView)this.findViewById(R.id.img_day);
	 
	    
	    
//	    ad = (AirAD) this.findViewById(R.id.airad);
//	    ad.setBackgroundAutoHidden(true);  
//	    ad.setAirADListener(new AirADListener() {
//
//	    	 public void onAirADFailed() {
//	    	 // airAD启动失败,当参数非法或IP地址不在国内时触发 
//              Toast.makeText(ZLAndroidActivity.this, "airAD启动失败,当参数非法或IP地址不在国内时触发", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdReceivedFailed() {
//	    	 // 广告填充失败 
//	    		 Toast.makeText(ZLAndroidActivity.this, "广告填充失败 ", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdReceived() {
//	    	 // 广告填充成功 
//	    		 Toast.makeText(ZLAndroidActivity.this, "广告填充成功 ", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdContentWillShow() {
//	    	 // 当广告内容将要展示时触发 
//	    		 Toast.makeText(ZLAndroidActivity.this, "onAdContentWillShow", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdContentWillDismiss() {
//	    	 // 当广告内容将要消失时触发 
//	    		 Toast.makeText(ZLAndroidActivity.this, "当广告内容将要消失时触发", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdContentLoadFinished() {
//	    	 // 当广告内容加载完全时触发 
//	    		 Toast.makeText(ZLAndroidActivity.this, "当广告内容加载完全时触发", Toast.LENGTH_SHORT).show();
//	    	}
//
//	    	 public void onAdContentDidShow() {
//	    	 // 当广告内容展示时触发 
//
//	    	}
//
//	    	 public void onAdContentDidDismiss() {
//	    	 // 当广告内容消失时触发 
//
//	    	}
//
//	    	 public void onAdBannerWillShow() {
//	    	 // 当广告条将要展示时触发 
//
//	    	}
//
//	    	 public void onAdBannerWillDismiss() {
//	    	 // 当广告条将要消失时触发 
//
//	    	}
//
//	    	 public void onAdBannerDidShow() {
//	    	 // 当广告条展示时触发 
//
//	    	}
//
//	    	 public void onAdBannerDidDismiss() {
//	    	 // 当广告条消失时触发 
//
//	    	}
//
//	    	 public void onAdBannerClicked() {
//	    	 // 当广告条被点击时触发 
//	    		 Toast.makeText(ZLAndroidActivity.this, "当广告条被点击时触发 ", Toast.LENGTH_SHORT).show();
//	    	}
//	    	 });
	    
	    
	    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		getLibrary().setActivity(this);

		final ZLFile fileToOpen = fileFromIntent(getIntent());
		if (((ZLAndroidApplication)getApplication()).myMainWindow == null) {
			System.out.println(this.getClass().getName()+"====onCreate()====>1="+fileToOpen);
			ZLApplication application = createApplication(fileToOpen);
			((ZLAndroidApplication)getApplication()).myMainWindow = new ZLAndroidApplicationWindow(application);
			application.initWindow();
		} else {
			System.out.println(this.getClass().getCanonicalName()+"====onCreate()====>2="+fileToOpen);
			
			ZLApplication.Instance().openFile(fileToOpen);
		}
		
		ZLApplication.Instance().getViewWidget().repaint();
		
		bt_INCREASE_FONT.setOnClickListener(clickListener);
	    bt_DECREASE_FONT.setOnClickListener(clickListener);
	    menu_toc.setOnClickListener(clickListener);
	    menu_bookmark.setOnClickListener(clickListener);
	    menu_day.setOnClickListener(clickListener);
		
	}

	private OnClickListener clickListener = new OnClickListener(){

		public void onClick(View v) {
	         switch(v.getId()){
	           case R.id.top_bt_reduce:
	        	   ZLApplication.Instance().doAction( ActionCode.DECREASE_FONT);
				   System.out.println(" ActionCode.DECREASE_FONT be clicked");
	        	   break;
	           case R.id.top_bt_add:
	        	   ZLApplication.Instance().doAction( ActionCode.INCREASE_FONT);
				   System.out.println(" ActionCode.INCREASE_FONT be clicked");
	        	   
	        	   break;
	           case R.id.img_booktoc:
	        	   final FBReaderApp fbreader = (FBReaderApp)FBReaderApp.Instance();
	        	   fbreader.openBook(Library.getPreviousBook(), null);
	        	   System.out.println(" ActionCode.img_booktoc");  
	        	   break;
	           case R.id.img_bookmark:
	        	 
	        	   break;
	           case R.id.img_day:
	        	   if(!isDayMode){
	        		   isDayMode = true;  
	        		   ZLApplication.Instance().doAction(ActionCode.SWITCH_TO_NIGHT_PROFILE);
		        	  }else{
		        		  isDayMode = false;
		        		  ZLApplication.Instance().doAction(ActionCode.SWITCH_TO_DAY_PROFILE);
		        	  }
		        	  
	        	   break;
	           default:
	        	   break;
	           }
				
				
		}};
	
	
	
	@Override
	public void onStart() {
		super.onStart();

		if (ZLAndroidApplication.Instance().AutoOrientationOption.getValue()) {
			setAutoRotationMode();
		} else {
			switch (myOrientation) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
					if (getRequestedOrientation() != myOrientation) {
						setRequestedOrientation(myOrientation);
						myChangeCounter = 0;
					}
					break;
				default:
					setAutoRotationMode();
					break;
			}
		}
	}

	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;

	public final void createWakeLock() {
		
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock =
						((PowerManager)getSystemService(POWER_SERVICE)).
							newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "FBReader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			ZLApplication.Instance().startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		switchWakeLock(
			ZLAndroidApplication.Instance().BatteryLevelToTurnScreenOffOption.getValue() <
			ZLApplication.Instance().getBatteryLevel()
		);
		myStartTimer = true;
		final int brightnessLevel =
			((ZLAndroidApplication)getApplication()).ScreenBrightnessLevelOption.getValue();
		if (brightnessLevel != 0) {
			setScreenBrightness(brightnessLevel);
		} else {
			setScreenBrightnessAuto();
		}

		registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public void onPause() {
		unregisterReceiver(myBatteryInfoReceiver);
		ZLApplication.Instance().stopTimer();
		switchWakeLock(false);
		ZLApplication.Instance().onWindowClosing();
		super.onPause();
	}

	@Override
	public void onLowMemory() {
		ZLApplication.Instance().onWindowClosing();
		super.onLowMemory();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		ZLApplication.Instance().openFile(fileFromIntent(intent));
	}

	private static ZLAndroidLibrary getLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View view = findViewById(R.id.main_view);
		return ((view != null) && view.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	private int myChangeCounter;
	private int myOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	private void setAutoRotationMode() {
		final ZLAndroidApplication application = ZLAndroidApplication.Instance();
		myOrientation = application.AutoOrientationOption.getValue() ?
			ActivityInfo.SCREEN_ORIENTATION_SENSOR : ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
		setRequestedOrientation(myOrientation);
		myChangeCounter = 0;
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		switch (getRequestedOrientation()) {
			default:
				break;
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				if (config.orientation != Configuration.ORIENTATION_PORTRAIT) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
				if (config.orientation != Configuration.ORIENTATION_LANDSCAPE) {
					myChangeCounter = 0;
				} else if (myChangeCounter++ > 0) {
					setAutoRotationMode();
				}
				break;
		}
	}

	void rotate() {
		View view = findViewById(R.id.main_view);
		if (view != null) {
			switch (getRequestedOrientation()) {
				case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
					myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				default:
					if (view.getWidth() > view.getHeight()) {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					} else {
						myOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					}
			}
			setRequestedOrientation(myOrientation);
			myChangeCounter = 0;
		}
	}

	BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			((ZLAndroidApplication)getApplication()).myMainWindow.setBatteryLevel(level);
			switchWakeLock(
				ZLAndroidApplication.Instance().BatteryLevelToTurnScreenOffOption.getValue() < level
			);
		}
	};
}
