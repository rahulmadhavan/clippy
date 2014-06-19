package com.rahulmadhavan.clippy;

public class Message {

	private String value;
	
	public Message(){
		
	}
	
	public Message(String _value){
		this.value = _value;
	}

	public synchronized boolean isValueSame(String data){
		
		boolean result = false;
		
		if( null == data || null == value ){
			
		}else if(data.compareTo(value) != 0){
			
		}else{
			result = true;
		}
					
		return result;
						
	}
	
	public synchronized String getValue() {
		return value;
	}

	public synchronized void setValue(String value) {
		this.value = value;
	}
		
	@Override
	public synchronized String toString(){
		return this.value;
	}
}
