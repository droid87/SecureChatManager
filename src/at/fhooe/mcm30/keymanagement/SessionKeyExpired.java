package at.fhooe.mcm30.keymanagement;

import java.io.Serializable;

import at.fhooe.mcm30.concersation.Conversation;


public interface SessionKeyExpired extends Serializable {

	public void sessionKeyExpired(Conversation _conversation);
}
