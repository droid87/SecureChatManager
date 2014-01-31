package at.fhooe.mcm30.keymanagement;

import java.io.Serializable;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class RSAKeyPair implements Serializable {

	private static final long serialVersionUID = -6972580425612234245L;
	private transient KeyPair mKeyPair;
	private byte[] mPublicKey;
	private byte[] mPrivateKey;

	public static final int DEFAULT_KEY_SIZE = 2048;

	public RSAKeyPair() {
		mKeyPair = createRSAKeyPair(DEFAULT_KEY_SIZE);
		mPublicKey = mKeyPair.getPublic().getEncoded();
		mPrivateKey = mKeyPair.getPrivate().getEncoded();
	}

	public RSAKeyPair(int _keySize) {
		mKeyPair = createRSAKeyPair(_keySize);
		mPublicKey = mKeyPair.getPublic().getEncoded();
		mPrivateKey = mKeyPair.getPrivate().getEncoded();
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
		if(mKeyPair==null)
			mKeyPair = createKeyPair(mPrivateKey, mPublicKey);
		
		return mKeyPair.getPrivate();
	}

	public Key getPublicKey() {
		if(mKeyPair==null)
			mKeyPair = createKeyPair(mPrivateKey, mPublicKey);
		
		return mKeyPair.getPublic();
	}

	public KeyPair createKeyPair(byte[] encodedPrivateKey,
			byte[] encodedPublicKey) {
		try {
			EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					encodedPrivateKey);
			KeyFactory generator = KeyFactory
					.getInstance("RSA");
			PrivateKey privateKey = generator.generatePrivate(privateKeySpec);

			EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
					encodedPublicKey);
			PublicKey publicKey = generator.generatePublic(publicKeySpec);
			return new KeyPair(publicKey, privateKey);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"Failed to create KeyPair from provided encoded keys", e);
		}
	}
}
