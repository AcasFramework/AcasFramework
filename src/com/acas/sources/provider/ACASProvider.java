package com.acas.sources.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.acas.sources.ACAS;
import com.acas.sources.provider.ACASContract.ModuleTable;
import com.acas.sources.provider.ACASDatabase.Tables;
import com.acas.sources.utils.SelectionBuilder;

public class ACASProvider extends ContentProvider {

	static final String TAG = ACASProvider.class.getSimpleName();

	private static final String MANY = "/*";
	
	/*
	 * Simple table
	 */
	static final int MODULE = 100;
	static final int MODULE_ID = 101;
	

	private ACASDatabase mOpenHelper;
	private static UriMatcher sUriMatcher;

	protected static UriMatcher buildUriMatcher(final String authority) {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		matcher.addURI(authority, ACASContract.PATH_MODULE, MODULE);
		matcher.addURI(authority, ACASContract.PATH_MODULE + MANY, MODULE_ID);
		
		return matcher;
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mOpenHelper = new ACASDatabase(context);
		sUriMatcher = buildUriMatcher(ACASContract.CONTENT_AUTHORITY);
		return true;
	}

	public String getType(Uri uri) {
		if (ACAS.DEBUG_MODE) {
			Log.d(TAG, "getType of uri=" + uri);
		}
		final int match = sUriMatcher.match(uri);
		switch (match) {

		case MODULE:
			return ModuleTable.CONTENT_TYPE;
		case MODULE_ID:
			return ModuleTable.CONTENT_ITEM_TYPE;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (ACAS.DEBUG_MODE) {
			Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ", sel=" + selection + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		final SelectionBuilder builder = buildSelection(uri);
		return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (ACAS.DEBUG_MODE) {
			Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final int match = sUriMatcher.match(uri);

		switch (match) {
		case MODULE: {
			final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			final long id = db.insertOrThrow(Tables.MODULE, null, values);
			return ModuleTable.buildUri(String.valueOf(id));
		}
		
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);

		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSelection(uri);
		return builder.where(selection, selectionArgs).delete(db);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {	
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSelection(uri);
		return builder.where(selection, selectionArgs).update(db, values);
	}

	protected SelectionBuilder buildSelection(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case MODULE: {
			final SelectionBuilder builder = new SelectionBuilder();
			return builder.table(Tables.MODULE);
		}
		case MODULE_ID: {
			final SelectionBuilder builder = new SelectionBuilder();
			final String id = ModuleTable.getId(uri);
			return builder.table(Tables.MODULE).where(ModuleTable.MODULE_NAME + "=?", id);
		}
		
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

}
