package org.avr.fileread.exceptions;

public class LayoutException extends Exception {
	private static final long serialVersionUID = -8463817700052919101L;
	
	private Exception originalException;
	
	
	public LayoutException(String msg ) {
		super(msg);
	}
	public LayoutException(String msg , Exception origEx) {
		super(msg);
		this.originalException = origEx;
	}

}
