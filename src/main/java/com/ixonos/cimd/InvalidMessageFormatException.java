package com.ixonos.cimd;

/**
 * Exception class for reporting invalid message format.
 * 
 * @author Ixonos / Marko Asplund
 */
public class InvalidMessageFormatException extends ContingencyException {
	private static final long serialVersionUID = -8564791745259804802L;

  public InvalidMessageFormatException(String message) {
	  super(message);
  }

	public InvalidMessageFormatException(String message, Throwable cause) {
		super(message, cause);
  }

}
