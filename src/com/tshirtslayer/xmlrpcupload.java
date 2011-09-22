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

import android.util.Log;

public class xmlrpcupload {

	private XMLRPCClient client;
	private URI uri;
	private String errorString;
	private String sessionKey;
	
	public xmlrpcupload()  {
		uri = URI.create("http://tshirtslayer.com:8888");
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
	
	public String getErrorString() {
		return errorString;
	}
}
