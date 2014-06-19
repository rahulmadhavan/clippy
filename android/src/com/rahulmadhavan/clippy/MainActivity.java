package com.rahulmadhavan.clippy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {


	
	public static SharedPreferences settings;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {    	
        super.onCreate(savedInstanceState);
        
        settings = getSharedPreferences(MulticastConfiguration.CLIPPY_CONFIGURATION, 0);        
        MulticastConfiguration.initalizeConfiguration(settings);
        
        
        
        stopService(new Intent(getBaseContext(), ClippyService.class));
        startService(new Intent(getBaseContext(), ClippyService.class));
        
        setContentView(R.layout.fragment_main);
        
        
		showToast("Clippy Clipping on "+MulticastConfiguration.getPort());		
		updatePortNoTextView();
		
		
    }


    /** 
     * Called when the user clicks the Send button 
     */
    public void restartService(View view) {    	    	
    	    	    	    	
    	EditText editText = (EditText) findViewById(R.id.edit_message_port_no);
    	String inputText = editText.getText().toString();
    	    	
    	if(inputText.compareTo("") != 0){
    		int inputPort = Integer.parseInt(inputText);
    		
        	if(inputPort != MulticastConfiguration.getPort()){
            	
        		MulticastConfiguration.setPortNo(settings,inputPort);

        		
        		try{
        			
            		stopService(new Intent(getBaseContext(), ClippyService.class));
            		startService(new Intent(getBaseContext(), ClippyService.class));
            		
            		
            		updatePortNoTextView();            		
            		showToast("Clippy Clipping on "+MulticastConfiguration.getPort());
            		
            		
            	}catch(Exception exception){
            		
            		Log.e("MainActivity", "Boooyaaa ka sha");
            		showToast("Clippy has failed !!!");            	
  
            		
            	}
            	
        		Log.e("MainActivity", "Clippy Service Restarted");
            	
        	}    		
    		
    	}
    	    	    	    	    	
    }
    
    public void updatePortNoTextView() {
    	
        TextView textView = (TextView) findViewById(R.id.label_current_port_no_value);
        textView.setText(""+ MulticastConfiguration.getPort());
        
    }
    
    public void showToast(String text){
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
  		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
    }
    
            

    
                    
}
