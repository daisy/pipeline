package org.daisy.cli;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DefaultCommandParserTest {
	
	@Test
	public void testSwitchProcessing_01() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.addSwitch(new SwitchArgument('c', "copy", "true", "Turns on copying."));
		CommandParserResult result = parser.parse(new String[]{"-c"});
		
		Map<String, String> opts = result.getOptional();

		assertEquals(1, opts.size());
		assertEquals("true", opts.get("copy"));
	}

	@Test
	public void testSwitchProcessing_02() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.addSwitch(new SwitchArgument('c', "copy", "true", "Turns on copying."));
		parser.addSwitch(new SwitchArgument('d', "delete", "all", "Delete originals."));
		
		CommandParserResult result = parser.parse(new String[]{"-c", "--copy=true", "-e", "-d"});
		
		List<String> req = result.getRequired();
		assertEquals(1, req.size());
		assertEquals("-e", req.get(0));

		Map<String, String> opts = result.getOptional();
		assertEquals(2, opts.size());
		assertEquals("true", opts.get("copy"));
		assertEquals("all", opts.get("delete"));
	}
	
	@Test
	public void testCommandParser_01() {
		DefaultCommandParser parser = new DefaultCommandParser();
		CommandParserResult result = parser.parse(new String[]{"R1", "R2", "--option=value"});
		Map<String, String> opts = result.getOptional();
		List<String> req = result.getRequired();
		assertEquals(2, req.size());
		assertEquals("R1", req.get(0));
		assertEquals("R2", req.get(1));

		assertEquals(1, opts.size());
		assertEquals("value", opts.get("option"));
	}
	
	@Test
	public void testCommandParser_02() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.addSwitch(new SwitchArgument('d', "option", "value", "Switch option value."));
		CommandParserResult result = parser.parse(new String[]{"R1", "R2", "-d"});
		Map<String, String> opts = result.getOptional();
		List<String> req = result.getRequired();
		assertEquals(2, req.size());
		assertEquals("R1", req.get(0));
		assertEquals("R2", req.get(1));

		assertEquals(1, opts.size());
		assertEquals("value", opts.get("option"));
	}
	
	@Test
	public void testCommandParser_03() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.setOptionalArgumentPrefix("--");
		parser.addSwitch(new SwitchArgument('d', "default", "option", "value", "Switch option value."));
		CommandParserResult result = parser.parse(new String[]{"--default"});
		Map<String, String> opts = result.getOptional();
		List<String> req = result.getRequired();
		assertEquals(0, req.size());

		assertEquals(1, opts.size());
		assertEquals("value", opts.get("option"));
	}

}
