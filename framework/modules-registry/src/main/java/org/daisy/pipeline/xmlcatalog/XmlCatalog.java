package org.daisy.pipeline.xmlcatalog;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Simple xml catalog respresentation which gives access to the mapped uri, systemId's and publicId's
 */
public class XmlCatalog {

	/** The m public mappings. */
	HashMap<String, URI> mPublicMappings = new HashMap<String, URI>();

	/** The m system id mappings. */
	HashMap<URI, URI> mSystemIdMappings = new HashMap<URI, URI>();

	/** The m uri mappings. */
	HashMap<URI, URI> mURIMappings = new HashMap<URI, URI>();

	/** The m uri mappings. */
	HashMap<URI, URI> mRewriteUris= new HashMap<URI, URI>();

	/**
	 * Instantiates a new xml catalog.
	 *
	 * @param publicMappings the public mappings
	 * @param systemIdMappings the system id mappings
	 * @param uriMappMappings the uri mapp mappings
	 */
	private XmlCatalog(HashMap<String, URI> publicMappings,
			HashMap<URI, URI> systemIdMappings,
			HashMap<URI, URI> uriMappMappings,
                        HashMap<URI, URI> rewriteUris) {

			mPublicMappings.putAll(publicMappings);
			mSystemIdMappings.putAll(systemIdMappings);
			mURIMappings.putAll(uriMappMappings);
			mRewriteUris.putAll(rewriteUris);
	}

	/**
	 * Gets the systemId mappings.
	 *
	 * @return the system id mappings
	 */
	public Map<URI, URI> getSystemIdMappings() {
		return mSystemIdMappings;
	}

	/**
	 * Gets the uri mappings.
	 *
	 * @return the uri mappings
	 */
	public Map<URI, URI> getUriMappings() {
		return mURIMappings;
	}

	/**
	 * Gets the publicId mappings.
	 *
	 * @return the public mappings
	 */
	public Map<String, URI> getPublicMappings() {
		return mPublicMappings;
	}

	/**
	 * Gets the rewriteUris.
	 *
	 * @return the uri mappings
	 */
	public Map<URI, URI> getRewriteUris() {
		return mRewriteUris;
	}

	/**
	 *  Builds a xml catalog
	 */
	public static class Builder {

		/** The m public mappings. */
		HashMap<String, URI> mPublicMappings = new HashMap<String, URI>();

		/** The m system id mappings. */
		HashMap<URI, URI> mSystemIdMappings = new HashMap<URI, URI>();

		/** The m uri mappings. */
		HashMap<URI, URI> mURIMappings = new HashMap<URI, URI>();

		/** The m uri mappings. */
		HashMap<URI, URI> mRewriteUris= new HashMap<URI, URI>();
		/**
		 * With public mapping.
		 *
		 * @param entity the entity
		 * @param uri the uri
		 * @return the builder
		 */
		public Builder withPublicMapping(String entity, URI uri) {
			mPublicMappings.put(entity, uri);
			return this;
		}

		/**
		 * With systemId mapping.
		 *
		 * @param sysId the sys id
		 * @param uri the uri
		 * @return the builder
		 */
		public Builder withSystemIdMapping(URI sysId, URI uri) {
			mSystemIdMappings.put(sysId, uri);
			return this;
		}

		/**
		 * With uri mapping.
		 *
		 * @param name the name
		 * @param uri the uri
		 * @return the builder
		 */
		public Builder withUriMapping(URI name, URI uri) {
			mURIMappings.put(name, uri);
			return this;
		}

		/**
		 * With uri mapping.
		 *
		 * @param name the name
		 * @param uri the uri
		 * @return the builder
		 */
		public Builder withRewriteUri(URI startString, URI rewritePrefix) {
			this.mRewriteUris.put(startString, rewritePrefix);
			return this;
		}
		/**
		 * gets the catalog based on the state of the builder
		 *
		 * @return the xml catalog
		 */
		public XmlCatalog build(){
			return new XmlCatalog(mPublicMappings,mSystemIdMappings,mURIMappings,mRewriteUris);
		}

	}
}
