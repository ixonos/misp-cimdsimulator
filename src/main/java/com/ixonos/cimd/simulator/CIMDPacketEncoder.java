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

import java.io.ByteArrayOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;

/**
 * A ProtocolDecoder which serializes CIMD protocol packets.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDPacketEncoder implements ProtocolEncoder {
	private PacketSerializer serializer;

	public CIMDPacketEncoder(PacketSerializer serializer) {
		this.serializer = serializer;
  }

	public void dispose(IoSession session) throws Exception {
  }

	public void encode(IoSession session, Object msg, ProtocolEncoderOutput out)
      throws Exception {
		Packet packet = (Packet)msg;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		serializer.serialize(packet, os);
		IoBuffer buf = IoBuffer.allocate(os.size());
		buf.put(os.toByteArray());
		buf.flip();
		out.write(buf);
  }

}
