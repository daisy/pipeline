package org.daisy.pipeline.script.impl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcPortInfo.Kind;
import org.daisy.pipeline.script.impl.parser.StaxXProcPipelineInfoParser;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

// TODO: Auto-generated Javadoc
/**
 * The Class XProcPipelineInfoParserTest.
 */
public class XProcPipelineInfoParserTest {

	/** The xproc. */
	private XProcPipelineInfo xproc;

	/**
	 * Sets the up.
	 *
	 * @throws URISyntaxException the uRI syntax exception
	 */
	@Before
	public void setUp() throws URISyntaxException {
		StaxXProcPipelineInfoParser parser = new StaxXProcPipelineInfoParser();
		parser.setFactory(XMLInputFactory.newInstance());
		xproc = parser.parse(this.getClass().getClassLoader()
				.getResource("script.xpl").toURI());
	}

	/**
	 * Test input ports.
	 */
	@Test
	public void testInputPorts() {
		Iterable<XProcPortInfo> ports = xproc.getInputPorts();
		assertNotNull(ports);
		assertEquals(2, Iterables.size(ports));
	}

	/**
	 * Test input port.
	 */
	@Test
	public void testInputPort() {
		XProcPortInfo port = xproc.getInputPort("source");
		assertNotNull(port);
		assertEquals("source", port.getName());
		assertEquals(Kind.INPUT, port.getKind());
		assertTrue(port.isPrimary());
		assertTrue(port.isSequence());

	}

	/**
	 * Test output ports.
	 */
	@Test
	public void testOutputPorts() {
		Iterable<XProcPortInfo> ports = xproc.getOutputPorts();
		assertNotNull(ports);
		assertEquals(3, Iterables.size(ports));
	}

	/**
	 * Test output port.
	 */
	@Test
	public void testOutputPort() {
		XProcPortInfo port = xproc.getOutputPort("result");
		assertNotNull(port);
		assertEquals("result", port.getName());
		assertEquals(Kind.OUTPUT, port.getKind());
		assertTrue(port.isPrimary());
		assertTrue(port.isSequence());

	}

	/**
	 * Test parameter port.
	 */
	@Test
	public void testParameterPort() {
		// FIXME test parameter ports
		// XProcPortInfo port = xproc.getParameterPorts();
		// assertNotNull(port);
		// assertEquals("source", port.getName());
		// assertEquals(true, port.isPrimary());
		// assertEquals(true, port.isSequence());

	}

	/**
	 * Test option.
	 */
	@Test
	public void testOption() {
		XProcOptionInfo info = xproc.getOption(new QName("option1"));
		assertNotNull(info);
		assertEquals(new QName("option1"), info.getName());
		assertEquals(".", info.getSelect());
		assertEquals(true, info.isRequired());
                 assertEquals("There are 3 options",3,Iterables.size(xproc.getOptions()));
	}
}
