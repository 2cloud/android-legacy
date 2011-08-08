package com.suchagit.android2cloud;

import android.app.ListActivity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendActivity extends ListActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	final EditText url_input = (EditText) findViewById(R.id.url_input);
    	Button url_button = (Button) findViewById(R.id.url_input_button);
    	
    	url_button.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {
    			ContentValues values = new ContentValues();
    			values.put("user", "foran.paddy@gmail.com");
    			values.put("name", url_input.getText().toString());
    			getContentResolver().insert(TwoCloud.Devices.CONTENT_URI, values);
    		}
    	});
    }
}