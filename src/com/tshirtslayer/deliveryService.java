package com.tshirtslayer;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.xmlrpc.android.XMLRPCException;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class deliveryService extends Service {
	/** For showing and hiding our notification. */
	NotificationManager mNM;
	/** Keeps track of all current registered clients. */
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	/** Holds last value set by a client. */
	int mValue = 0;
	AsyncTask<Context, Integer, Boolean>  _uploadMechanism;
	static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;
    static final int MSG_UPLOAD_STATUS_BUMP = 5;
    static final int MSG_RESTART_UPLOAD = 6;
    
	/**
	 * Command to the service to register a client, receiving callbacks from the
	 * service. The Message's replyTo field must be a Messenger of the client
	 * where callbacks should be sent.
	 */
	static final int MSG_REGISTER_CLIENT = 1;

	/**
	 * Command to the service to unregister a client, ot stop receiving
	 * callbacks from the service. The Message's replyTo field must be a
	 * Messenger of the client as previously given with MSG_REGISTER_CLIENT.
	 */
	static final int MSG_UNREGISTER_CLIENT = 2;

	/**
	 * Command to service to set a new value. This can be sent to the service to
	 * supply a new value, and will be sent by the service to any registered
	 * clients with the new value.
	 */
	static final int MSG_SET_VALUE = 3;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		public void handleMessage(Message msg) {
			Log.d("tshirtslayer","Got message from UI "+Integer.toString(msg.what));
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_VALUE:
				mValue = msg.arg1;
				if (mValue == MSG_RESTART_UPLOAD) {
					Log.d("tshirtslayer","Got notice from UI to restart sending again");
					mNM.cancel(R.string.local_service_started);
					_uploadMechanism.cancel(true);
					_uploadMechanism = new uploadMechanism().execute(getApplicationContext());

				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	private void sendTriggerToUI(Integer triggerID) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {

                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_INT_VALUE, triggerID, 0));


            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
		
	}
    private void sendMessageToUI(String msgText) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {

            	//Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", msgText);
                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }
	

    @Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Context context = this.getApplicationContext();
		Log.d("deliveryService", "We are running!");
		// Display a notification about us starting.
		_uploadMechanism = new uploadMechanism().execute(context);
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(R.string.local_service_started);
	}

	private class uploadMechanism extends AsyncTask<Context, Integer, Boolean> {
		private String errorString;
		private Context context;
		public Boolean keep_running;
		
		public boolean isOnline() {
		    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		        return true;
		    }
		    return false;
		}		
		protected Boolean doInBackground(Context... appContext) {
			
			context = appContext[0];
			keep_running = true;
			sendTriggerToUI(MSG_UPLOAD_STATUS_BUMP);

			//while(keep_running) {
			while ( !this.isCancelled()) {
				if(isOnline()) {
					// see if theres something to deliver and deliver it
					deliverItem();					
				}
				try {					
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			Log.d("tshirtslayer","deliveryService XMLRPC thread hitting the end of usage");
			this.cancel(false);
			return true;
		}

		@Override
		protected void onProgressUpdate(Integer... result) {			
			super.onProgressUpdate(result[0]);
			Log.d("tshirtslayer delivery thread", errorString);
			sendMessageToUI(errorString);
			
			// critical problem here
			if (result[0] == -1) {
				keep_running = false;
				this.cancel(true);
				showNotification(errorString);
			}
			
			if (result[0] == 0) {
				// not so critical, probably a timeout, alert user and try again
				showNotification("Retrying.. "+errorString);
			}
			
		}
		
		protected void onPostExecute(Long result) {
			Log.d("tshirtslayer deliveryService","Shutting down XMLRPC asyncthread");
		}
		
		private void deliverItem() {
			xmlrpcupload uploadInterface;
			Cursor uploadItem;
			DbAdapter dbHelper;			
			dbHelper = new DbAdapter(context);
			dbHelper.open();
			
			Log.d("tshirtslayer","Looking for items to deliver");
			uploadItem = dbHelper.fetchItem(0);
			if (uploadItem.getCount() > 0) {
				Log.d("tshirtslayer","items found, devliering");
				uploadInterface = new xmlrpcupload(context);
				if (uploadInterface.connectAndLogIn() == false) {
					Log.d("tshirtslayer","problem logging in");
					errorString = uploadInterface.getErrorString();
					publishProgress(-1);
					showNotification(errorString);
					
				} else {
					showNotification("Sending...");
					if (uploadInterface.uploadItemToTshirtSlayer(uploadItem) == true) {
						// remove the item from the queue 
						Log.d("Uploader", "Removed from queue! ");
						showNotification("Sending complete");
					} else {
						errorString = uploadInterface.getErrorString();
						// see if this was something todo with username/password
						publishProgress(0);
					}
					sendTriggerToUI(MSG_UPLOAD_STATUS_BUMP);
				}
					
			} else {
				Log.d("tshirtslayer","No items found to deliver");
			}
			dbHelper.close();
			uploadItem.close();
		}
		
		@Override
	    protected void onCancelled(){
	      Log.d("tshirtslayer", "onCancelled");	      
	      super.onCancelled();
		}
    }

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification(String message) {
		// In this sample, we'll use the same text for the ticker and the
		// expanded notification
		CharSequence text = message;

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, TshirtslayerActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this,
				getText(R.string.local_service_label), text, contentIntent);
		// should we vibrate?

		SharedPreferences app_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Boolean vibrate = app_preferences.getBoolean("vibrate", false);
		
		if(vibrate == true) {
			notification.vibrate = new long[] { 0, 250, 100, 500, 100, 100, 100,
					100 };
		}

		// Send the notification.
		// We use a string id because it is a unique number. We use it later to
		// cancel.
		mNM.notify(R.string.local_service_started, notification);
	}
}
