package at.fhooe.mcm30.concersation;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;


public class Contact implements Serializable {
	
	private static final long serialVersionUID = 8100688396248257275L;
	private String mName;
	private String mBTAddress;
	private String mWifiMacAddress;
	private transient Key mPublicKey;
	private byte[] mPublicKeyEncoded;
	
	
	public Contact(String _name, String _btAddress, String macAddress, Key _publicKey) {
		mName = _name;
		mBTAddress = _btAddress;
		mWifiMacAddress = macAddress;
		mPublicKey = _publicKey;
		
		mPublicKeyEncoded = mPublicKey.getEncoded();
	}
	
	public String getName() {
		return mName;
	}
	
	public String getBTAddress() {
		return mBTAddress;
	}
	
	public String getWifiMacAddress() {
		return mWifiMacAddress;
	}

	public Key getPuKey() {
		if (mPublicKey == null) {			
			try {
				X509EncodedKeySpec spec = new X509EncodedKeySpec(mPublicKeyEncoded);
				KeyFactory factory = KeyFactory.getInstance("RSA");
				mPublicKey = factory.generatePublic(spec);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
			
		}
		
		return mPublicKey;
	}
	
	@Override
	public String toString() {
		return  "Name: " + mName + "\n" +
				"Bluetooth-MAC: " + mBTAddress + "\n" +
				"Wifi-MAC: " + mWifiMacAddress + "\n" +
				"Public-Key: " + getPuKey().toString();
	}
}
