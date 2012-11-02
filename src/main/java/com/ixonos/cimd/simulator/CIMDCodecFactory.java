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
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ProtocolCodecFactory implementation that serializes and deserializes CIMD
 * protocol packets.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDCodecFactory implements ProtocolCodecFactory {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(CIMDCodecFactory.class);

  public CIMDCodecFactory() {
  }

  public ProtocolDecoder getDecoder(IoSession session) throws Exception {
    CIMDPacketDecoder dec = (CIMDPacketDecoder) session.getAttribute(SessionAttribute.CIMD_DECODER);
    return dec;
  }

  public ProtocolEncoder getEncoder(IoSession session) throws Exception {
    CIMDPacketEncoder enc = (CIMDPacketEncoder) session.getAttribute(SessionAttribute.CIMD_ENCODER);
    return enc;
  }

}
