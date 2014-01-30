package at.fhooe.mcm30.keymanagement;

public class SignedSessionKey {

	public byte[] message;
	public byte[] signature;
	
	public SignedSessionKey(byte[] _message, byte[] _signature) {
		message = _message;
		signature = _signature;
	}
}
