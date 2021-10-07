package org.liblouis;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.jna.Callback;
import com.sun.jna.DefaultTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.StringArray;
import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;
import com.sun.jna.TypeMapper;

import org.slf4j.LoggerFactory;

public class Louis {
	
	private static File libraryPath = null;
	
	/**
	 * To have any effect, this method needs to be called before any other method in Louis or
	 * Translator is called. If no library path is provided, the binaries embedded inside this JAR
	 * will be used.
	 */
	public static void setLibraryPath(File path) {
		libraryPath = path;
	}
	
	private static Lou_TableResolver lou_tableResolver = null;
	private static TableResolver tableResolver = null;
	private static boolean tableResolverIsRegistered = false;
	private static boolean tablesAreIndexed = false;
	// table names are generated for provided URLs without a name
	private final static Map<String,URL> generatedTableNames = new HashMap<String,URL>();
	// non-file URLs returned by resolver are read and stored to temporary files
	private final static Map<URL,File> tablesStoredToFile = new HashMap<URL,File>();
	private final static Map<File,URL> tablesStoredToFileInv = new HashMap<File,URL>();
	
	static synchronized String getTableNameForURL(URL table) {
		int i = 1;
		for (;;) {
			String name = "" + i;
			if (!generatedTableNames.containsKey(name)) {
				generatedTableNames.put(name, table);
				return name;
			}
			i++;
		}
	}
	
	public static synchronized void setTableResolver(final TableResolver tableResolver) {
		Louis.tableResolver = tableResolver;
		lou_tableResolver = new Lou_TableResolver() {
				public File[] invoke(String table, File base) {
					File[] ret = _invoke(table, base);
					if (ret == null)
						log(Logger.Level.ERROR, "Cannot resolve table '%s'", table);
					return ret;
				}
				private File[] _invoke(String table, File base) {
					URL baseURL;
					if (base == null)
						baseURL = null;
					else {
						try {
							base = base.getCanonicalFile(); }
						catch (IOException e) {
							throw new RuntimeException(e); }
						baseURL = tablesStoredToFileInv.get(base);
						if (baseURL == null)
							baseURL = asURL(base);
					}
					URL tableURL;
					if (base == null && generatedTableNames.containsKey(table)) {
						tableURL = generatedTableNames.get(table);
					} else {
						tableURL = tableResolver.resolve(table, baseURL);
						if (tableURL == null)
							return null; // table cannot be resolved
					}
					File tableFile;
					if (tablesStoredToFile.containsKey(tableURL)) {
						tableFile = tablesStoredToFile.get(tableURL);
					} else {
						if (tableURL.toString().startsWith("file:")) {
							tableFile = asFile(tableURL);
							if (!tableFile.exists())
								return null; // table cannot be resolved
						} else {
							// save to temporary file
							InputStream in = null;
							try {
								in = tableURL.openStream();
								tableFile = File.createTempFile("liblouis-java-", ".tbl");
								tableFile.delete();
								logger.trace("Copying " + tableURL + " to " + tableFile);
								Files.copy(in, tableFile.toPath());
								tableFile.deleteOnExit();
							} catch (IOException e) {
								return null; // table cannot be resolved
							} finally {
								if (in != null) try { in.close(); } catch (IOException e) {}
							}
							try {
								tableFile = tableFile.getCanonicalFile(); }
							catch (IOException e) {
								throw new RuntimeException(e); }
							tablesStoredToFile.put(tableURL, tableFile);
							tablesStoredToFileInv.put(tableFile, tableURL);
						}
					}
					return new File[]{tableFile};
				}
			};
		tableResolverIsRegistered = false;
	}
	
	private static Logger logCallback = null;
	private static Lou_LogCallback lou_logCallback = null;
	private static boolean loggerIsRegistered = false;
	
	public static synchronized void setLogger(final Logger logger) {
		logCallback = logger;
		lou_logCallback = new Lou_LogCallback() {
			public void invoke(int level, String message) {
				logger.log(Logger.Level.from(level), message);
			}
		};
		loggerIsRegistered = false;
	}
	
	public static void setLogLevel(Logger.Level level) {
		getLibrary().lou_setLogLevel(level.value());
	}
	
	static void log(Logger.Level level, String format, Object... args) {
		Slf4jLogger.INSTANCE.log(level, format, args);
		LouisLibrary lib = getLibrary();
		if (logCallback != null && !(logCallback instanceof Slf4jLogger))
			if (args.length > 0) {
				String[] message = new String[1 + args.length];
				message[0] = format;
				for (int i = 0; i < args.length; i++)
					message[1 + i] = args[i].toString();
				lib._lou_logMessage(level.value(), message);
			} else
				lib._lou_logMessage(level.value(), format);
	}
	
	/**
	 * Get the version number of Liblouis.
	 */
	public static String getVersion() {
		return getLibrary().lou_version();
	}
	
	/**
	 * Get a list of all the top-level tables.
	 */
	public static synchronized Set<Table> listTables() {
		Set<Table> tables = new HashSet<Table>();
		LouisLibrary lib = getLibrary();
		lazyIndexTables(lib);
		for (String t : lib.lou_listTables())
			tables.add(new Table(t));
		return Collections.unmodifiableSet(tables);
	}
	
	static synchronized String findTable(String query) {
		LouisLibrary lib = getLibrary();
		lazyIndexTables(lib);
		return lib.lou_findTable(query);
	}
	
	private static void lazyIndexTables(LouisLibrary lib) {
		if (!tablesAreIndexed && tableResolver != null) {
			Set<String> allFiles = tableResolver.list();
			logger.debug("Indexing tables");
			lib.lou_indexTables(allFiles.toArray(new String[allFiles.size()]));
			tablesAreIndexed = true;
		}
	}
	
	private static LouisLibrary INSTANCE;
	private static boolean standalone = true;
	
	static synchronized LouisLibrary getLibrary() {
		if (INSTANCE == null) {
			// look for binaries inside this JAR first (by default this is done only as a last resort in JNA)
			if (libraryPath == null) {
				try {
					libraryPath = Native.extractFromResourcePath(
						Platform.isWindows() ? "liblouis" : "louis", // otherwise we have to rename the DLL files which
						                                             // is not so easy to do in Maven
						Louis.class.getClassLoader()); }
				catch (IOException e) {
					// binaries are only included in standalone version
				}
			}
			try {
				String name = (libraryPath != null) ? libraryPath.getCanonicalPath() : "louis";
				LouisLibrary unsynced = (LouisLibrary)Native.loadLibrary(name, LouisLibrary.class);
				INSTANCE = (LouisLibrary)Native.synchronizedLibrary(unsynced);
				logger.debug("Loaded " + name + ": Liblouis v" + INSTANCE.lou_version() + " (UCS" + INSTANCE.lou_charSize() + ")");
			} catch (IOException e) {
				throw new RuntimeException("Could not load liblouis", e);
			} finally {
				// delete the binary if it was extracted by JNA
				if (libraryPath != null && libraryPath.getName().startsWith("jna"))
					if (!libraryPath.delete())
						// mark for later removal by JNA
						try {
							new File(libraryPath.getParentFile(), libraryPath.getName() + ".x").createNewFile(); }
						catch (IOException e) { /* ignore */ }
			}
		}
		if (tableResolver == null && standalone) {
			try {
				setTableResolver(new EmbeddedTableResolver(logger)); }
			catch (Throwable e) {
				// tables are included in "resources" artifact
				// if it is not on the class path it will result in an exception in EmbeddedTableResolver.listResources
				standalone = false;
			}
		}
		if (logCallback == null)
			// pass on log messages to SLF4J
			setLogger(Slf4jLogger.INSTANCE);
		if (lou_tableResolver != null && !tableResolverIsRegistered) {
			INSTANCE.lou_registerTableResolver(lou_tableResolver);
			tableResolverIsRegistered = true;
			tablesAreIndexed = false;
		}
		if (!loggerIsRegistered && lou_logCallback != null) {
			INSTANCE.lou_registerLogCallback(lou_logCallback);
			loggerIsRegistered = true;
		}
		return INSTANCE;
	}
	
	interface LouisLibrary extends Library {
		
		public int lou_translate(String tableList, WideCharString inbuf, IntByReference inlen,
				WideCharString outbuf, IntByReference outlen, short[] typeform, byte[] spacing,
				int[] outputPos, int[] inputPos, IntByReference cursorPos, int mode);
		
		public int lou_backTranslate(String tableList, WideCharString inbuf, IntByReference inlen,
				WideCharString outbuf, IntByReference outlen, short[] typeform, byte[] spacing,
				int[] outputPos, int[] inputPos, IntByReference cursorPos, int mode);
		
		public int lou_hyphenate(String tableList, WideCharString inbuf, int inlen, byte[] hyphens, int mode);
		
		public int lou_dotsToChar(String tableList, WideCharString inbuf, WideCharString outbuf, int length, int mode);
		
		public int lou_charToDots(String tableList, WideCharString inbuf, WideCharString outbuf, int length, int mode);
		
		public int lou_charSize();
		
		public String lou_version();
		
		public Pointer lou_getTable(String tableList);
		
		/**
		 * Note that keeping resolver from being garbage collection is the
		 * responsibility of the caller.
		 */
		public void lou_registerTableResolver(Lou_TableResolver resolver);
		
		/**
		 * Note that keeping logger from being garbage collection is the
		 * responsibility of the caller.
		 */
		public void lou_registerLogCallback(Lou_LogCallback logger);
		
		public void lou_setLogLevel(int level);
		
		public int lou_indexTables(String[] tables);
		
		public String lou_findTable(String query);
		
		public String lou_getTableInfo(String table, String key);
		
		public String[] lou_listTables();
		
		public void _lou_logMessage(int level, String... format);
		
		public String[] lou_getEmphClasses(String tableList);
		
	}
	
	interface Lou_TableResolver extends Callback {
		
		public File[] invoke(String tableList, File base);
		
		public TypeMapper TYPE_MAPPER = FileTypeMapper.INSTANCE;
		
	}
	
	interface Lou_LogCallback extends Callback {
		
		public void invoke(int level, String message);
		
	}
	
	static class FileTypeMapper extends DefaultTypeMapper {
		
		static final FileTypeMapper INSTANCE = new FileTypeMapper();
		
		private FileTypeMapper() {
			TypeConverter converter = new TypeConverter() {
				public Class<?> nativeType() {
					return String.class;
				}
				public Object toNative(Object file, ToNativeContext context) {
					if (file == null)
						return null;
					if (file instanceof File[]) {
						File[] files = ((File[])file);
						String[] paths = new String[files.length];
						for (int i = 0; i < files.length; i++)
							paths[i] = (String)toNative(files[i], context);
						return new StringArray(paths); }
					try { return ((File)file).getCanonicalPath(); }
					catch (IOException e) { return null; }
				}
				public Object fromNative(Object file, FromNativeContext context) {
					if (file == null)
						return null;
					return new File(((Pointer)file).getString(0));
				}
			};
			addToNativeConverter(File.class, converter);
			addToNativeConverter(File[].class, converter);
			addFromNativeConverter(File.class, converter);
		}
	}
	
	static File asFile(URL url) throws IllegalArgumentException {
		try {
			if (!"file".equals(url.getProtocol()))
				throw new RuntimeException("expected file URL");
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e); // should not happen
		}
	}
	
	@SuppressWarnings("deprecation")
	static URL asURL(File file) {
		try {
			file = file.getCanonicalFile();
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e); // should not happen
		} catch (IOException e) {
			throw new RuntimeException(e); // should not happen
		}
	}
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Louis.class);
	
	private static class Slf4jLogger implements Logger {
		static Slf4jLogger INSTANCE = new Slf4jLogger();
		public void log(Level level, String message) {
			switch (level) {
			case DEBUG: logger.debug(message); break;
			case INFO:  logger.info(message);  break;
			case WARN:  logger.warn(message);  break;
			case FATAL:
				message = "FATAL: " + message;
			case ERROR: logger.error(message); break;
			}
		}
		void log(Level level, String format, Object... args) {
			log(level, String.format(format, args));
		}
	}
}
