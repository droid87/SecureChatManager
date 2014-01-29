package at.fhooe.mcm30.concersation;

import java.io.Serializable;
import java.security.Key;
import java.security.PublicKey;

public class Contact implements Serializable {
	
	private static final long serialVersionUID = 8100688396248257275L;
	private String mName;
	private String mBTAddress;
	private transient Key mPublicKey;
	
	
	
	public Contact(String _name, String _btAddress, Key _publicKey) {
		mName = _name;
		mBTAddress = _btAddress;
		mPublicKey = _publicKey;
//		mPublicKey.getEncoded()
//		PublicKey key = 
	}
	
	public String getName() {
		return mName;
	}
	
	public String getBTAddress() {
		return mBTAddress;
	}

	public Key getPuKey() {
		return mPublicKey;
	}
}
