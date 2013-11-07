package com.acas.sources.exception;

import android.content.Context;

/**
 * <p>This exception was throw if you try to use the framework without a good apiKey</p>
 * 
 * @see {@link com.acas.sources.ACAS#initiate(Context, String)}
 */
public class ACASInvalidKeyException extends RuntimeException {

	public ACASInvalidKeyException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
