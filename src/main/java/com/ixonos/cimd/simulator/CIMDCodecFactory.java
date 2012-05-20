package com.ixonos.cimd.simulator;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

import com.googlecode.jcimd.PacketSequenceNumberGenerator;
import com.googlecode.jcimd.PacketSerializer;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;

/**
 * A ProtocolCodecFactory implementation that serializes and deserializes CIMD protocol packets. 
 * 
 * @author aspluma
 */
public class CIMDCodecFactory implements ProtocolCodecFactory {
	private CIMDPacketDecoder decoder;
	private CIMDPacketEncoder encoder; 

	public CIMDCodecFactory() {
		PacketSequenceNumberGenerator gen = new SmsCenterPacketSequenceNumberGenerator();
		PacketSerializer serializer = new PacketSerializer();
		serializer.setSequenceNumberGenerator(gen);
		decoder = new CIMDPacketDecoder(serializer);
		encoder = new CIMDPacketEncoder(serializer);
	}
	
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
	  return decoder;
  }

	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
	  return encoder;
  }

}
