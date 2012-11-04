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
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;

import com.ixonos.cimd.InvalidMessageFormatException;

/**
 * Protocol handler that receives text messages in CSV format via socket and
 * delivers them to CIMD applications. Allows text messages to be injected into
 * the CIMD server. Message format
 * 
 * <delivery count>,<receiver uid>,<destination address>,<origin
 * address>,<message text>
 * 
 * for example:
 * 
 * 555,*,123,456,test33
 * 1000,rlu,123,456,"hello, world #%d"
 * 
 * %d in message content will be replaced with the current message count.
 * 
 * @author Ixonos / Marko Asplund
 */
public class TextMessageHandler extends IoHandlerAdapter {
  private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
  private MessageInjector messageInjector;
  private Long messageInjectSleepTimeMillis;

  public TextMessageHandler(Map<Long, IoSession> cimdSessions, Long messageInjectSleepTimeMillis) {
    messageInjector = new MessageInjector(cimdSessions);
    this.messageInjectSleepTimeMillis = messageInjectSleepTimeMillis;
  }

  @Override
  public void messageReceived(IoSession session, Object message) throws Exception {
    String msgString = (String) message;
    logger.debug("msg: " + msgString);

    DeliveryRequest msg = DeliveryRequest.parseMessage(msgString);

    List<IoSession> receivers = messageInjector.getSessions(msg.getReceiverUid());
    if(receivers.size() == 0) {
      logger.warn("no receivers found: "+msg.getReceiverUid());
      return;
    }
    for (int i = 0; i < msg.getCount(); i++) {
      if(messageInjectSleepTimeMillis != null)
        Thread.sleep(messageInjectSleepTimeMillis);
      String text = msg.getText().replaceFirst("%d", String.valueOf(i));
      messageInjector.injectMessage(receivers, msg.getDestination(), msg.getOrigin(), text);
    }
    logger.debug(String.format("%d messages injected to %d sessions", msg.getCount(), receivers.size()));
  }

  private static class DeliveryRequest {
    private static final CSVParser csvParser = new CSVParser();
    private Integer count;
    private String receiverUid;
    private String destination;
    private String origin;
    private String text;

    public DeliveryRequest(Integer count, String receiverUid, String destination, String origin, String text) {
      this.count = count;
      this.receiverUid = receiverUid;
      this.destination = destination;
      this.origin = origin;
      this.text = text;
    }

    public static DeliveryRequest parseMessage(String msg) throws InvalidMessageFormatException {
      String[] data = null;
      Integer count = null;
      try {
        data = csvParser.parseLine(msg);
        if (data == null || data.length != 5)
          throw new InvalidMessageFormatException("Expecting 5 field values: " + msg);
        if (data[1].length() == 0 || data[2].length() == 0 || data[4].length() == 0)
          throw new InvalidMessageFormatException("Field values should be non-empty: " + msg);
        count = Integer.valueOf(data[0]);
        return new DeliveryRequest(count, data[1], data[2], data[3], data[4]);
      } catch (NumberFormatException ex) {
        throw new InvalidMessageFormatException("invalid message count: " + msg, ex);
      } catch (IOException ex) {
        throw new InvalidMessageFormatException("failed to parse CSV data: " + msg, ex);
      }
    }

    public Integer getCount() {
      return count;
    }

    public String getReceiverUid() {
      return receiverUid;
    }

    public String getDestination() {
      return destination;
    }

    public String getOrigin() {
      return origin;
    }

    public String getText() {
      return text;
    }

  }

}
