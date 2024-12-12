package org.daisy.pipeline.braille.common;

import java.net.URL;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterables.find;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

import static org.daisy.common.file.URLs.asURI;
import static org.daisy.pipeline.braille.common.util.OS;

/**
 * NativePath implementation from a ResourcePath which is unpacking and in
 * which the binaries are named according to the following convention:
 *
 * - x86/foo.so
 * - x86/foo.dll
 * - x86/bar
 * - x86/bar.exe
 * - x86_64/foo.so
 * - x86_64/foo.dll
 * - x86_64/bar
 * - x86_64/bar.exe
 * - linux/x86/foo.so
 * - linux/x86/bar
 * - linux/x86_64/foo.so
 * - linux/x86_64/bar
 * - linux/aarch64/foo.so
 * - linux/aarch64/bar
 * - macosx/x86/foo.dylib
 * - macosx/x86/bar
 * - macosx/x86_64/foo.dylib
 * - macosx/x86_64/bar
 * - macosx/aarch64/foo.dylib
 * - macosx/aarch64/bar
 * - windows/x86/foo.dll
 * - windows/x86/bar.exe
 * - windows/x86_64/foo.dll
 * - windows/x86_64/bar.exe
 */
public abstract class StandardNativePath implements NativePath {
	
	protected abstract ResourcePath delegate();
	
	public URI getIdentifier() {
		return delegate().getIdentifier();
	}
	
	public URL resolve(URI resource) {
		return delegate().resolve(resource);
	}
	
	public URI canonicalize(URI resource) {
		return delegate().canonicalize(resource);
	}
	
	/**
	 * Get an executable or a shared library.
	 * @param name The name (without the extension)
	 * @return A collection of executables or libraries.
	 */
	public Iterable<URI> get(String name) {
		URI path;
		if ((path = getExecutable(name)) != null) {}
		else if ((path = getSharedLibrary(name)) != null) {}
		else { return Optional.<URI>absent().asSet(); }
		return Optional.of(canonicalize(path)).asSet();
	}
	
	private URI getExecutable(String name) {
		String fileName = OS.isWindows() ? name + ".exe" : name;
		String os = OS.getFamily().toString().toLowerCase();
		String arch = OS.getArch();
		if (!OS.is64Bit()) arch = "x86";
		else if ("amd64".equals(arch)) arch = "x86_64";
		else if ("arm64".equals(arch)) arch = "aarch64";
		List<URI> possiblePaths = new ArrayList<URI>();
		possiblePaths.add(asURI(arch + "/" + fileName));
		possiblePaths.add(asURI(os + "/" + arch + "/" + fileName));
		if (OS.is64Bit() && OS.isWindows()) {
			possiblePaths.add(asURI("x86/" + fileName));
			possiblePaths.add(asURI(os + "/x86/" + fileName)); }
		try {
			return find(
				possiblePaths,
				new Predicate<URI>() { public boolean apply(URI p) { return (delegate().resolve(p) != null); }}); }
		catch (NoSuchElementException e) { return null; }
	}
	
	private URI getSharedLibrary(String name) {
		String fileName = name + (OS.isWindows() ? ".dll" : OS.isMacOSX() ? ".dylib" : ".so");
		String os = OS.getFamily().toString().toLowerCase();
		String arch = OS.getArch();
		if (!OS.is64Bit()) arch = "x86";
		else if ("amd64".equals(arch)) arch = "x86_64";
		else if ("arm64".equals(arch)) arch = "aarch64";
		List<URI> possiblePaths = new ArrayList<URI>();
		possiblePaths.add(asURI(arch + "/" + fileName));
		possiblePaths.add(asURI(os + "/" + arch + "/" + fileName));
		try {
			return find(
				possiblePaths,
				new Predicate<URI>() { public boolean apply(URI p) { return (delegate().resolve(p) != null); }}); }
		catch (NoSuchElementException e) { return null; }
	}
}
