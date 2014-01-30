package at.fhooe.mcm30.keymanagement;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public abstract class SessionKey implements Serializable {

	private static final long serialVersionUID = -4068275809337954214L;
	private static final int DEFAULT_KEY_LENGTH = 256;
	public static final int DEFAULT_MAX_COUNT = 3;
	private static final byte[] INITIALIZATION_VECTOR = new byte[] {0x00, 0x01, 0x02, 0x03,
		0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
	
	protected byte[] mSessionKey;
	protected int mMaxCount;
	protected int mCount;
	private SecretKey mSecretKey;
	private transient Cipher mCipher;
	
	
	public SessionKey() {
		mMaxCount = DEFAULT_MAX_COUNT;
		mSessionKey = createSessionKey();
		
		initCipher(mSessionKey);
	}
	
	public SessionKey(int _maxCount) {
		mMaxCount = _maxCount;
		mSessionKey = createSessionKey();
		
		initCipher(mSessionKey);
	}
	
	public void writeObject(ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
       out.defaultWriteObject();
    }
	
	 public void readObject(java.io.ObjectInputStream in)
			  throws IOException {
		
			try {
				in.defaultReadObject();
				initCipher(mSessionKey);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	}

	private byte[] createSessionKey() {
		KeyGenerator keygen;
		SecureRandom rand;
		
		try {
			keygen = KeyGenerator.getInstance("AES");
			rand = SecureRandom.getInstance("SHA1PRNG");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		rand.setSeed(INITIALIZATION_VECTOR);
		keygen.init(DEFAULT_KEY_LENGTH, rand);
		SecretKey skey = keygen.generateKey();
		
		return skey.getEncoded();
	}
	
	public void initCipher(byte[] _sessionKey) {
		try {
			
			mSecretKey = new SecretKeySpec(_sessionKey, "AES");
			mCipher = Cipher.getInstance("AES/CFB8/NoPadding");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getSessionKey() {
		return mSessionKey;
	}
	
	public byte[] getSessionKeyBase64() {
		return Base64.encode(mSessionKey, Base64.DEFAULT);
	}
	
	public int getMaxCount() {
		return mMaxCount;
	}
	
	public void setMaxCount(int _max) {
		mMaxCount = _max;
	}
	
	public int count() {
		return mCount;
	}
	
	/**
	 * encrypt plaintext with the cipher
	 * 
	 * @param plaintext
	 * @return encrypted text in base64 encoding 
	 */
	public byte[] encrypt(byte[] plain) {
		increaseCount();
		byte[] encrypted = null;

		try {
			mCipher.init(Cipher.ENCRYPT_MODE, mSecretKey, new IvParameterSpec(INITIALIZATION_VECTOR));
			encrypted = mCipher.doFinal(plain);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return Base64.encode(encrypted, Base64.DEFAULT);
	}

	/**
	 * decrypt the encrypted text with the cipher
	 * 
	 * @param encrypted text
	 * @return plain text
	 */
	public byte[] decrypt(byte[] encrypted) {
		byte[] decrypted = null;

		try {
			mCipher.init(Cipher.DECRYPT_MODE, mSecretKey, new IvParameterSpec(INITIALIZATION_VECTOR));
			decrypted = mCipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return decrypted;
	}
	
	public void renewSessionKey() {
		mCount = mMaxCount;
		
		mSessionKey = createSessionKey();
		initCipher(mSessionKey);
	}
	
	public void setNewSessionKey(byte[] _sessionKey) {
		mSessionKey = _sessionKey;
	}
	
	public void increaseCount() {
		if(--mCount<0)
			sessionKeyExpired();
	}
	
	abstract public void sessionKeyExpired();
}
