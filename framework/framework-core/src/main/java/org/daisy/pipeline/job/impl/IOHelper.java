package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.URIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO related utities.
 */
public class IOHelper {

	/** The Constant SLASH. */
	private static final String SLASH = "/";

	/** The Constant BLOCK_SIZE. */
	private static final int BLOCK_SIZE = 1024;



	private static final Logger logger = LoggerFactory
			.getLogger(IOHelper.class);

	public static File makeDirs(String ...pathParts) throws IOException{
		StringBuilder builder = new StringBuilder();
		for (String part:pathParts){
			builder.append(part);
			builder.append(File.separator);
		}
		return IOHelper.makeDirs(new File(builder.toString()));

	}

	public static File makeDirs(File dir) throws IOException{
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException("Could not create dir:"
					+ dir.getAbsolutePath());
		}
		return dir;
	}
	/**
	 * Dumps the content of the IS to the given path.
	 *
	 * @param is the is
	 * @param base the base
	 * @param path the path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void dump(InputStream is,URI base,URI path) throws IOException{
			//linux & mac doesnt create empty files out of outstreams where nothing was written
			//but win does, anyway this piece is more elegant than before.
			File fout = new File(base.resolve(path));
			if(!path.toString().endsWith(SLASH)){
				fout.getParentFile().mkdirs();
				FileOutputStream fos=new FileOutputStream(fout);
				dump(is,fos);
				fos.close();
				is.close();
			}else{
				fout.mkdirs();
			}
	}


	/**
	 * Dump
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void dump(JobResources resources,URIMapper mapper) throws IOException {
		for (String path : resources.getNames()) {
			IOHelper.dump(resources.getResource(path).get(),mapper.getInputBase() 
					, URI.create(path.replace("\\", "/")));
		}
	}

	/**
	 * Dumps the given input stream into the output stream
	 *
	 * @param is the is
	 * @param os the os
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void dump(InputStream is,OutputStream os) throws IOException{
		byte buff[]= new byte[BLOCK_SIZE];
		int read=0;
		while((read=is.read(buff))>0){
			os.write(buff,0,read);

		}
	}


	/**
	 * creates a flat list out of a tree directory.
	 *
	 * @param base the base
	 * @return the list
	 */
	public static List<URI> treeFileList(URI uriBase) {
		File base=new File(uriBase);	
		
		LinkedList<URI> result= new LinkedList<URI>();
		File[] fList=base.listFiles();
		if (fList!=null) 
			for(File f:base.listFiles()){
				if(f.isDirectory()){
					result.addAll(treeFileList(f.toURI()));
				}else{
					result.add(f.toURI());
				}
			}

		return result;
	}

	public static boolean delete(URI parent){
		return deleteDir(new File(parent));
	}
	public static boolean deleteDir(File parent){
		logger.debug("Deleting directory:"+parent);
		File[] fList=parent.listFiles();
		if (fList!=null)
		for( File f: parent.listFiles()){
			if (f.isDirectory()){
				IOHelper.deleteDir(f);
			}
			f.delete();
		}
		return parent.delete();
	}
}
