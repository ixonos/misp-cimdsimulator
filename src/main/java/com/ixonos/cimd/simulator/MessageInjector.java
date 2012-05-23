package com.ixonos.cimd.simulator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.Parameter;
import com.googlecode.jcimd.StringUserData;


public class MessageInjector {
	@SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(MessageInjector.class);
	private Map<Long, IoSession> cimdSessions;
	
	public MessageInjector(Map<Long, IoSession> managedSessions) {
		cimdSessions = managedSessions;
  }

	/*
	 * deliver message to CIMD sessions created with the specified session UID.
	 */
	public void injectMessage(String receiverUid, String destination, String origin, String text) {
		injectMessage(getSessions(receiverUid), destination, origin, text);
	}

	/*
	 * deliver message to specified target CIMD sessions
	 */
	public void injectMessage(List<IoSession> sessions, String destination, String origin, String text) {
		List<Parameter> params = new LinkedList<Parameter>();
		params.add(new Parameter(Parameter.DESTINATION_ADDRESS, destination));
		params.add(new Parameter(Parameter.ORIGINATING_ADDRESS, origin));
		params.add(new Parameter(Parameter.MC_TIMESTAMP, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
		params.add(new Parameter(Parameter.USER_DATA, new StringUserData(text).getBody()));
		Packet p = new Packet(Packet.OP_DELIVER_MESSAGE, params.toArray(new Parameter[]{}));

		for(IoSession s : sessions)
			s.write(p);
	}

	public List<IoSession> getSessions(String uid) {
		List<IoSession> sessions = new ArrayList<IoSession>();
		
		for(Map.Entry<Long, IoSession> se : cimdSessions.entrySet()) {
			String sessionUserId = (String)se.getValue().getAttribute(SessionAttribute.USER_ID);
			if("*".equals(uid) || uid.equals(sessionUserId))
				sessions.add(se.getValue());
		}
		return sessions;
	}
	
}
