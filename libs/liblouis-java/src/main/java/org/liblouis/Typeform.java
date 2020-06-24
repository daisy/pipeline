package org.liblouis;

public final class Typeform {

	public static final Typeform PLAIN_TEXT = new Typeform(null, (short)0);
	public static final Typeform COMPUTER = new Typeform("computer", (short)0x0400);
	
	private final String name;
	
	/**
	 * The name of this typeform as defined in the table. If it is a combination of typeforms, the
	 * name is null.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Combine with other typeform.
	 */
	public Typeform add(Typeform other) {
		if (other == null)
			return this;
		Translator table; {
			if (this.table == null)
				table = other.table;
			else if (other.table == null)
				table = this.table;
			else if (this.table.equals(other.table))
				table = this.table;
			else
				throw new IllegalArgumentException("Can not add two typeforms from different tables.");
		}
		return new Typeform(null, (short)(this.value | other.value), table);
	}
	
	final short value;
	
	/**
	 * The table that this typeform is specific to, or null if this is a globally usable typeform.
	 */
	final Translator table;

	// private constructors
	Typeform(String name, short value) {
		this(name, value, null);
	}

	Typeform(String name, short value, Translator table) {
		this.name = name;
		this.value = value;
		this.table = table;
	}
}
