package com.tshirtslayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TshirtslayerActivity extends Activity {

	private static final String TAG = "TshirtSlayer Application: ";
	private String cUser = new String();
	private String cPass = new String();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		checkSetup(this);
		
		
		Intent i = getIntent();
		String action = i.getAction();

		if (Intent.ACTION_SEND.equals(action)) {
			String type = i.getType();
			checkSetup(this);
			Log.i(TAG, "we have action send!");
			Uri stream = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
			if (stream != null && type != null) {
				ArrayList l = new ArrayList();
				l.add(stream);
				upload(l, this);
			} else {
				Log.i(TAG, "null URI");
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			checkSetup(this);
			String type = i.getType();
			Log.i(TAG, "we have action send!");
			// Bundle extras = getIntent().getExtras();
			ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			upload(l, this);
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.app_menu, menu);
      return true;
    }
    
    // Called when menu item is selected //
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
      
      switch(item.getItemId()){
      case R.id.app_quit:
	      setResult(RESULT_OK);
	      finish();    	  
	  break;
      case R.id.app_settings:
        // Launch Prefs activity
		Intent i_settings = new Intent(TshirtslayerActivity.this, settings.class);
        startActivity(i_settings);
        break;
        
      }    
      return true;
    }
	
	// check settings are complete, if not, show message and launch
	private void checkSetup(Context context) {

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cUser = app_preferences.getString("user", "");
        cPass = app_preferences.getString("pass", "");
        
        if(cUser.length() ==0 || cPass.length() == 0 ) {
			CharSequence text = "Please enter your username and password";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);  
			Intent i_settings = new Intent(context, settings.class);
	        startActivity(i_settings);
			toast.show();		
        }
		
	}
	
	// @todo: this needs to go into a queue
	private void upload(ArrayList contentUris, Context context) {
		
		Intent i_item = new Intent(TshirtslayerActivity.this, item.class);
        startActivity(i_item);
		
		for (int i = 0; i < contentUris.size(); i++) {
			Uri stream = (Uri) contentUris.get(i);
			Log.i(TAG, stream.getPath());			
		}
	}
}