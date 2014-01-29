package at.fhooe.mcm30.keymanagement;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import android.util.Base64;
import android.util.Log;

public class RSAKeyPair implements Serializable {
	
	private static final long serialVersionUID = -6972580425612234245L;
	private KeyPair mKeyPair;
		
	public static final int DEFAULT_KEY_SIZE = 2048;	
	
//	private byte[] encodedPublicKey;
//	private byte[] encodedPrivateKey;
	
	public RSAKeyPair() {
		mKeyPair = createRSAKeyPair(DEFAULT_KEY_SIZE);
//		encodedPrivateKey = mKeyPair.getPrivate().getEncoded();
//		encodedPublicKey = mKeyPair.getPublic().getEncoded();
		
//		Log.e("RSAKeyPair::", "method1: " + new String(Base64.encode(encodedPrivateKey, Base64.DEFAULT)));
	}
	
	public RSAKeyPair(int _keySize) {
		mKeyPair = createRSAKeyPair(_keySize);
//		encodedPrivateKey = mKeyPair.getPrivate().getEncoded();
//		encodedPublicKey = mKeyPair.getPublic().getEncoded();
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
			
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return kpg.genKeyPair();
	}
	
	public Key getPrivateKey() {
//		if ((mKeyPair == null) && (encodedPrivateKey != null) && (encodedPublicKey != null)) {
//			mKeyPair = new KeyPair(getPublicKeyFromEncoded(encodedPublicKey), getPrivateKeyFromEncoded(encodedPrivateKey));
//		}
		return mKeyPair.getPrivate();
	}
	
	public Key getPublicKey() {
//		if ((mKeyPair == null) && (encodedPrivateKey != null) && (encodedPublicKey != null)) {
//			mKeyPair = new KeyPair(getPublicKeyFromEncoded(encodedPublicKey), getPrivateKeyFromEncoded(encodedPrivateKey));
//		}
		return mKeyPair.getPublic();
	}
	
	
	
//	
//	public static PublicKey getPublicKeyFromEncoded(byte[] _pubKeyEncoded) {
//		try {
//			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(_pubKeyEncoded);
//			
//			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
//			
//			return pubKey;
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
//	public static PrivateKey getPrivateKeyFromEncoded(byte[] _privKeyEncoded) {
//		try {
//			X509EncodedKeySpec privKeySpec = new X509EncodedKeySpec(_privKeyEncoded);
//			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//			PrivateKey privateKey = keyFactory.generatePrivate(privKeySpec);
//			Log.e("RSAKeyPair::", "method2: " + new String(Base64.encode(privateKey.getEncoded(), Base64.DEFAULT)));
//			return privateKey;
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		} catch (InvalidKeySpecException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	
}
