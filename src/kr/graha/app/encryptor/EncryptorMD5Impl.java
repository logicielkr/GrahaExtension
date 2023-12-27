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


package kr.graha.app.encryptor;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import kr.graha.post.interfaces.Encryptor;
import kr.graha.app.lib.Digest;

/**
 * MD5 암호화(그라하(Graha) Encryptor 구현체)
 * 암호화한다.  복호화 함수는 항상 null 을 리턴한다.

 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Encryptor
 
 * @version 0.9
 * @since 0.9
 */

public class EncryptorMD5Impl implements Encryptor {
	
/**
 * 암호화한다.
 * @param plain 암호화할 평문 문자열
 * @return MD5 로 암호화 된 문자열
 */
	@Override
	public String encrypt(String plain) throws NoSuchProviderException {
		Digest digest = Digest.getInstance();
		try {
			return digest.md5(plain);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
/**
 * 복호화한다.  항상 null 을 리턴한다.
 * @param encrypted 암호화 된 문자열
 * @return null
 */
	@Override
	public String decrypt(String encrypted) throws NoSuchProviderException {
		return null;
	}
}

