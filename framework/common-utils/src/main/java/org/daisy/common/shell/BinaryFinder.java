package org.daisy.common.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.AbstractIterator;
import static com.google.common.collect.Iterables.concat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryFinder {
	
	private static final Logger logger = LoggerFactory.getLogger(BinaryFinder.class);
	
	/**
	 * Look for a given executable in the PATH environment variable.
	 * 
	 * @return the absolute path of the executable if it exists, an absent
	 *         optional otherwise.
	 */
	public static Optional<String> find(String executableName) {
		String os = System.getProperty("os.name");
		return find(executableName,
		            (os != null && os.startsWith("Windows")) ? winExtensions : nixExtensions,
		            getPath());
	}
	
	private static Iterable<String> path;
	
	private static Iterable<String> getPath() {
		if (path == null) {
			List<Iterable<String>> paths = new ArrayList<>();
			paths.add(pathFromEnv());
			String os = System.getProperty("os.name");
			if (os == null || !os.startsWith("Windows")) {
				paths.add(pathFromPathHelper());
				paths.add(pathFromShell());
				paths.add(asList(nixUltimateFallbackPath));
			}
			path = memoize(removeDuplicates(concat(paths)));
		}
		return path;
	}

	static Optional<String> find(String executableName, String[] extensions, Iterable<String> path) {
		for (String ext : extensions) {
			String fullname = executableName + ext;
			logger.debug("looking for " + fullname + " ...");
			for (String pathDir : path) {
				logger.debug("... in " + pathDir);
				File file = new File(pathDir, fullname);
				if (file.isFile()) {
					logger.debug("found: " + file.getAbsolutePath());
					return Optional.of(file.getAbsolutePath());
				}
			}
		}
		return Optional.empty();
	}

	private static final String[] winExtensions = {
	        ".exe", ".bat", ".cmd", ".bin", ""
	};
	private static final String[] nixExtensions = {
	        "", ".run", ".bin", ".sh"
	};
	private static final String[] nixUltimateFallbackPath = {
		"/usr/bin", "/bin", "/usr/sbin", "/sbin", "/usr/local/bin"
	};
	
	static Iterable<String> pathFromString(String path, String pathSeparator) {
		if (path == null || pathSeparator == null)
			return emptyList;
		return asList(path.split(pathSeparator));
	}
	
	/*
	 * When Pipeline is launched via a terminal window, the PATH
	 * environment variable is determined by what it was set to in the
	 * shell.
	 *
	 * When Pipeline is launched as a Mac OS app, the PATH is
	 * determined by what it was set to in launchd. Variables may be
	 * set via launchctl:
	 *
	 *     launchctl setenv PATH ...
	 *
	 * With up to and including Mac OS 10.9 (Mavericks), the
	 * /etc/launchd.conf file may be used to persist the setting
	 * across reboots. In 10.10 (Yosemite) and later a launch agent
	 * (~/Library/LaunchAgents/foo.plist) must be used for this.
	 *
	 * With 10.7 (Lion) and older you may set environment variables in
	 * ~/.MacOSX/environment.plist.
	 */
	static Iterable<String> pathFromEnv() {
		return pathFromString(System.getenv("PATH"), File.pathSeparator);
	}
	
	/*
	 * Because often environment variables are set by the shell and
	 * people may not realize that in general these variables are not
	 * available in Mac OS applications, we try to make them available
	 * anyway.
	 *
	 * /usr/libexec/path_helper is a utility intended to be used by
	 * shells for constructing the PATH environment variable. It reads
	 * from the files /etc/paths and /etc/paths.d/*.
	 */
	static Iterable<String> pathFromPathHelper() {
		return new PathFromPathHelper();
	}
	
	static class PathFromPathHelper implements Iterable<String> {
		public Iterator<String> iterator() {
			String pathHelperExec = "/usr/libexec/path_helper";
			if (new File(pathHelperExec).isFile()) {
				logger.debug("invoking: " + pathHelperExec + " -s");
				ProcessBuilder builder = new ProcessBuilder(pathHelperExec, "-s");
				try {
					Process process = builder.start();
					process.waitFor();
					BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
					try {
						String line;
						while ((line = br.readLine()) != null) {
							logger.debug(line);
							if (line.startsWith("PATH=\"") && line.endsWith("\"; export PATH;"))
								return pathFromString(line.substring(6, line.length() - 15), File.pathSeparator).iterator();
						}
					} finally {
						br.close();
					}
				} catch(IOException e) {
					logger.debug("failed: " + e);
				} catch(InterruptedException e) {
					logger.debug("failed: " + e);
				}
			}
			return emptyList.iterator();
		}
	}
	
	/*
	 * The PATH may be set in shell startup files such as
	 * /etc/profile, ~/.profile, /etc/bash.bashrc, ~/.bash_profile,
	 * ~/.bash_login, ~/.bashrc, /etc/csh.login and /etc/zshenv. Some
	 * of these files invoke /usr/libexec/path_helper but they can
	 * also set the PATH in other ways.
	 */
	static Iterable<String> pathFromShell() {
		return new PathFromShell();
	}
	
	static class PathFromShell implements Iterable<String> {
		public Iterator<String> iterator() {
			String shellExec = System.getenv("SHELL");
			if (shellExec == null)
				shellExec = "/bin/sh";
			if (new File(shellExec).isFile()) {
				logger.debug("invoking: " + shellExec + " -c \"echo $PATH\"");
				ProcessBuilder builder = new ProcessBuilder(shellExec, "-c", "echo $PATH");
				try {
					Process process = builder.start();
					process.waitFor();
					Scanner s = new Scanner(process.getInputStream()).useDelimiter("\\A");
					try {
						if (s.hasNext()) {
							String output = s.next();
							logger.debug(output);
							return pathFromString(output, File.pathSeparator).iterator();
						}
					} finally {
						s.close();
					}
				} catch(IOException e) {
					logger.debug("failed: " + e);
				} catch(InterruptedException e) {
					logger.debug("failed: " + e);
				}
			}
			return emptyList.iterator();
		}
	}
	
	private final static Iterable<String> emptyList = emptyList();
	
	static <T> Iterable<T> removeDuplicates(final Iterable<T> iterable) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return new AbstractIterator<T>() {
					Set<T> previous;
					Iterator<T> iterator;
					protected T computeNext() {
						if (iterator == null)
							iterator = iterable.iterator();
						while (iterator.hasNext()) {
							T next = iterator.next();
							if (previous == null)
								previous = new HashSet<T>();
							else if (previous.contains(next))
								continue;
							previous.add(next);
							return next;
						}
						return endOfData();
					}
				};
			}
		};
	}
	
	static <T> Iterable<T> memoize(final Iterable<T> iterable) {
		return new Iterable<T>() {
			final ArrayList<T> cache = new ArrayList<T>();
			public Iterator<T> iterator() {
				return new Iterator<T>() {
					Iterator<T> iterator;
					int index = 0;
					public boolean hasNext() {
						synchronized(cache) {
							if (index < cache.size())
								return true;
							if (iterator == null)
								iterator = iterable.iterator();
							return iterator.hasNext();
						}
					}
					public T next() throws NoSuchElementException {
						synchronized(cache) {
							if (index < cache.size())
								return cache.get(index++);
							if (iterator == null)
								iterator = iterable.iterator();
							T next = iterator.next();
							cache.add(next);
							index++;
							return next;
						}
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
