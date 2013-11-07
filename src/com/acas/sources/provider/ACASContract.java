package com.acas.sources.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ACASContract {
	
	public static final String CONTENT_AUTHORITY = "com.acas.sources.provider";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	// Path URI
	public static final String PATH_MODULE = "module";
	
	/*
	 * List columns table
	 */
	public interface ModuleListColumns {
		/* Default Column _ID */
		String MODULE_NAME = "moduleName";
		String MODULE_PACKAGE = "modulePackage";
		String MODULE_VERSION = "moduleVersion";
	}
	
	/*
	 * Table module
	 */
	public static class ModuleTable implements ModuleListColumns, BaseColumns {
		// URI
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MODULE).build();
		
		// Vendor
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.acas.sources.module";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.acas.sources.module";

		// Build URI
		public static Uri buildUri(String id) {
			return CONTENT_URI.buildUpon().appendPath(id).build();
		}

		// Get id
		public static String getId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

}
