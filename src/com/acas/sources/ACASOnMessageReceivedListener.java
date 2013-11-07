package com.acas.sources;

/**
 * Implement this interface for receive all notification of message
 * received by ACAS framework.
 */
public interface ACASOnMessageReceivedListener {
	/**
	 * Use this listener for get all message from other authorized module
	 * @param message
	 */
	public void onMessageReceived(ACASMessage message);
}