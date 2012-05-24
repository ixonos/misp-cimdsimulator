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
import com.googlecode.jcimd.TextMessageUserDataFactory;
import com.googlecode.jcimd.UserData;

/**
 * Class for injecting text messages into the CIMD server sessions.
 * 
 * @author Ixonos / Marko Asplund
 */
public class MessageInjector {
	private static final Logger logger = LoggerFactory.getLogger(MessageInjector.class);
	private Map<Long, IoSession> cimdSessions;
	private static final boolean SKIP_ENCODE_MESSAGE_CONTENT = true;
	
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
		if(SKIP_ENCODE_MESSAGE_CONTENT)
			params.add(new Parameter(Parameter.USER_DATA, new StringUserData(text).getBody()));
		else
			addMessageContent(params, text);
		Packet p = new Packet(Packet.OP_DELIVER_MESSAGE, params.toArray(new Parameter[]{}));

		for(IoSession s : sessions)
			s.write(p);
		logger.debug("message delivered to "+sessions.size()+" sessions");
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
	
  private static void addMessageContent(List<Parameter> params, String message) {
		UserData[] data = TextMessageUserDataFactory.newInstance(message);
		if(data.length > 1)
			logger.warn("message length exceeds expected size: "+message);
		UserData userData = data[0];
		params.add(new Parameter(Parameter.DATA_CODING_SCHEME, userData.getDataCodingScheme()));
		if(userData.getHeader() != null)
			params.add(new Parameter(Parameter.USER_DATA_HEADER, userData.getHeader()));
		if(!userData.isBodyBinary())
			params.add(new Parameter(Parameter.USER_DATA, userData.getBody()));
		else
			params.add(new Parameter(Parameter.USER_DATA_BINARY, userData.getBinaryBody()));
	}
	
}
