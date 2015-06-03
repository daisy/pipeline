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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Provides shorter names for factory identifiers, to be used in command line user interfaces.
 * The short forms are guaranteed to be consistent between executions as long as
 * the identifiers in the collection remains the same.  
 * @author Joel Håkansson
 */
public class ShortFormResolver {
	private final HashMap<String, String> idents;
	private final HashMap<String, String> shorts;
	
	public ShortFormResolver(String ... s) {
		this(toCollection(s));
	}
	
	private static Collection<String> toCollection(String ... s) {
		Collection<String> ret = new ArrayList<String>();
		Collections.addAll(ret, s);
		return ret;
	}
	
	/**
	 * Creates a new ShortFormResolver for the supplied collection of identifiers.
	 * @param obj the collection to create short forms for
	 */
	public ShortFormResolver(Collection<String> obj) {
		this.idents = new HashMap<String, String>();
		this.shorts = new HashMap<String, String>();
		//analyze uniqueness short forms
		HashMap<String, Integer> uniqueIndex = new HashMap<String, Integer>();
		for (String f : obj) {
			String identifier = f.toLowerCase();
			for (String p : identifier.split("\\.")) {
				Integer i = uniqueIndex.get(p);
				if (i!=null) {
					uniqueIndex.put(p, i+1);
				} else {
					uniqueIndex.put(p, 1);
				}
			}
		}
		//add short forms
		for (String f : obj) {
			String identifier = f.toLowerCase();
			String[] s = identifier.split("\\.");
			Integer x = uniqueIndex.get(s[s.length-1]);
			assert x!=null;
			if (x==1) {
				idents.put(s[s.length-1], f);
				shorts.put(f, s[s.length-1]);
			} else {
				//TODO: expand on this
				// Don't do anything
				idents.put(identifier, f);
				shorts.put(f, identifier);
			}
		}
	}

	/**
	 * Gets all short forms.
	 * @return returns a list of short forms
	 */
	public List<String> getShortForms() {
		ArrayList<String> ret = new ArrayList<String>(idents.keySet());
		Collections.sort(ret);
		return ret;
	}
	
	/**
	 * Get the short form for the specified identifier.
	 * @param id the identifier to get the short form for
	 * @return returns the short form for the identifier, or null if the identifier 
	 * does not have a short form
	 */
	public String getShortForm(String id) {
		return shorts.get(id);
	}
	
	/**
	 * Resolves a short form.
	 * @param shortForm the short form to resolve
	 * @return returns the full id for the supplied short form, or null if the short
	 * form does not have an identifier
	 */
	public String resolve(String shortForm) {
		return idents.get(shortForm.toLowerCase());
	}
}
