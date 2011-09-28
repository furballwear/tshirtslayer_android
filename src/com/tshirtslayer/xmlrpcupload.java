package com.tshirtslayer;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;
import org.xmlrpc.android.XMLRPCSerializable;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import com.tshirtslayer.DbAdapter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec_1_4.binary.Base64;

public class xmlrpcupload {

	private XMLRPCClient client;
	private URI uri;
	private String errorString;
	private Context context;
	private String logInSessID;

	public xmlrpcupload(Context thisContext) {
		context = thisContext;
		uri = URI.create("http://tshirtslayer.com/services/xmlrpc");
		try {
			client = new XMLRPCClient(uri);
		} catch (Exception e) {
			errorString = e.getMessage();
		}

	}

	public static byte[] getBytesFromFile(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return buffer.toByteArray();
		} catch (IOException e) {
			Log.e("xmlrpcupload", e.toString());
			return null;
		}
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
	 * 
	 * @return
	 */
	public boolean uploadItemToTshirtSlayer(Cursor uploadItem) {
		Log.d("xmlrpcupload", "Uploading item");
		DbAdapter dbHelper = new DbAdapter(context);
		Cursor uploadImageCursor;
		ContentResolver cr;
		InputStream is;
		Uri uri;
		Integer uploadCount = 0;
		String item_id;
		byte[] data;
		String encodedData;
		data = null;

		// find out the image relating to this item
		dbHelper.open();
		item_id = uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_ROWID));
		Log.d("xmlrpcupload tshirtslayer", "Looking for attachments relating to item " + item_id);
		uploadImageCursor = dbHelper.fetchAllImageItems(item_id);
		if (uploadImageCursor.moveToFirst()) {
			Log.d("xmlrpcupload", "Found images, iterating");
			do {
				try {
					// @todo: this API is not as efficient as it can be, first
					// we add images
					// and then add the tshirt, this is to try and keep memory
					// usage
					// as low as possible, storing multiple images in memory
					// hurts.
					// and i get tonnes of exceptions for storing multiple
					// images in ArrayList/lit
					//
					// @todo: Move away from xmlrpclib and use raw sockets and
					// just pipe the data in
					// @todo: report how much has been uploaded % complete

					Log.d("xmlrpcupload tshirtslayer", uploadImageCursor.getString(1));

					uri = Uri.parse(uploadImageCursor.getString(1));
					cr = context.getContentResolver();
					is = cr.openInputStream(uri);
					Log.d("Found item", uploadImageCursor.getString(1));
					// Get binary bytes for encode
					data = getBytesFromFile(is);
					Log.d("xmlrpcupload tshirtslayer", "Read "
							+ Integer.toString(data.length) + " bytes");
					// filename is generated automatically on receiving end
					Log.d("xmlrpcupload tshirtslayer",
							"Making the tshirtslayer.addImage call for "
									+ uploadImageCursor.getString(1));
					encodedData = new String(Base64.encodeBase64(data));
					Log.d("xmlrpcupload tshirtslauyer", "Encoded data length is "
							+ Integer.toString(encodedData.length()));
					try {
						client.call("tshirtslayer.addImage", logInSessID,
								encodedData);

					} catch (XMLRPCException e) {
						errorString = "Problem adding image " + e.getMessage();
						Log.d("xmlrpcupload tshirtslayer", errorString);
						dbHelper.close();
						if (uploadImageCursor != null
								&& !uploadImageCursor.isClosed()) {
							uploadImageCursor.close();
						}
						return false;
					}
					// assume the image was added
					// @todo assuming makes an ass out of me and you
					Log.d("xmlrpcupload tshirtslayer","Removing image from list of images to send");
					
					dbHelper.deleteImageItem(Integer.parseInt(uploadImageCursor.getString(0)));

				} catch (FileNotFoundException e) {
					errorString = "An image you tried to send no longer exists, skipping";
				}
				// safety catch, shouldnt happen but we should have a hardlimit
				// anyway
				uploadCount++;

			} while (uploadImageCursor.moveToNext() && uploadCount < 5);
		} else {
			Log.d("xmlrpcupload", "ERROR! No attachments found");
		}

		try {
			// filename is generated automatically on receiving end
			Log.d("xmlrpcupload tshirtslayer", "Making the tshirtslayer.additem call");
			client.call("tshirtslayer.addItem", logInSessID, 
					uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_TYPE)),
					uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_TRADE_TYPE)),
					uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_YEAR)), 
					uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_TITLE)),
					uploadItem.getString(uploadItem.getColumnIndex(DbAdapter.KEY_BAND))
							);
		} catch (Exception e) {
			errorString = e.getMessage();
			//return false;
		}
		Log.d("xmlrpc tshirtslayer","Closing dbhandlers and returning");
		
		uploadImageCursor.close();
		dbHelper.close();
		
		return true;
	}

	public boolean connectAndLogIn() {
		Log.d("xmlrpcupload", "Logging into tshirtslayer.com");
		try {

			HashMap<?, ?> siteConn = (HashMap<?, ?>) client
					.call("system.connect");
			// Getting initial session id
			String initSessID = (String) siteConn.get("sessid");
			Log.d("Base SessionID", initSessID);

			SharedPreferences app_preferences = PreferenceManager
					.getDefaultSharedPreferences(context);

			// Login to the site using session id
			HashMap<?, ?> logInConn = (HashMap<?, ?>) client.call(
					"tshirtslayer.login", initSessID, app_preferences
							.getString("user", ""), app_preferences.getString(
							"pass", ""));

			// Getting Login sessid
			logInSessID = (String) logInConn.get("sessid");
			Log.d("User SessionID", logInSessID);

		} catch (Exception e) {
			errorString = e.getMessage();
			return false;
		}
		return true;
	}

	public String getErrorString() {
		return errorString;
	}
}
