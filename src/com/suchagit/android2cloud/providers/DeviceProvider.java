package com.suchagit.android2cloud.providers;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.suchagit.android2cloud.TwoCloud;

/**
 * Provides access to a database of devices. Each device has a user associated with it,
 * a name, an address (a concatenation of the user and the name, separated by a "/"),
 * and whether the device is selected.
 */
public class DeviceProvider extends ContentProvider {
	
	// Used for debugging and logging
	private static final String TAG = "DeviceProvider";
	
	/**
	 * The database and version the provider uses as its underlying data store.
	 */
	private static final String DATABASE_NAME = "devices.db";
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * A projection map used to select columns from the database
	 */
	private static HashMap<String, String> sDevicesProjectionMap;
	
	/*
	 * Constants used by the Uri matcher to choose an action based on the path of the incoming
	 * URI.
	 */
	// The incoming URI matches the Devices URI pattern
	private static final int DEVICES = 1;
	
	// The incoming URI matches the Device ID URI pattern
	private static final int DEVICE_ID = 2;
	
	/**
	 * A UriMatcher instance
	 */
	private static final UriMatcher sUriMatcher;
	
	// Handle to a new DatabaseHelper
	private DatabaseHelper mOpenHelper;
	
	/**
	 * A block that instantiates and sets static objects
	 */
	static {
		
		/*
		 * Creates and initialises the URI matcher
		 */
		// Create a new instance
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		// Add a pattern that routes URIs terminated with "devices" to a DEVICES operation
		sUriMatcher.addURI(TwoCloud.AUTHORITY, "devices", DEVICES);
		
		// Add a pattern that routes URIs terminated with "devices" plus an integer to a
		// DEVICE_ID operation
		sUriMatcher.addURI(TwoCloud.AUTHORITY, "devices/#", DEVICE_ID);
		
		/*
		 * Creates and initialises a projection map that returns all columns
		 */
		
		// Creates a new projection map instance. The map returns a column name given a
		// string. The two are usually equal.
		sDevicesProjectionMap = new HashMap<String, String>();
		
		// Maps the string "_ID" to the column name "_ID"
		sDevicesProjectionMap.put(TwoCloud.Devices._ID, TwoCloud.Devices._ID);
		
		// Maps "user" to "user"
		sDevicesProjectionMap.put(TwoCloud.Devices.COLUMN_NAME_USER, TwoCloud.Devices.COLUMN_NAME_USER);
		
		// Maps "name" to "name"
		sDevicesProjectionMap.put(TwoCloud.Devices.COLUMN_NAME_NAME, TwoCloud.Devices.COLUMN_NAME_NAME);
		
		// Maps "address" to "address"
		sDevicesProjectionMap.put(TwoCloud.Devices.COLUMN_NAME_ADDRESS, TwoCloud.Devices.COLUMN_NAME_ADDRESS);
		
		// Maps "selected" to "selected"
		sDevicesProjectionMap.put(TwoCloud.Devices.COLUMN_NAME_SELECTED, TwoCloud.Devices.COLUMN_NAME_SELECTED);
	}
	
	static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			// calls the super constructor, requesting the default cursor factory
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		/**
		 * 
		 * Creates the underlying database with table name and column names taken from the
		 * TwoCloud class.
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TwoCloud.Devices.TABLE_NAME + " ("
					+ TwoCloud.Devices._ID + " INTEGER PRIMARY KEY,"
					+ TwoCloud.Devices.COLUMN_NAME_USER + " TEXT,"
					+ TwoCloud.Devices.COLUMN_NAME_NAME + " TEXT,"
					+ TwoCloud.Devices.COLUMN_NAME_ADDRESS + " TEXT,"
					+ TwoCloud.Devices.COLUMN_NAME_SELECTED + " INTEGER"
					+ ");");
		}
		
		/**
		 * 
		 * Handle the database upgrades
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			
			// Kills the table and existing data
			db.execSQL("DROP TABLE IF EXISTS " + TwoCloud.Devices.TABLE_NAME);
			
			// Recreates the database with a new version
			onCreate(db);
		}
	}
	
	/**
	 * 
	 * Initialises the provider by creating a new DatabaseHelper. onCreate() is called
	 * automatically when Android creates the provider in response to a resolver request
	 * from a client.
	 */
	@Override
	public boolean onCreate() {
		
		// Creates a new helper object. Note that the database itself isn't opened until
		// something tries to access it, and it's only created if it doesn't already exist.
		mOpenHelper = new DatabaseHelper(getContext());
		
		// Assumes that any failures will be reported by a thrown exception.
		return true;
	}
	
	/**
	 * This method is called when a client calls
	 * {#link {@link android.content.ContentResolver#query(Uri, String[], String, String[], String)}.
	 * Queries the database and returns a cursor containing the results.
	 * 
	 * @return A cursor containing the results of the query. The cursor exists but is empty if
	 * the query returns no results or an exception occurs.
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// Constructs a new query builder and sets its table name
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TwoCloud.Devices.TABLE_NAME);
		
		/**
		 * Choose the projection and adjust the "where" clause based on URI pattern-matching.
		 */
		switch (sUriMatcher.match(uri)) {
			// If the incoming URI is for devices, chooses the Devices projection
			case DEVICES:
				qb.setProjectionMap(sDevicesProjectionMap);
				break;
			
			/* If the incoming URI is for a single device identified by its ID, chooses the
			 * device ID projection and appends "_ID = <deviceID>" to the where clause, so that
			 * it selects that single device
			 */
			case DEVICE_ID:
				qb.setProjectionMap(sDevicesProjectionMap);
				qb.appendWhere(
						TwoCloud.Devices._ID + // the name of the ID column
						"=" +
						// the position of the device ID itself in the incoming URI
						uri.getPathSegments().get(TwoCloud.Devices.DEVICE_ID_PATH_POSITION));
				break;
			
			default:
				// If the URI doesn't match any of the known patterns, throw an exception.
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		String orderBy;
		// If no sort order is specified, uses the default
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = TwoCloud.Devices.DEFAULT_SORT_ORDER;
		} else {
			// otherwise, uses the incoming sort order
			orderBy = sortOrder;
		}
		
		// Opens the database object in "read" mode, since no writes need to be done
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		
		/*
		 * Performs the query. If no problems occur trying to read the database, then a Cursor
		 * object is returned; otherwise, the cursor variable contains null. If no records were
		 * selected, then the Cursor object is empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = qb.query(
			db,				// The database to query
			projection,		// The columns to return from the query
			selection,		// The columns for the where clause
			selectionArgs,	// The values for the where clause
			null,			// don't group the rows
			null,			// don't filter by row groups
			orderBy			// The sort order
		);
		
		// Tells the Cursor what URI to watch, so it knows when its source data changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	/**
	 * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
	 * Returns the MIME data type of the URI given as a parameter.
	 * 
	 * @param uri the URI whose MIME type is desired.
	 * @return The MIME type of the URI.
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	@Override
	public String getType(Uri uri) {
		
		/**
		 * Chooses the MIME type based on the incoming URI pattern
		 */
		switch (sUriMatcher.match(uri)) {
		
			// If the pattern is for devices, returns the general content type
			case DEVICES:
				return TwoCloud.Devices.CONTENT_TYPE;
			
			// If the pattern is for device IDs, returns the device ID content type
			case DEVICE_ID:
				return TwoCloud.Devices.CONTENT_ITEM_TYPE;
			
			// If the URI pattern doesn't match any permitted patterns, throws an exception
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * This is called when a client calls
	 * {@link android.content.ContentResolver#insert(Uri, ContentValues)}.
	 * Inserts a new row into the database. This method sets up default values for any
	 * columns that are not included in the incoming map, besides the required column ("user").
	 * If rows were inserted, then listeners are notified of the change.
	 * @return The row ID of the inserted row.
	 * @throws SQLException if the insertion fails.
	 * @throws IllegalArgumentException if the URI is not the full provider URI
	 * @throws IllegalArgumentException if initialValues does not include a user value.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		
		// Validates the incoming URI. Only the full provider URI is allowed for inserts.
		if (sUriMatcher.match(uri) != DEVICES) {
			throw new IllegalArgumentException("Unknown URI "+uri);
		}
		
		// A map to hold the new record's values.
		ContentValues values;
		
		// If the incoming values map is not null, uses it for the new values.
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			// Otherwise, create a new value map
			values = new ContentValues();
		}
		
		// If the values map doesn't contain a user, throws an error.
		if (values.containsKey(TwoCloud.Devices.COLUMN_NAME_NAME) == false) {
			throw new IllegalArgumentException("Devices must have a User associated with them.");
		}
		
		// If the values map doesn't contain a name, sets the value to the default name
		Resources r = Resources.getSystem();
		if (values.containsKey(TwoCloud.Devices.COLUMN_NAME_NAME) == false) {
			values.put(TwoCloud.Devices.COLUMN_NAME_NAME, r.getString(com.suchagit.android2cloud.R.string.default_device_name));
		}
		
		// If the values map doesn't contain an address, sets the value to the default address
		if (values.containsKey(TwoCloud.Devices.COLUMN_NAME_NAME) == false) {
			values.put(TwoCloud.Devices.COLUMN_NAME_NAME, values.getAsString(TwoCloud.Devices.COLUMN_NAME_USER) +"/"+values.getAsString(TwoCloud.Devices.COLUMN_NAME_NAME));
		}
		
		// If the values map doesn't contain a value for selected, sets the value to false
		if (values.containsKey(TwoCloud.Devices.COLUMN_NAME_NAME) == false) {
			values.put(TwoCloud.Devices.COLUMN_NAME_NAME, false);
		}
		
		//Opens the database object in "write" mode
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		
		// Performs the insert and returns the ID of the new device
		long rowId = db.insert(
			TwoCloud.Devices.TABLE_NAME,			// The table to insert into
			TwoCloud.Devices.COLUMN_NAME_SELECTED,	// A hack, SQLite sets this column value to
													// null if values is empty
			values									// A map of column names and the values to
													// insert into the columns.
		);
		
		// If the insert succeeded, the row ID exists
		if (rowId > 0) {
			// Creates a URI with the device ID pattern and the new row ID appended to it.
			Uri deviceUri = ContentUris.withAppendedId(TwoCloud.Devices.CONTENT_ID_URI_BASE, rowId);
			
			// Notifies observers registered against this provider that the data has changed.
			getContext().getContentResolver().notifyChange(deviceUri, null);
			return deviceUri;
		}
		
		throw new SQLException("Failed to insert row into " + uri);
	}

	/**
	 * This is called when a client calls
	 * {@link android.content.ContentResolver#delete(Uri, String, String[])}.
	 * Deletes records from the database. If the incoming URI matches the device ID URI pattern,
	 * this method deletes the one record specified by the ID in the URI. Otherwise, it deletes a
	 * set of records. The record or records must also match the input selection criteria
	 * specified by where and whereArgs.
	 * 
	 * If rows were deleted, then listeners are notified of the change.
	 * @return If a "where" clause is used, the number of rows affected is returned, otherwise
	 * 0 is returned. To delete all rows and get a row count, use "1" as the where clause.
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		
		// Opens the database object in "write" mode.
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String finalWhere;
		
		int count;
		
		// Does the delete based on the incoming URI pattern.
		switch (sUriMatcher.match(uri)) {
		
			// If the incoming pattern matches the general pattern for devices, does a delete
			// based on the incoming "where" columns and arguments.
			case DEVICES:
				count = db.delete(
						TwoCloud.Devices.TABLE_NAME,	// The database table name
						where,							// The incoming where clause column names
						whereArgs						// The incoming where clause values
				);
				break;
				
				// If the incoming URI matches a single device ID, does the delete based on the
				// incoming data, but modifies the where clause to restrict it to the
				// particular device ID.
			case DEVICE_ID:
				/*
				 * Starts a final WHERE clause by restricting it to the desired device ID.
				 */
				finalWhere = 
					TwoCloud.Devices._ID +								// The ID column name
					" = " +												// test for equality
					uri.getPathSegments().								// the incoming device ID
						get(TwoCloud.Devices.DEVICE_ID_PATH_POSITION)
				;
				
				// If there were additional selection criteria, append them to the final
				// WHERE clause
				if (where != null) {
					finalWhere = finalWhere + " AND " + where;
				}
				
				// Performs the delete
				count = db.delete(
						TwoCloud.Devices.TABLE_NAME,	// The database table name
						finalWhere,						// The final WHERE clause
						whereArgs						// The incoming where clause values
				);
				break;
			
			// If the incoming pattern is invalid, throws an exception.
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		/*Gets a handle to the content resolver object for the current context, and notifies it
		 * that the incoming URI changed. The object passes this along to the resolver framework,
		 * and observers that have registered themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);
		
		// Returns the number of rows deleted
		return count;
	}

	/**
	 * This is called when a client calls
	 * {@link android.content.ContentResolver#update(Uri, ContentValues, String, String[])}
	 * Updates records in the database. The column names specified by the keys in the values map
	 * are updated with new data specified by the values in the map. If the incoming URI matches the
	 * device ID URI pattern, then the method updates the one record specified by the ID in the URI;
	 * otherwise, it updates a set of records. The record or records must match the input
	 * selection criteria specified by where and whereArgs.
	 * If rows were updated, then listeners are notified of the change.
	 * 
	 * @param uri the URI pattern to match and update
	 * @param values A map of column names (keys) and new values (values).
	 * @param where An SQL WHERE clause that selects records based on their column values. If this
	 * is null, then all records that match the URI pattern are selected.
	 * @param whereArgs An array of selection criteria. If the "where" param contains value placeholders ("?"),
	 * then each placeholder is replaced by the corresponding element in the array.
	 * @return the number of rows updated
	 * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		
		//Opens the database object in "write" mode
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		String finalWhere;
		
		// Does the update based on the incoming URI pattern
		switch (sUriMatcher.match(uri)) {
		
			// If the incoming URI matches the general devices pattern, does the update based on
			// the incoming data
			case DEVICES:
				
				// Does the update and returns the number of rows updated.
				count = db.update(
					TwoCloud.Devices.TABLE_NAME,	//The database table name
					values,							// A map of column names and new values to use
					where,							// The where clause column names
					whereArgs						// The where clause column values to select on
				);
				break;
				
			// If the incoming URI matches a single device ID, does the update based on the incoming
			// data, but modifies the where clause to restrict it to the particular device ID
			case DEVICE_ID:
				// From the incoming URI, get the device ID
				String deviceId = uri.getPathSegments().get(TwoCloud.Devices.DEVICE_ID_PATH_POSITION);
				
				/*
				 * Starts creating the final WHERE clause by restricting it to the incoming device ID
				 */
				finalWhere =
					TwoCloud.Devices._ID +									// The ID column name
					" = " +													// test for equality
					deviceId									// The incoming device ID
				;
				
				// If there were additional selection criteria, append them to the final WHERE clause
				if (where != null) {
					finalWhere = finalWhere + " AND " + where;
				}
				
				// Does the update and returns the number of rows updated.
				count = db.update(
					TwoCloud.Devices.TABLE_NAME,	// The database table name
					values,							// A map of column names and new values to use.
					finalWhere,						// The final WHERE clause to use
					whereArgs						// The where clause column values to select on, or
													// null if the values are in the where argument
				);
				break;
			// If the incoming pattern is invalid, throws an exception
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		/*Gets a handle to the content resolver object for the current context and notifies it that the
		 * incoming URI changed. The object passes this along to the resolver framework,
		 * and observers that have registered themselves for the provider are notified.
		 */
		getContext().getContentResolver().notifyChange(uri, null);
		
		// Returns the number of rows updated.
		return count;
	}

	/**
	 * A test package can call this to get a handle to the database underlying DeviceProvider,
	 * so it can insert test data into the database. The test case class is responsible for
	 * instantiating the provider in a test context; {@link android.test.ProviderTestCase2} does
	 * this during the call to setUp()
	 * 
	 * @return a handle to the database helper object for the provider's data.
	*/
	DatabaseHelper getOpenHelperForTest() {
		return mOpenHelper;
	}
}