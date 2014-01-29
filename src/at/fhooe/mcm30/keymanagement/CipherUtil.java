package at.fhooe.mcm30.keymanagement;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

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
	
	public static byte[] signData(byte[] _data, Key _privateKey) {
		Signature signer;
		try {
			signer = Signature.getInstance("SHA1withRSA");
			signer.initSign((PrivateKey)_privateKey, new SecureRandom()); //Where do you get the key?
			signer.update(_data, 0, _data.length);
			
			return signer.sign();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}
	
	public static boolean verifyData(byte[] _data, byte[] _sigBytes, Key _publicKey){
        Signature signature;
		try {
			signature = Signature.getInstance("SHA1withRSA");
			signature.initVerify((PublicKey) _publicKey);
	        signature.update(_data);
	        
	        return signature.verify(_sigBytes);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return false;
	}
}
