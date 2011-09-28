package com.tshirtslayer;
import android.app.Service;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.database.Cursor;
import com.tshirtslayer.DbAdapter;
import android.view.View;
import android.app.Service;


public class TshirtslayerActivity extends Activity {
	
	private static final String TAG = "TshirtSlayer Application: ";
	private String cUser = new String();
	private String cPass = new String();
	private boolean uploaderLaunched = false;
	AsyncTask<Context, Integer, Boolean>  _uploadMechanism;	

	void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void onStart() {
		super.onStart();

	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("tshirtslayer","tshirtslayer application now running");
		setContentView(R.layout.main);
		checkSetup(this);
		Context context = this.getApplicationContext();
        context.startService(new Intent(context, deliveryService.class));
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
			} 
		} else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			checkSetup(this);
			Log.i(TAG, "we have action send!");
			// Bundle extras = getIntent().getExtras();
			ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			upload(l, this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.app_menu, menu);
		return true;
	}

	// Called when menu item is selected //
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.app_quit:
			Context context = this.getApplicationContext();
	        context.stopService(new Intent(context, deliveryService.class));			
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.app_settings:
			// Launch Prefs activity
			Intent i_settings = new Intent(TshirtslayerActivity.this,
					settings.class);
			startActivity(i_settings);
			break;

		}
		return true;
	}
	/**
	 * Check setup requirements before being able to send
	 * @return boolean
	 */
	private boolean setUpIsComplete() {
		SharedPreferences app_preferences = PreferenceManager
		.getDefaultSharedPreferences(this);
		cUser = app_preferences.getString("user", "");
		cPass = app_preferences.getString("pass", "");
		
		if (cUser.length() == 0 || cPass.length() == 0) {
		  return false;
		}
		
		return true;
	}
	
	// check settings are complete, if not, show message and launch
	private void checkSetup(Context context) {
		if ( setUpIsComplete() == false ) {
			CharSequence text = "Please configure your username and password (In the application settings)";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			Intent i_settings = new Intent(context, settings.class);
			startActivity(i_settings);
			toast.show();
		}

	}

	private void upload(ArrayList contentUris, Context context) {
		Intent i_item = new Intent(getApplicationContext(), item.class);
		i_item.putStringArrayListExtra("tshirtslayer_contentUris", contentUris);
		startActivityForResult(i_item,0);
	}
	


}
