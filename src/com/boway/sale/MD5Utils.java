package com.boway.sale;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
	
	protected static char hexDigitsByte[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
		"a", "b", "c", "d", "e", "f" };
	
	private static MessageDigest messageDegest = null;
	
	public static String encode(String origin) {
		if(origin == null)
			return null;
		String resultString = null;

		resultString = new String(origin);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return resultString;
	}
	
	private static String byteArrayToHexString(byte[] b) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			buf.append(byteToHexString(b[i]));
		}
		return buf.toString();
	}
	
	private static String byteToHexString(byte b) {
		return hexDigits[(b & 0xf0) >> 4] + hexDigits[b & 0x0f];
	}
	
	static {
		try {
			messageDegest = MessageDigest.getInstance("MD5" );
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public static String getMD5String(String str) {
		return getMD5String(str.getBytes());
	}
	
	public static String getMD5String(byte [] bytes) {
		messageDegest.update(bytes);
		return bufferToHex(messageDegest.digest());
	}
	
	private static String bufferToHex(byte [] bytes) {
		return bufferToHex(bytes, 0, bytes.length);
	}
	
	private static String bufferToHex(byte [] bytes, int m, int lenght) {
		StringBuffer stringBuffer = new StringBuffer(2 * lenght);
		int k = m + lenght;
		for (int i = m; i < k; i ++) {
			appendHexPair(bytes[i], stringBuffer);
		}
		return stringBuffer.toString();
	}
	
	private static void appendHexPair(byte bytes, StringBuffer stringBuffer) {
		char char0 = hexDigitsByte[(bytes & 0xf0) >> 4];
		char char1 = hexDigitsByte[bytes & 0xf];
		stringBuffer.append(char0);
		stringBuffer.append(char1);
	}
	
}
