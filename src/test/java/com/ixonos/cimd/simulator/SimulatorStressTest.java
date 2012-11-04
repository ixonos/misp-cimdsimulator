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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
//import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for stress testing CIMD simulator protocol communication.
 * 
 * @author Ixonos / Marko Asplund
 */
public class SimulatorStressTest {
  private static final Logger logger = LoggerFactory.getLogger(SimulatorStressTest.class);
  private String host = "127.0.0.1";
  private int cimdPort = 9071;
  private int msgPort = 9072;
  private int shutdownWaitTime = 90; // seconds

  private ExecutorService executorService;

  @Test
  public void doStressTest() throws Exception {
    int numClients = 10;
    int numMessages = 30000;
    executorService = Executors.newFixedThreadPool(numClients);
    
    Phaser phaser = new Phaser(numClients+1);

    // start simulator
    CIMDSimulator sim = new CIMDSimulator(cimdPort, msgPort, null);
    sim.start();
    
    List<ReceiverAppMock> receivers = new ArrayList<ReceiverAppMock>(numClients);
    Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>(numClients);
    StringBuilder messages = new StringBuilder();
    List<Integer> expectedCounts = new ArrayList<Integer>(numClients);
    for(int i = 0; i < numClients; i++) {
      String messageMatch = String.format("hello %s:", "user"+i);
      int expectedMessages = numMessages-(i*3);
      ReceiverAppMock r = new ReceiverAppMock(host, cimdPort, "user"+i, "pwd"+i, phaser,
          messageMatch, expectedMessages);
      receivers.add(r);
      r.connect();
      tasks.add(new CIMDReceiverCallable(r));
      messages.append(String.format("%d,%s,111,222,%s\n", expectedMessages, "user"+i, messageMatch+"%d"));
      expectedCounts.add(expectedMessages);
    }

    logger.info("starting apps");
    List<Future<Integer>> results = new ArrayList<Future<Integer>>(numClients);
    for(Callable<Integer> t : tasks)
      results.add(executorService.submit(t));

    phaser.arriveAndAwaitAdvance();
    logger.info("sending messages to CIMD simulator: ");
    logger.info("messages: "+messages.toString());
    Socket s = new Socket(host, msgPort);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    bw.write(messages.toString());
    bw.flush();

    // wait for the receiver apps to finish
    logger.info("starting receiver app shutdown, waiting for "+shutdownWaitTime+"s");
    executorService.shutdown();
    executorService.awaitTermination(shutdownWaitTime, TimeUnit.SECONDS);
    logger.info("finished receiver app shutdown");

    s.close();

    logger.info("receiver app stats: ");
    for(int i = 0; i < receivers.size(); i++) {
      ReceiverAppMock a = receivers.get(i);
      Future<Integer> r = results.get(i);
      logger.info(String.format("receiver: %s: isDone: %b, messages: %d, expected: %d", a.getUid(),
          r.isDone(), a.getMessageCount(), expectedCounts.get(i)));
      receivers.get(i).disconnect();
//      assertEquals(expectedCounts.get(i), a.getMessageCount());
    }

    sim.stop();
    
  }

  
  private static class CIMDReceiverCallable implements Callable<Integer> {
    private ReceiverAppMock receiver;
    public CIMDReceiverCallable(ReceiverAppMock receiver) {
      this.receiver = receiver;
    }
    @Override
    public Integer call() throws Exception {
      logger.debug("run()");
      receiver.start();
      return receiver.getMessageCount();
    }
  }
  
}
