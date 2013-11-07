package com.acas.sources;

import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;

public class ACASMessage {
	
	static final String TAG = ACASMessage.class.getSimpleName();
	
	public static final String ID_BROADCAST = null;	
	
	String mIdSender;
	long mId;
	String mIdReceiver;
	Bundle mExtras;
	boolean mDelivered = false;

	/**
	 * Use this for directly respond to the sender
	 * @param extras
	 */
	public void respond(Bundle extras) {
		ACAS.mCommunication.respond(this, extras);
	}
	
	/**
	 * Get if this message has been delivered
	 * @return
	 */
	public final boolean isDelevered() {
		return mDelivered;
	}
	
	/**
	 * Get message extras bundle
	 * @return
	 */
	public final Bundle getExtras() {
		return mExtras;
	}
	
	/**
	 * Get the sender id of this message
	 * 
	 * @return
	 */
	public final String getSenderId() {
		return mIdSender;
	}
	
	/**
	 * Get the receiver id of this message
	 * 
	 * @return
	 */
	public final String getReceiverId() {
		return mIdReceiver;
	}
	
	/**
	 * Get the id of this message
	 * @return
	 */
	public final long getId() {
		return mId;
	}

	/**
	 * <p>Returns a string containing a concise, human-readable description of this object.</p>
	 * 
	 * @return a printable representation of this object. 
	 */
	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("IdSender=").append(mIdSender).append("\n");
		sb.append("Id=").append(mId).append("\n");
		sb.append("Bundle:\n");
		if (mExtras != null && !mExtras.isEmpty()) {
			final Set<String> keys = mExtras.keySet();
			final Iterator<String> iterator = keys.iterator();
			while (iterator.hasNext()) {
			   	final String key = iterator.next();
			   	final Object val = mExtras.get(key);
			   	sb.append("++ ").append(key).append(" = ").append(val).append("\n");
			}
		} else {
			sb.append("++ No extras data\n");
		}
		return sb.toString();
	}

}