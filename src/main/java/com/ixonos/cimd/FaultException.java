package com.ixonos.cimd;

public class FaultException extends RuntimeException {
  private static final long serialVersionUID = 8087959355422987224L;

	public FaultException(String message) {
	  super(message);
  }

}
