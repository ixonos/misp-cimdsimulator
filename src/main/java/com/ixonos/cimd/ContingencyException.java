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
