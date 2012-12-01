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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;

/**
 * CIMD protocol packet mock handler.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDMessageHandler extends IoHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(CIMDMessageHandler.class);

  public CIMDMessageHandler() {
    super();
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
          SessionUtils.setUserId(session, p.getValue());
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
      CIMDPacket cp = new CIMDPacket(req);
      if(cp.getDestinationAddress() == null) {
        logger.error("destination parameter missing: "+req);
        return;
      }
      logger.debug("received message from app:");
      logger.debug(cp.getUserData());
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber(),
        new Parameter(Parameter.DESTINATION_ADDRESS, cp.getDestinationAddress()),
        new Parameter(Parameter.MC_TIMESTAMP, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
      break;
    case Packet.OP_ALIVE:
      res = new Packet(req.getOperationCode() + 50, req.getSequenceNumber());
      break;
    case CIMDConstants.OP_DELIVER_MESSAGE_RSP:
      // positive response to "deliver message" - msg from SMSC to app
      return;
    case Packet.OP_NACK:
      logger.error("operation rejected by application");
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

    // instantiate packet sequence number generator and store it in session
    SessionUtils.setPacketSequenceNumberGenerator(session, new SmsCenterPacketSequenceNumberGenerator());
  }

  @Override
  public void sessionClosed(IoSession session) throws Exception {
    super.sessionClosed(session);
    logger.debug("session closed: " + session.getId());
  }

  public static class CIMDPacket {
    private String destinationAddress;
    private String userData; // user data un-encoded
    private static final Charset charset = Charset.forName("GSM");

    public CIMDPacket(Packet p) {
      this.parsePacket(p);
    }

    private void parsePacket(Packet p) {
      Integer dataCodingScheme = null;
      String userDataBinary = null;
      for(Parameter pr : p.getParameters()) {
        switch (pr.getNumber()) {
          case Parameter.DESTINATION_ADDRESS:
            destinationAddress = pr.getValue();
            break;
          case Parameter.DATA_CODING_SCHEME:
            dataCodingScheme = Integer.valueOf(pr.getValue());
            break;
          case Parameter.USER_DATA:
            userData = pr.getValue();
            break;
          case Parameter.USER_DATA_BINARY:
            userDataBinary = pr.getValue();
        }
      }
      userData = decodeUserDataBinary(dataCodingScheme, userDataBinary);
    }

    private String decodeUserDataBinary(Integer dataCodingScheme, String userDataBinary) {
      if(dataCodingScheme == null) {
        logger.info("data coding scheme not specified for user binary data");
        return userDataBinary;
      }
      if(dataCodingScheme == 0) {
        CharBuffer cb = null;
        try {
          CharsetDecoder decoder = charset.newDecoder();
          byte[] encHex = Hex.decodeHex(userDataBinary.toCharArray());
          cb = decoder.decode(ByteBuffer.wrap(encHex));
        } catch (DecoderException | CharacterCodingException e) {
          logger.info("failed to decode user data: "+userDataBinary, e);
        }
        return new String(cb.array());
      } else {
        logger.info("Unsupported data coding scheme: "+dataCodingScheme);
      }
      return userDataBinary;
    }

    public String getDestinationAddress() {
      return destinationAddress;
    }

    public String getUserData() {
      return userData;
    }
  }

}
