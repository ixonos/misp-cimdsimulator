/*
 * Copyright 2012 Ixonos Plc, Finland. All rights reserved.
 * 
 * This file is part of Ixonos MISP CIMD Simulator.
 *
 * This file is licensed under GNU LGPL version 3.
 * Please see the 'license.txt' file in the root directory of the package you received.
 * If you did not receive a license, please contact the copyright holder
 * (sales@ixonos.com).
 *
 */
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
