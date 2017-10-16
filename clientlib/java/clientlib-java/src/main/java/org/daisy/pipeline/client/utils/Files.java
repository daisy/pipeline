package org.daisy.pipeline.client.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.daisy.pipeline.client.Pipeline2Logger;

/**
 * Utility class for working with files, especially ZIP-files.
 * 
 * Based on:
 * 		http://snippets.dzone.com/posts/show/3468
 * 		http://stackoverflow.com/a/8829253/281065
 * 		http://stackoverflow.com/a/1399432/281065
 * 		http://stackoverflow.com/a/2265206/281065
 * 
 */
public class Files {

	/**
	 * Convenience method for {@code addFilesToZip(File zipFile, Map<String,File> files) }.
	 * 
	 * Appends all the files in the `directory` into the `zipFile` with paths
	 * relative to `directory`s parent directory.
	 * 
	 * @param zipFile The ZIP-file.
	 * @param directory The directory to add to the ZIP.
	 * @throws IOException thrown when an IOException occurs
	 */
	public static void addDirectoryToZip(File zipFile, File directory) throws IOException {
		Map<String, File> files = listFilesRecursively(directory, directory.getParentFile().toURI(), true);
		addFilesToZip(zipFile, files);
	}

	/**
	 * Convenience method for {@code addFilesToZip(File zipFile, Map<String,File> files) }.
	 * 
	 * Appends all the files in the `directory` into the `zipFile` with paths
	 * relative to the `directory`.
	 * 
	 * @param zipFile The ZIP-file.
	 * @param directory The directory to add to the ZIP.
	 * @throws IOException thrown when an IOException occurs
	 */
	public static void addDirectoryContentsToZip(File zipFile, File directory) throws IOException {
		Map<String, File> files = listFilesRecursively(directory, directory.toURI(), true);
		addFilesToZip(zipFile, files);
	}

	/**
	 * Lists all files recursively, starting at `directory`.
	 * 
	 * This is the same as {@code listFilesRecursively(File directory, URI base, boolean includeDirectories) } except
	 * with directory and base pointing to the same directory.
	 * 
	 * @param directory The directory
	 * @param includeDirectories whether or not to include the directories themselves in the result
	 * @return The set of files
	 * @throws IOException thrown when an IOException occurs
	 */
	public static Map<String, File> listFilesRecursively(File directory, boolean includeDirectories) throws IOException {
		return listFilesRecursively(directory, directory.toURI(), includeDirectories);
	}
	
	/**
	 * Lists all files recursively, starting at `directory`, resolving their
	 * relative paths against `base`. The return value can be used as an argument
	 * for {@code addFilesToZip(File zipFile, Map<String,File> files); }
	 * 
	 * @param directory The directory
	 * @param base The base URI
	 * @param includeDirectories whether or not to include the directories themselves in the result
	 * @return The set of files
	 * @throws IOException thrown when an IOException occurs
	 */
	public static Map<String, File> listFilesRecursively(File directory, URI base, boolean includeDirectories) throws IOException {
		Map<String, File> files = new HashMap<String, File>();

		if (directory.isDirectory()) {
			if (includeDirectories) {
				String name = base.relativize(directory.toURI()).getPath();
				if (!name.endsWith("/"))
					name += "/";
				files.put(name, directory);
			}
			
			for (File file : directory.listFiles()) {
//				if (file.isDirectory()) {
					Map<String, File> subfiles = listFilesRecursively(file, base, includeDirectories);
					files.putAll(subfiles);

//				} else if (file.isFile()) {
//					files.put(base.relativize(file.toURI()).getPath(), file);
//
//				}
			}

		} else if (directory.isFile()) {
			files.put(base.relativize(directory.toURI()).getPath(), directory);
		}

		return files;
	}
	
	/**
	 * Does not actually load the files, since that could potentially eat up your RAM.
	 * All File objects are set to null instead.
	 * 
	 * @param zipfile The ZIP file
	 * @return A list of the files in the ZIP
	 */
	public static List<String> listZipFiles(File zipfile) {
		List<String> files = new ArrayList<String>();
		
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipfile);
			Enumeration<? extends ZipEntry> zipEntries = zip.entries();
			
			while (zipEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipEntries.nextElement();
				if (!entry.isDirectory())
					files.add(entry.getName());
			}
			
		} catch (IOException e) {
			Pipeline2Logger.logger().error("Unable to list zip files", e);
		} finally {
			try {
				zip.close();
			} catch (IOException e) {
				Pipeline2Logger.logger().error("Unable to close zip stream", e);
			}
		}
		
		return files;
	}

	/**
	 * Convenience method for {@code addFilesToZip(File zipFile, Map<String,File> }
	 * files).
	 * 
	 * Appends all the `files` into the `zipFile` with paths relative to
	 * `baseDirectory`.
	 * 
	 * @param zipFile The ZIP-file.
	 * @param files The list of files to append to the ZIP-file.
	 * @param baseDirectory The directory to resolve the relative file paths against.
	 * @throws IOException thrown when an IOException occurs
	 */
	public static void addFilesToZip(File zipFile, File[] files, File baseDirectory) throws IOException {
		URI base = baseDirectory.toURI();

		Map<String, File> relativeFiles = new HashMap<String, File>();
		for (File file : files) {
			if (file.isFile()) {
				String name = base.relativize(file.toURI()).getPath();
				relativeFiles.put(name, file);
			}
		}

		addFilesToZip(zipFile, relativeFiles);
	}

	/**
	 * This is where the action happens. The other functions uses this one, but
	 * this can be used directly as well.
	 * 
	 * @param zipFile The ZIP file.
	 * @param files A map of all the files to add to the ZIP file, where the key is the ZIP entry name to use (the relative file paths).
	 * @throws IOException thrown when an IOException occurs
	 */
	public static void addFilesToZip(File zipFile, Map<String, File> files) throws IOException {

		// get a temp file
		File tempFile = File.createTempFile(zipFile.getName(), null);

		// delete it, otherwise you cannot rename your existing zip to it.
		tempFile.delete();

		if (!zipFile.renameTo(tempFile)) {
			throw new RuntimeException("could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
		}

		// 4MiB buffer
		byte[] buf = new byte[4096 * 1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String entryName = entry.getName();
			boolean notInFiles = true;
			for (String fileName : files.keySet()) {
				if (fileName.equals(entryName)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				Pipeline2Logger.logger().debug("keeping in ZIP: "+entryName);
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(entryName));
				// Transfer bytes from the ZIP file to the output file
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		// Close the streams
		zin.close();

		// Compress the files
		for (String fileName : files.keySet()) {
			Pipeline2Logger.logger().debug("adding to ZIP: "+fileName);
			if (files.get(fileName).isDirectory()) {
				// TODO
			} else {
				InputStream in = new FileInputStream(files.get(fileName));
				// Add ZIP entry to output stream.
				out.putNextEntry(new ZipEntry(fileName));
				// Transfer bytes from the file to the ZIP file
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				// Complete the entry
				out.closeEntry();
				in.close();
			}
		}

		// Complete the ZIP file
		out.close();
		tempFile.delete();
	}
	
	/**
	 * Unzip the ZIP-file `zip` to the directory `dir`.
	 * 
	 * @param zip ZIP file
	 * @param dir output directory
	 * @throws IOException thrown when an IOException occurs 
	 */
	public static void unzip(File zip, File dir) throws IOException {
		if (!zip.exists()) {
			IOException e = new IOException("ZIP file does not exist: "+(zip!=null?zip.getAbsolutePath():"[null]"));
			Pipeline2Logger.logger().error("ZIP file does not exist: "+(zip!=null?zip.getAbsolutePath():"[null]"), e);
			throw e;
		}
		if (!zip.isFile()) {
			IOException e = new IOException("ZIP file is not a file: "+(zip!=null?zip.getAbsolutePath():"[null]"));
			Pipeline2Logger.logger().error("ZIP file is not a file: "+(zip!=null?zip.getAbsolutePath():"[null]"), e);
			throw e;
		}
		if (dir.exists() && !dir.isDirectory()) {
			IOException e = new IOException("ZIP output is not a directory: "+(dir!=null?dir.getAbsolutePath():"[null]"));
			Pipeline2Logger.logger().error("ZIP output is not a directory: "+(dir!=null?dir.getAbsolutePath():"[null]"), e);
			throw e;
		}
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zip), StandardCharsets.UTF_8)) {
	        ZipEntry entry = zipIn.getNextEntry();
	        while (entry != null) {
	            File file = new File(dir, entry.getName());
	            if (entry.isDirectory()) {
	                file.mkdirs();
	            } else {
	                File parent = file.getParentFile();
	                if (!parent.exists()) {
	                    parent.mkdirs();
	                }
	                
	                try (BufferedOutputStream outputStream = new BufferedOutputStream
	                        (new FileOutputStream(file))) {
	                    byte[] buffer = new byte[4096];
	                    int location;
	                    while ((location = zipIn.read(buffer)) != -1) {
	                        outputStream.write(buffer, 0, location);
	                    }
	                }
	                
	            }
	            zipIn.closeEntry();
	            entry = zipIn.getNextEntry();
	        }
	    }
	}
    
	/**
	 * Zip up the directory `dir` into a new ZIP-file `zip`. If `dir` is a file, then only that single file is zipped.
	 * 
	 * @param dir The directory
	 * @param zip The ZIP file
	 * @throws IOException thrown when an IOException occurs 
	 */
	public static void zip(File dir, File zip) throws IOException {
		zip.getParentFile().mkdirs();
		ZipOutputStream zipOs = new ZipOutputStream(new FileOutputStream(zip));
		
		byte buff[]= new byte[4 * 1024 * 1024];
        
		Map<String, File> files = listFilesRecursively(dir, dir.toURI(), true);
        for (String entryName : files.keySet()){
            ZipEntry entry = new ZipEntry(entryName);
            if (entry.isDirectory())
            	continue;
            zipOs.putNextEntry(entry);
            InputStream is=new FileInputStream(files.get(entryName));
            int read=0;
            while((read=is.read(buff))>0){
                zipOs.write(buff,0,read);
            }
            is.close();
        }
        
        zipOs.close();
        
	}
	
	/**
	 * Copy a file from one location to another
	 * 
	 * @param from Original location
	 * @param to Target location
	 * @throws IOException thrown when an IOException occurs
	 */
	public static void copy(File from, File to) throws IOException {
		if (from.isDirectory()) {
			to.mkdirs();
			for (File fileOrDir : from.listFiles()) {
				copy(fileOrDir, new File(to, fileOrDir.getName()));
			}
			
		} else if (from.isFile()) {
			java.nio.file.Files.copy(from.toPath(), to.toPath(),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING,
					java.nio.file.StandardCopyOption.COPY_ATTRIBUTES,
					java.nio.file.LinkOption.NOFOLLOW_LINKS );
		}
	}
	
	/**
	 * Encodes a file path for use as a URI. The "/" directory separator is not escaped.
	 * Example: {@code encodeURI("dir1/dir 2/file [  ]].xml") returns "dir1/dir%202/file%20%5B%20%20%5D%5D.xml" }
	 * 
	 * @param path The path
	 * @return The encoded URI
	 */
	public static String encodeURI(String path) {
		try {
			String url = "";
			String[] dirSplit = path.split("/");
			for (int d = 0; d < dirSplit.length; d++) {
				if (d > 0) url += "/";
				String[] spaceSplit = dirSplit[d].split(" ");
				for (int s = 0; s < spaceSplit.length; s++) {
					if (s > 0) url += "%20";
					url += URLEncoder.encode(spaceSplit[s], "UTF-8");
				}
			}
			return url;
		} catch (UnsupportedEncodingException e) {
			Pipeline2Logger.logger().warn("Could not create URI from '"+path+"'", e);
			return path;
		}
	}
	
	/**
	 * Read text from file
	 * 
	 * @param file The file to read
	 * @return The text contained in the file
	 */
	public static String read(File file) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
			
		} catch (FileNotFoundException e) {
			Pipeline2Logger.logger().error("File not found", e);
			
		} catch (IOException e) {
			Pipeline2Logger.logger().error("An error occured while reading the file", e);
			
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				Pipeline2Logger.logger().error("Could not close FileReader", e);
			}
		}
		return null;
	}
	
}
