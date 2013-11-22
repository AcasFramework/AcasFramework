package com.acasframework;

import java.util.ArrayList;
import java.util.Iterator;

import com.acasframework.exception.ACASInvalidKeyException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

class ACASCommunication {

	static final String TAG = ACASCommunication.class.getSimpleName();
	
	static final String EXCEPTION_INVALIDE_KEY = "The keyApi is not set or invalid";

	static final String EXTRA_SENDER_ID = "com.acas.sources.EXTRA_SENDER_ID";
	static final String EXTRA_RECEIVER_ID = "com.acas.sources.EXTRA_RECEIVER_ID";
	static final String EXTRA_ID = "com.acas.sources.EXTRA_ID";
	
	ArrayList<ACASOnMessageReceivedListener> mListeners = new ArrayList<ACASOnMessageReceivedListener>();
	ArrayList<ACASMessage> mSendedList = new ArrayList<ACASMessage>();
	ArrayList<ACASMessage> mReceivedList = new ArrayList<ACASMessage>();
	
	static int sNumberMessageSended = 0;
	static int sNumberMessageReceived = 0;
	static int sNumberMessageReceivedMax = 1000;

	/**
	 * Add an listener for received messages
	 * 
	 * @param listener
	 */
	public void setOnMessageReceivedListener(ACASOnMessageReceivedListener listener) {
		mListeners.add(listener);
		deliveryUndeliveredMessage();
	}
	
	/**
	 * Delete an lister from list
	 * 
	 * @param listener
	 */
	public void removeOnMessageReceiverListener(ACASOnMessageReceivedListener listener) {
		mListeners.remove(listener);
	}

	/**
	 * Receive the message from the Receiver
	 * 
	 * @param message
	 */
	public void onMessageReceiv(ACASMessage message) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "onMessageReceiv");
		}
		
		// Check the key validity
		if (!ACAS.mSecurity.mIsValid) {
			throw new ACASInvalidKeyException(EXCEPTION_INVALIDE_KEY);
		}
		
		// Check if message is for us
		if (message.mIdReceiver != ACASMessage.ID_BROADCAST && !message.mIdReceiver.equalsIgnoreCase(ACAS.mAppId)) {
			if (ACAS.DEBUG_MODE) {
				Log.i(TAG, "This message is not for us");
			}
			return;
		}

		// Save into received list
		synchronized (mReceivedList) {
			mReceivedList.add(message);
			if (mReceivedList.size() >= sNumberMessageReceivedMax) {
				if (ACAS.DEBUG_MODE) {
					Log.i(TAG, "Received list contain more than "+ sNumberMessageReceivedMax +" messages...");
				}
				clearAllDeliveredMessageFromReceivList();
				if (ACAS.DEBUG_MODE) {
					Log.i(TAG, "Received list cleared from delivered message");
				}
				if (mReceivedList.size() >= sNumberMessageReceivedMax) {
					if (ACAS.DEBUG_MODE) {
						Log.i(TAG, "Received list always contain more than "+ sNumberMessageReceivedMax +" messages...");
					}
					mReceivedList.clear();
					if (ACAS.DEBUG_MODE) {
						Log.i(TAG, "Received list cleared !");
					}
				}
			}
			sNumberMessageReceived++;
		}

		// Notify all listener
		deliveryMessage(message);
	}
	
	/**
	 * Use this for delivery all undelivered message to all alive listeners
	 */
	private void deliveryUndeliveredMessage() {
		synchronized (mReceivedList) {
			Iterator<ACASMessage> itr = mReceivedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (message == null) {
					if (ACAS.DEBUG_MODE) {
						Log.d(TAG, "Message is null, remove it.");
					}
					itr.remove();
				} else {
					deliveryMessage(message);
				}
			}
		}
	}
	
	/**
	 * Delivery one message to all alive listener
	 * @param message
	 */
	private void deliveryMessage(ACASMessage message) {
		int nbrDelivery = 0;
		Iterator<ACASOnMessageReceivedListener> itr = mListeners.iterator();
		while (itr.hasNext()) {
			final ACASOnMessageReceivedListener elem = itr.next();
			if (elem == null) {
				if (ACAS.DEBUG_MODE) {
					Log.d(TAG, "Listener is null, remove it.");
				}
				itr.remove();
			} else {
				nbrDelivery++;
				elem.onMessageReceived(message);
			}
		}
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "Message tag="+ message.mId +" delivered "+ nbrDelivery +" time(s)");
		}
		if (nbrDelivery > 0) {
			if (ACAS.DEBUG_MODE) {
				Log.i(TAG, "Message tag="+ message.mId +" has been delivered");
			}
			message.mDelivered = true;
		} else if (ACAS.DEBUG_MODE) {
			Log.w(TAG, "Message tag="+ message.mId +" not delivered...");
		}
	}
	
	/**
	 * Get previous message by tag
	 * 
	 * @param tag
	 * @return
	 */
	ACASMessage getMessageById(long messageId) {
		synchronized (mReceivedList) {
			Iterator<ACASMessage> itr = mReceivedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (message.mId == messageId) {
					return message;
				}
			}
		}
		return null;
	}
	
	/**
	 * Use for send one message to a particular receiver
	 * 
	 * @param messageId
	 * @param idReceiver
	 * @param extras
	 * @see {@link com.acasframework.ACASCommunication#send(ACASMessage)} for an advanced use
	 */
	void send(long messageId, String idReceiver, Bundle extras) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "send to idReceiver="+ idReceiver +" messageId="+ messageId);
		}
		
		// Create message
		final ACASMessage message = new ACASMessage();
		message.mExtras = extras;
		message.mId = messageId;
		message.mIdSender = ACAS.mAppId;
		message.mIdReceiver = idReceiver;

		// Send message
		send(message);
	}
	
	/**
	 * <p>Use for send an custom message</p>
	 * <p>It's better to use {@link com.acasframework.ACASCommunication#send(long, String, Bundle)}</p>
	 * 
	 * @param message
	 * @see {@link com.acasframework.ACASCommunication#send(long, String, Bundle)}
	 */
	void send(ACASMessage message) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "send to idReceiver="+ message.mIdReceiver +" idSender="+ message.mIdSender +" messageId="+ message.mId);
		}
		synchronized (mSendedList) {
			mSendedList.add(message);
			sNumberMessageSended++;
		}
		sendStack();
	}

	/**
	 * Use for send one message to all receiver
	 * 
	 * @param messageId
	 * @param extras
	 * @see {@link com.acasframework.ACASCommunication#broadcast(ACASMessage)} for an advanced use
	 */
	void broadcast(long messageId, Bundle extras) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "broadcast tag="+ messageId);
		}
		
		// Create broadcast message
		final ACASMessage message = new ACASMessage();
		message.mExtras = extras;
		message.mId = messageId;
		message.mIdSender = ACAS.mAppId;
		message.mIdReceiver = ACASMessage.ID_BROADCAST;
		
		// Send message
		send(message);
	}
	
	/**
	 * <p>Use for send one custom message to all receiver</p>
	 * <p>It's better to use {@link com.acasframework.ACASCommunication#broadcast(long, Bundle)}</p>
	 * 
	 * @param message
	 * @see {@link com.acasframework.ACASCommunication#broadcast(long, Bundle)}
	 */
	void broadcast(ACASMessage message) {
		// Add broadcast tag
		message.mIdReceiver = ACASMessage.ID_BROADCAST;

		// Send message
		send(message);
	}
	
	/**
	 * <p>Use for respond to a received message</p>
	 * 
	 * @param message
	 * @param extras
	 */
	void respond(ACASMessage message, Bundle extras) {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "respond to "+ message.mIdSender +" tag="+ message.mId);
		}
		
		// Create response message
		final ACASMessage response = new ACASMessage();
		response.mExtras = extras;
		response.mId = message.mId;
		response.mIdSender = ACAS.mAppId;
		response.mIdReceiver = message.mIdSender;
		
		// Send response
		send(response);
	}

	/**
	 * Use for send all message in the stack
	 */
	void sendStack() {
		if (ACAS.DEBUG_MODE) {
			Log.i(TAG, "sendStack");
		}
		if (!ACAS.mSecurity.mIsValid) {
			throw new ACASInvalidKeyException("The keyApi is not set or invalid");
		}
		synchronized (mSendedList) {
			Iterator<ACASMessage> itr = mSendedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (ACAS.DEBUG_MODE) {
					Log.i(TAG, "> Tag="+ message.mId +" receiver="+ message.mIdReceiver + " sender="+ message.mIdSender);
				}
				final Intent intent = new Intent();
				intent.setAction(ACAS.ACTION_MESSAGE);
				intent.putExtras(message.mExtras);
				intent.putExtra(EXTRA_RECEIVER_ID, message.mIdReceiver);
				intent.putExtra(EXTRA_SENDER_ID, message.mIdSender);
				intent.putExtra(EXTRA_ID, message.mId);
				ACAS.mContext.sendBroadcast(intent);
				itr.remove();
			}
		}
	}
	
	/**
	 * Use this method for clear all message from the send and received list
	 */
	int clearAllMessageList() {
		int delNbr = clearReceivedMessageList();
		delNbr += clearSendedMessageList();
		return delNbr;
	}
	
	/**
	 * Use this method for clear all message from the received list
	 */
	int clearReceivedMessageList() {
		synchronized (mReceivedList) {
			final int delNbr = mReceivedList.size();
			mReceivedList.clear();
			return delNbr;
		}
	}
	
	/**
	 * Use this method for clear all message from the send list
	 */
	int clearSendedMessageList() {
		synchronized (mSendedList) {
			final int delNbr = mSendedList.size();
			mSendedList.clear();
			return delNbr;
		}
	}
	
	int clearAllDeliveredMessage() {
		int delNbr = clearAllDeliveredMessageFromSendList();
		delNbr += clearAllDeliveredMessageFromReceivList();
		return delNbr;
	}
	
	int clearAllDeliveredMessageFromSendList() {
		int delNbr = 0;
		synchronized (mSendedList) {
			Iterator<ACASMessage> itr = mSendedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (message.isDelevered()) {
					itr.remove();
					delNbr++;
				}
			}
		}
		return delNbr;
	}
	
	int clearAllDeliveredMessageFromReceivList() {
		int delNbr = 0;
		synchronized (mReceivedList) {
			Iterator<ACASMessage> itr = mReceivedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (message.isDelevered()) {
					itr.remove();
					delNbr++;
				}
			}
		}
		return delNbr;
	}
	
	int clearAllUndeliveredMessage() {
		int delNbr = clearAllUndeliveredMessageFromSendList();
		delNbr += clearAllUndeliveredMessageFromReceivList();
		return delNbr;
	}
	
	int clearAllUndeliveredMessageFromSendList() {
		int delNbr = 0;
		synchronized (mSendedList) {
			Iterator<ACASMessage> itr = mSendedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (!message.isDelevered()) {
					itr.remove();
					delNbr++;
				}
			}
		}
		return delNbr;
	}
	
	int clearAllUndeliveredMessageFromReceivList() {
		int delNbr = 0;
		synchronized (mReceivedList) {
			Iterator<ACASMessage> itr = mReceivedList.iterator();
			while (itr.hasNext()) {
				final ACASMessage message = itr.next();
				if (!message.isDelevered()) {
					itr.remove();
					delNbr++;
				}
			}
		}
		return delNbr;
	}

}