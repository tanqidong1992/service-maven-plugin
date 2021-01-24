package com.hngd.tool.exception;

public class CustomJreImageException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CustomJreImageException(String msg,Throwable cause) {
		super(msg, cause);
	}

}
