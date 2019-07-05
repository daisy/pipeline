package com.xmlcalabash.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import com.xmlcalabash.io.DataStore.DataInfo;
import com.xmlcalabash.io.DataStore.DataReader;
import com.xmlcalabash.io.DataStore.DataWriter;

public class FileDataStoreTest extends TestCase {
	// Some changes must be taking in accound when computing the temp directory : 
	// On windows, a valid path should be file:/C:/path/to/temp/
	// On UNIX, a valid path should be file:/path/to/temp/
	// by default, System.getProperty("java.io.tmpdir") returns "/tmp" on unix,
	// and on Window it return "C:\Users\abidbol\appdata" //FIXME
	private final String tmp = "file:" + 
			(System.getProperty("java.io.tmpdir").startsWith("/") ? "" : "/") + 
			System.getProperty("java.io.tmpdir").replace(File.separatorChar, '/') +
			(System.getProperty("java.io.tmpdir").endsWith("/") ? "" : "/");
	private FileDataStore store;

	public void setUp() throws Exception {
		store = new FileDataStore(new FallbackDataStore());
	}

	public void testWriteFile() throws IOException {
		store.writeEntry("file.txt", tmp, "text/plain", new DataWriter() {
			public void store(OutputStream content) throws IOException {
				content.write("content".getBytes());
			}
		});
		store.readEntry("file.txt", tmp, "text/plain", null, new DataReader() {
			public void load(URI id, String media, InputStream content, long len)
					throws IOException {
				byte[] buf = new byte[1024];
				assertEquals("content", new String(buf, 0, content.read(buf)));
			}
		});
		store.deleteEntry("file.txt", tmp);
	}

	public void testWriteDirectory() throws IOException {
		// Clean the temp folder before starting the test
		URI dir = store.createList("testWriteDirectory", tmp);
		for(File toDelete : new File(dir).listFiles()) {
			toDelete.delete();
		}
		store.deleteEntry("testWriteDirectory/", tmp);
		// Start the tests
		URI file = store.writeEntry("testWriteDirectory/", tmp, "text/plain", new DataWriter() {
			public void store(OutputStream content) throws IOException {
				content.write("content".getBytes());
			}
		});
		store.infoEntry(file.getPath(), tmp, "text/plain", new DataInfo() {
			public void list(URI id, String media, long lastModified)
					throws IOException {
				assertEquals("text/plain", media);
			}
		});
		store.readEntry(file.getPath(), tmp, "text/plain", null, new DataReader() {
			public void load(URI id, String media, InputStream content, long len)
					throws IOException {
				byte[] buf = new byte[1024];
				assertEquals("content", new String(buf, 0, content.read(buf)));
			}
		});
		
		final int rootLength = tmp.length();
	    final String absFileName = file.toString();
	    final String relFileName = absFileName.substring(rootLength);
		store.deleteEntry(relFileName, tmp);
		store.deleteEntry("testWriteDirectory/", tmp);
	}

	public void testReadEntry() throws IOException {
		File file = File.createTempFile("testReadEntry", ".txt");
		FileWriter writer = new FileWriter(file);
		try {
			writer.write("read content");
		} finally {
			writer.close();
		}
		
		String uri = file.toURI().toASCIIString();
		store.readEntry(uri, uri, "*/*", null, new DataReader() {
			public void load(URI id, String media, InputStream content, long len)
					throws IOException {
				byte[] buf = new byte[1024];
				assertEquals("read content", new String(buf, 0, content.read(buf)));
			}
		});
		
		final int rootLength = tmp.length();
	    final String absFileName = file.toURI().toString();
	    final String relFileName = absFileName.substring(rootLength);
		store.deleteEntry(relFileName, tmp);
	}

	public void testInfoEntry() throws IOException {
		File file = File.createTempFile("test", ".txt");
		FileWriter writer = new FileWriter(file);
		try {
			writer.write("read content");
		} finally {
			writer.close();
		}
		String uri = file.toURI().toASCIIString();
		store.readEntry(uri, uri, "*/*", null, new DataReader() {
			public void load(URI id, String media, InputStream content, long len)
					throws IOException {
				assertEquals("text/plain", media);
			}
		});
		
		final int rootLength = tmp.length();
	    final String absFileName = file.toURI().toString();
	    final String relFileName = absFileName.substring(rootLength);
		store.deleteEntry(relFileName, tmp);
	}

	public void testListEachEntry() throws IOException {
		// Clean the temp folder before starting the test
		URI dir = store.createList("testListEachEntry", tmp);
		for(File toDelete : new File(dir).listFiles()) {
			toDelete.delete();
		}
		store.deleteEntry("testListEachEntry/", tmp);
		// Start the tests
		dir = store.createList("testListEachEntry", tmp);
		final File text = File.createTempFile("test", ".txt", new File(dir));
		final File xml = File.createTempFile("test", ".xml", new File(dir));
		store.listEachEntry("dir", tmp, "text/plain", new DataInfo() {
			public void list(URI id, String media, long lastModified)
					throws IOException {
				assertEquals("text/plain", media);
				assertEquals(text.getAbsolutePath(), new File(id).getAbsolutePath());
			}
		});
		store.listEachEntry("testListEachEntry", tmp, "application/xml", new DataInfo() {
			public void list(URI id, String media, long lastModified)
					throws IOException {
				assertEquals("application/xml", media);
				assertEquals(xml.getAbsolutePath(), new File(id).getAbsolutePath());
			}
		});
		
		final int rootLength = tmp.length();
		store.deleteEntry(text.toURI().toString().substring(rootLength), tmp);
		store.deleteEntry(xml.toURI().toString().substring(rootLength), tmp);
		store.deleteEntry("testListEachEntry", tmp);
	}

	public void testCreateList() throws IOException {
		URI dir = store.createList("dir", tmp);
		assertTrue(new File(dir).isDirectory());
	}

	public void testDeleteEntry() throws IOException {
		URI file = store.writeEntry("testDeleteEntry/", tmp, "text/plain", new DataWriter() {
			public void store(OutputStream content) throws IOException {
				content.write("content".getBytes());
			}
		});
		assertTrue(new File(file).exists());
		final int rootLength = tmp.length();
	    final String absFileName = new File(file).toURI().toString();
	    final String relFileName = absFileName.substring(rootLength);
		store.deleteEntry(relFileName, tmp);
		assertFalse(new File(file).exists());
		new File(file).delete();
		try {
			new File(new URI(tmp).resolve("testDeleteEntry")).delete();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testListAcceptableFiles() throws IOException {
		URI dir = store.createList("testListAcceptableFiles", tmp);
		// Clean the content of the folder if it is not empty
		for(File toDelete : new File(dir).listFiles()) {
			toDelete.delete();
		}
		
		final File text = File.createTempFile("test", ".txt", new File(dir));
		final File xml = File.createTempFile("test", ".xml", new File(dir));
		final File json = File.createTempFile("test", ".json", new File(dir));
		final File zip = File.createTempFile("test", ".zip", new File(dir));
		assertEquals(4, store.listAcceptableFiles(new File(dir), "*/*").length);
		assertEquals(Collections.singletonList(text), Arrays.asList(store.listAcceptableFiles(new File(dir), "text/plain")));
		assertEquals(Collections.singletonList(xml), Arrays.asList(store.listAcceptableFiles(new File(dir), "application/xml")));
		assertEquals(Collections.singletonList(json), Arrays.asList(store.listAcceptableFiles(new File(dir), "application/json")));
		assertEquals(Collections.singletonList(zip), Arrays.asList(store.listAcceptableFiles(new File(dir), "application/zip")));
		zip.delete();
		json.delete();
		xml.delete();
		text.delete();
		new File(dir).delete();
	}

	public void testGetContentTypeFromName() throws IOException {
		assertEquals("text/plain", store.getContentTypeFromName(".txt"));
		assertEquals("application/xml", store.getContentTypeFromName(".xml"));
		assertEquals("application/json", store.getContentTypeFromName(".json"));
		assertEquals("application/zip", store.getContentTypeFromName(".zip"));
	}

	public void testGetFileSuffixFromType() throws IOException {
		assertEquals(".txt", 
				store.getFileSuffixFromType("text/plain"));
		assertEquals(".xml", 
				store.getFileSuffixFromType("application/xml"));
		assertEquals(".xml", 
				store.getFileSuffixFromType("text/xml"));
		assertEquals(".xml", 
				store.getFileSuffixFromType("image/svg+xml"));
		assertEquals(".json", 
				store.getFileSuffixFromType("application/json"));
		assertEquals(".zip", 
				store.getFileSuffixFromType("application/zip"));
	}

}
