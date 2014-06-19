package com.rahulmadhavan.clippy;

public class Condition{
	
	private volatile boolean active = true;

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void setActive(boolean active) {
		this.active = active;
	}
	
	
	

}
