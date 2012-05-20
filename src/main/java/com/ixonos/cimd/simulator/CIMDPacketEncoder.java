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
 * @author Marko Asplund
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
