package org.daisy.pipeline.braille.common;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.daisy.common.spi.ActivationException;
import static org.daisy.pipeline.braille.common.util.OS;

public class BundledNativePath extends StandardNativePath {
	
	private static final String OS_FAMILY = "os.family";
	
	private BundledResourcePath resourcePath;
	
	protected ResourcePath delegate() {
		return resourcePath;
	}
	
	/**
	 * @throws RuntimeException if the binaries don't work on the current OS
	 */
	protected void activate(Map<?,?> properties, Class<?> context) throws RuntimeException {
		if (properties.get(OS_FAMILY) == null
				|| properties.get(OS_FAMILY).toString().isEmpty()) {
			throw new IllegalArgumentException(OS_FAMILY + " property must not be empty"); }
		if (OS.Family.valueOf(properties.get(OS_FAMILY).toString().toUpperCase()) != OS.getFamily()) {
			String errorMessage = "does not work on " + OS.getFamily();
			try {
				SPIHelper.failToActivate(errorMessage);
			} catch (NoClassDefFoundError e) {
				// we are probably in OSGi context
				throw new RuntimeException(errorMessage);
			}
		}
		if (properties.get(BundledResourcePath.UNPACK) != null)
			throw new IllegalArgumentException(BundledResourcePath.UNPACK + " property not supported");
		Map<Object,Object> props = new HashMap<Object,Object>(properties);
		props.remove(OS_FAMILY);
		props.put(BundledResourcePath.UNPACK, true);
		props.put(BundledResourcePath.EXECUTABLES, true);
		resourcePath = new BundledResourcePath();
		resourcePath.activate(props, context);
	}
	
	private boolean unpacked = false;
	
	@Override
	public URL resolve(URI resource) {
		URL resolved = super.resolve(resource);
		if (!unpacked && resolved != null) {
			// unpack everything (because of possible dependencies between binaries)
			resourcePath.resolve(getIdentifier());
			unpacked = true; }
		return resolved;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (getClass() != object.getClass())
			return false;
		return super.equals((BundledNativePath)object);
	}

	// static nested class in order to delay class loading
	private static class SPIHelper {
		private SPIHelper() {}
		public static void failToActivate(String message) throws ActivationException {
			throw new ActivationException(message);
		}
	}
}
