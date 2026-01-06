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


package kr.graha.app.downloader;

import java.nio.file.Path;
import kr.graha.helper.LOG;
import javax.servlet.http.HttpServletRequest;
import kr.graha.post.lib.Record;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.graha.post.interfaces.FileDownloader;
import kr.graha.app.lib.Digest;
import java.security.NoSuchAlgorithmException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Graha(그라하) FileDownloader 구현체
 *
 * mod_auth_token.so 에서 처리하도록 Redirect 한다.
 *
 * mod_auth_token-1.0.6 를 설치하였고,
 * httpd.conf 의 VirtualHost 에 다음과 같이 설정했다고 가정한다. 
 
 * JkUnMount /download/* balancer
 * <Location /download/>
 * 	AuthTokenSecret "secret"
 * 	AuthTokenPrefix /download/
 * 	AuthTokenTimeout 300
 * 	AuthTokenLimitByIp on
 * </Location>
 
 * Graha 의 XML 정의파일에는 다음과 같은 prop 설정을 추가한다.
 
 * <prop name="mod_auth_token.AuthTokenSecret" value="secret" />
 * <prop name="mod_auth_token.AuthTokenPrefix" value="/download/" />
 * <prop name="mod_auth_token.prefix" value="/file/memo/${query.memo.memo_id}" />
 
 * mod_auth_token.prefix 값은 https://github.com/logicielkr/memo 기준이며 적절히 수정해야 한다.
 * <file> 의 path 속성값에서 %{DocumentRoot}/download 아래에 Symbolic Link 걸려 있는 부분의 앞 부분을 제거한 값이면 된다.
 
 * AuthTokenSecret = "secret" 은 반드시 변경한다. 
 
 * Graha 의 XML 정의파일의 file 요소에 다음과 같이 downloader 속성을 추가한다.
 
 * <file ... downloader="kr.graha.app.downloader.FileDownloaderModAuthTokenImpl" />
 
 * @author HeonJik, KIM
 * @version 0.5
 * @since 0.1
 */


public class FileDownloaderModAuthTokenImpl implements FileDownloader {
	public void execute(
		Path path,
		String fileName,
		HttpServletRequest request,
		HttpServletResponse response,
		Record params
	) throws IOException {
		String authTokenSecret = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mod_auth_token.AuthTokenSecret")); 
		String authTokenPrefix = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mod_auth_token.AuthTokenPrefix"));
		String hexTimeNow = Long.toHexString(System.currentTimeMillis() / 1000L);
		String prefix = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "mod_auth_token.prefix"));
		if(prefix.endsWith("/")) {
		} else {
			prefix = prefix + "/";
		}
		Digest digest = Digest.getInstance();
		try {
			String token = digest.md5(new String[]{authTokenSecret, prefix + fileName, hexTimeNow, request.getRemoteAddr()});
			LOG.debug(authTokenPrefix  + token + "/" + hexTimeNow + prefix + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20"));
			response.sendRedirect(authTokenPrefix  + token + "/" + hexTimeNow + prefix + URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20"));
		} catch (NoSuchAlgorithmException e) {
			LOG.severe(e);
		}
	}
}
