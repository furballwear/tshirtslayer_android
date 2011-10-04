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
import android.widget.Toast;
import com.tshirtslayer.DbAdapter;

public class item extends Activity {

	private static final String TAG = "TshirtSlayer item intent: ";
	private String sType;
	private String sTradeType;
	private String sYear;
	private String sTitle;
	private String sBand;
	private DbAdapter dbHelper;
	
	void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	  dbHelper = new DbAdapter(this);
	  dbHelper.open();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dbHelper.close();
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.item);

		// setup item type
		Spinner typeSpinner = (Spinner) findViewById(R.id.item_type);

		ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter
				.createFromResource(this, R.array.item_types,
						android.R.layout.simple_spinner_item);
		typeAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		typeSpinner.setAdapter(typeAdapter);

		typeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
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

		yearSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
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

		tradeTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						sTradeType = parent.getItemAtPosition(position)
								.toString();
					}

					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		Button confirmButton = (Button) findViewById(R.id.addButton);
		// check that no more than 2 items are in the queue for this entry
		ArrayList<String> contentUris = getIntent().getStringArrayListExtra("tshirtslayer_contentUris");
		
		if(contentUris.size() > 2) {
			showToast("You can add at most 2 images at a time (Front and Back image)");
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		}
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				EditText mTitle = (EditText) findViewById(R.id.item_title);
				EditText mBand = (EditText) findViewById(R.id.item_band);
				
				ArrayList<String> contentUris = getIntent()
						.getStringArrayListExtra("tshirtslayer_contentUris");
				// check all the fields are entered and ready
				if (sType.length() > 0 && sYear.length() > 0
						&& sTradeType.length() > 0
						&& mTitle.getText().length() > 0) {
					// store this in the queue DB
					sTitle = mTitle.getText().toString();
					sBand = mBand.getText().toString();
					long id = dbHelper.createItem(sTitle, sType, sYear, sTradeType, sBand, contentUris);
					Intent intent = new Intent();
					setResult(RESULT_OK, intent);
					finish();

				} else {
					showToast("Error! Please fill in all the fields");
				}
			}
		});

	}

}