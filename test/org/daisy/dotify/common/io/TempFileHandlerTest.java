package org.daisy.dotify.common.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TempFileHandlerTest {

	@Test
	public void testReset() throws IOException {
		File in = File.createTempFile(this.getClass().getName(), ".tmp");
		in.deleteOnExit();
		File out = File.createTempFile(this.getClass().getName(), ".tmp");
		out.deleteOnExit();
		//prepare input
		List<String> lines = new ArrayList<>();
		lines.add("test");
		Files.write(in.toPath(), lines);
		//test
		try (TempFileHandler tf = new TempFileHandler(in, out)) {
			//not processing input to output here, because that's not what this test is about
			//instead, just write the same contents to the temporary output 
			Files.write(tf.getOutput().toPath(), lines);
			tf.reset();
			assertEquals(in.length(), tf.getInput().length());
			assertTrue(tf.getOutput().exists());
			assertEquals(0, tf.getOutput().length());
		}
	}
}
