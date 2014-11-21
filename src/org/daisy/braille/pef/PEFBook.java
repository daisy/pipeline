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
package org.daisy.braille.pef;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

/**
 * Provides useful information about a PEF-document. 
 * @author Joel Håkansson
 */
public class PEFBook implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3068081596918714831L;
	
	private final Map<String, List<String>> metadata;
	
	// Book properties
	private final int volumes;
	private final int pageTags;
	private final int pages;
	private final int maxWidth;
	private final int maxHeight;
	private final String inputEncoding;
	private final boolean containsEightDot;
	private final int[] startPages;

	/**
	 * Loads information about a PEF-document from the supplied uri.
	 * @param uri the uri to a PEF-document
	 * @return returns a PEFBook object containing the information collected from the supplied PEF-document
	 */
	public static PEFBook load(URI uri) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
		return XPathPEFBook.load(uri);
	}
	
	PEFBook(Map<String, List<String>> metadata, int volumes, int pages, int pageTags, int maxWidth, int maxHeight, String inputEncoding, boolean containsEightDot, int[] startPages) {
		this.metadata = metadata;
		this.volumes = volumes;
		this.pages = pages;
		this.pageTags = pageTags;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
		this.inputEncoding = inputEncoding;
		this.containsEightDot = containsEightDot;
		this.startPages = startPages;
	}

	/**
	 * Gets the encoding used for this document at the time of the parsing.
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}
	
	/**
	 * Gets the number of volumes in this document.
	 */
	public int getVolumes() {
		return volumes;
	}
	
	/**
	 * Gets the total number of pages in this document
	 */
	public int getPages() {
		return pages;
	}
	
	/**
	 * Gets the number of page tags in this document.
	 */
	public int getPageTags() {
		return pageTags;
	}
	
	/**
	 * Gets the number of sheets in this document.
	 */
	public int getSheets() {
		return (pages+1)/2;
	}

	/**
	 * Gets the number of sheets in the specified volume
	 * @param volume the desired volume, where the first volume is 1 and the last equals getVolumes
	 * @return returns the number of sheets in the specified volume
	 * @throws IllegalArgumentException if the volume is less than 1 or greater than getVolumes
	 */
	public int getSheets(int volume) {
		if (volume<1 || volume > getVolumes()) {
			throw new IllegalArgumentException("Value out of range: " + volume);
		}
		return ((getLastPage(volume)-(getFirstPage(volume)-1))+1)/2;
	}
	
	/**
	 * Gets the first page number in the specified volume
	 * @param volume the desired volume, where the first volume is 1 and the last equals getVolumes.
	 * @return returns the first page number in the specified volume
	 * @throws IllegalArgumentException if the volume is less than 1 or greater than getVolumes
	 */
	public int getFirstPage(int volume) {
		if (volume<1 || volume > getVolumes()) {
			throw new IllegalArgumentException("Value out of range: " + volume);
		}
		return startPages[volume - 1];
	}
	
	/**
	 * Gets the last page number in the specified volume
	 * @param volume the desired volume (the first volume is 1 and the last is getVolumes)
	 * @return returns the last page number in the specified volume
	 * @throws IllegalArgumentException if the volume is less than 1 or greater than getVolumes
	 */
	public int getLastPage(int volume) {
		if (volume<1 || volume > getVolumes()) {
			throw new IllegalArgumentException("Value out of range: " + volume);
		}
		if (volume == getVolumes()) {
			return getPages();
		} else {
			// since startPages is zero based, "volume" means next volume
			return startPages[volume] - 1;
		}
	}
	
	/**
	 * Gets the maximum defined page width, in chars 
	 */
	public int getMaxWidth() {
		return maxWidth;
	}
	
	/**
	 * Gets the maximum defined page height, in rows
	 */
	public int getMaxHeight() {
		return maxHeight;
	}
	
	/**
	 * Returns true if this document contains eight dot patterns, false otherwise
	 */
	public boolean containsEightDot() {
		return containsEightDot;
	}
/*
	public HashMap<String, List<String>> getMetadata() {
		return metadata;
	}
*/	
	/**
	 * Gets a collection of all metadata keys in this document.
	 * A metadata key is a local element name in the http://purl.org/dc/elements/1.1/ namespace.
	 * 
	 */
	public Iterable<String> getMetadataKeys() {
		return metadata.keySet();
	}

	/**
	 * Gets a collection of values for a specfied metadata key.
	 * A metadata key is a local element name in the http://purl.org/dc/elements/1.1/ namespace.
	 * @param key the metadata to get values for
	 */
	public Iterable<String> getMetadata(String key) {
		List<String> c = metadata.get(key);
		if (c!=null) {
			return c;
		}
		return null;
		/*
		if (metadata.containsKey(key)) {
			String[] ret = new String[metadata.get(key).size()];
			metadata.get(key).toArray(ret);
			return ret;
		}
		return null;*/
	}
	
	/**
	 * Gets the document title from this document's metadata. Convenience method for getMetadata("title")  
	 */
	public Iterable<String> getTitle() {
		return getMetadata("title");
	}
	
	/**
	 * Gets the document authors from this document's metadata. Convenience method for getMetadata("creator")
	 */
	public Iterable<String> getAuthors() {
		return getMetadata("creator");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (containsEightDot ? 1231 : 1237);
		result = prime * result
				+ ((inputEncoding == null) ? 0 : inputEncoding.hashCode());
		result = prime * result + maxHeight;
		result = prime * result + maxWidth;
		result = prime * result
				+ ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result + pageTags;
		result = prime * result + pages;
		result = prime * result + Arrays.hashCode(startPages);
		result = prime * result + volumes;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PEFBook other = (PEFBook) obj;
		if (containsEightDot != other.containsEightDot)
			return false;
		if (inputEncoding == null) {
			if (other.inputEncoding != null)
				return false;
		} else if (!inputEncoding.equals(other.inputEncoding))
			return false;
		if (maxHeight != other.maxHeight)
			return false;
		if (maxWidth != other.maxWidth)
			return false;
		if (metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!metadata.equals(other.metadata))
			return false;
		if (pageTags != other.pageTags)
			return false;
		if (pages != other.pages)
			return false;
		if (!Arrays.equals(startPages, other.startPages))
			return false;
		if (volumes != other.volumes)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PEFBook [metadata=" + metadata + ", volumes=" + volumes
				+ ", pageTags=" + pageTags + ", pages=" + pages + ", maxWidth="
				+ maxWidth + ", maxHeight=" + maxHeight + ", inputEncoding="
				+ inputEncoding + ", containsEightDot=" + containsEightDot
				+ ", startPages=" + Arrays.toString(startPages) + "]";
	}

}
