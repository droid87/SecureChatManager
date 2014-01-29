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
	private transient Key mPublicKey;
	private byte[] mPublicKeyEncoded;
	
	
	public Contact(String _name, String _btAddress, Key _publicKey) {
		mName = _name;
		mBTAddress = _btAddress;
		mPublicKey = _publicKey;
		
		mPublicKeyEncoded = _publicKey.getEncoded();		
	}
	
	public String getName() {
		return mName;
	}
	
	public String getBTAddress() {
		return mBTAddress;
	}

	public Key getPuKey() {
		if (mPublicKeyEncoded != null) {
			mPublicKey = getPublicKeyFromEncoded(mPublicKeyEncoded);
		}
		return mPublicKey;
	}
	
	public static Key getPublicKeyFromEncoded(byte[] _pubKeyEncoded) {
		try {
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(_pubKeyEncoded);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			Key pubKey = keyFactory.generatePublic(pubKeySpec);
			
			return pubKey;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String toString() {
		return  "Name: " + mName + "\n" +
				"Bluetooth-MAC: " + mBTAddress + "\n" +
				"Public-Key: " + getPuKey().toString();
	}
}
