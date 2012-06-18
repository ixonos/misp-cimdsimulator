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
package com.ixonos.cimd.simulator;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;

/**
 * CIMD protocol packet mock handler.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDMessageHandler extends IoHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(CIMDMessageHandler.class);

  @Override
  public void messageReceived(IoSession session, Object msg) throws Exception {
    Packet req = (Packet) msg;
    logger.debug("p: " + req);

    Packet res = null;

    switch (req.getOperationCode()) {

    case Packet.OP_LOGIN:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber());
      for (Parameter p : req.getParameters()) {
        if (p.getNumber() == Parameter.USER_IDENTITY) {
          session.setAttributeIfAbsent(SessionAttribute.USER_ID, p.getValue());
          break;
        }
      }
      break;
    case Packet.OP_LOGOUT:
      // logger.info("Ending session with " + session.getRemoteAddress());
      session.close(false);
      break;
    case CIMDConstants.OP_DELIVER_MESSAGE_RSP:
      return;
    case Packet.OP_ALIVE:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber());
      break;
    case Packet.OP_DELIVER_MESSAGE:
      res = null;
      break;
    case Packet.OP_SUBMIT_MESSAGE:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber(), new Parameter(60,
          new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
      break;
    default:
      res = new Packet(Packet.OP_GENERAL_ERROR_RESPONSE);
      break;
    }

    session.write(res); // write response Packet
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    super.sessionClosed(session);
    logger.debug("session closed: " + session.getId());
  }

}
