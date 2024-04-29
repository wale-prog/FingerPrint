package com.cloudpos.util;

import java.util.Scanner;

/**
 *  @author john
 *  Convert byte[] to hex string
 * */
public class ByteConvertStringUtil {
	/**
	 *  Change byte to int. Then use Integer.toHexString(int) change to Hex String.
	 *  
	 *  @param src byte[] data  
	 *  @param hex string  
	 *  
	 * */
	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
	    if (src == null || src.length <= 0) {  
	        return null;  
	    }  
	    for (int i = 0; i < src.length; i++) {  
	        int v = src[i] & 0xFF;  
	        String hv = Integer.toHexString(v);
	        if (hv.length() < 2) {  
	            stringBuilder.append(0);  
	        }  
	        stringBuilder.append(hv);  
	    }  
	    return stringBuilder.toString();  
	}
	
	public static String byteToHexString(byte src){
		StringBuilder stringBuilder = new StringBuilder("");
		int v = src & 0xFF;
		String hv = Integer.toHexString(v);
	    return hv;  
	}
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		System.out.println("input:");
//		String input = scan.next();
		Byte input = scan.nextByte();
		System.out.println(":"+byteToHexString(input));
	}
	/**
	 * int to byte[]
	 */
	public static byte[] intToBytes(int i) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (i & 0xff);
		bytes[1] = (byte) ((i >> 8) & 0xff);
		bytes[2] = (byte) ((i >> 16) & 0xff);
		bytes[3] = (byte) ((i >> 24) & 0xff);
		return bytes;
	}

}
