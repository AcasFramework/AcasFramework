package com.acasframework.exception;

import android.content.Context;

/**
 * <p>This exception was throw if you try to use the framework without having previously initialized it</p>
 * 
 * @see {@link com.acasframework.ACAS#initiate(Context, String)}
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
