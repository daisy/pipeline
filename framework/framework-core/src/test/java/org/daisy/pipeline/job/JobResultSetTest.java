package org.daisy.pipeline.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.impl.IOHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobResultSetTest   {

	JobResult jres1;
	JobResult jres2;
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
		
		jres1= new JobResult.Builder().withPath(res1).withIdx(new Index(uri1.toString())).build();
		jres2= new JobResult.Builder().withPath(res2).withIdx(new Index(uri2.toString())).build();
		List<JobResult> results= new LinkedList<JobResult>();
		results.add(jres1);
		results.add(jres2);
		InputStream is=JobResultSet.asZippedInputStream(results);
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
