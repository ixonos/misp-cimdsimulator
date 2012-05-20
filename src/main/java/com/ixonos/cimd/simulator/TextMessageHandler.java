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

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.StringUserData;

/**
 * Protocol handler that receives text messages in CSV format via socket and delivers them to CIMD applications.
 * 
 * @author Marko Asplund
 */
public class TextMessageHandler extends IoHandlerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
	private Map<Long, IoSession> cimdSessions;
	
	public TextMessageHandler(Map<Long, IoSession> managedSessions) {
		cimdSessions = managedSessions;
  }

	@Override
  public void messageReceived(IoSession session, Object message) throws Exception {
		String msg = (String)message;
		logger.debug("msg: "+msg);
		
		for(Map.Entry<Long, IoSession> se : cimdSessions.entrySet()) {
			String userId = (String)se.getValue().getAttribute("USER_ID");

			List<Parameter> params = new LinkedList<Parameter>();
			params.add(new Parameter(Parameter.DESTINATION_ADDRESS, "040123"));
			params.add(new Parameter(Parameter.ORIGINATING_ADDRESS, "040456"));
			params.add(new Parameter(Parameter.MC_TIMESTAMP, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
			params.add(new Parameter(Parameter.USER_DATA, new StringUserData("hello, world").getBody()));
			Packet p = new Packet(Packet.OP_DELIVER_MESSAGE, params.toArray(new Parameter[]{}));
			se.getValue().write(p);
		}
		
	}

}
