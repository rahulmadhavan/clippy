package com.rahulmadhavan.clippy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {


	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                       
        startService(new Intent(getBaseContext(), ClippyService.class));
        setContentView(R.layout.fragment_main);

    }


    /** 
     * Called when the user clicks the Send button 
     */
    public void restartService(View view) {    	    	
    	
    	EditText editText = (EditText) findViewById(R.id.edit_message);
    	String inputText = editText.getText().toString();

    	if(inputText.compareTo("") != 0){
    		int inputPort = Integer.parseInt(inputText);
        	if(inputPort != MulticastConfiguration.getPort()){
            	
        		MulticastConfiguration.setPort(inputPort);
        		
        		try{
            		stopService(new Intent(getBaseContext(), ClippyService.class));
            	}catch(Exception exception){
            		Log.e("MainActivity", "Boooyaaa ka sha");
            	}
            	
            	startService(new Intent(getBaseContext(), ClippyService.class));
            	Log.e("MainActivity", "Clippy Service Restarted");
        	}    		
    		
    	}
    	    	    	    	    	
    }
    
    
    
                    
}
