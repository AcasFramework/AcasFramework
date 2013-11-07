package com.acas.sources;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.acas.sources.exception.ACASAlreadyInititateException;
import com.acas.sources.exception.ACASIllegalNumberException;
import com.acas.sources.exception.ACASNonInititateException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * This is the main class of the ACAS framework, use it for access all features.
 * 
 * @author Christophe
 *
 */
public class ACAS {

	public static boolean DEBUG_MODE = true;
	static final String TAG = ACAS.class.getSimpleName();

	static final String EXCEPTION_ALREADY_INIT = "Your are already initiate the ACAS lib";
	static final String EXCEPTION_NON_INITIATE = "Please initiate the ACAS lib before use this feature";
	static final String EXCEPTION_NEGATIVE_NUMBER = "The number must be positive";
	static final String EXCEPTION_ZERO_NUMBER = "Number must be superior to zero";
	
	static final String ACTION_MESSAGE = "com.acas.sources.ACTION_MESSAGE";

	static ACASCommunication mCommunication;
	static ACASSecurity mSecurity;
	
	static String mAppId;
	static Context mContext;

	static ArrayList<ACASModule> mModuleList = new ArrayList<ACASModule>();
    static ACASListModuleReceiver mListModuleReceiver;

    private static ACASModule mMother;

	/**
	 * <p>Use for initiate the ACAS library</p>
	 * <p>It is mandatory to use this before all other.</p>
	 * 
	 * @param applicationContext
	 *            The application context
	 * @param securityKey
	 *            The security key provided at the creation of your application
	 *            on the ACAS website.
	 * @return <strong>true</strong> if the library has been initiate,
	 *         <strong>false</strong> otherwise.
	 */
	public static void initiate(Context applicationContext, String securityKey) {
		if (DEBUG_MODE) {
			Log.i(TAG, "Try to init the ACAS lib");
		}
		if (isInitiate()) {
			throw new ACASAlreadyInititateException(EXCEPTION_ALREADY_INIT);
		}
		
		// Store context
		mContext = applicationContext;
		mAppId = applicationContext.getPackageName();
		
		// Manage security
		mSecurity = new ACASSecurity(applicationContext, securityKey);
		
		// Start the receiver
		final ACASReceiver receiver = new ACASReceiver();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_MESSAGE);
		applicationContext.registerReceiver(receiver, filter);

		// Start synchronize module list
		final ACASGetListModuleTask syncTask = new ACASGetListModuleTask(applicationContext);
		syncTask.execute(mSecurity);
		
		// ACAS Communication initiation
		mCommunication = new ACASCommunication();
        mListModuleReceiver = new ACASListModuleReceiver();
	}
	
	/**
	 * <p>Use this method for set the debug mode</p>
	 * 
	 * @param debugState true for enable the debug mode
	 */
	public static void setDebugMode(boolean debugState) {
		if (debugState) {
			Log.w(TAG, "The DEBUG MODE is set");
		}
		ACAS.DEBUG_MODE = debugState;
	}
	
	/**
	 * <p>Get if the debug mode is enable</p>
	 * 
	 * @return true if debug mode is enable or false otherwise
	 */
	public static boolean isDebugMode() {
		return ACAS.DEBUG_MODE;
	}
	
	/**
	 * <p>Get if the ACAS lib is initiate</p>
	 * 
	 * @return true if the lib has been initiate or false otherwise
	 */
	public static boolean isInitiate() {
		if (mCommunication == null) {
			return false;
		}
		return true;
	}

	/**
	 * <p>Return the associated module list of this application.</p>
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * */
	public static synchronized ArrayList<ACASModule> getModuleList() {
		return mModuleList;
	}

	/**
	 * <p>Use for send a message to a particular module identified by idDest param.</p>
	 * 
	 * @param tag
	 *            An unique id for this transaction
	 * @param idDest
	 *            The unique id of the receiver of message
	 * @param extras
	 *            The bundle container of data
	 * @throws ACASNonInititateException
	 *             If library non initiate by
	 *             {@link com.acas.sources.ACAS#initiate(Context, String)}
	 */
	public static void sendMessage(int tag, String idDest, Bundle extras) {
		if (DEBUG_MODE) {
			Log.i(TAG, "sendMessage tag=" + tag + " idRecepteur=" + idDest);
		}
		if (!isInitiate()) {
			throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
		}
		mCommunication.send(tag, idDest, extras);
	}

	/**
	 * <p>Use for send a message to all receiver of this application.</p>
	 * 
	 * @param tag
	 *            An unique id for this transaction
	 * @param extras
	 *            The bundle container of data
	 * @throws ACASNonInititateException
	 *             If library non initiate by
	 *             {@link com.acas.sources.ACAS#initiate(Context, String)}
	 */
	public static void broadcast(int tag, Bundle extras) {
		if (DEBUG_MODE) {
			Log.i(TAG, "broadcast tag=" + tag);
		}
		if (!isInitiate()) {
			throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
		}
		mCommunication.broadcast(tag, extras);
	}

	/**
	 * <p>Get a message by tag, return null if not found.</p>
	 * 
	 * @param tag
	 * @return ACASMessage or null if no message were found
	 * @throws ACASNonInititateException
	 *             If library non initiate by
	 *             {@link com.acas.sources.ACAS#initiate(Context, String)}
	 * @see {@link com.acas.sources.ACASMessage}
	 */
	public static ACASMessage getMessageByTag(int tag) {
		if (DEBUG_MODE) {
			Log.i(TAG, "getMessageByTag tag=" + tag);
		}
		if (!isInitiate()) {
			throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
		}
		return mCommunication.getMessageById(tag);
	}

    /**
     * <p>Use for add an receiver to listener list, it will notified of all future  {@link com.acas.sources.ACASMessage} who application receive</p>
     * <p>Don't forget to remove it with {@link com.acas.sources.ACAS#removeToListenerList(ACASOnMessageReceivedListener)}} !</p>
     *
     * @param listener
     *            The listener who was added into list
     * @throws ACASNonInititateException
     *             If library non initiate by
     *             {@link com.acas.sources.ACAS#initiate(Context, String)}
     * @see {@link com.acas.sources.ACAS#removeToListenerList(ACASOnMessageReceivedListener)}}
     */
    public static void addToListenerList(ACASOnMessageReceivedListener listener) {
        if (DEBUG_MODE) {
            Log.i(TAG, "addToListenerList");
        }
        if (!isInitiate()) {
            throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
        }
        if (listener != null) {
            mCommunication.setOnMessageReceivedListener(listener);
        } else if (DEBUG_MODE) {
            Log.w(TAG, "Impossible to add this listener in list because is null");
        }
    }

    /**
     * <p>Remove one listener from the list of active {@link com.acas.sources.ACASMessage} receiver.</p>
     *
     * @param listener
     *            The listener who was removed from list
     * @throws ACASNonInititateException
     *             If library non initiate by
     *             {@link com.acas.sources.ACAS#initiate(Context, String)}
     * @see {@link com.acas.sources.ACAS#addToListenerList(ACASOnMessageReceivedListener)}
     */
    public static void removeToListenerList(ACASOnMessageReceivedListener listener) {
        if (!isInitiate()) {
            throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
        }
        if (listener != null) {
            mCommunication.removeOnMessageReceiverListener(listener);
        } else if (DEBUG_MODE) {
            Log.w(TAG, "Impossible to remove this listener from list because is null");
        }
    }

    /**
     * <p>Use for add an receiver to listener list, it will notified of all future  {@link com.acas.sources.ACASModule} who the application receives</p>
     * <p>Don't forget to remove it with {@link com.acas.sources.ACAS#removeToListenerModuleList(ACASOnListModuleReceiver)}  !</p>
     *
     * @param listener
     *            The listener who was added into list
     * @throws ACASNonInititateException
     *             If library non initiate by
     *             {@link com.acas.sources.ACAS#initiate(Context, String)}
     * @see {@link com.acas.sources.ACAS#removeToListenerModuleList(ACASOnListModuleReceiver)}
     */
    public static void addToListenerModuleList(ACASOnListModuleReceiver listener) {
        if (DEBUG_MODE) {
            Log.i(TAG, "addToListenerList");
        }
        if (!isInitiate()) {
            throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
        }
        if (listener != null) {
            mListModuleReceiver.setOnListModuleReceivedListener(listener);
        } else if (DEBUG_MODE) {
            Log.w(TAG, "Impossible to add this listener in list because is null");
        }
    }

    /**
     * <p>Remove one listener from the list of active {@link com.acas.sources.ACASModule} receiver.</p>
     *
     * @param listener
     *            The listener who was removed from list
     * @throws ACASNonInititateException
     *             If library non initiate by
     *             {@link com.acas.sources.ACAS#initiate(Context, String)}
     * @see {@link com.acas.sources.ACAS#addToListenerModuleList(ACASOnListModuleReceiver)} }
     */
    public static void removeToListenerModuleList(ACASOnListModuleReceiver listener) {
        if (!isInitiate()) {
            throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
        }
        if (listener != null) {
            mListModuleReceiver.removeOnListModuleReceiverListener(listener);
        } else if (DEBUG_MODE) {
            Log.w(TAG, "Impossible to remove this listener from list because is null");
        }
    }


    /**
     * <p>Use for add an receiver to listener list, it will notified of the  {@link com.acas.sources.ACASModule} that the application receives filtered by @entryPoint </p>
     * <p>Don't forget to remove it with {@link com.acas.sources.ACAS#removeToListenerModuleList(ACASOnListModuleReceiver)}  !</p>
     *
     * @param listener
     *            The listener who was added into list
     * @param entryPoint
     *            The entry point type to filter.
     * @throws ACASNonInititateException
     *             If library non initiate by
     *             {@link com.acas.sources.ACAS#initiate(Context, String)}
     * @see {@link com.acas.sources.ACAS#removeToListenerModuleList(ACASOnListModuleReceiver)}
     */
    public static void addToListenerModuleList(ACASOnListModuleReceiver listener, String entryPoint) {
        if (DEBUG_MODE) {
            Log.i(TAG, "addToListenerList");
        }
        if (!isInitiate()) {
            throw new ACASNonInititateException(EXCEPTION_NON_INITIATE);
        }
        if (listener != null) {
            mListModuleReceiver.setOnListModuleReceivedListener(listener, entryPoint);
        } else if (DEBUG_MODE) {
            Log.w(TAG, "Impossible to add this listener in list because is null");
        }
    }
    
    /**
     * <p>Get the number of message sent during the last session<p>
     * 
     * @return the number of sent message
     */
    public static int getNumberMessageSended() {
    	synchronized (mCommunication.mSendedList) {
           	return ACASCommunication.sNumberMessageSended;
		}
    }
    
    /**
     * <p>Get the number of message receive during the last session<p>
     * 
     * @return the number of receive message
     */
    public static int getNumberMessageReceived() {
    	synchronized (mCommunication.mReceivedList) {
    		return ACASCommunication.sNumberMessageReceived;
    	}
    }
    
    /**
     * <p>Get the number of message present into the sent history list<p>
     * 
     * @return the number of message into sent history
     */
    public static int getStoredSendMessageNumber() {
    	synchronized (mCommunication.mSendedList) {
    		return mCommunication.mSendedList.size();
    	}
    }
    
    /**
     * <p>Get the number of message present into the receive history list<p>
     * 
     * @return the number of message into receive history
     */
    public static int getStoredReceivMessageNumber() {
    	synchronized (mCommunication.mReceivedList) {
    		return mCommunication.mReceivedList.size();
    	}
    }

    /**
     * Get the ACASModule corresponding to the parent' application
     * @return the module
     */
    public static ACASModule getMother() {
        return ACAS.mMother;
    }

    public static void resetNumberOfMessageSent() {
    	if (DEBUG_MODE) {
    		Log.i(TAG, "Reset number of message sent");
    	}
    	synchronized (mCommunication.mSendedList) {
    		ACASCommunication.sNumberMessageSended = 0;
    	}
    }
    
    public static void resetNumberOfMessageReceiv() {
    	if (DEBUG_MODE) {
    		Log.i(TAG, "Reset number of message receive");
    	}
    	synchronized (mCommunication.mReceivedList) {
    		ACASCommunication.sNumberMessageReceived = 0;
    	}
    }
    
    public static void setStoredReceivMessageNumberMax(int numberMessage) {
    	if (numberMessage < 0) {
    		throw new ACASIllegalNumberException(EXCEPTION_NEGATIVE_NUMBER);
    	} else if (numberMessage == 0) {
    		throw new ACASIllegalNumberException(EXCEPTION_ZERO_NUMBER);
    	}
    	if (DEBUG_MODE) {
    		Log.i(TAG, "Set number max of received message to "+ numberMessage);
    	}
    	ACASCommunication.sNumberMessageReceivedMax = numberMessage;
    }
    
    public static int getStoredReceivMessageNumberMax() {
    	return ACASCommunication.sNumberMessageReceivedMax;
    }
    
    public static int clearAllMessageList() {
    	return mCommunication.clearAllMessageList();
	}
	
    public static int clearReceivedMessageList() {
		return mCommunication.clearReceivedMessageList();
	}
	
    public static int clearSendedMessageList() {
    	return mCommunication.clearSendedMessageList();
    }
    
    public static int clearAllDeliveredMessage() {
    	return mCommunication.clearAllDeliveredMessage();
	}
	
    public static int clearAllDeliveredMessageFromSendList() {
		return mCommunication.clearAllDeliveredMessageFromSendList();
	}
	
    public static int clearAllDeliveredMessageFromReceivList() {
		return mCommunication.clearAllDeliveredMessageFromReceivList();
	}
    
    public static int clearAllUndeliveredMessage() {
    	return mCommunication.clearAllUndeliveredMessage();
	}
	
    public static int clearAllUndeliveredMessageFromSendList() {
		return mCommunication.clearAllUndeliveredMessageFromSendList();
	}
	
    public static int clearAllUndeliveredMessageFromReceivList() {
		return mCommunication.clearAllUndeliveredMessageFromReceivList();
	}


    public static void setMother(ACASModule mother) {
        ACAS.mMother = mother;
    }
}