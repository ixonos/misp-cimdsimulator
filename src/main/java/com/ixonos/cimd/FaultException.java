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
 * Fault exception base class.
 * 
 * @author Ixonos / Marko Asplund
 */
public class FaultException extends RuntimeException {
  private static final long serialVersionUID = 8087959355422987224L;

  public FaultException(String message) {
    super(message);
  }

}
