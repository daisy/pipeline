package org.daisy.pipeline.persistence.impl.job;

import java.io.File;

import javax.persistence.Embeddable;

import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResourcesDir;

@Embeddable
public class PersistentMapper {

	String inputBase;
	String outputBase;

	public PersistentMapper() {
	}

	public PersistentMapper(File resultDir, JobResources resources) {
		this.outputBase = resultDir.toURI().toString();
		if (resources == null)
			this.inputBase = "";
		else {
			if (!(resources instanceof JobResourcesDir))
				throw new IllegalArgumentException(); // could happen if ScriptInput.storeToDisk was not called
			this.inputBase = ((JobResourcesDir)resources).getBaseDir().toURI().toString();
		}
	}
}
