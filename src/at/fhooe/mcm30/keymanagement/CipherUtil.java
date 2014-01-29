package at.fhooe.mcm30.keymanagement;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import android.util.Base64;

public class CipherUtil {

	
	/**
	 * encrypt the plaintext with the RSA key
	 * 
	 * @param rsa key
	 * @param plaintext
	 * @return encrypted text in base64 encoding
	 */
	public static byte[] encryptRSA(Key rsaKey, byte[] plain) {
		  Cipher cipher;
		  byte[] encryptedByteData = null;
		  
		  try {
			  cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			  cipher.init(Cipher.ENCRYPT_MODE, rsaKey);      
			  encryptedByteData = cipher.doFinal(plain);
				  
		  } catch (Exception e) {
			  e.printStackTrace();
			  return null;
		  }    
		  
		  return Base64.encode(encryptedByteData, Base64.DEFAULT);
	}
	
	/**
	 * decrypt the decrypted text into plaintext
	 * 
	 * @param rsaKey
	 * @param encrypted text
	 * @return plaintext
	 */
	public static byte[] decryptRSA(Key rsaKey, byte[] encrypted) {
		Cipher cipher;
		byte[] encryptedByteData = null;
		  
		try {
			cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, rsaKey);      
			encryptedByteData = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
	
		} catch (Exception e) {
			e.printStackTrace();
		}    
		  
		return encryptedByteData;
	}
	
	public static byte[] getHash(byte[] _data, Key _privateKey){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
			return null;
		}
        md.update(_data, 0, _data.length);
        
        return encryptRSA(_privateKey, md.digest());
	}
	
	public static byte[] getHash(byte[] _data){
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			
			return null;
		}
        md.update(_data, 0, _data.length);
        
        return md.digest();
	}
}
