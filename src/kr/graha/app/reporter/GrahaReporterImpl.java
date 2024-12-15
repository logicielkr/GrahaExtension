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

package kr.graha.app.reporter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kr.graha.post.interfaces.Reporter;
import kr.graha.post.lib.Record;
import kr.graha.helper.LOG;
import java.sql.Connection;
import kr.graha.post.lib.Buffer;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import kr.graha.post.model.utility.TextParser;
import java.net.URI;


/**
 * kr.graha.post.interfaces.Reporter 표준 구현체

 * @author HeonJik, KIM
 
 * @see kr.graha.post.interfaces.Reporter
 
 * @version 0.9
 * @since 0.9
 */
public class GrahaReporterImpl implements Reporter {
	public GrahaReporterImpl() {
		
	}
/**
 * Graha 가 호출하는 메소드
 
 * @param request HttpServlet 에 전달된 HttpServletRequest
 * @param response HttpServlet 에 전달된 HttpServletResponse
 * @param params Graha 에서 각종 파라미터 정보를 담아서 넘겨준 객체
 * @param xml Graha 에서 sql 실행결과를 XML 로 변경한 것(StringBuffer 유사)
 * @param con 데이타베이스 연결(Connection)

 * @see javax.servlet.http.HttpServletRequest (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletRequest (Apache Tomcat 10 이상)
 * @see javax.servlet.http.HttpServletResponse (Apache Tomcat 10 미만)
 * @see jakarta.servlet.http.HttpServletResponse (Apache Tomcat 10 이상)
 * @see kr.graha.post.lib.Record
 * @see kr.graha.post.lib.Buffer
 * @see java.sql.Connection 
 */
	public void execute(HttpServletRequest request, HttpServletResponse response, Record params, Buffer xml, Connection con) {
		if(!params.hasKey(Record.key(Record.PREFIX_TYPE_PROP, "reporter.config.file.path"))) {
		}
		GrahaReporter reporter = new GrahaReporter();
		ReporterEntries entries = null;
		try {
			entries = reporter.parse(params.getString(Record.key(Record.PREFIX_TYPE_PROP, "reporter.config.file.path")));
			if(entries == null) {
			} else {
				String basePath = entries.getBasePath();
				if(basePath != null) {
					String parsed = TextParser.parse(basePath, params);
					entries.setBasePath(parsed);
				}
				List<ReporterEntry> entry = entries.getEntries();
				for(int i = 0; i < entry.size(); i++) {
					ReporterEntry item = entry.get(i);
					
					String templatePath = item.getTemplatePath();
					if(templatePath != null) {
						String parsed = TextParser.parse(templatePath, params);
						item.setTemplatePath(parsed);
					}
					
					Map<String, String> appends = item.getAppends();
					if(appends != null) {
						Iterator keys = appends.keySet().iterator();
						while(keys.hasNext()) {
							String key = (String)keys.next();
							String value = (String)appends.get(key);
							String parsed = TextParser.parse(value, params);
							appends.replace(key, value, parsed);
						}
					}
				}
				reporter.report(entries, xml.toByte());
				if(entries.getUniqueArchiveFileURI() != null) {
					params.put(Record.key(Record.PREFIX_TYPE_RESULT, "reporter.archive_file_name"), this.decodeFileName(entries.getUniqueArchiveFileURI()));
				}
				if(entries.getUniqueArchivePdfFileURI() != null) {
					params.put(Record.key(Record.PREFIX_TYPE_RESULT, "reporter.archive_pdf_file_name"), this.decodeFileName(entries.getUniqueArchivePdfFileURI()));
				}
				for(int i = 0; i < entry.size(); i++) {
					ReporterEntry item = entry.get(i);
					if(item.getUniqueFileURI() != null) {
						params.put(Record.key(Record.PREFIX_TYPE_RESULT, "reporter.file_name." + i), this.decodeFileName(item.getUniqueFileURI()));
					}
					if(item.getUniquePdfFileURI() != null) {
						params.put(Record.key(Record.PREFIX_TYPE_RESULT, "reporter.pdf_file_name." + i), this.decodeFileName(item.getUniquePdfFileURI()));
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException | URISyntaxException | TransformerException e) {
			LOG.warning(e);
		} finally {
		}
	}
/**
 * URI 객체로부터 파일이름만 가져온다.
 *
 * 이 메소드는 kr/graha/post/xml/GFile.java 에서 그대로 복사했다.
 *
 * @param uri URI 객체(Path.toURI() 메소드의 결과)
 * @return 파일이름
 */
	private String decodeFileName(URI uri) {
		return uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1);
	}
}
