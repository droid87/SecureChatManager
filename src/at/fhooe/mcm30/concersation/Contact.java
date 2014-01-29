package at.fhooe.mcm30.concersation;

import java.io.Serializable;
import java.security.Key;

public class Contact implements Serializable {
	
	private static final long serialVersionUID = 8100688396248257275L;
	private String mName;
	private String mBTAddress;
	private Key mPublicKey;
	
	
	public Contact(String _name, String _btAddress, Key _publicKey) {
		mName = _name;
		mBTAddress = _btAddress;
		mPublicKey = _publicKey;	
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
	
	@Override
	public String toString() {
		return  "Name: " + mName + "\n" +
				"Bluetooth-MAC: " + mBTAddress + "\n" +
				"Public-Key: " + getPuKey().toString();
	}
}
