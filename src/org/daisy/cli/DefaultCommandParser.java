package org.daisy.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultCommandParser {
	private String delimiter;
	private String optionalArgumentPrefix;
	private String switchArgumentPrefix;
	private Map<Character, SwitchArgument> switches;
	
	public DefaultCommandParser() {
		delimiter = "=";
		optionalArgumentPrefix = "-";
		switchArgumentPrefix = "-";
		switches = new HashMap<Character, SwitchArgument>();
	}
	
	public String getKeyValueDelimiter() {
		return delimiter;
	}

	/**
	 * Sets the delimiter to use between key and value in the argument
	 * strings passed to the UI.
	 * @param value the delimiter to use
	 */
	public void setKeyValueDelimiter(String value) {
		delimiter = value;
	}
	
	public String getOptionalArgumentPrefix() {
		return optionalArgumentPrefix;
	}

	/**
	 * Sets the optional argument prefix to use in argument strings passed to
	 * the UI.
	 * @param value the prefix to use
	 */
	public void setOptionalArgumentPrefix(String value) {
		optionalArgumentPrefix = value;
	}

	public String getSwitchArgumentPrefix() {
		return switchArgumentPrefix;
	}

	public void setSwitchArgumentPrefix(String switchArgumentPrefix) {
		this.switchArgumentPrefix = switchArgumentPrefix;
	}
	
	public SwitchArgument addSwitch(SwitchArgument value) {
		return switches.put(value.getKey(), value);
	}
	
	/**
	 * Gets the switch arguments.
	 * @return returns the switch arguments
	 */
	public Collection<SwitchArgument> getSwitches() {
		return switches.values();
	}

	//Process switches and turn them into optional arguments
	//TODO:remove, integrated in "parse" below
	String[] processSwitches(String[] args) {
		for (int i=0; i<args.length; i++) {
			String s = args[i];
			if (s.startsWith(switchArgumentPrefix) && s.length()==switchArgumentPrefix.length()+1) {
				String t = s.substring(switchArgumentPrefix.length());
				SwitchArgument sc = switches.get(t.charAt(0));
				if (sc!=null) {
					args[i] = optionalArgumentPrefix+sc.getName()+delimiter+sc.getValue();
				}
			}
		}
		return args;
	}
	
	public CommandParserResult parse(String[] args) {
		String[] t;
		DefaultCommandParserResult.Builder builder = new DefaultCommandParserResult.Builder();
		for (String s : args) {
			s = s.trim();
			t = s.split(delimiter, 2);
			if (s.startsWith(optionalArgumentPrefix) && t.length==2) {
				builder.addOptional(t[0].substring(1), t[1]);
			} else if (s.startsWith(switchArgumentPrefix) && s.length()==switchArgumentPrefix.length()+1) {
				SwitchArgument sc = switches.get(s.substring(switchArgumentPrefix.length()).charAt(0));
				if (sc!=null) {
					builder.addOptional(sc.getName(), sc.getValue());
				}
			} else {
				builder.addRequired(s);
			}
		}
		return builder.build();
	}

}
