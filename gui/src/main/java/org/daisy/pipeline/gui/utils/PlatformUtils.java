package org.daisy.pipeline.gui.utils;

public class PlatformUtils {
	
	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isMac() {
		return OS.contains("mac"); 
	}
	public static boolean isWin() {
		return OS.contains("win");
	}
	public static boolean isUnix() {
		return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
	}
	public static String getFileBrowserCommand() {
		if (isMac()) {
			return "open";
		}
		if (isWin()) {
			return "explorer";
		}
		if (isUnix()) {
			return "xdg-open"; 
		}
		return "";
	}
	
}
