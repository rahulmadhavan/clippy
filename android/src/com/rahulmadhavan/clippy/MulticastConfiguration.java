package com.rahulmadhavan.clippy;

public class MulticastConfiguration {

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
	
	
	
	
}
