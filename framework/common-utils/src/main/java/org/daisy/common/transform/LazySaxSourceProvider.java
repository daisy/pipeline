package org.daisy.common.transform;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import com.google.common.base.Supplier;

public class LazySaxSourceProvider implements Supplier<Source>{

	private String systemId;


	/**
	 * Constructs a new instance.
	 *
	 * @param systemId The systemId for this instance.
	 */
	public LazySaxSourceProvider(String systemId) {
		this.systemId=systemId;
	}


	@Override
	public Source get() {
		SAXSource src=new ProxiedSAXSource();
		src.setSystemId(this.systemId);
		return src;
	}
	private class ProxiedSAXSource extends SAXSource{

		@Override
		public void setSystemId(String systemId) {
			super.setSystemId(systemId);
			LazySaxSourceProvider.this.systemId=systemId;
		}

	}
}
