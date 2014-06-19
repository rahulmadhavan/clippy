package com.rahulmadhavan.clippy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;


@SuppressLint("NewApi")
public class ClippyReceiver extends Thread {
		
	private WifiManager wifi;
	private ClipboardManager clipboard;
	private Message message;
	private int port;
	private String address;
	
	public ClippyReceiver(WifiManager _wifi,ClipboardManager _clipboard, Message _message){
		super();
		this.wifi = _wifi;
		this.clipboard = _clipboard;
		this.message = _message;
		this.port = MulticastConfiguration.getPort();
		this.address = MulticastConfiguration.getIp();
		
	}
	
	@Override
    public void run() {        
        MulticastLock mLock = wifi.createMulticastLock("mylock");
        mLock.acquire();
        MulticastSocket socket = null;
        try {
                                               
            try {
                socket = new MulticastSocket(this.port);
                InetAddress add = InetAddress.getByName(this.address);
                socket.joinGroup(add);
                String oldData = null;
                String newData = null;
                int comparator = 1;
                
                while (true) {
                    try {
                    	byte[] buffer = new byte[10000];
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, add, this.port);
                                                
                        socket.receive(packet);
                        
                        newData = new String(packet.getData()).trim();
                        
                        if(newData != null && oldData !=null) 
                        	comparator = newData.compareTo(oldData);                                               
                        	
                        if(comparator != 0){
                        	
                            Log.e("ClippyReceiver", "Received from " + packet.getAddress().toString() + "\n");
                            Log.e("ClippyReceiver", "Received data: '" + new String(packet.getData()).trim() + "'");
                            oldData =  newData;
                            message.setValue(oldData);
                            ClipData clip = ClipData.newPlainText("clippy !!!",oldData);
                            clipboard.setPrimaryClip(clip);
                        }
                                                                                                                        
                        buffer = null;
                    } catch (UnknownHostException ue) {
                    	
                    }
                }
            } catch (java.net.BindException b) {
            	
            }
            

            
        } catch (IOException e) {
            System.err.println(e);
        }
        
        if(socket != null)
        	socket.close();
        
    }
	
	
}
