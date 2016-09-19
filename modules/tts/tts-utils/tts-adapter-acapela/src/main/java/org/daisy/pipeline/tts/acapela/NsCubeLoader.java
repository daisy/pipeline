package org.daisy.pipeline.tts.acapela;

class NsCubeLoader {
	//it could be moved to NscubeLibrary.java but NscubeLibrary.java is auto-generated
	static String GetLibName() {
		String arch = System.getProperty("os.arch");
		if (arch != null && arch.endsWith("64"))
			return "nscube64";
		else
			return "nscube";
	}
}
