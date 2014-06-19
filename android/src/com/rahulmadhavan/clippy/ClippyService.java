package com.rahulmadhavan.clippy;




import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class ClippyService extends Service{

	WifiManager wifi;
	ClipboardManager clipboard;
	ClippyReceiver clippyReceiverThread;
	ClippySender clippySenderThread;
	Condition condition;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){		
					
		Log.e("ClippyService", "Clippy Service Created");
		
		SharedPreferences settings = getSharedPreferences(MulticastConfiguration.CLIPPY_CONFIGURATION, 0);
		MulticastConfiguration.initalizeConfiguration(settings);
		
		Message message = new Message("");
		
		if(condition == null){
			condition = new Condition();
		}

		if(null == this.wifi){				
			this.wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);			
		}
		if(null == this.clipboard){
			this.clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		}
		
		if(null == clippyReceiverThread){						
			clippyReceiverThread = new ClippyReceiver(wifi,clipboard,message,condition);
			clippyReceiverThread.setDaemon(true);			
			clippyReceiverThread.start();
						
		}

		if(null == clippySenderThread){							
			clippySenderThread = new ClippySender(wifi,clipboard,message,condition);
			clippySenderThread.setDaemon(true);
			clippySenderThread.start();
						
		}
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.e("ClippyService", "ClippyReceiver Service Start Command");
		
		return START_STICKY;		
	}
	
	
	@Override
	public void onDestroy(){
		Log.e("ClippyService", "ClippyReceiver Service Destroyed");
		if(clippySenderThread != null){
			Log.e("ClippyService", "clippySenderThread Service Destroyed");
			clippySenderThread.condition.setActive(false);
			clippySenderThread = null;
		}
		if(clippyReceiverThread != null){
			Log.e("ClippyService", "clippyReceiverThread Service Destroyed");
			clippyReceiverThread.condition.setActive(false);
			clippyReceiverThread = null;
		}
		
		
		
	}

	
	

}
