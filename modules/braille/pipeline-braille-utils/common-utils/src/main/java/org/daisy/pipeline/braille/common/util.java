package org.daisy.pipeline.braille.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import static com.google.common.base.Functions.forPredicate;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.primitives.Bytes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class util {
	
	public static interface Function0<T> {
		public T apply();
	}
	
	public static interface Function2<T1,T2,T3> {
		public T3 apply(T1 object1, T2 object2);
	}
	
	public static class Tuple2<T1,T2> {
		public final T1 _1;
		public final T2 _2;
		public Tuple2(T1 _1, T2 _2) {
			this._1 = _1;
			this._2 = _2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
			result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple2<?,?> other = (Tuple2<?,?>) obj;
			if (_1 == null) {
				if (other._1 != null)
					return false;
			} else if (!_1.equals(other._1))
				return false;
			if (_2 == null) {
				if (other._2 != null)
					return false;
			} else if (!_2.equals(other._2))
				return false;
			return true;
		}
	}
	
	public static class Tuple3<T1,T2,T3> {
		public final T1 _1;
		public final T2 _2;
		public final T3 _3;
		public Tuple3(T1 _1, T2 _2, T3 _3) {
			this._1 = _1;
			this._2 = _2;
			this._3 = _3;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
			result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
			result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tuple3<?,?,?> other = (Tuple3<?,?,?>) obj;
			if (_1 == null) {
				if (other._1 != null)
					return false;
			} else if (!_1.equals(other._1))
				return false;
			if (_2 == null) {
				if (other._2 != null)
					return false;
			} else if (!_2.equals(other._2))
				return false;
			if (_3 == null) {
				if (other._3 != null)
					return false;
			} else if (!_3.equals(other._3))
				return false;
			return true;
		}
	}
		
	public static abstract class Functions {
		
		public static Function0<Void> noOp = new Function0<Void> () {
			public Void apply() { return null; }
		};
		
		public static <F> Function<F,Void> sideEffect(final Function<? super F,?> function) {
			return new Function<F,Void>() {
				public Void apply(F input) {
					function.apply(input);
					return null;
				}
			};
		}
		
		public static <F> Function<F,Void> doAll(final Iterable<Function<? super F,Void>> functions) {
			return new Function<F,Void>() {
				public Void apply(F input) {
					for (Function<? super F,Void> f : functions)
						f.apply(input);
					return null;
				}
			};
		}
	}
	
	public static abstract class Predicates {
		
		private static Predicate<String> matchesRegexPattern(final Pattern pattern) {
			return new Predicate<String>() {
				public boolean apply(String s) {
					return pattern.matcher(s).matches(); }};
		}
		
		public static Predicate<String> matchesRegexPattern(final String pattern) {
			return matchesRegexPattern(Pattern.compile(pattern));
		}
		
		public static Predicate<String> matchesGlobPattern(final String pattern) {
			return matchesRegexPattern(GlobPattern.compile(pattern));
		}
	}
	
	public static abstract class Iterators {
		
		public static <T1,T2> T1 fold(Iterator<T2> iterator, Function2<? super T1,? super T2,? extends T1> function, T1 seed) {
			T1 result = seed;
			while(iterator.hasNext()) result = function.apply(result, iterator.next());
			return result;
		}
		
		public static <T> T reduce(Iterator<T> iterator, Function2<? super T,? super T,? extends T> function) {
			T seed = iterator.next();
			return Iterators.<T,T>fold(iterator, function, seed);
		}
		
		public static <T> Tuple2<Collection<T>,Collection<T>> partition(Iterator<T> iterator, Predicate<? super T> predicate) {
			Multimap<Boolean,T> map = Multimaps.index(iterator, forPredicate(predicate));
			return new Tuple2<Collection<T>,Collection<T>>(map.get(true), map.get(false));
		}
	}
	
	public static abstract class Iterables {
		
		public static <T> Iterable<T> memoize(final Iterable<T> iterable) {
			return new Memoize<T>() {
				protected Iterator<T> _iterator() {
					return iterable.iterator();
				}
				@Override
				public String toString() {
					return "memoize( " + iterable + " )";
				}
			};
		}
		
		protected static abstract class Memoize<T> implements Iterable<T> {
			private final ArrayList<T> cache = new ArrayList<T>();
			protected abstract Iterator<T> _iterator();
			private Iterator<T> _iterator;
			public final Iterator<T> iterator() {
				return new Iterator<T>() {
					private int index = 0;
					public boolean hasNext() {
						synchronized(cache) {
							if (index < cache.size())
								return true;
							if (_iterator == null)
								_iterator = _iterator();
							return _iterator.hasNext();
						}
					}
					public T next() throws NoSuchElementException {
						synchronized(cache) {
							if (index < cache.size())
								return cache.get(index++);
							if (_iterator == null)
								_iterator = _iterator();
							T next = _iterator.next();
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
		}
		
		public static <T extends Cloneable> Iterable<T> clone(final Iterable<T> iterable) {
			return new Clone<T>() {
				protected Iterator<T> _iterator() {
					return iterable.iterator();
				}
			};
		}
		
		protected static abstract class Clone<T extends Cloneable> implements Iterable<T> {
			protected abstract Iterator<T> _iterator();
			public final Iterator<T> iterator() {
				return new AbstractIterator<T>() {
					Iterator<T> iterator = _iterator();
					@SuppressWarnings("unchecked")
					protected T computeNext() {
						T next;
						try {
							next = iterator.next(); }
						catch (NoSuchElementException e) {
							return endOfData(); }
						try {
							return (T)next.getClass().getMethod("clone").invoke(next); }
						catch (IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException
								| NoSuchMethodException
								| SecurityException e) {
							throw new RuntimeException("Could not invoke clone() method", e);
						}
					}
				};
			}
		}
	}
	
	public static abstract class OS {
		
		public static enum Family {
			LINUX ("linux"),
			MACOSX ("macosx"),
			WINDOWS ("windows");
			private final String name;
			private Family(String name) { this.name = name; }
			public String toString() { return name; }
		}
		
		public static Family getFamily() {
			String name = System.getProperty("os.name").toLowerCase();
			if (name.startsWith("windows"))
				return Family.WINDOWS;
			else if (name.startsWith("mac os x"))
				return Family.MACOSX;
			else if (name.startsWith("linux"))
				return Family.LINUX;
			else
				throw new RuntimeException("Unsupported OS: " + name);
		}
		
		public static boolean isWindows() {
			return getFamily() == Family.WINDOWS;
		}
		
		public static boolean isMacOSX() {
			return getFamily() == Family.MACOSX;
		}
		
		public static String getArch() {
			return System.getProperty("os.arch").toLowerCase();
		}
		
		public static boolean is64Bit() {
			return getArch().equals("amd64") || getArch().equals("x86_64");
		}
	}
	
	public static abstract class Strings {
		
		@SuppressWarnings(
			"unchecked" // safe cast to Iterator<Object>
		)
		public static <T> String join(Iterator<? extends T> strings, final Object separator, final Function<? super T,String> toStringFunction) {
			if (!strings.hasNext()) return "";
			String seed = toStringFunction.apply(strings.next());
			return Iterators.<String,T>fold(
				(Iterator<T>)strings,
				new Function2<String,T,String>() {
					public String apply(String s1, T s2) {
						return s1 + separator.toString() + toStringFunction.apply(s2); }},
				seed);
		}
		
		public static String join(Iterator<? extends Object> strings, final Object separator) {
			return join(strings, separator, toStringFunction());
		}
		
		public static <T> String join(Iterable<? extends T> strings, final Object separator, final Function<? super T,String> toStringFunction) {
			return join(strings.iterator(), separator, toStringFunction);
		}
		
		public static String join(Iterable<?> strings, final Object separator) {
			return join(strings.iterator(), separator);
		}
		
		public static String join(Iterable<?> strings) {
			return join(strings, "");
		}
		
		public static <T> String join(T[] strings, Object separator, final Function<? super T,String> toStringFunction) {
			return join(Arrays.asList(strings), separator, toStringFunction);
		}
		
		public static String join(Object[] strings, Object separator) {
			return join(Arrays.asList(strings), separator);
		}
		
		public static String join(Object[] strings) {
			return join(strings, "");
		}
		
		public static String normalizeSpace(Object object) {
			return String.valueOf(object).replaceAll("\\s+", " ").trim();
		}
		
		public static Tuple2<String,byte[]> extractHyphens(String text, Character... characters) {
			return extractHyphens(null, text, characters);
		}
		
		public static Tuple2<String,byte[]> extractHyphens(byte[] addTo, String text, Character... characters) {
			StringBuilder unhyphenatedText = new StringBuilder();
			List<Byte> hyphens = new ArrayList<Byte>();
			byte hyphen = 0;
			int j = -1;
			next_char: for (char c : text.toCharArray()) {
				j++;
				if (addTo != null && j > 0)
					hyphen |= addTo[j - 1];
				for (int i = 0; i < characters.length; i++)
					if (characters[i] != null && characters[i] == c) {
						hyphen |= (1 << i);
						continue next_char; }
				unhyphenatedText.append(c);
				hyphens.add(hyphen);
				hyphen = 0; }
			if (unhyphenatedText.length() > 0) {
				hyphens.remove(0);
				return new Tuple2<String,byte[]>(unhyphenatedText.toString(), Bytes.toArray(hyphens)); }
			else
				return new Tuple2<String,byte[]>("", null);
		}
		
		public static String insertHyphens(String text, byte hyphens[], Character... characters) {
			if (text.equals("")) return "";
			if (hyphens == null) return text;
			if (hyphens.length != text.length()-1)
				throw new RuntimeException("hyphens.length must be equal to text.length() - 1");
			StringBuilder hyphenatedText = new StringBuilder();
			int i;
			for (i = 0; i < hyphens.length; i++) {
				hyphenatedText.append(text.charAt(i));
				for (int j = 0; j < characters.length; j++) {
					byte b = (byte)(1 << j);
					Character c = characters[j];
					if (c != null && (hyphens[i] & b) == b)
						hyphenatedText.append(c); }}
			hyphenatedText.append(text.charAt(i));
			return hyphenatedText.toString();
		}
		
		public static Iterable<String> splitInclDelimiter(final String text, final Pattern delimiterPattern) {
			return new Iterable<String>() {
				public final Iterator<String> iterator() {
					return new AbstractIterator<String>() {
						Matcher m = delimiterPattern.matcher(text);
						int i = 0;
						String nextNext = null;
						protected String computeNext() {
							if (nextNext != null) {
								String next = nextNext;
								nextNext = null;
								return next; }
							if (m.find()) {
								String next = text.substring(i, m.start());
								nextNext = m.group();
								i = m.end();
								return next; }
							else if (i >= 0) {
								String next = text.substring(i);
								i = -1;
								return next; }
							else
								return endOfData();
						}
					};
				}
			};
		}
	}
	
	public static abstract class URIs {
		
		/* If object is a String, it is assumed to represent a URI */
		public static URI asURI(Object o) {
			if (o == null)
				return null;
			try {
				if (o instanceof String)
					return new URI((String)o);
				if (o instanceof File)
					return ((File)o).toURI();
				if (o instanceof URL) {
					URL url = (URL)o;
					if (url.getProtocol().equals("jar"))
						return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
					String authority = (url.getPort() != -1) ?
						url.getHost() + ":" + url.getPort() :
						url.getHost();
					return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }
				if (o instanceof URI)
					return (URI)o; }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to URI: " + o);
		}
		
		public static Function<Object,URI> asURI = new Function<Object,URI>() {
			public URI apply(Object o) {
				return asURI(o);
			}
		};
		
		public static URI resolve(Object base, Object uri) {
			URI baseURI = asURI(base);
			if (baseURI.toString().startsWith("jar:"))
				return asURI("jar:" + asURI(baseURI.toString().substring(4)).resolve(asURI(uri)).toASCIIString());
			else
				return baseURI.resolve(asURI(uri));
		}
		
		public static URI relativize(Object base, Object uri) {
			return asURI(base).relativize(asURI(uri));
		}
	}
	
	public static abstract class URLs {
		
		/* If object is a String, it is assumed to represent a URI */
		public static URL asURL(Object o) {
			if (o == null)
				return null;
			try {
				if (o instanceof String)
					return asURL(URIs.asURI(o));
				if (o instanceof File)
					return asURL(URIs.asURI(o));
				if (o instanceof URL)
					return (URL)o;
				if (o instanceof URI)
					return new URL(decode(o.toString())); }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to URL: " + o);
		}
		
		public static URL resolve(Object base, Object url) {
			if (url instanceof URI)
				return asURL(URIs.asURI(base).resolve((URI)url));
			if (url instanceof String) {
				try { return new URL(asURL(base), url.toString()); }
				catch (MalformedURLException e) { throw new RuntimeException(e); }}
			return asURL(url);
		}
		
		public static String relativize(Object base, Object url) {
			return decode(URIs.asURI(base).relativize(URIs.asURI(url)).toString());
		}
		
		@SuppressWarnings(
			"deprecation" // URLDecode.decode is deprecated
		)
		public static String decode(String uri) {
			// URIs treat the + symbol as is, but URLDecoder will decode both + and %20 into a space
			return URLDecoder.decode(uri.replace("+", "%2B"));
		}
		
		public static Function<String,String> decode = new Function<String,String>() {
			public String apply(String uri) {
				return decode(uri);
			}
		};
	}
	
	public static abstract class Files {
		
		/* If object is a String, it is assumed to represent a URI */
		public static File asFile(Object o) {
			if (o == null)
				return null;
			try {
				if (o instanceof String)
					return asFile(URIs.asURI(o));
				if (o instanceof File)
					return (File)o;
				if (o instanceof URL)
					return asFile(URIs.asURI(o));
				if (o instanceof URI)
					return new File((URI)o); }
			catch (Exception e) {}
			throw new RuntimeException("Object can not be converted to File: " + o);
		}
		
		public static boolean isAbsoluteFile(Object o) {
			if (o instanceof String)
				return ((String)o).startsWith("file:");
			try { asFile(o); }
			catch (Exception e) { return false; }
			return true;
		}
		
		public static String fileName(Object o) {
			if (o instanceof File)
				return ((File)o).getName();
			String file = URLs.asURL(o).getFile();
			return file.substring(file.lastIndexOf('/')+1);
		}
		
		public static File normalize(File file) {
			try { return file.toPath().toRealPath().normalize().toFile(); }
			catch (Exception e) { throw new RuntimeException(e); }
		}
		
		public static boolean unpack(URL url, File file) {
			if (file.exists()) return false;
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
				FileOutputStream writer = new FileOutputStream(file);
				url.openConnection();
				InputStream reader = url.openStream();
				byte[] buffer = new byte[153600];
				int bytesRead = 0;
				while ((bytesRead = reader.read(buffer)) > 0) {
					writer.write(buffer, 0, bytesRead);
					buffer = new byte[153600]; }
				writer.close();
				reader.close();
				logger.debug("Unpacking file {} ...", file.getName());
				return true; }
			catch (Exception e) {
				throw new RuntimeException(
						"Exception occured during unpacking of file '" + file.getName() + "'", e); }
		}
		
		public static Iterable<File> unpack(Iterator<URL> urls, File directory) {
			if (!directory.exists()) directory.mkdirs();
			Collection<File> files = new ArrayList<File>();
			while(urls.hasNext()) {
				URL url = urls.next();
				File file = new File(directory.getAbsolutePath(), fileName(url));
				if (unpack(url, file)) files.add(file); }
			return files;
		}
		
		public static void chmod775(File file) {
			if (OS.isWindows()) return;
			try {
				Runtime.getRuntime().exec(new String[] { "chmod", "775", file.getAbsolutePath() }).waitFor();
				logger.debug("Chmodding file {} ...", file.getName());}
			catch (Exception e) {
				throw new RuntimeException(
						"Exception occured during chmodding of file '" + file.getName() + "'", e); }
		}
	}
	
	public static abstract class Locales {
		
		public static Locale parseLocale(String locale) {
			StringTokenizer parser = new StringTokenizer(locale, "-_");
			if (parser.hasMoreTokens()) {
				String lang = parser.nextToken();
				if (parser.hasMoreTokens()) {
					String country = parser.nextToken();
					if (parser.hasMoreTokens()) {
						String variant = parser.nextToken();
						return new Locale(lang, country, variant); }
					else
						return new Locale(lang, country); }
				else
					return new Locale(lang); }
			else
				throw new IllegalArgumentException("Locale '" + locale + "' could not be parsed");
		}
		
		public static String toString(Locale locale, char separator) {
			StringBuilder string = new StringBuilder();
			String language = locale.getLanguage();
			String country = locale.getCountry();
			String variant = locale.getVariant();
			string.append(language);
			if (country.length() > 0 || variant.length() > 0)
				string.append(separator);
			if (country.length() > 0)
				string.append(country);
			if (variant.length() > 0) {
				string.append(separator);
				string.append(variant); }
			return string.toString();
		}
	}
	
	/*
	 * Naive implementation of glob syntax. Assumes the pattern is valid.
	 * @see http://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
	 */
	private static abstract class GlobPattern {
		
		private static Pattern compile(String pattern) {
			return new RegexBuilder(pattern).build();
		}
		
		private static class RegexBuilder {
			
			private final StringCharacterIterator glob;
			private boolean inClass;
			private boolean inGroup;
			
			private RegexBuilder(String globPattern) {
				glob = new StringCharacterIterator(globPattern);
			}
			
			private Pattern build() {
				StringBuilder regex = new StringBuilder();
				regex.append('^');
				inClass = false;
				inGroup = false;
				for (char c = glob.first(); c != CharacterIterator.DONE; c = glob.next()) {
					if (!inClass) {
						if (c == '[') {
							regex.append("[[^/]&&[");
							if (lookAhead() == '!')
								regex.append(glob.next());
							if (lookAhead() == '^')
								regex.append("\\" + glob.next());
							inClass = true; }
						else if (c == '{' && !inGroup) {
							regex.append("(?:(?:");
							inGroup = true; }
						else if (c == '}' && inGroup) {
							regex.append("))");
							inGroup = false; }
						else if (c == ',' && inGroup)
							regex.append(")|(?:");
						else if (c == '*' && lookAhead() == '*') {
							regex.append(".*");
							glob.next(); }
						else if (c == '*')
							regex.append("[^/]*");
						else if (c == '?')
							regex.append("[^/]");
						else if (".^$+]|()".indexOf(c) > -1)
							regex.append("\\" + c);
						else
							regex.append(c); }
					else {
						if (c == ']') {
							regex.append("]]");
							inClass = false; }
						else if (c == '&' && lookAhead() == '&')
							regex.append("\\" + c + glob.next());
						else if (c == '\\')
							regex.append("\\" + c);
						else
							regex.append(c); }}
				regex.append('$');
				return Pattern.compile(regex.toString());
			}
			
			private char lookAhead() {
				char la = glob.next();
				glob.previous();
				return la;
			}
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(util.class);
	
}
