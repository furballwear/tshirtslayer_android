package com.tshirtslayer;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.conn.HttpHostConnectException;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;
import org.xmlrpc.android.XMLRPCSerializable;
import org.xmlrpc.android.XMLRPCFault;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

public class xmlrpcupload {

	private XMLRPCClient client;
	private URI uri;
	private String errorString;
	private String sessionKey;
	private Context context;
	private String logInSessID;
	
	public xmlrpcupload(Context thisContext)  {	
		context = thisContext;
		uri = URI.create("http://tshirtslayer.com/services/xmlrpc");
		try {
			client = new XMLRPCClient(uri);
		} catch (Exception e) {
			errorString = e.getMessage();
		}
		
	}
	
	public boolean ping() {
		Integer i;
		i=0;
		try {
			i = (Integer) client.call("ping");
		} catch (XMLRPCException e) {
			errorString = e.getMessage();
			return false;
		}
		if ( i > 0 ) {
			Log.d("Ping response: ", "seconds " + i);
			return true;
		} 
			
		return false;				
	}

	public boolean logOut() {
		Log.d("xmlrpcupload", "Logging out");

		try {
			client.call("user.logout");
		} catch (XMLRPCException e) {
			errorString = e.getMessage();
			return false;			
		}
		return true;
	}
	/**
	 * hit up the queue and grab the first one
	 * @return
	 */
	public boolean uploadItem(Cursor uploadItem) {
		Log.d("xmlrpcupload", "Uploading item" );
		
		try {
			client
					.call("tshirtslayer.addItem", logInSessID, Integer
							.toString(uploadItem
									.getColumnIndex(DbAdapter.KEY_TYPE)),
							Integer.toString(uploadItem
									.getColumnIndex(DbAdapter.KEY_TRADE_TYPE)),
							Integer.toString(uploadItem
									.getColumnIndex(DbAdapter.KEY_YEAR)),
							Integer.toString(uploadItem
									.getColumnIndex(DbAdapter.KEY_TITLE)),
							"someimages");
		} catch (XMLRPCException e) {
			errorString = e.getMessage();
			return false;
		}		
		return true;
	}
	
	public boolean connectAndLogIn() {
		Log.d("xmlrpcupload","TRYING TO FUCKING LOGIN!");
		try {

		    HashMap<?, ?> siteConn =(HashMap<?, ?>)  client.call("system.connect");
		    // Getting initial session id
		    String initSessID=(String)siteConn.get("sessid");
		    Log.d("Base SessionID", initSessID);

			SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
			
		    //Login to the site using session id        
		    HashMap<?, ?> logInConn =(HashMap<?, ?>)  client.call("tshirtslayer.login",
		    														initSessID,
		    														app_preferences.getString("user", ""),
		    														app_preferences.getString("pass", "")
		    														);

		    //Getting Login sessid
		    logInSessID=(String)logInConn.get("sessid");
		    Log.d("User SessionID", logInSessID);
		    
		} catch (XMLRPCException e) {
			errorString = e.getMessage();
			return false;
		}			
		return true;				
	}
	
	public String getErrorString() {
		return errorString;
	}
}
