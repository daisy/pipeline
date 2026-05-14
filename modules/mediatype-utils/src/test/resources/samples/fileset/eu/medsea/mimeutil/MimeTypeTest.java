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

import junit.framework.TestCase;

public class MimeTypeTest extends TestCase {

	public final void testHashCode() {
		MimeType mt1 = new MimeType("text/plain");
		MimeType mt2 = new MimeType("text/plain");
		MimeType mt3 = new MimeType("application/xml");

		assertFalse(mt1 == mt2);
		assertTrue(mt1.equals(mt2));
		assertTrue(mt1.hashCode() == mt2.hashCode());
		assertFalse(mt1.hashCode() == mt3.hashCode());
	}

	public final void testMimeTypeMimeType() {
		MimeType mt1 = new MimeType("text/plain");
		MimeType mt2 = new MimeType(mt1);
		assertTrue(mt1.equals(mt2));
	}

	public final void testGetMediaType() {
		MimeType mt1 = new MimeType("text/plain");
		assertEquals(mt1.getMediaType(), "text");
	}

	public final void testGetSubType() {
		MimeType mt1 = new MimeType("text/plain");
		assertEquals(mt1.getSubType(), "plain");
	}

	public final void testGetSpecificity() {
		MimeTypeHashSet mimeTypes = new MimeTypeHashSet();
		MimeType mt = new MimeType("text/plain");
		// This is the first MimeType added with this media and sub type
		// and adding mime type with the same media and sub types will update
		// only the specificity of the entry
		mimeTypes.add(mt);

		assertTrue(mt.getSpecificity() == 1);

		mimeTypes.add("text/plain");
		mimeTypes.add("text/plain,text/plain");

		assertTrue(mimeTypes.size() == 1);
		assertTrue(mimeTypes.toString().equals("text/plain"));

		assertTrue(mt.getSpecificity() == 4);

		MimeType mt2 = new MimeType("text/plain");
		mt2.setSpecificity(6);
		mimeTypes.add(mt2);

		// Make sure that a new MimeType as well as the original are given the same specificity
		assertTrue(mt2.getSpecificity() == 10);
		assertTrue(mt.getSpecificity() == 10);

		// Make sure that setting the original reference to null does not remove it from the collection
		mt = null;
		assertTrue(mimeTypes.contains("text/plain"));
	}

	public final void testCompareTo() {
		MimeType mt1 = new MimeType("a/b");
		MimeType mt2 = new MimeType("b/c");
		MimeType mt3 = new MimeType("a/b");

		assertTrue(mt1.compareTo(mt2) < 0);
		assertTrue(mt2.compareTo(mt1) > 0);
		assertTrue(mt1.compareTo(mt3) == 0);
	}
}
