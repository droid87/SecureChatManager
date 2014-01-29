package at.fhooe.mcm30.concersation;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import at.fhooe.mcm30.keymanagement.SessionKey;
import at.fhooe.mcm30.keymanagement.SessionKeyExpired;

public class Conversation extends SessionKey implements Serializable {
	
	private static final long serialVersionUID = 191435905707678967L;
	private Contact mContact;
	private transient SessionKeyExpired mExpired;
	
	
	public void writeObject(ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
       out.defaultWriteObject();
    }
	
	
	 public void readObject(java.io.ObjectInputStream in)
			  throws IOException {
		 super.readObject(in);
//		try {
//			in.defaultReadObject();
//			
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public Conversation(Contact _contact) {
		mContact = _contact;
	}
	
	public Conversation(Contact _contact, int _maxCount) {
		mContact = _contact;
		mMaxCount = _maxCount;
	}
	
	public Contact getContact() {
		return mContact;
	}

	@Override
	public void increaseCount() {
		if(--mCount<0 && mExpired!=null)
			mExpired.sessionKeyExpired(this);
	}

	@Override
	public void registerExpiredSessionKey(SessionKeyExpired _expired) {
		mExpired = _expired;
	}
}
