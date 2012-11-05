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

import org.apache.mina.core.session.IoSession;

import com.googlecode.jcimd.PacketSequenceNumberGenerator;

public class SessionUtils {

  public static String getUserId(IoSession session) {
    return (String) session.getAttribute(SessionAttribute.USER_ID);
  }

  public static void setUserId(IoSession session, String userId) {
    session.setAttributeIfAbsent(SessionAttribute.USER_ID, userId);
  }
  
  public static PacketSequenceNumberGenerator getPacketSequenceNumberGenerator(IoSession session) {
    return (PacketSequenceNumberGenerator) session.getAttribute(SessionAttribute.CIMD_PACKET_SEQUENCE_GENERATOR);
  }

  public static void setPacketSequenceNumberGenerator(IoSession session, PacketSequenceNumberGenerator seq) {
    session.setAttributeIfAbsent(SessionAttribute.CIMD_PACKET_SEQUENCE_GENERATOR, seq);
  }

}
