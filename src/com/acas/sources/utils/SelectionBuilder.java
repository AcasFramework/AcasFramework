package com.acas.sources.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.acas.sources.ACAS;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each appended clause is combined using {@code AND}. This
 * class is <em>not</em> thread safe.
 */
public class SelectionBuilder {

	static final String TAG = SelectionBuilder.class.getSimpleName();

	private StringBuilder mTable = new StringBuilder();
	private Map<String, String> mProjectionMap = Maps.newHashMap();
	private StringBuilder mGroupBy = new StringBuilder();
	private StringBuilder mSelection = new StringBuilder();
	private ArrayList<String> mSelectionArgs = Lists.newArrayList();

	/**
	 * Reset any internal state, allowing this builder to be recycled.
	 */
	public SelectionBuilder reset() {
		mTable = null;
		mSelection.setLength(0);
		mGroupBy.setLength(0);
		mSelectionArgs.clear();
		return this;
	}

	/**
	 * Append the given selection clause to the internal state. Each clause is surrounded with parenthesis and combined using {@code AND}.
	 */
	public SelectionBuilder where(String selection, String... selectionArgs) {
		if (TextUtils.isEmpty(selection)) {
			if (selectionArgs != null && selectionArgs.length > 0) {
				throw new IllegalArgumentException("Valid selection required when including arguments=");
			}

			// Shortcut when clause is empty
			return this;
		}

		if (mSelection.length() > 0) {
			mSelection.append(" AND ");
		}

		mSelection.append("(").append(selection).append(")");
		if (selectionArgs != null) {
			for (String arg : selectionArgs) {
				mSelectionArgs.add(arg);
			}
		}

		return this;
	}

	public SelectionBuilder table(String table) {
		if (mTable.length() > 0)
			mTable.append(", ");
		mTable.append(table);
		return this;
	}

	private void assertTable() {
		if (mTable == null) {
			throw new IllegalStateException("Table not specified");
		}
	}

	public SelectionBuilder mapToTable(String column, String table) {
		mProjectionMap.put(column, table + "." + column);
		return this;
	}

	public SelectionBuilder map(String fromColumn, String toClause) {
		mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
		return this;
	}
	
	public SelectionBuilder groupBy(String group) {
		if (mGroupBy.length() > 0)
			mGroupBy.append(", ");
		mGroupBy./*append("`").*/append(group)/*.append("`")*/;
		return this;
	}

	/**
	 * Return selection string for current internal state.
	 * 
	 * @see #getSelectionArgs()
	 */
	public String getSelection() {
		return mSelection.toString();
	}

	public String getTable() {
		return mTable.toString();
	}
	/**
	 * Return selection arguments for current internal state.
	 * 
	 * @see #getSelection()
	 */
	public String[] getSelectionArgs() {
		return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
	}

	private void mapColumns(String[] columns) {
		for (int i = 0; i < columns.length; i++) {
			final String target = mProjectionMap.get(columns[i]);
			if (target != null) {
				columns[i] = target;
			}
		}
	}

	@Override
	public String toString() {
		return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection() + ", selectionArgs=" + Arrays.toString(getSelectionArgs()) + "]";
	}

	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
		return query(db, columns, mGroupBy.toString(), null, orderBy, null);
	}


	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String groupBy, String having, String orderBy, String limit) {
		assertTable();
		if (columns != null) {
			mapColumns(columns);
		}
		if (ACAS.DEBUG_MODE) {
			Log.d(TAG, "query(columns=" + Arrays.toString(columns) + ") " + this);
		}
		return db.query(getTable(), columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
	}

	/**
	 * Execute update using the current internal state as {@code WHERE} clause.
	 */
	public int update(SQLiteDatabase db, ContentValues values) {
		assertTable();
		if (ACAS.DEBUG_MODE) {
			Log.d(TAG, "update() " + this);
		}
		return db.update(getTable(), values, getSelection(), getSelectionArgs());
	}

	/**
	 * Execute delete using the current internal state as {@code WHERE} clause.
	 */
	public int delete(SQLiteDatabase db) {
		assertTable();
		if (ACAS.DEBUG_MODE) {
			Log.d(TAG, "delete() " + this);
		}
		return db.delete(getTable(), getSelection(), getSelectionArgs());
	}
}
