package org.daisy.pipeline.persistence.impl.job;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.transform.Source;

import com.google.common.base.Supplier;

@Embeddable
public class PersistentSource implements Source,Serializable,Supplier<Source>{
	static final long serialVersionUID=98749124L;

        @Column(length=32672)
	private String systemId;

	/**
	 * Constructs a new instance.
	 */
	public PersistentSource() {
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param systemId The systemId for this instance.
	 */
	public PersistentSource(String systemId) {
		this.systemId = systemId;
	}

	@Override
	public String getSystemId() {
		return systemId;
	}

	@Override
	public void setSystemId(String systemId) {
		this.systemId=systemId;
	}

	@Override
	public Source get() {
		return this;
	}

}
