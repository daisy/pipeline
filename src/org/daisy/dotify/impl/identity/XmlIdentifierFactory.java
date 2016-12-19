package org.daisy.dotify.impl.identity;

import org.daisy.dotify.api.identity.Identifier;
import org.daisy.dotify.api.identity.IdentifierFactory;
import org.daisy.dotify.api.tasks.FileDetails;

import aQute.bnd.annotation.component.Component;

@Component
public class XmlIdentifierFactory implements IdentifierFactory {

	/**
	 * Creates a new xml identifer factory.
	 */
	public XmlIdentifierFactory() {
		// no fields
	}

	@Override
	public Identifier newIdentifier() {
		return new XmlIdentifier();
	}

	@Override
	public boolean accepts(FileDetails type) {
				// accepts if the format either hasn't been identified or it has been identified as xml ...
		return (type.getFormatName()==null||"xml".equals(type.getFormatName())) 
				// and this factory hasn't been tried already
				&& !type.getProperties().containsKey("xmlns");
	}

}
