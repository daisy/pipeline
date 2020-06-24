package org.liblouis;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Proxy for a Translator, with metadata.
 */
public class Table {
	
	private final String table;
	private final TableInfo info;
	private Translator translator = null;
	private final static Pattern QUERY_SYNTAX = Pattern.compile(
		"^[\\s\\t]*([a-zA-Z0-9-._]+(:[a-zA-Z0-9-._]+)?)([\\s\\t]+[a-zA-Z0-9-._]+(:[a-zA-Z0-9-._]+)?)*[\\s\\t]*");
	
	Table(String table) {
		this.table = table;
		this.info = new TableInfo(table);
	}
	
	public TableInfo getInfo() {
		return info;
	}
	
	public Translator getTranslator() throws CompilationException {
		if (translator == null)
			translator = new Translator(table);
		return translator;
	}
	
	/**
	 * @param query A table query
	 * @throws IllegalArgumentException if the query does not use the right syntax.
	 * @throws NoSuchElementException if no match could be found.
	 */
	public static Table find(String query) throws IllegalArgumentException, NoSuchElementException {
		if (!QUERY_SYNTAX.matcher(query).matches())
			throw new IllegalArgumentException("Query does not have the right syntax: " + query);
		Louis.log(Logger.Level.DEBUG, "Finding table for query ", query);
		String table = Louis.findTable(query);
		if (table == null)
			throw new NoSuchElementException("No match found for query '" + query + "'");
		return new Table(table);
	}
}
