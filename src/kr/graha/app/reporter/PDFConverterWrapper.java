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

import java.io.InputStream;
import java.io.OutputStream;
import kr.graha.app.pdf.JODConverterManager;
import org.jodconverter.core.office.OfficeException;
import java.nio.file.Path;
import java.net.URISyntaxException;
import java.io.IOException;
import java.util.logging.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * JODConverterWrapper
 *
 * @author HeonJik, KIM
 * @version 0.1
 * @since 0.1
 */

public class PDFConverterWrapper {
	
	private final static Logger logger = Logger.getLogger(PDFConverterWrapper.class.getName());
	
	public static void init() {
		JODConverterManager converter = JODConverterManager.getInstance();
		try {
			converter.init(	);
		} catch (OfficeException e) {
			logger.warning(PDFConverterWrapper.toString(e));
			e.printStackTrace();
		}
	}
	public static void destory() {
		JODConverterManager converter = JODConverterManager.getInstance();
		try {
			converter.destory();
		} catch (OfficeException e) {
			logger.warning(PDFConverterWrapper.toString(e));
		}
	}
	public static void convert(Path in) throws URISyntaxException, IOException {
		try {
			JODConverterManager.getInstance().convert(in);
		} catch (OfficeException e) {
			logger.warning(PDFConverterWrapper.toString(e));
		}
	}
	public static void convert(Path in, Path out) throws URISyntaxException, IOException {
		try {
			JODConverterManager.getInstance().convert(in, out);
		} catch (OfficeException e) {
			logger.warning(PDFConverterWrapper.toString(e));
		}
	}
	public static void convert(InputStream in, OutputStream out) {
		try {
			JODConverterManager.getInstance().convert(in, out);
		} catch (OfficeException e) {
			logger.warning(PDFConverterWrapper.toString(e));
		}
	}
	public static Path getPDFOutputFilePath(Path in) throws URISyntaxException {
		return JODConverterManager.getPDFOutputFilePath(in);
	}
/**
 * 예외(Exception) 가 발생한 StackTrace 를 반환한다.
 
 * kr.graha.helper.LOG 에서 복사해왔다.
 
 * @param e 예외(Exception)
 * @return 예외(Exception) 가 발생한 StackTrace
 */
	public static String toString(Exception e) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			if(sw != null) {
				return sw.toString();
			}
		} catch(Exception e1) {
			e1.printStackTrace();
		} finally {
			if(sw != null) {
				try {
					sw.close();
				} catch(IOException e1) {
					e1.printStackTrace();
				}
			}
			if(pw != null) {
				pw.close();
			}
		}
		return null;
	}
}

