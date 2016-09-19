package org.daisy.pipeline.tts.attnative;

import java.io.File;

public class ATTHelpers {
	private static String getLibPath(File location) {
		if (location.isDirectory()) {
			for (File f : location.listFiles()) {
				String res = getLibPath(f);
				if (res != null)
					return res;
			}
		} else if (location.getName().contains(".so")) {
			String path = location.getAbsolutePath();
			String arch = System.getProperty("os.arch");
			boolean mustBe64 = path.contains("64");
			boolean mustBe32 = path.contains("86");
			if ((!mustBe64 && !mustBe32) || (mustBe64 && arch.endsWith("64"))
			        || (mustBe32 && !arch.endsWith("64")))
				return path;
		}

		return null;
	}
	
	static String SSML(String str) {
		return "<speak version=\"1.0\"><s id=\"2\" useless=\"attr\">"+str+"</s></sspeak>";
	}

	static String SSML(String str, String voiceName) {
		return SSML("<voice name=\"" + voiceName + "\">" + str + "</voice>");
	}

	
	static void loadATT(){
		String libpath = getLibPath(new File(System.getProperty("user.dir")
		        + "/target/generated-resources"));
		System.load(libpath);
	}
}
