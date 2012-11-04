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
import java.net.Socket;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSequenceNumberGenerator;
import com.googlecode.jcimd.PacketSerializer;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;
import com.ixonos.cimd.ContingencyException;

/**
 * Simple CIMD receiving application mock for stress testing CIMD simulator protocol communication.
 * 
 * @author Ixonos / Marko Asplund
 */
public class ReceiverAppMock {
  private static final Logger logger = LoggerFactory.getLogger(ReceiverAppMock.class);
  private PacketSerializer cimdSerializer;
  private Socket socket;
  private String host;
  private int port;
  private String uid;
  private String pwd;
  private Phaser phaser;
  private String msgMatch;
  private int maxMessages;
  private AtomicInteger messageCount = new AtomicInteger();
  
  public ReceiverAppMock(String host, int port, String uid, String pwd, Phaser phaser, String msgMatch, int maxMessages) {
    this.host = host;
    this.port = port;
    this.uid = uid;
    this.pwd = pwd;
    this.phaser = phaser;
    this.msgMatch = msgMatch; 
    this.maxMessages = maxMessages;
    
    PacketSequenceNumberGenerator gen = new SmsCenterPacketSequenceNumberGenerator();
    cimdSerializer = new PacketSerializer("ser");
    cimdSerializer.setSequenceNumberGenerator(gen);
  }

  public void connect() throws ContingencyException {
    try {
      socket = new Socket(host, port);
      
      // CIMD login
      Packet p = new Packet(1,
          new Parameter(10, uid),
          new Parameter(11, pwd));
      cimdSerializer.serialize(p, socket.getOutputStream());

    } catch (IOException e) {
      throw new ContingencyException("failed to connect to CIMD simulator", e);
    }
  }

  public void start() throws Exception {
    logger.debug("start: "+uid);
    int pn = phaser.arriveAndAwaitAdvance();
    logger.debug("starting receiver: "+uid+", "+pn);
    // listen for messages
    while(messageCount.get() < maxMessages) {
      Packet p = cimdSerializer.deserialize(socket.getInputStream());
      if(p.getOperationCode() == Packet.OP_DELIVER_MESSAGE && getDeliveredMessage(p).contains(msgMatch)) {
        messageCount.addAndGet(1);
      }
      if(messageCount.get()%1000==0)
        logger.debug(String.format("%s: messages: %d (max: %d)", uid, messageCount.get(), maxMessages));
    }
    logger.debug("stopping: "+uid+": "+messageCount);
  }
  
  private String getDeliveredMessage(Packet packet) {
    for(Parameter p : packet.getParameters()) {
      if(Parameter.USER_DATA == p.getNumber()) {
        return p.getValue();
      }
    }
    return "";
  }
  
  public void disconnect() throws ContingencyException {
    logger.debug("disconnecting: "+uid);
    try {
      // CIMD logout
      Packet p = new Packet(2);
      cimdSerializer.serialize(p, socket.getOutputStream());
      socket.getOutputStream().flush();
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }

      socket.close();
    } catch (IOException e) {
      throw new ContingencyException("failed to disconnect from CIMD simulator", e);
    }
  }
  
  public Integer getMessageCount() {
    return messageCount.get();
  }
  
  public String getUid() {
    return uid;
  }
  
  public String getMessageMatch() {
    return msgMatch;
  }

}
