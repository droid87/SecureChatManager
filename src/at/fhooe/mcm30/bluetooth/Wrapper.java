package at.fhooe.mcm30.bluetooth;

import java.io.Serializable;

public class Wrapper implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5141216259715443256L;

	public enum MessageCodes {
		CONTACT,
		SIGNED_SESSIONKEY,
		CHAT_MESSAGE,
		ACK,
		NACK
	}
	
	public MessageCodes messageCode;
	public Object messageObject;
	
	public Wrapper(MessageCodes _messageCode, Object _messageObject) {
		messageCode = _messageCode;
		messageObject = _messageObject;
	}

}
