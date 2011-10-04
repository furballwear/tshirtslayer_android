package com.tshirtslayer;
import android.app.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.database.Cursor;

import com.tshirtslayer.DbAdapter;
import com.tshirtslayer.deliveryService;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Service;


public class TshirtslayerActivity extends Activity {
	
	private static final String TAG = "TshirtSlayer Application: ";
	private static final int INTENT_PICTURE_SHARE = 1;
	private static final int INTENT_PICTURE_GALLERY  = 2;
	private static final int INTENT_PICTURE_CAMERA = 3;
	private static final int INTENT_PICTURE_DESCRIBE = 4;
	
	ArrayList l = new ArrayList();
	
	private String cUser = new String();
	private String cPass = new String();
	private Button cameraButton;
	private Button galleryButton;	
	private Intent addImageIntent;
	private Integer addImageIntent_ID;
	private File photoFile;
	
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
		
	
		  cameraButton = (Button)this.findViewById(R.id.buttonFromCamera);
		  cameraButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
		    	launchCamera();	
		    }
		  });
		  
		  galleryButton = (Button)this.findViewById(R.id.buttonFromGallery);
		  galleryButton.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
	            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	            intent.setType("image/*");
	            startActivityForResult(intent, INTENT_PICTURE_GALLERY);
		    }
		  });
		  
		if (Intent.ACTION_SEND.equals(action)) {
			String type = i.getType();
			checkSetup(this);
			Log.i(TAG, "we have action send!");
			Uri stream = (Uri) i.getParcelableExtra(Intent.EXTRA_STREAM);
			if (stream != null && type != null) {
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

	private boolean launchCamera() {
		addImageIntent = new Intent("android.media.action.IMAGE_CAPTURE");
		Time startTime = new Time(); 
		startTime.setToNow();
		
		photoFile = new File(Environment.getExternalStorageDirectory()+"/tshirtslayer");		
		photoFile.mkdirs(); // @todo: I dont understand why they have a function that constructed with a filename and also creates that as a directory
		photoFile = new File(Environment.getExternalStorageDirectory()+"/tshirtslayer", "image"+String.valueOf(startTime.toMillis(true)) +".jpeg");
		addImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		Log.d("tshirtslayer image saving to",Uri.fromFile(photoFile).toString());
		startActivityForResult(addImageIntent, INTENT_PICTURE_CAMERA);

		return true;
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
		startActivityForResult(i_item,INTENT_PICTURE_DESCRIBE);
	}
	
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case INTENT_PICTURE_DESCRIBE:
			if (resultCode == RESULT_OK) {
				showToast("Your item is queued and will be sent.");
			}
		break;
		case INTENT_PICTURE_GALLERY:
			if (resultCode == RESULT_OK) {
				Uri stream = (Uri) imageReturnedIntent.getData();
				l.add(stream.toString());
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            addImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
	            addImageIntent.setType("image/*");
				addImageIntent_ID = INTENT_PICTURE_GALLERY;
				
				builder.setMessage("Add another image to this entry?")
						.setPositiveButton("Yes, add another image", dialogClickListener)
						.setNegativeButton("No - Create my entry",
								dialogClickListener).show();
			}
			break;

		case INTENT_PICTURE_CAMERA:
			if (resultCode == RESULT_OK) {
				Time startTime = new Time(); 
				startTime.setToNow();				
				addImageIntent = new Intent("android.media.action.IMAGE_CAPTURE");
				l.add("file://"+photoFile.toString());
				Log.d("tshirtslayer main camera activity", "Adding "+photoFile.toString());
				photoFile = new File(Environment.getExternalStorageDirectory()+"/tshirtslayer");		
				photoFile.mkdirs(); // @todo: I dont understand why they have a function that constructed with a filename and also creates that as a directory
				photoFile = new File(Environment.getExternalStorageDirectory()+"/tshirtslayer", "image"+String.valueOf(startTime.toMillis(true)) +".jpeg");
				addImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				
				addImageIntent_ID = INTENT_PICTURE_CAMERA;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Add another photo to this entry?")
						.setPositiveButton("Yes - Add another photo", dialogClickListener)
						.setNegativeButton("No - Create my entry",
								dialogClickListener).show();
			}
			break;
		}			

	}
	
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	        switch (which){
	        case DialogInterface.BUTTON_NEGATIVE:
				upload(l, getApplicationContext());
	            break;

	        case DialogInterface.BUTTON_POSITIVE:
	            startActivityForResult(addImageIntent, addImageIntent_ID);	        	
	            break;
	        }
	    }
	};
}
