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
import com.googlecode.jcimd.PacketSequenceNumberGenerator;
import com.googlecode.jcimd.PacketSerializer;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;

/**
 * CIMD protocol packet mock handler.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDMessageHandler extends IoHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(CIMDMessageHandler.class);
  private boolean useCimdCheckSum;

  public CIMDMessageHandler(boolean useCimdCheckSum) {
    super();
    this.useCimdCheckSum = useCimdCheckSum;
  }
  
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
          logger.debug("session "+session.getId()+": user: "+p.getValue());
          break;
        }
      }
      break;
    case Packet.OP_LOGOUT:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber());
      session.write(res);
      session.close(false);
      break;
    case Packet.OP_SUBMIT_MESSAGE:
      // message from application to SMSC
      Parameter dest = null;
      for(Parameter pr : req.getParameters()) {
        if(Parameter.DESTINATION_ADDRESS == pr.getNumber())
          dest = pr;
          break;
      }
      if(dest != null) {
        logger.error("destination parameter missing: "+req);
        return;
      }
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber(), dest,
          new Parameter(60, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
      break;
    case Packet.OP_ALIVE:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber());
      break;
    case Packet.OP_DELIVER_MESSAGE:
      res = null;
      break;
    case CIMDConstants.OP_DELIVER_MESSAGE_RSP:
      // positive response to "deliver message" - msg from SMSC to app
      return;
    default:
      res = new Packet(Packet.OP_GENERAL_ERROR_RESPONSE);
      logger.error("no handler for CIMD operation: "+req);
      break;
    }

    session.write(res); // write response Packet
  }

  @Override
  public void sessionCreated(IoSession session) throws Exception {
    super.sessionCreated(session);
    logger.debug("session created: "+session.getId());

    // instantiate CIMD packet codec and store it in session
    PacketSequenceNumberGenerator gen = new SmsCenterPacketSequenceNumberGenerator();
    PacketSerializer ser = new PacketSerializer("ser", useCimdCheckSum);
    ser.setSequenceNumberGenerator(gen);
    session.setAttribute(SessionAttribute.CIMD_ENCODER, new CIMDPacketEncoder(ser));
    session.setAttribute(SessionAttribute.CIMD_DECODER, new CIMDPacketDecoder(ser));
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    super.sessionClosed(session);
    logger.debug("session closed: " + session.getId());
  }

}
