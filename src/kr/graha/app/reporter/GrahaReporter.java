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

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.zip.ZipOutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.StandardCharsets;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.Iterator;

/**
 *  
 * 레포팅 도구
 * kr.graha.post.interfaces.Reporter 표준 구현체가 내부적으로 사용한다.
 *
 * @author HeonJik, KIM
 
 * @version 0.9
 * @since 0.9
 */
public class GrahaReporter {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public GrahaReporter() {
		
	}
	public static void main(String[] args) throws Exception {
		if(args == null || args.length < 2) {
			System.err.println("Usage :");
			System.err.println("java \\");
			System.err.println("-classpath  \\");
			System.err.println("kr.graha.app.reporter.GrahaReporter \\");
			System.err.println("${Config File Path} ${XML Data File Path}");
			return;
		}
		GrahaReporter reporter = new GrahaReporter();
		ReporterEntries entries = null;
		try {
			entries = reporter.parse(args[0]);
			if(entries != null) {
				if(entries.getPdf()) {
					PDFConverterWrapper.init();
				}
				byte[] xmlData = reporter.loadXml(args[1]);
				reporter.report(entries, xmlData);
				if(entries.getPdf()) {
					PDFConverterWrapper.destory();
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException | URISyntaxException e) {
			e.printStackTrace();
		} finally {
		}
	}
	protected ReporterEntries parse(String configFilePath)
		throws ParserConfigurationException, SAXException, IOException
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		GrahaReporterConfigSAXHandler handler = new GrahaReporterConfigSAXHandler();
		SAXParser parser = null;
		try {
			parser = factory.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			parser = null;
			throw e;
		}
		if(parser != null) {
			try {
				parser.parse(configFilePath, handler);
				return handler.getEntries();
			} catch (SAXException | IOException e) {
				throw e;
			}
		}
		return null;
	}
	private byte[] loadXml(String xmlFile) throws IOException {
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			fis = new FileInputStream(xmlFile);
			this.copyStream(fis, baos);
			fis.close();
			fis = null;
			return baos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {}
			}
		}
	}
/**
 * 유일한 파일이름을 URI로 가져온다.
 *
 * basePath 에 fileName 과 동일한 파일이 있는 경우,
 * 확장자가 있는 경우 확장자 앞에 확장자가 없는 경우 파일이름의 끝에 "-일련번호" 를 붙인다.
 *
 * 확장자는 fileName 에서 "." 이 있는 경우, 마지막 "." 뒷부분을 확장자로 한다.
 * 확장자가 유효한지 여부를 따지지 않고, 파일이름에서 마지막 "." 뒷부분을 확장자로 취급된다.
 * 또한 ".tar.gz" 혹은 ".tar.bz2" 와 같은 경우에도 "gz", "bz2" 가 확장자가 된다(이 부분은 향후에 개선할 의향이 있고, 만약 그렇게 된다면, kr.graha.helper 아래에 위치하게 될 가능성이 크다).
 *
 * 이 메소드는 kr.graha.post.model.File 에서 그대로 복사했다.
 *
 * @param basePath 디렉토리 경로
 * @param fileName 파일이름
 * @return 디렉토리 경로(basePath) 에서 중복되지 않은 파일이름(fileName)
 */
	private URI getUniqueFileURI(String basePath, String fileName) throws UnsupportedEncodingException, URISyntaxException {
		URI uri = null;
		int index = 0;
		while(true) {
			if(index == 0) {
				uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20"));
			} else {
				if(fileName.lastIndexOf(".") > 0) {
					uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName.substring(0, fileName.lastIndexOf(".")), "UTF-8").replaceAll("\\+", "%20")  + "-" + index + "." + java.net.URLEncoder.encode(fileName.substring(fileName.lastIndexOf(".") + 1), "UTF-8").replaceAll("\\+", "%20"));
				} else {
					uri = new URI("file://" + basePath + java.io.File.separator + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "-" + index);
				}
			}
			if(Files.notExists(Paths.get(uri))) {
				break;
			}
			index++;
		}
		return uri;
	}
	protected void report(ReporterEntries entries, byte[] xmlData)
		throws IOException, TransformerConfigurationException, TransformerException, URISyntaxException
	{
		List<ReporterEntry> entry = entries.getEntries();
		if(entry != null) {
			OutputStream fos = null;
			ZipOutputStream zip = null;
			
			OutputStream pdfFos = null;
			ZipOutputStream pdfZip = null;
			
			ZipOutputStream zos = null;
			ByteArrayOutputStream baos = null;
			try {
				if(!Files.exists(Paths.get(entries.getBasePath()))) {
					Files.createDirectories(Paths.get(entries.getBasePath()));
				}
				if(entries.getArchive()) {
					URI uri = this.getUniqueFileURI(entries.getBasePath(), entries.getArchiveFileName());
					entries.setUniqueArchiveFileURI(uri);
					fos = Files.newOutputStream(Paths.get(entries.getUniqueArchiveFileURI()));
					zip = new ZipOutputStream(fos);
					if(entries.getPdf()) {
						uri = this.getUniqueFileURI(entries.getBasePath(), entries.getArchivePdfFileName());
						entries.setUniqueArchivePdfFileURI(uri);
						pdfFos = Files.newOutputStream(Paths.get(entries.getUniqueArchivePdfFileURI()));
						pdfZip = new ZipOutputStream(pdfFos);
					}
				}
				for(int i = 0; i < entry.size(); i++) {
					ReporterEntry item = entry.get(i);
					if(entries.getArchive()) {
						baos = new ByteArrayOutputStream();
						zos = new ZipOutputStream(baos);
					} else {
						URI uri = this.getUniqueFileURI(entries.getBasePath(), item.getFileName());
						item.setUniqueFileURI(uri);
						fos = Files.newOutputStream(Paths.get(item.getUniqueFileURI()));
						zos = new ZipOutputStream(fos);
					}
					this.report(item, zos, xmlData);
					if(entries.getArchive()) {
						zos.close();
						zos = null;
						if(entries.getPdf()) {
							pdfZip.putNextEntry(new ZipEntry(item.getPdfFileName()));
							ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
							ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
							PDFConverterWrapper.convert(bais, pdfStream);
							copyStream(pdfStream, pdfZip);
						}
						zip.putNextEntry(new ZipEntry(item.getFileName()));
						copyStream(baos, zip);
						baos.close();
						baos = null;
					} else {
						zos.close();
						zos = null;
						fos.close();
						fos = null;
						if(entries.getPdf()) {
							URI uri = this.getUniqueFileURI(entries.getBasePath(), item.getPdfFileName());
							item.setUniquePdfFileURI(uri);
							PDFConverterWrapper.convert(Paths.get(item.getUniqueFileURI()), Paths.get(item.getUniquePdfFileURI()));
						}
					}
				}
				if(entries.getArchive()) {
					zip.close();
					zip = null;
					fos.close();
					fos = null;
					if(entries.getPdf()) {
						pdfZip.close();
						pdfZip = null;
						pdfFos.close();
						pdfFos = null;
					}
				}
			} catch(IOException | TransformerException | URISyntaxException e) {
				throw e;
			} finally {
				if(zos != null) {
					try {
						zos.close();
						zos = null;
					} catch (IOException e) {}
				}
				if(baos != null) {
					try {
						baos.close();
						baos = null;
					} catch (IOException e) {}
				}
				if(zip != null) {
					try {
						zip.close();
						zip = null;
					} catch (IOException e) {}
				}
				if(fos != null) {
					try {
						fos.close();
						fos = null;
					} catch (IOException e) {}
				}
				if(pdfZip != null) {
					try {
						pdfZip.close();
						pdfZip = null;
					} catch (IOException e) {}
				}
				if(pdfFos != null) {
					try {
						pdfFos.close();
						pdfFos = null;
					} catch (IOException e) {}
				}
			}
		}
	}
	private void report(ReporterEntry item, ZipOutputStream zos, byte[] xmlData)
		throws IOException, TransformerConfigurationException, TransformerException
	{
		String templatePath = item.getTemplatePath();
		File templateFile = new File(templatePath);
		if(templateFile.exists()) {
			this.report(templateFile, zos, xmlData);
			Map<String, String> appends = item.getAppends();
			if(appends != null) {
				FileInputStream is = null;
				try {
					Iterator keys = appends.keySet().iterator();
					while(keys.hasNext()) {
						String key = (String)keys.next();
						zos.putNextEntry(new ZipEntry(key));
						is = new FileInputStream((String)appends.get(key));
						copyStream(is, zos);
						is.close();
						is = null;
					}
				} catch (IOException e) {
					throw e;
				} finally {
					if(is != null) {
						try {
							is.close();
							is = null;
						} catch(IOException e) {
						}
					}
				}
			}
		} else {
			logger.warning("templatePath(" + templatePath + ") is wrong");
		}
	}
	private void report(File templateFile, ZipOutputStream zos, byte[] xmlData)
		throws IOException, TransformerConfigurationException, TransformerException
	{
		if(templateFile.exists()) {
			if(templateFile.isDirectory()) {
				this.reportAsDirectory(templateFile.toPath(), null, zos, xmlData);
			} else {
				this.reportAsZipArchive(templateFile, zos, xmlData);
			}
		} else {
			logger.warning("templatePath(" + templateFile.getPath() + ") is wrong");
		}
	}
	private ByteArrayOutputStream xslt(InputStream template, byte[] xmlData)
		throws TransformerConfigurationException, TransformerException
	{
		Source xml = new StreamSource(new ByteArrayInputStream(xmlData));
		StreamSource xsl = new StreamSource(template);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(xsl);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		transformer.transform(xml, new StreamResult(result));
		return result;
	}
	private void copyStream(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[8192];
		int length;
		while ((length = is.read(buffer)) >= 0) {
			os.write(buffer, 0, length);
		}
		os.flush();
	}
	private void copyStream(ByteArrayOutputStream baos, OutputStream os) throws IOException {
		baos.writeTo(os);
		os.flush();
	}
	private void copyStream(Path path, OutputStream os) throws IOException {
		Files.copy(path, os);
		os.flush();
	}
	private boolean excludes(String fileName) {
		if(fileName == null) {
			return true;
		}
		if(fileName.endsWith(".swp")) {
			return true;
		} else if(fileName.endsWith("~")) {
			return true;
		} else if(fileName.endsWith("#")) {
			return true;
		}
		return false;
	}
	private void reportAsZipArchive(File templateFile, ZipOutputStream zos, byte[] xmlData)
		throws IOException, TransformerConfigurationException, TransformerException
	{
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ZipEntry entry = null;
		try {
			fis = new FileInputStream(templateFile);
			zis = new ZipInputStream(fis, StandardCharsets.ISO_8859_1);
			while((entry = zis.getNextEntry()) != null) {
				if(entry.getName().endsWith(".xslt")) {
					String fileName = entry.getName().substring(0, entry.getName().length() - 5);
					ByteArrayOutputStream result = this.xslt(zis, xmlData);
					zos.putNextEntry(new ZipEntry(fileName));
					copyStream(result, zos);
				} else {
					if(this.excludes(entry.getName())) {
					} else {
						zos.putNextEntry(new ZipEntry(entry.getName()));
						copyStream(zis, zos);
					}
				}
				zis.closeEntry();
				entry = null;
			}
			zis.close();
			zis = null;
			fis.close();
			fis = null;
		} catch (IOException e) {
			throw e;
		} finally {
			if(entry != null) {
				try {
					zis.closeEntry();
				} catch (IOException e) {
				}
			}
			if(zis != null) {
				try {
					zis.close();
					zis = null;
				} catch (IOException e) {
				}
			}
			if(fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
				}
			}
		}
	}
	private void reportAsDirectory(Path templateFilePath, Path parent, ZipOutputStream zos, byte[] xmlData)
		throws IOException, TransformerConfigurationException, TransformerException
	{
		DirectoryStream<Path> stream = null;
		InputStream is = null;
		try {
			if(parent == null) {
				if(Files.exists(templateFilePath) && Files.isDirectory(templateFilePath)) {
					stream = Files.newDirectoryStream(templateFilePath);
				} else {
					return;
				}
			} else {
				if(Files.exists(parent) && Files.isDirectory(parent)) {
					stream = Files.newDirectoryStream(parent);
				} else {
					return;
				}
			}
			for(Path path : stream) {
				if(Files.isDirectory(path)) {
					this.reportAsDirectory(templateFilePath, path, zos, xmlData);
				} else {
					String fileName = templateFilePath.relativize(path).toString();
					if(path.toString().endsWith(".xslt")) {
						is = Files.newInputStream(path);
						fileName = fileName.substring(0, fileName.length() - 5);
						zos.putNextEntry(new ZipEntry(fileName));
						ByteArrayOutputStream result = this.xslt(is, xmlData);
						copyStream(result, zos);
						is.close();
						is = null;
					} else {
						if(this.excludes(fileName)) {
						} else {
							zos.putNextEntry(new ZipEntry(fileName));
							copyStream(path, zos);
						}
					}
				}
			}
			stream.close();
			stream = null;
		} catch(IOException e) {
			throw e;
		} finally {
			if(is != null) {
				try {
					is.close();
					is = null;
				} catch(IOException e) {
				}
			}
			if(stream != null) {
				try {
					stream.close();
					stream = null;
				} catch(IOException e) {
				}
			}
		}
	}
}
class GrahaReporterConfigSAXHandler extends DefaultHandler {
	private StringBuffer characters = new StringBuffer();
	private static int PARENT_NODE_NONE = 0;
	private static int PARENT_NODE_REPORTER = 1;
	private static int PARENT_NODE_ENTRY = 2;
	
	private int parentNode = GrahaReporterConfigSAXHandler.PARENT_NODE_NONE;
	private ReporterEntries entries = null;
	private ReporterEntry entry = null;
	protected ReporterEntries getEntries() {
		return this.entries;
	}
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if(qName.equals("reporter")) {
			this.parentNode = GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER;
			this.entries = new ReporterEntries();
		} else if(qName.equals("entry")) {
			this.parentNode = GrahaReporterConfigSAXHandler.PARENT_NODE_ENTRY;
			this.entry = new ReporterEntry();
		} else if(qName.equals("append")) {
			this.entry.add(attributes.getValue("name"), attributes.getValue("path"));
		}
		this.characters.setLength(0);
	}
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("reporter")) {
			this.parentNode = GrahaReporterConfigSAXHandler.PARENT_NODE_NONE;
		} else if(qName.equals("entry")) {
			this.parentNode = GrahaReporterConfigSAXHandler.PARENT_NODE_NONE;
			this.entries.add(this.entry);
		} else if(qName.equals("base_path")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setBasePath(this.characters.toString());
			}
		} else if(qName.equals("file_name")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_ENTRY) {
				this.entry.setFileName(this.characters.toString());
			}
		} else if(qName.equals("archive_file_name")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setArchiveFileName(this.characters.toString());
			}
		} else if(qName.equals("pdf_file_name")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_ENTRY) {
				this.entry.setPdfFileName(this.characters.toString());
			}
		} else if(qName.equals("archive_pdf_file_name")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setArchivePdfFileName(this.characters.toString());
			}
		} else if(qName.equals("template_path")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_ENTRY) {
				this.entry.setTemplatePath(this.characters.toString());
			}
		} else if(qName.equals("temp_path")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setTempPath(this.characters.toString());
			}
		} else if(qName.equals("pdf")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setPdf(this.characters.toString());
			}
		} else if(qName.equals("archive")) {
			if(this.parentNode == GrahaReporterConfigSAXHandler.PARENT_NODE_REPORTER) {
				this.entries.setArchive(this.characters.toString());
			}
		}
	}
	public void characters(char[] ch, int start, int length) {
		this.characters.append(ch,start,length);
	}
	public GrahaReporterConfigSAXHandler() {
	}
}
class ReporterEntries {
	private String basePath = null;
	private String archiveFileName = null;
	private String archivePdfFileName = null;
	private boolean pdf = false;
	private boolean archive = false;
	private String tempPath = null;
	private URI uniqueArchiveFileURI;
	private URI uniqueArchivePdfFileURI;
	private List<ReporterEntry> entries = null;
	public ReporterEntries() {
	}
	public ReporterEntries(String basePath, String archiveFileName) {
		this.basePath = basePath;
		this.archiveFileName = archiveFileName;
	}
	public ReporterEntries(String basePath, String archiveFileName, List<ReporterEntry> entries) {
		this.basePath = basePath;
		this.archiveFileName = archiveFileName;
		this.entries = entries;
	}
	public void setArchiveFileName(String archiveFileName) {
		this.archiveFileName = archiveFileName;
	}
	public void setArchivePdfFileName(String archivePdfFileName) {
		this.archivePdfFileName = archivePdfFileName;
	}
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}
	public void setTempPath(String tempPath) {
		this.tempPath = tempPath;
	}
	private boolean bool(String text) {
		if(text != null) {
			if(text.equalsIgnoreCase("y")) {
				return true;
			} else if(text.equalsIgnoreCase("yes")) {
				return true;
			} else if(text.equalsIgnoreCase("t")) {
				return true;
			} else if(text.equalsIgnoreCase("true")) {
				return true;
			}
		}
		return false;
	}
	public void setPdf(String pdf) {
		this.pdf = this.bool(pdf);
	}
	public void setArchive(String archive) {
		this.archive = this.bool(archive);
	}
	public void setUniqueArchiveFileURI(URI uniqueArchiveFileURI) {
		this.uniqueArchiveFileURI = uniqueArchiveFileURI;
	}
	public void setUniqueArchivePdfFileURI(URI uniqueArchivePdfFileURI) {
		this.uniqueArchivePdfFileURI = uniqueArchivePdfFileURI;
	}
	public void add(ReporterEntry entry) {
		if(this.entries == null) {
			this.entries = new ArrayList<ReporterEntry>();
		}
		this.entries.add(entry);
	}
	public String getArchiveFileName() {
		return this.archiveFileName;
	}
	public String getArchivePdfFileName() {
		return this.archivePdfFileName;
	}
	public String getBasePath() {
		return this.basePath;
	}
	public String getTempPath() {
		return this.tempPath;
	}
	public boolean getPdf() {
		return this.pdf;
	}
	public boolean getArchive() {
		return this.archive;
	}
	public URI getUniqueArchiveFileURI() {
		return this.uniqueArchiveFileURI;
	}
	public URI getUniqueArchivePdfFileURI() {
		return this.uniqueArchivePdfFileURI;
	}
	public List<ReporterEntry> getEntries() {
		return this.entries;
	}
}
class ReporterEntry {
	private String templatePath = null;
	private String fileName = null;
	private String pdfFileName = null;
	private Map<String, String> appends = null;
	private URI uniqueFileURI = null;
	private URI uniquePdfFileURI = null;
	public ReporterEntry() {
	}
	public ReporterEntry(String templatePath, String fileName) {
		this.templatePath = templatePath;
		this.fileName = fileName;
	}
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public void setPdfFileName(String pdfFileName) {
		this.pdfFileName = pdfFileName;
	}
	public void setUniqueFileURI(URI uniqueFileURI) {
		this.uniqueFileURI = uniqueFileURI;
	}
	public void setUniquePdfFileURI(URI uniquePdfFileURI) {
		this.uniquePdfFileURI = uniquePdfFileURI;
	}
	public String getTemplatePath() {
		return this.templatePath;
	}
	public String getFileName() {
		return this.fileName;
	}
	public String getPdfFileName() {
		return this.pdfFileName;
	}
	public URI getUniqueFileURI() {
		return this.uniqueFileURI;
	}
	public URI getUniquePdfFileURI() {
		return this.uniquePdfFileURI;
	}
	public void add(String name, String path) {
		if(this.appends == null) {
			this.appends = new HashMap<String, String>();
		}
		this.appends.put(name, path);
	}
	public Map<String, String> getAppends() {
		return this.appends;
	}
}