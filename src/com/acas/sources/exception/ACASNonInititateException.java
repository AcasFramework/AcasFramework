package com.acas.sources.exception;

import android.content.Context;

/**
 * <p>This exception was throw if you try to use the framework without having previously initialized it</p>
 * 
 * @see {@link com.acas.sources.ACAS#initiate(Context, String)}
 */
public class ACASNonInititateException extends RuntimeException {

	public ACASNonInititateException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
