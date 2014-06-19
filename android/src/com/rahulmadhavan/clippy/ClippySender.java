package com.rahulmadhavan.clippy;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.net.wifi.WifiManager;
import android.util.Log;


@SuppressLint("NewApi")
public class ClippySender extends Thread {

	private WifiManager wifi;
	private ClipboardManager clipboard;
	private Message message;
	private int port;
	private String address;
	public Condition condition;
	
	public ClippySender(WifiManager _wifi,ClipboardManager _clipboard,Message _message, Condition _condition){
		super();
		this.wifi = _wifi;
		this.clipboard = _clipboard;
		this.message = _message;
		this.port = MulticastConfiguration.getPort();
		this.address = MulticastConfiguration.getIp();
		this.condition = _condition;
		
	}
	
	@Override
	public void run(){
	
		String oldData = null;
		String newData = null;		
        MulticastSocket socket = null;
        InetAddress add;
        
		try {
			
			Log.e("ClippySender", "starting sender");
			
			socket = new MulticastSocket(this.port);
			add = InetAddress.getByName(this.address);
	        socket.joinGroup(add);
	    
	        Log.e("ClippySender", "Listening");
	        
	        while(condition.isActive()){
				
				if (clipboard.hasPrimaryClip()) {
				    if (clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
				    	ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
				    	CharSequence data = item.getText();
				    					    					    	
				    	if(null != data){
				    		newData = data.toString();
				    		if(null != newData){
				    							    							    				
				    			if(oldData == null || (oldData != null && newData.compareTo(oldData) != 0)){
				    				
				    				if(!message.isValueSame(newData)){
				    									    					
						    			Log.e("ClippySender", "new Data " + newData);
						    			byte[] byteData = newData.getBytes();
						    			DatagramPacket dp = new DatagramPacket(byteData,byteData.length,add,this.port);
						    			socket.send(dp);
						    			
				    				}					    				
				    				oldData = newData;
				    				message.setValue(oldData);
				    			}
				    			
				    		}
				    		
				    	}
				    	
				    	
				    					    					    	
				    } 		
				}
				
				Thread.sleep(2000);
			}
	        
			
		} catch (UnknownHostException e) {
			Log.e("ClippySender", "UnknownHostException");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("ClippySender", "IOException");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("ClippySender", "InterruptedException");
			e.printStackTrace();
		}
         

		if(null != socket)
        	socket.close();
        
		Log.e("ClippySender", "Receiver has stopped");
		
	}
	
}
