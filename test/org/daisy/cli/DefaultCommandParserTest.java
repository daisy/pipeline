package org.daisy.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DefaultCommandParserTest {
	
	@Test
	public void testSwitchProcessing_01() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.addSwitch(new SwitchArgument('c', "copy", "true", "Turns on copying."));
		String[] ret = parser.processSwitches(new String[]{"-c"});
		assertArrayEquals(new String[]{"-copy=true"}, ret);
	}
	
	@Test
	public void testSwitchProcessing_02() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.setOptionalArgumentPrefix("--");
		parser.addSwitch(new SwitchArgument('c', "copy", "true", "Turns on copying."));
		String[] ret = parser.processSwitches(new String[]{"-c"});
		assertArrayEquals(new String[]{"--copy=true"}, ret);
	}
	
	@Test
	public void testSwitchProcessing_03() {
		DefaultCommandParser parser = new DefaultCommandParser();
		parser.setOptionalArgumentPrefix("--");
		parser.addSwitch(new SwitchArgument('c', "copy", "true", "Turns on copying."));
		parser.addSwitch(new SwitchArgument('d', "delete", "all", "Delete originals."));
		String[] ret = parser.processSwitches(new String[]{"-c", "--copy=true", "-e", "-d"});
		assertArrayEquals(new String[]{"--copy=true", "--copy=true", "-e", "--delete=all"}, ret);
	}
	
	@Test
	public void testCommandParser_01() {
		DefaultCommandParser parser = new DefaultCommandParser();
		CommandParserResult result = parser.parse(new String[]{"R1", "R2", "-option=value"});
		Map<String, String> opts = result.getOptional();
		List<String> req = result.getRequired();
		assertEquals(2, req.size());
		assertEquals("R1", req.get(0));
		assertEquals("R2", req.get(1));

		assertEquals(1, opts.size());
		assertEquals("option", opts.keySet().iterator().next());
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
		assertEquals("option", opts.keySet().iterator().next());
		assertEquals("value", opts.get("option"));
	}

}
