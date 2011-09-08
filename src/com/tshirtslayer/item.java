package com.tshirtslayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class item extends Activity {

  
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.item);


		// setup item type
	    Spinner typeSpinner = (Spinner) findViewById(R.id.item_type);

	    ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
	            this, R.array.item_types, android.R.layout.simple_spinner_item);
	    
	    typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    typeSpinner.setAdapter(typeAdapter);

	    // setup item trade type
	    Spinner tradeTypeSpinner = (Spinner) findViewById(R.id.item_trade_type);

	    ArrayAdapter<CharSequence> tradeTypeAdapter = ArrayAdapter.createFromResource(
	            this, R.array.item_trade_types, android.R.layout.simple_spinner_item);
	    
	    tradeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    tradeTypeSpinner.setAdapter(tradeTypeAdapter);
	    
		
        Button confirmButton = (Button) findViewById(R.id.addButton);

        
        confirmButton.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {

        	// @todo queue sqlite here
        	  
  			CharSequence text = "Your item has been queued for addition to the worlds biggest gallery of metal tshirts and battlejackets, awesome!";
  			int duration = Toast.LENGTH_SHORT;
  			Toast toast = Toast.makeText(item.this, text, duration);  
  			toast.show();		
  			
            setResult(RESULT_OK);
            finish();
          }
        });

	}

}