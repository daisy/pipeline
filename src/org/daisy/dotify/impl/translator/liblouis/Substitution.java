package org.daisy.dotify.impl.translator.liblouis;

import java.util.HashSet;
import java.util.Set;

class Substitution {
	private final String replacement;
	private final Set<CharClass> groups;
	
	public Substitution(String replacement) {
		super();
		this.replacement = replacement;
		this.groups = new HashSet<>();
	}
	
	public String getReplacement() {
		return replacement;
	}
	
	public void addGroup(CharClass group) {
		groups.add(group);
	}
	
	public void removeGroup(CharClass group) {
		groups.remove(group);
	}
	
	public Set<CharClass> getGroups() {
		return groups;
	}
}
