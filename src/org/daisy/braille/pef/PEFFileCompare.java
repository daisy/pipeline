package org.daisy.braille.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.daisy.braille.tools.XMLFileCompare;

public class PEFFileCompare {
	private final static NormalizationResource def = new PackageNormalizationResource("resource-files/strip-meta.xsl");
	private final NormalizationResource nr;
	private int pos = -1;

	public PEFFileCompare() {
		this(def);
	}

	public PEFFileCompare(NormalizationResource nr) {
		this.nr = nr;
	}

	public PEFFileCompare(String path) {
		this(new PackageNormalizationResource(path));
	}

	public PEFFileCompare(URL nr) {
		this(new URLNormalizationResource(nr));
	}
	
	public boolean compare(File f1, File f2) throws PEFFileCompareException {
		return compare(new StreamSource(f1), new StreamSource(f2));
	}

	public boolean compare(StreamSource xml1, StreamSource xml2) throws PEFFileCompareException {
		pos = -1;

		TransformerFactory factory = TransformerFactory.newInstance();
		try {
			factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
		} catch (IllegalArgumentException iae) {
			iae.printStackTrace();
		}

		try {
			File t1 = File.createTempFile("FileCompare", ".tmp");
			t1.deleteOnExit();
			File t2 = File.createTempFile("FileCompare", ".tmp");
			t2.deleteOnExit();

			try {

				Source xslt;
				Transformer transformer;

				xslt = new StreamSource(nr.getNormalizationResourceAsStream());
				transformer = factory.newTransformer(xslt);
				transformer.transform(xml1, new StreamResult(t1));

				xslt = new StreamSource(nr.getNormalizationResourceAsStream());
				transformer = factory.newTransformer(xslt);
				transformer.transform(xml2, new StreamResult(t2));
				XMLFileCompare fc = new XMLFileCompare(factory);
				boolean ret = fc.compareXML(new FileInputStream(t1), new FileInputStream(t2));
				pos = fc.getPos();
				return ret;
			} catch (TransformerConfigurationException e) {
				throw new PEFFileCompareException(e);
			} catch (TransformerException e) {
				throw new PEFFileCompareException(e);
			} catch (IOException e) {
				throw new PEFFileCompareException(e);
			} finally {
				if (!t1.delete()) {
					System.err.println("Delete failed");
				}
				if (!t2.delete()) {
					System.err.println("Delete failed");
				}
			}
		} catch (IOException e) {
			throw new PEFFileCompareException("Failed to create temp files.", e);
		}
	}

	public int getPos() {
		return pos;
	}

	static class URLNormalizationResource implements NormalizationResource {
		private final URL url;

		public URLNormalizationResource(URL url) {
			this.url = url;
		}


		public InputStream getNormalizationResourceAsStream() {
			try {
				return url.openStream();
			} catch (IOException e) {
				return null;
			}
		}
	}

	static class PackageNormalizationResource implements NormalizationResource {
		private final String path;

		public PackageNormalizationResource(String path) {
			this.path = path;
		}


		public InputStream getNormalizationResourceAsStream() {
			return this.getClass().getResourceAsStream(path);
		}
	}

}
