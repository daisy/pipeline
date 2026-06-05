/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.mimeutil;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * This is a comprehensive set of unit test for the MimeTypeHashSet class
 *
 * @author Steven McArdle
 *
 */
public class MimeTypeHashSetTest extends TestCase {

	public final void testMimeTypeHashSet() {
		assertTrue(new MimeTypeHashSet().size() == 0);
	}

	public final void testMimeTypeHashSetCollection() {
		Collection c = new HashSet();
		c.add(new MimeType("text/plain"));
		assertTrue(new MimeTypeHashSet(c).size() == 1);

		// Test the types of objects we can use in a collection

		// String representation
		c.add("application/xml");

		// comma separated string
		c.add("application/a, application/b");

		// String [] including comma operated strings
		c.add(new String [] {"application/c", "application/d,application/e"});

		// Some other non supported type that we should ignore
		c.add(new Integer(20));

		assertTrue(new MimeTypeHashSet(c).size() == 7);

		// Now add another collection with some type suitable as mime types
		Collection d = new Vector();
		d.add(new MimeType("text/a"));
		d.add("text/b,text/c");
		d.add(new String []{"text/d", "text/e,text/f"});
		d.add(new Float(1.1)); // Should ignore this

		c.add(d);

		assertTrue(new MimeTypeHashSet(c).size() == 13);
	}

	public final void testMimeTypeHashSetString() {
		assertTrue(new MimeTypeHashSet("application/xml").size() == 1);
		assertTrue(new MimeTypeHashSet("application/xml,text/plain").size() == 2);
	}

	public final void testMimeTypeHashSetStringArray() {
		assertTrue(new MimeTypeHashSet(new String[]{"text/a","text/b,text/c","text/d"}).size() == 4);
	}

	public final void testMimeTypeHashSetMimeType() {
		assertTrue(new MimeTypeHashSet(new MimeType("text/plain")).size() == 1);
	}

	public final void testAdd() {

		Collection c = new MimeTypeHashSet();

		// add a MimeType
		c.add(new MimeType("text/a"));
		assertTrue(c.size() == 1);

		// Add a string
		c.add("text/b");
		assertTrue(c.size() == 2);

		// Add a comma separated string
		c.add("text/c,text/d,text/e");
		assertTrue(c.size() == 5);

		// Add a String array including an entry containing a comma separated string
		c.add(new String [] {"text/f", "text/g,text/h"});
		assertTrue(c.size() == 8);

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		d.add(new Integer(3)); // Should ignore this
		c.add(d);
		assertTrue(c.size() == 11);

		// Add an object that cannot be converted to a mime type
		c.add(new Float(1.1));
		assertTrue(c.size() == 11);

		// Test the boolean return value of the add
		assertTrue(c.add(new MimeType("text/l")));
		assertFalse(c.add(new MimeType("text/l")));

		assertFalse(c.add(new Integer(10)));
	}

	public final void testAddAll() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		d.add(new Integer(3)); // Should ignore this

		// The collection should be modified
		assertTrue(c.addAll(d));

		// The collection should not be modified as we have already added all these types
		assertFalse(c.addAll(d));

		assertTrue(c.size() == 3);

		// Test addAll boolean return types
	}

	public final void testClear() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		d.add(new Integer(3)); // Should ignore this
		c.addAll(d);

		assertTrue(c.size() == 3);

		c.clear();

		assertTrue(c.size() == 0);
	}

	public final void testContains() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		Integer i = new Integer(3);
		d.add(i); // Should ignore this
		c.addAll(d);

		assertTrue(c.size() == 3);

		assertTrue(c.contains("text/i"));
		assertTrue(c.contains("text/j"));
		assertTrue(c.contains("text/k"));
		c.remove("text/i");
		assertFalse(c.contains("text/i"));

		c.add("text/i");
		assertFalse(c.contains(d)); // The passed in collection contains an Integer type that can't be converted to a MimeType

		d.remove(i);
		assertTrue(c.contains(d));

		assertFalse(c.contains("abc/xyz"));

	}

	public final void testContainsAll() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		c.addAll(d);

		assertTrue(c.containsAll(d));

		d.add("abc/xyz");

		assertFalse(c.containsAll(d));
	}

	public final void testRemove() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k"});
		c.addAll(d);

		assertTrue(c.remove("text/i"));
		assertFalse(c.remove("text/i"));

		assertTrue(c.size() == 2);
		assertTrue(c.remove(d));
		assertTrue(c.isEmpty());
	}

	public final void testRemoveAll() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k,text/p"});
		d.add("text/l");
		d.add("text/m,text/n,text/o");
		c.addAll(d);

		assertTrue(c.size() == 8);
		assertTrue(c.removeAll(d));
		assertTrue(c.isEmpty());
	}

	public final void testRetainAll() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k, text/l,text/m"});
		c.add("text/n,text/o");
		c.addAll(d);

		assertTrue(c.size() == 7);

		String mimeTypes = "text/i,text/l,text/o";
		assertTrue(c.retainAll(new MimeTypeHashSet(mimeTypes)));

		assertTrue(c.size() == 3);
	}


	public final void testEqualsObject() {
		Collection c = new MimeTypeHashSet();

		// Add a collection
		Collection d = new Vector();
		d.add(new MimeType("text/i"));
		d.add(new String [] {"text/j", "text/k, text/l,text/m"});
		c.add("text/n,text/o");
		c.addAll(d);

		assertTrue(c.size() == 7);

		assertTrue(c.equals("text/i,text/j,text/k,text/l,text/m,text/n,text/o"));
		assertTrue(c.equals(new String[] {"text/i","text/j","text/k","text/l","text/m","text/n","text/o"}));
		assertTrue(c.equals(new String[] {"text/i","text/j,text/k,text/l","text/m","text/n","text/o"}));

		c.add("a/b");
		Collection e = new Vector();
		e.add(new MimeType("text/o"));
		e.add("text/n,text/m");

		Collection f = new TreeSet(new Comparator(){
			// We add a comparator to the TreeSet to allow String and MimeType objects to be compared with a String.
			// Without this we would get a ClassCastException as soon as we add the String object after the MimeType object.
			// This is only important because we chose to add String's and MimeType's to the TreeSet.
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		f.add(new MimeType("a/b"));
		f.add("text/i");
		f.add("text/j,text/k");
		f.add(new MimeType("text/l").toString());
		e.add(f);

		assertTrue(c.equals(e));

		// This TreeSet is not constructed with a Comparator to allow strings and MimeType's to be
		// added to the collection. The addition of the String will cause a ClassCastException. See the
		// example above for the reason.
		Collection g = new TreeSet();
		g.add(new MimeType("text/plain"));
		try {
			g.add("text/plain");
			fail("Should not get here.");
		}catch(ClassCastException cce) {
			// This will get thrown by the add method above as String and MimeType cannot be compared
			// naturally.
		}
	}

	public void testToString() {
		Collection c = new MimeTypeHashSet();

		c.add(new MimeType("text/x"));
		c.add(new String [] {"text/j", "text/b, text/l,text/m"});
		c.add("text/n,text/a");

		// Check that the collection maintains insertion order
		assertEquals(c.toString(), "text/x,text/j,text/b,text/l,text/m,text/n,text/a");
	}

	public void testMatches() {
		MimeTypeHashSet c = new MimeTypeHashSet();

		c.add(new MimeType("text/x"));
		c.add(new String [] {"text/j", "text/b, text/l,text/m"});
		c.add("text/n,text/a");
		c.add("application/n,application/a");
		c.add("abc/def,application/pdf,application/x-pdf-something");
		c.add("xyz/abc");

		Collection d = c.matches(".*abc.*");
		assertTrue(d.size() == 2);
		assertEquals(d.toString(), "abc/def,xyz/abc");

		d = c.matches("text/.*");
		assertTrue(d.size() == 7);

		d = c.matches(".*pdf.*");
		assertTrue(d.size() == 2);
		assertEquals(d.toString(), "application/pdf,application/x-pdf-something");
	}
}
