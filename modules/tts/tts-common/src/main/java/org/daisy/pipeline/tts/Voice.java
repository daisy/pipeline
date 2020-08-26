package org.daisy.pipeline.tts;

public class Voice {
	public enum MarkSupport {
		DEFAULT,
		MARK_SUPPORTED,
		MARK_NOT_SUPPORTED
	}

	public Voice(String engine, String name) {
		this(engine, name, MarkSupport.DEFAULT);
	}

	public Voice(String engine, String name, MarkSupport markSupport) {
		//we keep the strings in their full case form because some engines might be case sensitive
		this.engine = engine;
		if (engine == null)
			this.engine = "";
		this.name = name;
		if (name == null)
			this.name = "";

		mMarkSupport = markSupport;

		mEngine_lo = this.engine.toLowerCase();
		mName_lo = this.name.toLowerCase();
	}

	public int hashCode() {
		return mEngine_lo.hashCode() ^ mName_lo.hashCode();
	}

	public boolean equals(Object other) {
		if (other == null)
			return false;
		Voice v2 = (Voice) other;
		return mEngine_lo.equals(v2.mEngine_lo) && mName_lo.equals(v2.mName_lo);
	}

	public String toString() {
		return "{engine:" + (!engine.isEmpty() ? engine : "%unknown%") + ", name:"
		        + (!name.isEmpty() ? name : "%unkown%") + "}";
	}

	public MarkSupport getMarkSupport() {
		return mMarkSupport;
	}

	//the upper-case versions need to be kept because some TTS Processors like SAPI
	//are case-sensitive. Lower-case versions are only used for comparison.
	public String engine;
	public String name;
	private String mEngine_lo;
	private String mName_lo;
	private MarkSupport mMarkSupport;
}
