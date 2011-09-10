package com.tshirtslayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import com.tshirtslayer.DbAdapter;

public class item extends Activity {
	
	private DbAdapter dbHelper;
	private static final String TAG = "TshirtSlayer item intent: ";
	private String sType;
	private String sTradeType;
	private String sYear;
	private String sTitle;
	
	void showToast(CharSequence msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.item);

		 dbHelper = new DbAdapter(this);
		 dbHelper.open();
		
		
		// setup item type
		Spinner typeSpinner = (Spinner) findViewById(R.id.item_type);

		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter
				.createFromResource(this, R.array.item_types,
						android.R.layout.simple_spinner_item);				
		typeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeAdapter);
		
        typeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        sType = parent.getItemAtPosition(position).toString();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    	sType = "";
                    }
                });
		
		
		
        // setup year spinner
		Spinner yearSpinner = (Spinner) findViewById(R.id.item_year);

		ArrayAdapter<CharSequence> yearAdapter = ArrayAdapter
				.createFromResource(this, R.array.item_year_list,
						android.R.layout.simple_spinner_item);

		yearAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		yearSpinner.setAdapter(yearAdapter);
		
        yearSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        sYear = parent.getItemAtPosition(position).toString();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    	sYear = "";
                    }
                });

        
		// setup item trade type
		Spinner tradeTypeSpinner = (Spinner) findViewById(R.id.item_trade_type);

		ArrayAdapter<CharSequence> tradeTypeAdapter = ArrayAdapter
				.createFromResource(this, R.array.item_trade_types,
						android.R.layout.simple_spinner_item);

		tradeTypeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tradeTypeSpinner.setAdapter(tradeTypeAdapter);
		
        tradeTypeSpinner.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        sTradeType = parent.getItemAtPosition(position).toString();
                        showToast(sTradeType);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

		
		
		Button confirmButton = (Button) findViewById(R.id.addButton);

		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

			    EditText mTitle   = (EditText)findViewById(R.id.item_title);
			    
				ArrayList<String> contentUris = getIntent().getStringArrayListExtra("tshirtslayer_contentUris");
/*				Object uri[] = contentUris.toArray();
				for(int n=0; n<uri.length; n++) {
					Log.i(TAG, (String)uri[n].toString() );
				}
*/
				// check all the fields are entered and ready
				if (sType.length() > 0  && sYear.length() > 0 && sTradeType.length() > 0 
						&& (mTitle.getText().toString().equals(getText(R.string.item_title_default)) == false) ) {

					sTitle = mTitle.getText().toString();
					long id = dbHelper.createItem(sTitle, sType, sYear, sTradeType, contentUris);

					showToast("Queued for addition!");					
					setResult(RESULT_OK);
					finish();
					
				} else {
					showToast("Error! Please fill in all the fields");
				}				
			}
		});

	}

}