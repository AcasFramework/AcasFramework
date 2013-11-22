package com.acasframework;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ACASModuleDAO extends ACASDAO {
	
	static final String TAG = ACASModuleDAO.class.getSimpleName();
	
	private static final String NAME = "name";
	private static final String PACKAGE = "package";
	private static final String ENTRIES = "entries";
	private static final String IS_MOTHER = "ismother";
	private static final String VERSION = "version";

	public ACASModuleDAO(Context context) {
		super(context, "acas_module");

		createTable("id INTEGER PRIMARY KEY, `" + NAME + "` VARCHAR(100), `" + PACKAGE + "` VARCHAR(100), `" + VERSION + "` VARCHAR(10), `" + ENTRIES
				+ "` VARCHAR(100), `" + IS_MOTHER + "` SHORT");
	}

	public void insert(ACASModule e, boolean is_mother) {
		try {
			db.beginTransaction();

			final ContentValues newCon = new ContentValues();
			newCon.put(NAME, e.mName);
			newCon.put(PACKAGE, e.mPackage);
			newCon.put(ENTRIES, e.mEntryPoint);
			newCon.put(VERSION, e.mVersion);
			newCon.put(IS_MOTHER, (is_mother ? 1 : 0));

			db.insert(mTableName, null, newCon);
			db.setTransactionSuccessful();
			if (ACAS.DEBUG_MODE) {
				Log.i("module DAO", "insert module : " + e.toString());
			}
		} finally {
			db.endTransaction();
		}
	}

	public ArrayList<ACASModule> select(String where) {
		Cursor cursor = null;
		String mWhere = "";
		if (where != null) {
			mWhere = "WHERE " + where;
		}
		try {
			cursor = db.rawQuery("SELECT * FROM " + mTableName + mWhere, null);
			if (cursor.getCount() > 0) {
				ArrayList<ACASModule> opt = new ArrayList<ACASModule>();
				cursor.moveToFirst();

				while (!cursor.isAfterLast()) {

					int name = cursor.getColumnIndex(NAME);
					int pckg = cursor.getColumnIndex(PACKAGE);
					int entries = cursor.getColumnIndex(ENTRIES);
					int version = cursor.getColumnIndex(VERSION);

					opt.add(new ACASModule(cursor.getString(pckg), cursor.getString(name), cursor.getString(version), cursor.getString(entries), null));

					if (ACAS.DEBUG_MODE) {
						Log.i("module DAO", "select module : " + opt.get(opt.size() - 1).toString());
					}

					cursor.moveToNext();

				}
				return opt;
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	/**
	 * Get the mother application data
	 * 
	 * @return the ACASModule contain mother data
	 */
	ACASModule selectMother() {
		// Execute request
		final Cursor cursor = db.rawQuery("SELECT * FROM " + mTableName + " WHERE `" + IS_MOTHER + "` = 1 LIMIT 1", null);

		// Check cursor
		if (cursor == null) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Cursor is null !");
			}
			return null;
		}
		if (cursor.moveToFirst()) {
			if (ACAS.DEBUG_MODE) {
				Log.e(TAG, "Cursor is empty !");
			}
			cursor.close();
			return null;
		}
		
		// Extract data
		final int name = cursor.getColumnIndex(NAME);
		final int pckg = cursor.getColumnIndex(PACKAGE);
		final int entries = cursor.getColumnIndex(ENTRIES);
		final int version = cursor.getColumnIndex(VERSION);

		// Build ACAS Module
		final ACASModule module = new ACASModule(cursor.getString(pckg), cursor.getString(name), cursor.getString(version), cursor.getString(entries), null);				
		if (ACAS.DEBUG_MODE) {
			Log.i("module DAO", "Select mother: " + module.toString());
		}
		
		// Close
		cursor.close();
		
		return (module);
	}

}