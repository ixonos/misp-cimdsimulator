package com.ixonos.cimd.simulator;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;

/**
 * A ProtocolDecoder which deserializes CIMD protocol packets. 
 * 
 * @author Marko Asplund
 */
public class CIMDPacketDecoder extends CumulativeProtocolDecoder {
	private PacketSerializer serializer;

	private enum Token {
		STX((byte)2), ETX((byte)3);
		private byte val;
		private Token(byte b) {
			val = b;
		}
	}

	public CIMDPacketDecoder(PacketSerializer serializer) {
		this.serializer = serializer;
  }

	@Override
  protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		int start = in.position();
		
		while (in.hasRemaining()) {
			byte c = in.get();
			if (c == Token.ETX.val) {
				int pos = in.position();
				int lim = in.limit();

				try {
					in.position(start);
					in.limit(pos);
					Packet packet = serializer.deserialize(in.slice().asInputStream());
					out.write(packet);
				} finally {
					in.position(pos);
					in.limit(lim);
				}
				return true;
			}
		}
		
		in.position(start);
		
	  return false;
  }

}
