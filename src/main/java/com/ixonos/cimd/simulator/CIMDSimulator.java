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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CIMD server simulator.
 * 
 * @author Ixonos / Marko Asplund
 */
public class CIMDSimulator {
  private static final Logger logger = LoggerFactory.getLogger(CIMDSimulator.class);
  private IoAcceptor cimdAcceptor;
  private int port;
  private IoAcceptor msgAcceptor;
  private int messagePort;
  private static final String CHARSET = "ISO-8859-15";

  public CIMDSimulator(int port, int messagePort, long messageInjectSleepTimeMillis) {
    this.port = port;
    this.messagePort = messagePort;

    // CIMD connection acceptor.
    cimdAcceptor = new NioSocketAcceptor();
    cimdAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
    cimdAcceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new CIMDCodecFactory()));
    CIMDMessageHandler handler = new CIMDMessageHandler();
    cimdAcceptor.setHandler(handler);

    // acceptor for injecting text messages into the CIMD server.
    msgAcceptor = new NioSocketAcceptor();
    msgAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
    msgAcceptor.getFilterChain().addLast("protocol",
        new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName(CHARSET))));
    TextMessageHandler textHandler = new TextMessageHandler(cimdAcceptor.getManagedSessions(),
        messageInjectSleepTimeMillis);
    msgAcceptor.setHandler(textHandler);
  }

  public void start() throws IOException {
    cimdAcceptor.bind(new InetSocketAddress(port));
    msgAcceptor.bind(new InetSocketAddress(messagePort));
    logger.info("running");
  }

  public static void main(String... args) throws IOException {
    String propBase = CIMDSimulator.class.getSimpleName().toLowerCase();
    CIMDSimulator simu = new CIMDSimulator(Integer.getInteger(propBase + ".port"),
        Integer.getInteger(propBase + ".messagePort"),
        Long.getLong(propBase + ".messageInjectSleepTimeMillis"));
    simu.start();
  }

}
