package at.fhooe.mcm30.wifip2p;

import java.math.BigInteger;
import java.util.Locale;

public class MacAddressHelper {
	
	private static final String HEXES = "0123456789abcdef";
	
	public static String changeMacAddress(String macAddress)
    {
		StringBuffer sb = new StringBuffer();
        String[] bytes = macAddress.split(":");
        BigInteger temp = new BigInteger(bytes[0], 16);
        byte[] raw = temp.toByteArray();
        byte firstByte = raw[raw.length - 1];
        firstByte = (byte) ((byte) firstByte + (byte) 2);
        sb.append(getHex(firstByte));

        for (int i = 1; i < bytes.length; i++)
        {
            sb.append(":" + bytes[i]);
        }
        return sb.toString().toLowerCase();
    }


	private static String getHex(byte raw) {
		final StringBuilder hex = new StringBuilder(2);
		hex.append(HEXES.charAt((raw & 0xF0) >> 4)).append(
				HEXES.charAt((raw & 0x0F)));
		return hex.toString();
	}
}
