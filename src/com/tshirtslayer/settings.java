package com.tshirtslayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class settings extends Activity {

	private EditText mUser;
	private EditText mPass;
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mUser = (EditText) findViewById(R.id.user);
		mPass = (EditText) findViewById(R.id.pass);
		mContext = this;

		Button confirmButton = (Button) findViewById(R.id.confirm);

		SharedPreferences app_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		mUser.setText(app_preferences.getString("user", ""));
		mPass.setText(app_preferences.getString("pass", ""));

		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				mUser = (EditText) findViewById(R.id.user);
				mPass = (EditText) findViewById(R.id.pass);

				// Get the app's shared preferences
				SharedPreferences app_preferences = PreferenceManager
						.getDefaultSharedPreferences(mContext);
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putString("user", mUser.getText().toString());
				editor.putString("pass", mPass.getText().toString());
				editor.commit(); // Very important

				setResult(RESULT_OK);
				finish();
			}
		});

	}

}