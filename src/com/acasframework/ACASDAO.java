package com.acasframework;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

class ACASDAO {

	static final String DB_NAME = "crm.db";
	
	static SQLiteDatabase db = null;

	protected String mTableName;
	protected Context mContext;

	public ACASDAO(Context context, String tableName) {
		mTableName = tableName;
		mContext = context;
		
		if (db == null) {
			db = context.openOrCreateDatabase(DB_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
		}
	}

	protected void createTable(String params) {
		try {
			db.beginTransaction();
			String tableSql = "CREATE TABLE IF NOT EXISTS " + mTableName + " (" + params + ");";
			db.execSQL(tableSql);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isTableEmpty() {
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT count(*) FROM " + mTableName, null);
			int countIndex = cursor.getColumnIndex("count(*)");
			cursor.moveToFirst();
			int rowCount = cursor.getInt(countIndex);
			if (rowCount > 0) {
				return false;
			}
			return true;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void deleteAll() {
		try {
			if (db == null) {
				return;
			}
			// begin the transaction
			db.beginTransaction();
			db.delete(mTableName, null, null);
			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
			}
		}
	}

	public void drop() {
		try {
			if (db == null) {
				return;
			}
			// begin the transaction
			db.beginTransaction();
			String tableSql = "DROP TABLE IF EXISTS " + mTableName;
			db.execSQL(tableSql);
			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
			}
		}
	}

}
