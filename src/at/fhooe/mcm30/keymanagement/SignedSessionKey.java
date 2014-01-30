package at.fhooe.mcm30.keymanagement;

import java.io.Serializable;

public class SignedSessionKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7037732695067990383L;
	public byte[] message;
	public byte[] signature;
	
	public SignedSessionKey(byte[] _message, byte[] _signature) {
		message = _message;
		signature = _signature;
	}
}
