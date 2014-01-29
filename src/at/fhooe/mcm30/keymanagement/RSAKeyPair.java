package at.fhooe.mcm30.keymanagement;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKeyPair implements Serializable {
	
	private static final long serialVersionUID = -6972580425612234245L;
	private transient KeyPair mKeyPair;
	public static final int DEFAULT_KEY_SIZE = 4096;
	
	
	public RSAKeyPair() {
		mKeyPair = createRSAKeyPair(DEFAULT_KEY_SIZE);
	}
	
	public RSAKeyPair(int _keySize) {
		mKeyPair = createRSAKeyPair(_keySize);
	}

	/**
	 * Simply creates a RSA Keypair
	 * 
	 * @return keypair
	 */
	private KeyPair createRSAKeyPair(int _size) {
		KeyPairGenerator kpg = null;
		
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(_size);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return kpg.genKeyPair();
	}
	
	public Key getPrivateKey() {
		return mKeyPair.getPrivate();
	}
	
	public Key getPublicKey() {
		return mKeyPair.getPublic();
	}
	
	
}
