package com.rahulmadhavan.clippy;

import android.content.SharedPreferences;

public class MulticastConfiguration {

	public static final String CLIPPY_CONFIGURATION = "ClippyConfiguration";
	private static int port = 9292;
	private static String ip = "224.1.1.1";
	
	public static int getPort() {
		return port;
	}
	public static void setPort(int _port) {
		port = _port;
	}
	public static String getIp() {
		return ip;
	}
	public static void setIp(String _ip) {
		ip = _ip;
	}
	
    public static void setPortNo(SharedPreferences settings, int portNo){        
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("portNo",portNo);
        editor.commit();
        MulticastConfiguration.setPort(portNo);         	
    }
	
    public static void initalizeConfiguration(SharedPreferences settings){        
        int savedPortNo = settings.getInt("portNo",9292);
        MulticastConfiguration.setPort(savedPortNo);        
    }
    
    
	
}
