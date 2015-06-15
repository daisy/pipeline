package org.daisy.braille.pef;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Provides comparing of two folders with xml files for differences.
 * Meta data in the files is ignored when comparing.
 * 
 * A flat organization of files is assumed.
 * 
 * Warnings are generated if stray files are found (a file with the same name
 * cannot be found in the other folder) or if the folders contain folders
 * or files not matching the file filter.
 * 
 * @author Joel Håkansson
 */
public class PEFFileBatchCompare {
	private final FileFilter filter;
	private final NormalizationResource nr;
	private final List<String> notices;
	private final List<String> warnings;
	private final List<Diff> diffs;
	private final List<String> oks;
	private int checked;
	private String unbraillerTable;
	
	public static class Diff {
		private final String key;
		private final int pos;

		public Diff(String key, int pos) {
			super();
			this.key = key;
			this.pos = pos;
		}

		public String getKey() {
			return key;
		}

		public int getPos() {
			return pos;
		}

	}

	/**
	 * 
	 * @param path1 a folder
	 * @param path2 another folder
	 * @throws FileNotFoundException
	 * @throws IllegalArgumentException if path is not a directory
	 */
	public PEFFileBatchCompare(FileFilter filter, NormalizationResource nr) {
		this.filter = filter;
		this.nr = nr;
		notices = new ArrayList<String>();
		warnings = new ArrayList<String>();
		diffs = new ArrayList<Diff>();
		oks = new ArrayList<String>();
		checked = 0;
		this.unbraillerTable = null;
	}
	
	public PEFFileBatchCompare(FileFilter filter) {
		this(filter, null);
	}

	public String getUnbraillerTable() {
		return unbraillerTable;
	}

	public void setUnbraillerTable(String unbraillerTable) {
		this.unbraillerTable = unbraillerTable;
	}

	public void run(String path1, String path2) throws FileNotFoundException {
		final File dir1 = getExistingPath(path1);
		final File dir2 = getExistingPath(path2);
		if (!dir1.isDirectory()) {
			throw new IllegalArgumentException("Path is not a directory: " +path1);
		}
		if (!dir2.isDirectory()) {
			throw new IllegalArgumentException("Path is not a directory: " +path2);
		}
		
		final HashMap<String, Integer> x = new HashMap<String, Integer>();
		final HashMap<String, File> files1 = new HashMap<String, File>();
		final HashMap<String, File> files2 = new HashMap<String, File>();
		PefFileFilter dir1Matches = new PefFileFilter(filter);
		PefFileFilter dir2Matches = new PefFileFilter(filter);
		for (File f : dir1.listFiles(dir1Matches)) {
			files1.put(f.getName(), f);
			x.put(f.getName(), 1);
		}
		for (File f : dir2.listFiles(dir2Matches)) {
			files2.put(f.getName(), f);
			Integer val = x.get(f.getName());
			if (val==null) {
				val = 2;
			} else {
				val = 0;
			}
			x.put(f.getName(), val);
		}
		
		for (File f : dir1Matches.getOtherFiles()) {
			warning(f + " will not be examined.");
		}
		
		for (File f : dir2Matches.getOtherFiles()) {
			warning(f + " will not be examined.");
		}

		checked += Math.min(files1.size(), files2.size());
		
		ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		int i2 = 1;
		for (final String key : x.keySet()) {
			final int i = i2;
			i2++;
			e.execute(new Runnable() {
		        public void run() {

					System.out.println("Comparing file " + key + " in " + dir1 + " and " + dir2 + " (" + i + "/" + x.size() + ")");
					
					int v = x.get(key);
					if (v!=0) {
						notice("Unmatched file '" + key + "' in " + (v==1?dir1:dir2));
					} else {
						File f1 = files1.get(key);
						File f2 = files2.get(key);
		
						try {
							PEFFileCompare fcc;
							if (nr == null) {
								fcc = new PEFFileCompare();
							} else {
								fcc = new PEFFileCompare(nr);
							}
							boolean ok = fcc.compare(f1, f2);
							if (!ok) {
								diff(key, fcc.getPos());
							} else {
								ok(key);
							}
						} catch (PEFFileCompareException e) {
							warning("An exception was thrown.");
							e.printStackTrace();
						}
					}
		        }
		    });

		}
		e.shutdown();
		try {
			e.awaitTermination(10 * 60, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}

	private void notice(String msg) {
		notices.add(msg);
	}

	private void warning(String msg) {
		warnings.add(msg);
	}
	
	private void diff(String filename, int pos) {
		diffs.add(new Diff(filename, pos));
	}
	
	private void ok(String filename) {
		oks.add(filename);
	}
	
	public List<String> getNotices() {
		return notices;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public List<Diff> getDiffs() {
		return diffs;
	}
	
	public List<String> getOk() {
		return oks;
	}
	
	public int checkedCount() {
		return checked;
	}

	private static File getExistingPath(String path) throws FileNotFoundException {
		File ret = new File(path);
		if (!ret.exists()) {
			throw new FileNotFoundException("Path does not exist: " + path);
		}
		return ret;
	}
	
	private class PefFileFilter implements FileFilter {
		private final FileFilter filter;
		private ArrayList<File> noMatch;
		
		public PefFileFilter(FileFilter filter) {
			this.filter = filter;
			noMatch = new ArrayList<File>();
		}
		
		public List<File> getOtherFiles() {
			return noMatch;
		}

		public boolean accept(File pathname) {
			boolean isMatch = filter.accept(pathname);
			if (!isMatch) {
				noMatch.add(pathname);
			}
			return isMatch;
		}
		
	}

}
