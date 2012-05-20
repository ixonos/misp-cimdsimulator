package com.ixonos.cimd;

/**
 * Contingency exception base class.
 * 
 * @author Marko Asplund
 */
public class ContingencyException extends Exception {
	private static final long serialVersionUID = -7260037049820487170L;

  public ContingencyException(String message) {
	  super(message);
  }

}
