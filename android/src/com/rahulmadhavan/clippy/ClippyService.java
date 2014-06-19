package com.rahulmadhavan.clippy;




import android.app.Service;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class ClippyService extends Service{

	WifiManager wifi;
	ClipboardManager clipboard;
	Thread clippyReceiverThread;
	Thread clippySenderThread;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate(){		
		Log.e("ClippyService", "Clippy Service Created");
		
		Message message = new Message("");

		if(null == this.wifi){				
			this.wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);			
		}
		if(null == this.clipboard){
			this.clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		}
		
		if(null == clippyReceiverThread){						
			clippyReceiverThread = new ClippyReceiver(wifi,clipboard,message);		                        		
			clippyReceiverThread.start();
						
		}

		if(null == clippySenderThread){							
			clippySenderThread = new ClippySender(wifi,clipboard,message);		                        		
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
		clippySenderThread = null;
		clippyReceiverThread = null;
		
	}

	
	

}
