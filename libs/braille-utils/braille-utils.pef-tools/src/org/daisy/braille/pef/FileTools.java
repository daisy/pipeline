/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.pef;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Provides common file tools useful for braille.
 * @author Joel Håkansson
 */
public class FileTools {
	private final static Logger logger = Logger.getLogger(FileTools.class.getCanonicalName());
	
	/**
	 * Copies an input stream to an output stream
	 * @param is the input stream
	 * @param os the output stream
	 * @throws IOException if IO fails
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		InputStream bis = new BufferedInputStream(is);
		OutputStream bos = new BufferedOutputStream(os);
		int b;
		while ((b = bis.read())!=-1) {
			bos.write(b);
		}
		bos.flush();
		bos.close();
		bis.close();
	}
	
	/**
	 * Lists files in a directory with a given extension.
	 * @param dir the directory to list files in
	 * @param ext the extension to test
	 * @return returns an array of files with the specified extension 
	 */
	public static File[] listFiles(File dir, final String ext) {
		return dir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(ext);
			}});
	}
	
	/**
	 * Converts an array of File objects into URL's
	 * @param files the files to convert
	 * @return returns an array of URL's
	 */
	public static URL[] toURL(File[] files) {
		ArrayList<URL> urls = new ArrayList<URL>();
		if (files!=null && files.length>0) {
			for (File f : files) {
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					logger.warning("Failed to convert " + f + " into an URL.");
				}
			}
		}
		return urls.toArray(new URL[]{});
	}

}
