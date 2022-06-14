/*
 *
 * Copyright (C) HeonJik, KIM
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


package kr.graha.app.lib;

import java.util.Formatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5, SHA-512 암호화 유틸리티
 * @author HeonJik, KIM
 * @version 0.5
 * @since 0.5
 */

public class Digest {
	private static volatile Digest INSTANCE;
	public static Digest getInstance() {
		if(INSTANCE == null) {
			synchronized(Digest.class) {
				if(INSTANCE == null)
					INSTANCE = new Digest();
			}
		}
		return INSTANCE;
	}
	private Digest() {
	}
	private String digest(String plain, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm); 
		md.update(plain.getBytes()); 
		byte[] b = md.digest();
	    return hex(b);
	}
	private String hex(byte[] b) {
		Formatter formatter = new Formatter();
		for(int i = 0 ; i < b.length ; i++) {
			formatter.format("%02x", b[i]);
		}
		String hash = formatter.toString();
		formatter.close();
		return hash;
	}
	public String md5(String plain) throws NoSuchAlgorithmException {
		return digest(plain, "MD5");
	}
	public String sha512(String plain) throws NoSuchAlgorithmException {
		return digest(plain, "SHA-512");
	}
}
