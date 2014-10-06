package org.daisy.cli;

import org.junit.Test;
import static org.junit.Assert.*;

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

}
