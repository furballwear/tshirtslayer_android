package com.tshirtslayer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.database.Cursor;
import com.tshirtslayer.DbAdapter;



public class TshirtslayerActivity extends Activity {


	private static final String TAG = "TshirtSlayer Application: ";
	private String cUser = new String();
	private String cPass = new String();
	AsyncTask<Context, Integer, Integer>  _uploadMechanism;

	void showToast(CharSequence msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		checkSetup(this);

		Intent i = getIntent();
		String action = i.getAction();
		_uploadMechanism = new uploadMechanism().execute(this);
		
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
			String type = i.getType();
			Log.i(TAG, "we have action send!");
			// Bundle extras = getIntent().getExtras();
			ArrayList l = i.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			upload(l, this);
		}

	}

	/**
	 * sub-class of AsyncTask
	 * keep an eye on the database and see if theres something in the queue to send
	 * if there is, then send it
	 */
	protected class uploadMechanism extends AsyncTask<Context, Integer, Integer> {

	    private volatile boolean keep_running = true;		
		private DbAdapter dbHelper;

	    @Override
	    protected void onCancelled() {
	        keep_running = false;
	        Log.i(TAG, "UploadThread: Got cancel!");
	    }
		protected Integer doInBackground(Context... context) {
			dbHelper = new DbAdapter(context[0]);
			Cursor uploadItem;
			
			while (keep_running == true) {
				try {
					// loop and see if theres anything in the DB to send
					// @todo: move open/close out of the way until app closes
					Thread.sleep(500);
					if ( keep_running == true ) {
						dbHelper.open();
						uploadItem = dbHelper.fetchItem(0);
						startManagingCursor(uploadItem);
						if (uploadItem.getCount() > 0) {
							// only do stuff if the setup is complete
							if (setUpIsComplete() == true) {
								// begin upload
								String title = uploadItem.getString(uploadItem.getColumnIndex("item_title"));
								triggerNotification(title);
							}
						}
						dbHelper.close();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.i(TAG,"UploadMechanism: Waiting was interrupted");
				}
			}
			return 0;
	    }

		protected void onPostExecute(Integer result) {
        	super.onPostExecute(result);
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
			// @todo: send correct message to thread to shut down
			_uploadMechanism.cancel(true);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.i(TAG, "OUCH!!!! Thread shutdown in appquit");
			}
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
			CharSequence text = "Please enter your username and password";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			Intent i_settings = new Intent(context, settings.class);
			startActivity(i_settings);
			toast.show();
		}

	}

	private void upload(ArrayList contentUris, Context context) {
		Intent i_item = new Intent(TshirtslayerActivity.this, item.class);
		i_item.putStringArrayListExtra("tshirtslayer_contentUris", contentUris);
		startActivity(i_item);		
	}
	


    private void triggerNotification(String message)
    {
    	int HELLO_ID = 1;

    	String ns = Context.NOTIFICATION_SERVICE;
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
    	int icon = R.drawable.icon;
    	CharSequence tickerText = message;
    	long when = System.currentTimeMillis();

    	Notification notification = new Notification(icon, tickerText, when);
    	Context context = getApplicationContext();
    	CharSequence contentTitle = "TShirtSlayer Uploading";
    	CharSequence contentText = message;
    	Intent notificationIntent = new Intent(this, TshirtslayerActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

    	mNotificationManager.notify(HELLO_ID, notification);
    	
    }    

}
