package org.daisy.pipeline.webservice.restlet;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;

/**
 * Convenience class for representing the parts of a multipart request
 */
public class MultipartRequestData {

	private static final Logger logger = LoggerFactory.getLogger(MultipartRequestData.class.getName());

	private final ZipFile zip;
	private final Document xml;

	private MultipartRequestData(ZipFile zip, Document xml) {
		this.zip = zip;
		this.xml = xml;
	}

	/**
	 * Get the zip file.
	 */
	public ZipFile getZipFile() {
		return zip;
	}

	/**
	 * Get the XML.
	 */
	public Document getXml() {
		return xml;
	}

	/*
	 * taken from an example at:
	 * http://wiki.restlet.org/docs_2.0/13-restlet/28-restlet/64-restlet.html
	 */
	/**
	 * Process multipart request.
	 *
	 * @param dataFieldName field name of the ZIP part
	 * @param xmlFieldName field name of the XML part
	 * @param tempdir temporary directory for storing ZIP part
	 * @return the multipart request data, or {@code null} in case of a bad request.
	 */
	public static MultipartRequestData processMultipart(Request request, String dataFieldName, String xmlFieldName, File tmpdir)
			throws Exception {

		logger.debug("tmpdir: " + tmpdir);
		// 1. Create a factory for disk-based file items
		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
		fileItemFactory.setSizeThreshold(1000240);
		// 2. Create a new file upload handler based on the Restlet
		// FileUpload extension that will parse Restlet requests and
		// generates FileItems.
		RestletFileUpload upload = new RestletFileUpload(fileItemFactory);
		List<FileItem> items;
		ZipFile zip = null;
		String xml = "";
		items = upload.parseRequest(request);
		Iterator<FileItem> it = items.iterator();
		while (it.hasNext()) {
			FileItem fi = it.next();
			if (fi.getFieldName().equals(dataFieldName)) {
				logger.debug("Reading zip file");
				File file = File.createTempFile("p2ws", ".zip", tmpdir);
				fi.write(file);
				// re-opening the file after writing to it
				File file2 = new File(file.getAbsolutePath());
				zip = new ZipFile(file2);
			} else if (fi.getFieldName().equals(xmlFieldName)) {
				xml = fi.getString("utf-8");
				logger.debug("XML multi:" + xml);
			}
		}
		if (zip == null)
			return null;
		Document doc = null; {
			if (xml.length() > 0) {
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				docFactory.setNamespaceAware(true);
				DocumentBuilder builder = docFactory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(xml));
				doc = builder.parse(is);
			}
		}
		MultipartRequestData data = new MultipartRequestData(zip, doc);
		return data;
	}
}
