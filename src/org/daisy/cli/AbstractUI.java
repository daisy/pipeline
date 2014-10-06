/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.daisy.factory.Factory;
import org.daisy.factory.FactoryCatalog;

/**
 * Provides an abstract base for command line UI's.
 * @author Joel Håkansson
 */
public abstract class AbstractUI {

	/**
	 * Prefix used for required arguments in the arguments map
	 */
	public final static String ARG_PREFIX = "required-";
	protected final DefaultCommandParser parser;
	
	/**
	 * Expands the short form of the value with the given key in the provided map using the specified resolver.
	 * @param map 
	 * @param key
	 * @param resolver
	 */
	public void expandShortForm(Map<String, String> map, String key, ShortFormResolver resolver) {
		String value = map.get(key);
		if (value!=null) {
			String id = resolver.resolve(value);
			if (id!=null) {
				map.put(key, id);
			} else {
				System.out.println("Unknown value for "+key+": '" + value + "'");
				System.exit(-ExitCode.ILLEGAL_ARGUMENT_VALUE.ordinal());
			}
		}
	}
	
	/**
	 * Creates a list of definitions based on the contents of the supplied FactoryCatalog.
	 * @param catalog the catalog to create definitions for
	 * @param resolver 
	 * @return returns a list of definitions
	 */
	public List<Definition> getDefinitionList(FactoryCatalog<? extends Factory> catalog, ShortFormResolver resolver) {
		List<Definition> ret = new ArrayList<Definition>();
		for (String key : resolver.getShortForms()) {
			ret.add(new Definition(key, catalog.get(resolver.resolve(key)).getDescription()));
		}
		return ret;
	}
	
	/**
	 * Creates a new AbstractUI using the default key/value delimiter '=' and
	 * the default optional argument prefix '-'
	 */
	public AbstractUI() {
		this.parser = new DefaultCommandParser();
		setKeyValueDelimiter("=");
		setOptionalArgumentPrefix("-");
	}

	/**
	 * Sets the delimiter to use between key and value in the argument
	 * strings passed to the UI.
	 * @param value the delimiter to use
	 */
	public void setKeyValueDelimiter(String value) {
		parser.setKeyValueDelimiter(value);
	}
	
	/**
	 * Sets the optional argument prefix to use in argument strings passed to
	 * the UI.
	 * @param value the prefix to use
	 */
	public void setOptionalArgumentPrefix(String value) {
		if (ARG_PREFIX.equals(value)) {
			throw new IllegalArgumentException("Prefix is reserved: " + ARG_PREFIX);
		}
		parser.setOptionalArgumentPrefix(value);
	}
	
	/**
	 * Gets the name for the UI
	 * @return returns the UI name
	 */
	public abstract String getName();
	
	/**
	 * Gets required arguments
	 * @return returns a list of required arguments that can be
	 * passed to the UI on startup
	 */
	public abstract List<Argument> getRequiredArguments();
	
	/**
	 * Gets optional arguments
	 * @return returns a list of optional arguments that can be
	 * passed to the UI on startup
	 */
	public abstract List<OptionalArgument> getOptionalArguments();

	/**
	 * Converts a string based definition of UI arguments, typically 
	 * passed by the main method, into a key-value map as described
	 * by displayHelp
	 * @param args the arguments passed to the application
	 * @return returns a map of application arguments
	 */
	public Map<String, String> toMap(String[] args) {
		args = parser.processSwitches(args);
		Hashtable<String, String> p = new Hashtable<String, String>();
		int i = 0;
		String[] t;
		for (String s : args) {
			s = s.trim();
			t = s.split(parser.getKeyValueDelimiter(), 2);
			if (s.startsWith(parser.getOptionalArgumentPrefix()) && t.length==2) {
				p.put(t[0].substring(1), t[1]);
			} else {
				p.put(ARG_PREFIX+i, s);
				i++;
			}
		}
		return p;
	}

	public Map<String, String> getOptional(String[] args) {
		args = parser.processSwitches(args);
		Hashtable<String, String> p = new Hashtable<String, String>();
		String[] t;
		for (String s : args) {
			s = s.trim();
			t = s.split(parser.getKeyValueDelimiter(), 2);
			if (s.startsWith(parser.getOptionalArgumentPrefix()) && t.length==2) {
				p.put(t[0].substring(1), t[1]);
			}
		}
		return p;
	}
	
	public List<String> getRequired(String[] args) {
		Vector<String> p = new Vector<String>();
		String[] t;
		for (String s : args) {
			s = s.trim();
			t = s.split(parser.getKeyValueDelimiter(), 2);
			if (!(s.startsWith(parser.getOptionalArgumentPrefix()) && t.length==2)) {
				p.add(s);
			}
		}
		return p;
	}
	
	public static void exitWithCode(ExitCode e) {
		exitWithCode(e, null);
	}
	
	public static void exitWithCode(ExitCode e, String message) {
		if (message!=null) {
			System.out.println(message);
		}
		System.exit(-e.ordinal());
	}
	
	/**
	 * Displays a help text for the UI based on the implementation of 
	 * the methods getName, getOptionalArguments and getRequiredArguments. 
	 * @param ps The print stream to use, typically System.out
	 */
	public void displayHelp(PrintStream ps) {
		ps.print(getName());
		if (getRequiredArguments()!=null && getRequiredArguments().size()>0) {
			for (Argument a : getRequiredArguments()) {
				ps.print(" ");
				ps.print("<"+a.getName()+">");
			}
		}
		if (getOptionalArguments()!=null && getOptionalArguments().size()>0) {
			ps.print(" [options ... ]");
		}
		ps.println();
		ps.println();
		if (getRequiredArguments()!=null && getRequiredArguments().size()>0) {
			for (Argument a : getRequiredArguments()) {
				ps.println("Required argument:");
				ps.println("\t" + a.getName()+ " - " + a.getDescription());
				ps.println();
				if (a.hasValues()) {
					ps.println("\tValues:");
					for (Definition value : a.getValues()) {
						ps.println("\t\t'"+value.getName() + "' - " + value.getDescription());
					}
					ps.println();
				}
			}
		}
		if (getOptionalArguments()!=null && getOptionalArguments().size()>0) {
			ps.println("Optional arguments:");
			for (OptionalArgument a : getOptionalArguments()) {
				ps.print("\t" + parser.getOptionalArgumentPrefix() + a.getName() + parser.getKeyValueDelimiter() + "[value]");
				if (!a.hasValues()) {
					ps.print(" (default '"  + a.getDefault() + "')");
				}
				ps.println();
				ps.println("\t\t" + a.getDescription());
				if (a.hasValues()) {
					ps.println("\t\tValues:");
					for (Definition value : a.getValues()) {
						ps.print("\t\t\t'"+value.getName() + "' - " + value.getDescription());
						if (value.getName().equals(a.getDefault())) {
							ps.println(" (default)");
						} else {
							ps.println();
						}
					}
				}
			}
			ps.println();
		}
		displaySwitches(ps);
	}
	
	public void displaySwitches(PrintStream ps) {
		if (parser.getSwitches()!=null && parser.getSwitches().size()>0) {
			ps.println("Switches:");
			for (SwitchArgument a : parser.getSwitches()) {
				ps.print("\t" + parser.getSwitchArgumentPrefix() + a.getKey());
				ps.println();
				ps.println("\t\t" + a.getDescription());
			}
			ps.println();
		}
	}
}
