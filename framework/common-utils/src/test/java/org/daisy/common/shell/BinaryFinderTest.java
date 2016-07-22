package org.daisy.common.shell;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.base.Optional;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BinaryFinder.class)
public class BinaryFinderTest {

	public Optional<String> regularTest(String existingFolder, String existingBin,
	        String existingExtension, String path, String lookForBin,
	        String... availableExtensions) throws Exception {
		String expectedPath = existingFolder + "/" + existingBin + existingExtension;

		File goodFileMock = PowerMockito.mock(File.class);
		File badFileMock = PowerMockito.mock(File.class);

		PowerMockito.whenNew(File.class).withArguments(Mockito.anyString(),
		        Mockito.anyString()).thenReturn(badFileMock);
		PowerMockito.when(badFileMock.isFile()).thenReturn(false);

		PowerMockito.whenNew(File.class).withArguments(existingFolder,
		        existingBin + existingExtension).thenReturn(goodFileMock);
		PowerMockito.when(goodFileMock.isFile()).thenReturn(true);
		PowerMockito.when(goodFileMock.getAbsolutePath()).thenReturn(expectedPath);

		return BinaryFinder.find(lookForBin, availableExtensions, path, ":");
	}

	@Test
	public void easyFound() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", "", "/e:/f:/a/b", "bin", "");
		Assert.assertEquals("/a/b/bin", actualPath.get());
	}

	@Test
	public void okWithDirtyPath() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", "", ":/e:/f::::/a/b::",
		        "bin", "");
		Assert.assertEquals("/a/b/bin", actualPath.get());
	}

	@Test
	public void lastExtension() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", ".ext3", "/e:/f:/a/b:/i/j",
		        "bin", ".ext1", ".ext2", ".ext3");
		Assert.assertEquals("/a/b/bin.ext3", actualPath.get());
	}

	@Test
	public void wrongExtension() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", ".ext4", "/e:/f:/a/b", "bin",
		        ".ext1", ".ext2", ".ext3");
		Assert.assertFalse(actualPath.isPresent());
	}

	@Test
	public void wrongDirectory() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", ".ext1", "/e:/f:/a/k", "bin",
		        ".ext1");
		Assert.assertFalse(actualPath.isPresent());
	}

	@Test
	public void wrongBinary() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", ".ext1", "/e:/f:/a/b",
		        "exec", ".ext1");
		Assert.assertFalse(actualPath.isPresent());
	}

	@Test
	public void nullPath() throws Exception {
		Optional<String> actualPath = regularTest("/a/b", "bin", ".ext1", null, "bin", ".ext1");
		Assert.assertFalse(actualPath.isPresent());
	}

	@Test
	public void malformedPath() {
		Optional<String> result = BinaryFinder.find("x", new String[]{
			""
		}, ":@%*<>!?##+:^^", ":");
		Assert.assertFalse(result.isPresent());
	}
}
