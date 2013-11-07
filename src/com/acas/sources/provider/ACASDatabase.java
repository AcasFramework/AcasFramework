package com.acas.sources.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.acas.sources.ACAS;
import com.acas.sources.provider.ACASContract.ModuleTable;


public class ACASDatabase extends SQLiteOpenHelper {

	static final String TAG = ACASDatabase.class.getSimpleName();
	static final int DATABASE_VERSION = 30;
    static final String DATABASE_NAME = "ACAS.db";
    
    public interface Tables {
    	String MODULE = "module";
    }
    
    public ACASDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    
        db.execSQL("CREATE TABLE " + Tables.MODULE + " ("
        		+ BaseColumns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                + ModuleTable.MODULE_NAME + " TEXT, "
                + ModuleTable.MODULE_PACKAGE + " TEXT, "
                + ModuleTable.MODULE_VERSION + " TEXT, "              
                + "UNIQUE (" + BaseColumns._ID + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	if (ACAS.DEBUG_MODE) {
    		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
    	}
        
        if (oldVersion != DATABASE_VERSION) {
        	if (ACAS.DEBUG_MODE) {
        		Log.w(TAG, "Destroying old data during upgrade");
        	}

            db.execSQL("DROP TABLE IF EXISTS " + Tables.MODULE);
            
            onCreate(db);
        }
    }
    
}
