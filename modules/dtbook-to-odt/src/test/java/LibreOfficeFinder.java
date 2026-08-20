import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.common.properties.Properties;

public class LibreOfficeFinder {

	private static Optional<LibreOfficeConverter> libreoffice = null;

	public static LibreOfficeConverter get() throws NoSuchElementException {
		if (libreoffice == null) {
			try {
				String path = Properties.getGlobalProperty("org.daisy.pipeline.libreoffice.path");
				if (path != null && !"".equals(path)) {
					File file = new File(path);
					if (file.isFile())
						libreoffice = Optional.of(new LibreOfficeConverter(file));
					else
						LibreOfficeConverter.LOGGER.warn("File does not exist: " + path);
				}
				if (libreoffice == null && System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
					File file = new File("/Applications/LibreOffice.app/Contents/MacOS/soffice");
					if (file.isFile())
						libreoffice = Optional.of(new LibreOfficeConverter(file));
				}
				if (libreoffice == null) {
					libreoffice = BinaryFinder.find("libreoffice")
						                      .map(f -> new LibreOfficeConverter(new File(f)));
				}
				if (libreoffice == null || !libreoffice.isPresent()) {
					libreoffice = BinaryFinder.find("soffice")
						                      .map(f -> new LibreOfficeConverter(new File(f)));
				}
			} catch (RuntimeException e) {
				LibreOfficeConverter.LOGGER.debug(
					"LibreOfficeConverter could not be initialized", e);
				libreoffice = Optional.empty();
			}
		}
		return libreoffice.get();
	}
}
