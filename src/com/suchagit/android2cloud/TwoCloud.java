package com.suchagit.android2cloud;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines a contract between the Devices content provider and its clients. A contract defines the
 * information that a client needs to access the provider as one or more data tables. A contract
 * is a public, non-extendable (final) class that contains constants defining column names and
 * URIs. A well-written client depends only on the constants in the contract.
 */
public class TwoCloud {
	public static final String AUTHORITY = "com.suchagit.android2cloud.providers.TwoCloud";
	
	// This class cannot be instantiated
	private TwoCloud() {
	}
	
	/**
	 * Devices table contract
	 */
	public static final class Devices implements BaseColumns {
		// This class cannot be instantiated
		private Devices() {}
		
		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "devices";
		
		/*
		 * URI definitions
		 */
		
		/**
		 * The scheme part for this provider's URI
		 */
		private static final String SCHEME = "content://";
		
		/**
		 * Path parts for the URIs
		 */
		
		/**
		 * Path part for the Devices URI
		 */
		private static final String PATH_DEVICES = "/devices";
		
		/**
		 * Path part for the Device ID URI
		 */
		private static final String PATH_DEVICE_ID = "/devices/";
		
		/**
		 * 0-relative position of a device ID segment in the path part of a device ID URI
		 */
		public static final int DEVICE_ID_PATH_POSITION = 1;
		
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_DEVICES);
		
		/**
		 * The content URI base for a single device. Callers must
		 * append a numeric device ID to this Uri to retrieve a device
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_DEVICE_ID);
		
		/**
		 * The content URI match pattern for a single device, specified by its ID. Use this to match
		 * incoming URIs or to construct an Intent.
		 */
		public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_DEVICE_ID + "/#");
		
		/**
		 * The MIME type of a {@link #CONTENT_URI} providing a directory of devices
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.suchagit.android2cloud.device";
		
		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single device
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.suchagit.android2cloud.device";
		
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = "address ASC";
		
		/*
		 * Column definitions
		 */
		
		/**
		 * Column name for the name of the device
		 * <P>Type: TEXT</p>
		 */
		public static final String COLUMN_NAME_NAME = "name";
		
		/**
		 * Column name for the user who owns the device
		 * <P>Type: TEXT</p>
		 */
		public static final String COLUMN_NAME_USER = "user";
		
		/**
		 * Column name for the address of the device
		 * <P>Type: TEXT</p>
		 */
		public static final String COLUMN_NAME_ADDRESS = "address";
		
		/**
		 * Column name for whether a device is selected or not
		 * <P>Type: INTEGER</p>
		 */
		public static final String COLUMN_NAME_SELECTED = "selected";
	}
}
