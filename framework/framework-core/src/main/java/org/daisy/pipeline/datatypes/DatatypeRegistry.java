/*
 *
 */
package org.daisy.pipeline.datatypes;

import com.google.common.base.Optional;


// TODO: Auto-generated Javadoc
/**
 * Keeps track of the registered datatypes 
 */
public interface DatatypeRegistry {

	/**
	 * Gets the datatype using its id.
	 *
	 * @param uri the uri
	 * @return the script
	 */
	public Optional<DatatypeService> getDatatype(String id);

	/**
	 * Gets all the datatypes
	 *
	 * @return the scripts
	 */
	public Iterable<DatatypeService> getDatatypes();

        public void register(DatatypeService service);
        public void unregister(DatatypeService service);
}
