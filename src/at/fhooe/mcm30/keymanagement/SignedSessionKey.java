package at.fhooe.mcm30.keymanagement;

public class SignedSessionKey {

	public byte[] message;
	public byte[] signedHash;
	public boolean verified = false;
	
	public SignedSessionKey(byte[] _message, byte[] _signedHash) {
		message = _message;
		signedHash = _signedHash;
	}
	
	public SignedSessionKey(byte[] _message, boolean _verified) {
		message = _message;
		signedHash = null;
		verified = _verified;
	}
}
