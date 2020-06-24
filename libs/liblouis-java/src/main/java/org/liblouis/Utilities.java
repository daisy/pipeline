package org.liblouis;

import java.util.Arrays;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class Utilities {
	
	public static class Pair<T1,T2> {
		public final T1 _1;
		public final T2 _2;
		public Pair(T1 _1, T2 _2) {
			this._1 = _1;
			this._2 = _2;
		}
	}
	
	public static class Hyphenation {
		
		public static Pair<String,byte[]> extractHyphens(String string, Character shy, Character zwsp) {
			if ((shy == null || !string.contains(String.valueOf(shy))) &&
			    (zwsp == null || !string.contains(String.valueOf(zwsp))))
				return new Pair<String,byte[]>(string, null);
			final byte SHY = 1;
			final byte ZWSP = 2;
			StringBuffer unhyphenatedString = new StringBuffer();
			byte[] hyphens = new byte[string.length()/2];
			int j = 0;
			boolean seenShy = false;
			boolean seenZwsp = false;
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				if (c == shy)
					seenShy = true;
				else if (c == zwsp)
					seenZwsp = true;
				else {
					unhyphenatedString.append(c);
					hyphens[j++] = (seenShy ? SHY : seenZwsp ? ZWSP : 0);
					seenShy = false;
					seenZwsp = false; }}
			return new Pair<String,byte[]>(unhyphenatedString.toString(), Arrays.copyOf(hyphens, j-1));
		}
		
		public static String insertHyphens(String string, byte hyphens[], Character shy, Character zwsp) {
			if ((shy == null && zwsp == null) || hyphens == null)
				return string;
			final byte SHY = 1;
			final byte ZWSP = 2;
			if (string.equals("")) return "";
			if (hyphens.length != string.length()-1)
				throw new RuntimeException("hyphens.length must be equal to string.length() - 1");
			StringBuffer hyphenatedString = new StringBuffer();
			int i;
			for (i = 0; i < hyphens.length; i++) {
				hyphenatedString.append(string.charAt(i));
				if (shy != null && hyphens[i] == SHY)
					hyphenatedString.append(shy);
				else if (zwsp != null && hyphens[i] == ZWSP)
					hyphenatedString.append(zwsp); }
			hyphenatedString.append(string.charAt(i));
			return hyphenatedString.toString();
		}
	}
	
	public static class Environment {
		
		public static void setLouisTablePath(String value) {
			if (libc instanceof UnixCLibrary)
				((UnixCLibrary)libc).setenv("LOUIS_TABLEPATH", value, 1);
			else {
				((WindowsCLibrary)libc)._putenv("LOUIS_TABLEPATH=");
				((WindowsCLibrary)libc)._putenv("LOUIS_TABLEPATH=" + value); }
		}
	}
	
	private static Object libc;
	
	static {
		switch (Platform.getOSType()) {
			case Platform.MAC:
			case Platform.LINUX:
				libc = Native.loadLibrary("c", UnixCLibrary.class);
				break;
			case Platform.WINDOWS:
				libc = Native.loadLibrary("msvcrt", WindowsCLibrary.class);
				break;
		}
	}
	
	public interface UnixCLibrary extends Library {
		public int setenv(String name, String value, int overwrite);
	}
	
	public interface WindowsCLibrary extends Library {
		public int _putenv(String string);
	}
}
