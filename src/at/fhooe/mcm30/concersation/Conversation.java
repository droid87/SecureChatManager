package at.fhooe.mcm30.concersation;

import java.io.Serializable;

import at.fhooe.mcm30.keymanagement.SessionKey;
import at.fhooe.mcm30.keymanagement.SessionKeyExpired;

public class Conversation implements Serializable {
	
	private static final long serialVersionUID = 191435905707678967L;
	private Contact mContact;
	private SessionKey mSessionKey;
	private transient SessionKeyExpired mExpired;
	
	
	public Conversation(Contact _contact) {
		mContact = _contact;
		mSessionKey = new SessionKey() {
			private static final long serialVersionUID = -4609454401908851459L;

			@Override
			public void sessionKeyExpired() {
				expired();
			}
		};
	}
	
	public Conversation(Contact _contact, byte[] _sessionKey) {
		mContact = _contact;
		
		mSessionKey = new SessionKey(_sessionKey) {
			private static final long serialVersionUID = -4609454401908851459L;

			@Override
			public void sessionKeyExpired() {
				expired();
			}
		};
	}
	
	public Conversation(Contact _contact, int _maxCount) {
		mContact = _contact;
		mSessionKey = new SessionKey(_maxCount) {
			private static final long serialVersionUID = 5975565014965623139L;

			@Override
			public void sessionKeyExpired() {
				expired();
			}
		};
	}
	
	private void expired() {
		if(mExpired!=null)
			mExpired.sessionKeyExpired(this);
	}
	
	public Contact getContact() {
		return mContact;
	}
	
	public byte[] getSessionKey() {
		return mSessionKey.getSessionKey();
	}
	
	public byte[] getSessionKeyBase64() {
		return mSessionKey.getSessionKeyBase64();
	}
	
	public void initCipher(byte[] _sessionKey) {
		mSessionKey.initCipher(_sessionKey);
	}
	
	public void registerExpiredSessionKey(SessionKeyExpired _expired) {
		mExpired = _expired;
	}
	
	public void renewSessionKey()  {
		mSessionKey.renewSessionKey();
	}
	
	public void setNewSessionKey(byte[] _newSessionKey) {
		mSessionKey.setNewSessionKey(_newSessionKey);
	}
	
	public byte[] encrypt(byte[] _plain) {
		return mSessionKey.encrypt(_plain);
	}
	
	public byte[] decrypt(byte[] _encrypted) {
		return mSessionKey.decrypt(_encrypted);
	}
}
