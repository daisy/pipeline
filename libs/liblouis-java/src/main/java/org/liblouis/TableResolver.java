package org.liblouis;

import java.net.URL;
import java.util.Set;

public interface TableResolver {
	
	/**
	 * Resolve a table.
	 *
	 * @param table The table name. Can be a relative or absolute file path, or some other identifier.
	 * @param base  If non-null, resolving against this base URL.
	 * @return The resolved resource as a URL, or null.
	 *
	 * If the table can be resolved, it must resolve to exactly one table file. Liblouis supports
	 * specifying a comma-separated list of tables, but that feature is not supported via this
	 * interface. The returned URL must represent one table file, however that does not mean that
	 * the URL must be a file URL. It may be any kind of URL as long as an InputStream is
	 * obtained upon calling the openStream() method.
	 */
	public URL resolve(String table, URL base);
	
	/**
	 * List all the available table files. This includes "top-level" tables but also table files
	 * intended for inclusion in other tables.
	 */
	public Set<String> list();
}
