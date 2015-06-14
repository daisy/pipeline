package org.daisy.braille.pef;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class PEFLibrary {
    private final static FileFilter ff;
    private final static Logger logger;
    static {
    	ff = new FileFilter() {
    		@Override
    		public boolean accept(File pathname) {
    			return pathname.isDirectory() || pathname.getName().endsWith(".pef");
    		}
        };
    	logger = Logger.getLogger(PEFLibrary.class.getCanonicalName());
    }

    /**
     * Lists pef files in the specified folder and sub folders
     * @param dir the folder to start search
     * @return
     */
	public static Collection<File> listFiles(File dir) {
		return listFiles(dir, true);
	}

	public static Collection<File> listFiles(File dir, boolean recursive) {
    	ArrayList<File> files = new ArrayList<File>();
    	listFiles(files, dir, recursive);
		return files;
	}
	
    private static void listFiles(List<File> files, File dir, boolean recursive) {
    	File[] listFiles = dir.listFiles(ff);
    	if (listFiles==null) {
    		return;
    	}
		for (File f : listFiles) {
			if (recursive && f.isDirectory()) {
				logger.fine("Scanning dir " + f);
				listFiles(files, f, recursive);
			} else if (f.exists()) {
				logger.fine("Adding file: " + f);
				files.add(f);
			} else {
				// ignore
			}
		}
    }
}