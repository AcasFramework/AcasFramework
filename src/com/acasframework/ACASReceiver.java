package com.acasframework;

import java.util.Iterator;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class aims at receiving/sending datas from the other modules.
 * 
 * @author Morgan
 * 
 */
public class ACASReceiver extends BroadcastReceiver {

	static final String TAG = ACASReceiver.class.getSimpleName();

	public ACASReceiver() {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Receiver created");
		}
	}
	
	@Override 
	public void onReceive(Context context, Intent intent)  {
		// Get intent variable
		final String action = intent.getAction();
		final Bundle extras = intent.getExtras();
		
		// Debug the receive data
		if (ACAS.DEBUG_MODE) {
			// Check action
			if (action != null && action.length() > 0) {
				Log.d(TAG, "onReceive action="+ intent.getAction());
			} else {
				Log.w(TAG, "onReceive with no action...");
			}
			
			// Check extras
			Log.i(TAG, "+ Extras size="+ extras.size() +" list of all this:");
			if (extras != null && !extras.isEmpty()) {
				final Set<String> keys = extras.keySet();
				final Iterator<String> iterator = keys.iterator();
			    while (iterator.hasNext()) {
			    	final String key = iterator.next();
			    	final Object val = extras.get(key);
			    	if (val != null) {
			    		Log.d(TAG, "++ Key: "+ key +" = "+ val.toString());
			    	} else {
			    		Log.d(TAG, "++ Key: "+ key +" = null");
			    	}
			    }
			} else {
				Log.d(TAG, "++ No extras data");
			}
		}
		
		// Build Message
		final ACASMessage msg = new ACASMessage();
		
		// Extract message data
		msg.mIdSender = extras.getString(ACASCommunication.EXTRA_SENDER_ID);
		msg.mIdReceiver = extras.getString(ACASCommunication.EXTRA_RECEIVER_ID);
		msg.mId = extras.getLong(ACASCommunication.EXTRA_ID);
		
		// Remove useless extras
		extras.remove(ACASCommunication.EXTRA_RECEIVER_ID);
		extras.remove(ACASCommunication.EXTRA_SENDER_ID);
		extras.remove(ACASCommunication.EXTRA_ID);
		
		// Storage user extras data
		msg.mExtras = extras;
		
		// Transmit to communication part
		ACAS.mCommunication.onMessageReceiv(msg);
	}
}
