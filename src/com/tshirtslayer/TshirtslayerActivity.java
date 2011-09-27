package com.tshirtslayer;

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



public class TshirtslayerActivity extends Activity {
	
	private static final String TAG = "TshirtSlayer Application: ";
	private String cUser = new String();
	private String cPass = new String();
	AsyncTask<Context, Integer, Boolean>  _uploadMechanism;
	

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
	protected class uploadMechanism extends AsyncTask<Context, Integer, Boolean> {

	    private volatile boolean keep_running = true;		
		private DbAdapter dbHelper;
		private xmlrpcupload uploadInterface;
		private Context context;
		private String errorString;
		private Integer retryStatus;
		
	    @Override
	    protected void onCancelled() {
	        keep_running = false;
	        Log.i(TAG, "UploadThread: Got cancel!");
	    }
		protected Boolean doInBackground(Context... thisContext) {
			context = thisContext[0];
			dbHelper = new DbAdapter(thisContext[0]);
			Cursor uploadItem;
			Log.d("tshirtslayer","Launched and exec thread for updateMechanism!!");
			while (keep_running == true) {
				try {
					// loop and see if theres anything in the DB to send
					Thread.sleep(000);
					if ( keep_running == true ) {
						dbHelper.open();
						uploadItem = dbHelper.fetchItem(0);
						startManagingCursor(uploadItem);
						if (uploadItem.getCount() > 0) {
							// only do stuff if the setup is complete
							if (setUpIsComplete() == true) {
								// begin upload 
								//triggerNotification(title);
								uploadInterface = new xmlrpcupload(thisContext[0]);
								if( uploadInterface.connectAndLogIn() == false ) {
									errorString = uploadInterface.getErrorString();									
									publishProgress(-1);
							    	keep_running = false;								    	
								} else {
									if ( uploadInterface.uploadItem(uploadItem) == true ) {
										// remove the item from the queue
										// @todo do i need this parseInt/getString? could be a better way?
										dbHelper.deleteItem( Integer.parseInt(uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_ROWID)) ));
										Log.d("Uploader","Removed from queue! "+uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_ROWID)));
									} else {
										errorString = uploadInterface.getErrorString();									
										publishProgress(-1);
								    	keep_running = false;								    											
									}										
								}
							}
						}
						dbHelper.close();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.i(TAG,"UploadMechanism: Waiting was interrupted");
				}
			}
			return false;
	    }

		protected void onPostExecute(Boolean result) {
			Log.i(TAG, "UploadThread mechanism shutting down");
			super.onPostExecute(result);
        }
		
		@Override
		protected void onProgressUpdate(Integer... result) {
			AlertDialog alertDialog;
			retryStatus =0;
			//showToast(errorString);
			
			alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle("Error Connecting");
			alertDialog.setMessage(errorString);
			alertDialog.setButton("Retry", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					_uploadMechanism = new uploadMechanism().execute(context);
				}
			});
			alertDialog.setButton2("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// nothing!
						}
					});

			alertDialog.show();
			super.onProgressUpdate(retryStatus);
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
			_uploadMechanism.cancel(true);
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
    	CharSequence contentTitle = "TShirtSlayer";
    	CharSequence contentText = message;
    	Intent notificationIntent = new Intent(this, TshirtslayerActivity.class);
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

    	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

    	mNotificationManager.notify(HELLO_ID, notification);
    	
    }    

}
