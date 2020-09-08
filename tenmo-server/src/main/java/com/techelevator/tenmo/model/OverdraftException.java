package com.techelevator.tenmo.model;

import org.springframework.web.bind.annotation.ResponseStatus;

public class OverdraftException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public OverdraftException() {
		super("Transfer is for more money than you have.");
	}

}
