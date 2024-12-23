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

package kr.graha.app.qrcode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.graha.post.interfaces.Processor;
import kr.graha.post.lib.Record;
import kr.graha.helper.LOG;
import java.sql.Connection;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import java.io.IOException;
import kr.graha.post.model.utility.TextParser;

/**
 * QRCode 이미지를 생성한다.
 * 
 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Processor;
 
 * @version 0.9
 * @since 0.9
 */
public class QRCodeProcessorImpl implements Processor {
	
	public QRCodeProcessorImpl() {
		
	}

/**
 * Graha 가 호출하는 메소드
 * QRCode 를 생성한다.
 
 * @param request HttpServlet 에 전달된 HttpServletRequest
 * @param response HttpServlet 에 전달된 HttpServletResponse
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param con 데이타베이스 연결(Connection)

 * @see javax.servlet.http.HttpServletRequest (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletRequest (Apache Tomcat 10 이상)
 * @see javax.servlet.http.HttpServletResponse (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletResponse (Apache Tomcat 10 이상)
 * @see kr.graha.post.lib.Record 
 * @see java.sql.Connection 
 */

	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Connection con) {
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.filepath"))) {
			LOG.severe("prop.qrcode.filepath is missing");
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.1000001");
			return;
		}
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.contents"))) {
			LOG.severe("prop.qrcode.contents is missing");
			params.put(Record.key(Record.PREFIX_TYPE_ERROR, "error"), "message.1000002");
			return;
		}
		String filePath = TextParser.parse(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.filepath")), params);
		String contents = TextParser.parse(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.contents")), params);
		int width = 1000;
		int height = 1000;
		String format = "PNG";
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.width"))) {
			height = params.getInt(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.width"));
		}
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.height"))) {
			height = params.getInt(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.height"));
		}
		if(params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.format"))) {
			format = params.getString(Record.key(Record.PREFIX_TYPE_PROP, "qrcode.format"));
		}
		try {
			this.generate(filePath, contents, width, height, format);
		} catch (WriterException | IOException e) {
			LOG.severe(e);
		}
	}
	public void generate(String filePath, String contents, int width, int height, String format) throws WriterException, IOException {
		Path path = Paths.get(filePath);
		if(!Files.exists(path.getParent())) {
			Files.createDirectories(path.getParent());
		}
		this.generate(path, contents, width, height, format);
	}
	public void generate(Path path, String contents, int width, int height, String format) throws WriterException, IOException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(
			contents,
			BarcodeFormat.QR_CODE,
			width, height
		);
		MatrixToImageWriter.writeToPath(bitMatrix, format, path);
	}
}
