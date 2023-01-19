package org.daisy.pipeline.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.impl.IOHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobResultSetTest   {

	String content1="Bonjour le monde!";
	String content2="Hei verden!";
	InputStream is;
	File tmpFile;
	URI uri1;
	URI uri2;

	@Before
	public void setUp() throws IOException {
		URI tmp=new File(System.getProperty("java.io.tmpdir")).toURI();
		uri1= URI.create("file1.xml");
		uri2= URI.create("file2.xml");
		URI res1=tmp.resolve(uri1);
		URI res2=tmp.resolve(uri2);
		(new File(res1)).createNewFile();
		(new File(res2)).createNewFile();
		//write the files	
		PrintWriter pw1= new PrintWriter(new FileOutputStream(new File(res1)));
		pw1.append(content1);
		pw1.close();

		PrintWriter pw2= new PrintWriter(new FileOutputStream(new File(res2)));
		pw2.append(content2);
		pw2.close();

		Collection<JobResult> results = new JobResultSet.Builder().addResult("foo", uri1.toString(), res1, null)
		                                                          .addResult("foo", uri2.toString(), res2, null)
		                                                          .build()
		                                                          .getResults("foo");

		InputStream is=JobResultSet.asZip(results);
		tmpFile=File.createTempFile("diasy_test",".zip");
		IOHelper.dump(is,new FileOutputStream(tmpFile));
	}

	@Test
	public void asZipCount() throws Exception{
		ZipFile file= new ZipFile(tmpFile);
		Enumeration<? extends ZipEntry> entries=file.entries();
		int count=0;
		while(entries.hasMoreElements()){
			count++;
			entries.nextElement();
		}
		Assert.assertEquals(2,count);
			
	}

	@Test
	public void asZipEntryNames() throws Exception{
		ZipFile file= new ZipFile(tmpFile);
		Enumeration<? extends ZipEntry> entries=file.entries();
		HashSet<String> readEnt=new HashSet<String>();
		while(entries.hasMoreElements()){
			ZipEntry entry=entries.nextElement();
			readEnt.add(entry.getName());
		}
		Assert.assertTrue(readEnt.contains(uri1.toString()));
		Assert.assertTrue(readEnt.contains(uri2.toString()));
			
	}
	@Test
	public void asZipEntryContents() throws Exception{
		ZipFile file= new ZipFile(tmpFile);
		Enumeration<? extends ZipEntry> entries=file.entries();
		HashSet<String> readEnt=new HashSet<String>();
		while(entries.hasMoreElements()){
			ZipEntry entry=entries.nextElement();
			String contents=new BufferedReader(new InputStreamReader(file.getInputStream(entry))).readLine();
			readEnt.add(contents);

		}
		Assert.assertTrue(readEnt.contains(content1));
		Assert.assertTrue(readEnt.contains(content2));
	}
}
