package com.ixonos.cimd.simulator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.StringUserData;
import com.ixonos.cimd.InvalidMessageFormatException;

/**
 * Protocol handler that receives text messages in CSV format via socket and delivers them to CIMD applications.
 * 
 * @author Marko Asplund
 */
public class TextMessageHandler extends IoHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
	private static final CSVParser csvParser = new CSVParser();
	private Map<Long, IoSession> cimdSessions;
	private boolean deliverToReceiverOnly = false;
	
	public TextMessageHandler(Map<Long, IoSession> managedSessions) {
		cimdSessions = managedSessions;
  }

	@Override
  public void messageReceived(IoSession session, Object message) throws Exception {
		String msg = (String)message;
		logger.debug("msg: "+msg);
		
		String[] data = csvParser.parseLine(msg);
		validateMessage(msg, data);
		String receiverUid = data[0];
		String destination = data[1];
		String origin = data[2];
		String text = data[3];
		
		for(Map.Entry<Long, IoSession> se : cimdSessions.entrySet()) {
			String sessionUserId = (String)se.getValue().getAttribute("USER_ID");
			if(deliverToReceiverOnly && !receiverUid.equals(sessionUserId))
				continue;

			List<Parameter> params = new LinkedList<Parameter>();
			params.add(new Parameter(Parameter.DESTINATION_ADDRESS, destination));
			params.add(new Parameter(Parameter.ORIGINATING_ADDRESS, origin));
			params.add(new Parameter(Parameter.MC_TIMESTAMP, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
			params.add(new Parameter(Parameter.USER_DATA, new StringUserData(text).getBody()));
			Packet p = new Packet(Packet.OP_DELIVER_MESSAGE, params.toArray(new Parameter[]{}));
			se.getValue().write(p);
		}
	}
	
	private void validateMessage(String msg, String[] data) throws InvalidMessageFormatException {
		if(data == null || data.length != 4)
			throw new InvalidMessageFormatException("Expecting 4 field values: "+msg);
		if(data[0].length() == 0 || data[1].length() == 0 || data[3].length() == 0)
			throw new InvalidMessageFormatException("Field values should be non-empty: "+msg);
	}

}
