package org.daisy.common.xproc.calabash;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.xmlcalabash.io.DocumentSequence;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.model.Step;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.TransformerException;

public class XMLCalabashInputValue extends SaxonInputValue {

	public static XMLCalabashInputValue of(ReadablePipe pipe)  {
		return new XMLCalabashInputValue(pipe);
	}

	public static XMLCalabashInputValue of(InputValue<?> value) throws IllegalArgumentException, NoSuchElementException {
		if (value instanceof XMLCalabashInputValue)
			return (XMLCalabashInputValue)value;
		else if (value instanceof SaxonInputValue)
			return new XMLCalabashInputValue((SaxonInputValue)value);
		else
			throw new IllegalArgumentException("can not create XMLCalabashInputValue from " + value);
	}

	private final ReadablePipe pipe;

	protected XMLCalabashInputValue(ReadablePipe value) {
		super(
			new Iterator<XdmNode>() {
				public boolean hasNext() {
					try {
						return value.moreDocuments();
					} catch (SaxonApiException e) {
						throw new TransformerException(e);
					}
				}
				public XdmNode next() throws NoSuchElementException, TransformerException {
					if (!hasNext())
						throw new NoSuchElementException();
					try {
						return value.read();
					} catch (SaxonApiException e) {
						throw new TransformerException(e);
					}
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			});
		this.pipe = value;
	}

	protected XMLCalabashInputValue(SaxonInputValue value) throws NoSuchElementException {
		super(value, true);
		pipe = createReadablePipe(asXdmItemIterator());
	}

	public ReadablePipe asReadablePipe() {
		return pipe;
	}

	private static ReadablePipe createReadablePipe(Iterator<XdmItem> itemIterator) {
		return new ReadablePipe() {
			private int read = 0;
			public void canReadSequence(boolean sequence) {
				throw new UnsupportedOperationException();
			}
			public boolean readSequence() {
				return true;
			}
			public XdmNode read() throws SaxonApiException {
				XdmItem i = itemIterator.next();
				if (i instanceof XdmNode)
					return (XdmNode)i;
				else
					throw new TransformerException(new IllegalArgumentException("expected a node"));
			}
			public void setReader(Step step) {}
			public void setNames(String stepName, String portName) {}
			public void resetReader() {
				if (read > 0)
					throw new UnsupportedOperationException("Can not reset reader");
			}
			public boolean moreDocuments() {
				return itemIterator.hasNext();
			}
			public boolean closed() {
				throw new UnsupportedOperationException();
			}
			public int documentCount() {
				return read;
			}
			public DocumentSequence documents() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
