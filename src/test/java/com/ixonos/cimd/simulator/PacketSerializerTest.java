package com.ixonos.cimd.simulator;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;

public class PacketSerializerTest {
  // allow spaces to make data more readable
  private static final String data1 = "02 30 31 3A 30 30 31 09 30 31 30 3A 73 6D 73 31 36 32 30 35 09 30 31 31 3A 78 69 72 75 63 38 63 6A 09 03";

  @Test
  public void deserialize() throws Exception {
    PacketSerializer d = new PacketSerializer("ser", false);
    Packet p = d.deserialize(getPacketAsStream(data1));
  }

  private static InputStream getPacketAsStream(String packet) throws DecoderException {
    packet = packet.replaceAll(" ", "");
    byte[] input = Hex.decodeHex(packet.toCharArray());
    return new ByteArrayInputStream(input);
  }
  
}
