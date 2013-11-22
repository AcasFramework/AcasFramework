package com.acasframework.exception;


/**
 * <p>This exception was throw if you try to reinitialize the ACAS lib</p>
 */
public class ACASAlreadyInititateException extends RuntimeException {

	public ACASAlreadyInititateException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
