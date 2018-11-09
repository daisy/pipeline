package org.daisy.dotify.common.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
@SuppressWarnings("javadoc")
public class FileIOTest {

	@Test
	public void testDiffEqual() throws IOException {
		byte[] buf = new byte[]{32, 64, 65, 66};
		assertEquals(-1, FileIO.diff(new ByteArrayInputStream(buf), new ByteArrayInputStream(buf)));
	}
	
	@Test
	public void testDiffUnequal() throws IOException {
		byte[] buf1 = new byte[]{32, 64, 65, 66};
		byte[] buf2 = new byte[]{32, 64, 66, 66};
		assertEquals(2, FileIO.diff(new ByteArrayInputStream(buf1), new ByteArrayInputStream(buf2)));
	}
	
	@Test
	public void testCopyStream() throws IOException {
		byte[] buf = new byte[]{32, 64, 65, 66};
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		FileIO.copy(new ByteArrayInputStream(buf), os);
		assertArrayEquals(buf, os.toByteArray());
	}
	
	@Test
	public void testCopyFile() throws IOException {
		File in = File.createTempFile(this.getClass().getName(), ".tmp");
		in.deleteOnExit();
		File out = File.createTempFile(this.getClass().getName(), ".tmp");
		out.deleteOnExit();
		List<String> lines = new ArrayList<>();
		lines.add("test");
		Files.write(in.toPath(), lines);
		Files.copy(in.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
		assertEquals(-1, FileIO.diff(new FileInputStream(in), new FileInputStream(out)));
	}

}