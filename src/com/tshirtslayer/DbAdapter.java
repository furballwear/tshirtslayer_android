				
package com.tshirtslayer;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


public class DbAdapter {

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TITLE = "item_title";
	public static final String KEY_TYPE = "item_type";
	public static final String KEY_TRADE_TYPE = "item_trade_type";	
	public static final String KEY_YEAR = "item_year";
	public static final String KEY_BAND = "item_band";
	
	// database for attachments	
	public static final String KEY_ATTACHMENT_ITEM_ID = "item_id";	
	public static final String KEY_ATTACHMENT_NAME =  "attachment";
	
	private static final String DATABASE_TABLE = "items";
	private static final String DATABASE_ATTACHMENT_TABLE = "item_attachment";
	
	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;

	public DbAdapter(Context context) {
		this.context = context;
	}

	public DbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(this.context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Create a new item If the item is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createItem(String title, String type, String year, String trade_type, String band, ArrayList images) {
		ContentValues initialValues = createContentValues(title, type, year, trade_type, band);
		
		long itemId = database.insert(DATABASE_TABLE, null, initialValues);
		
		Object uri[] = images.toArray();
		for(int n=0; n<uri.length; n++) {
			ContentValues initialImageValues = createImageContentValues(itemId,(String)uri[n].toString()); 
			database.insert(DATABASE_ATTACHMENT_TABLE, null, initialImageValues);
		}
		
		return itemId;
	}

	/**
	 * Deletes item
	 */
	public boolean deleteItem(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	/**
	 * Deletes item
	 */
	public boolean deleteImageItem(long rowId) {		
		return database.delete(DATABASE_ATTACHMENT_TABLE, "_id" + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list 
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllItems() {
		return database.query(DATABASE_TABLE, new String[] { KEY_ROWID,
				KEY_TITLE, KEY_TYPE, KEY_YEAR, KEY_BAND  }, null, null, null,
				null, null);
	}

	/**
	 * Return a Cursor over the list to obtain list of images associated 
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllImageItems(String itemID) {
		
		Cursor mCursor =  database.query(DATABASE_ATTACHMENT_TABLE, new String[] { "item_id",
				"attachment", "_id" }, "item_id = "+itemID, null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor positioned at the defined item
	 */
	public Cursor fetchItem(long rowId) throws SQLException {
/*		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_TYPE, KEY_YEAR, KEY_TRADE_TYPE },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
*/
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_TITLE, KEY_TYPE, KEY_YEAR, KEY_TRADE_TYPE, KEY_BAND },
				null, null, null, null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createImageContentValues(long itemId, String attachment) {
		ContentValues values = new ContentValues();
		values.put(KEY_ATTACHMENT_ITEM_ID, itemId);
		values.put(KEY_ATTACHMENT_NAME, attachment);
		return values;
	}
	

	private ContentValues createContentValues(String title, String type,
			String year, String trade_type, String band) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, title);
		values.put(KEY_TYPE, type);
		values.put(KEY_YEAR, year);
		values.put(KEY_TRADE_TYPE, trade_type);	
		values.put(KEY_BAND, band);
		return values;
	}

	
}

	