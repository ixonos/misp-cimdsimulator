package com.ixonos.cimd;

/**
 * Contingency exception base class.
 * 
 * @author Ixonos / Marko Asplund
 */
public class ContingencyException extends Exception {
	private static final long serialVersionUID = -7260037049820487170L;

  public ContingencyException(String message) {
	  super(message);
  }

	public ContingencyException(String message, Throwable cause) {
		super(message, cause);
  }

}
