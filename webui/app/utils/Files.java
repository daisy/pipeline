package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import play.Logger;

/**
 * Utility class for working with files, especially ZIP-files.
 * 
 */
public class Files {
	
	/**
	 * Same as listZipFiles(File zipfile), except that this will
	 * return a list of [ zip entry name , content type ] pairs.
	 * The content type is based purely on the filename.
	 * 
	 * @param zipfile
	 * @return
	 */
	public static List<FileInfo> listZipFilesWithContentType(File zipfile) {
		List<FileInfo> files = new ArrayList<FileInfo>();
		
		try {
			ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				
				if (!entry.isDirectory()) {
					String entryName = entry.getName();
					String contentType = ContentType.probe(entryName, zin);
					Long entrySize = entry.getSize();
					Logger.debug("file: "+entryName+" | contentType: "+contentType+" | "+entrySize);
					files.add(new FileInfo(entryName, contentType, entrySize));
				}
				
				entry = zin.getNextEntry();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return files;
	}
	
}
