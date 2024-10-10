package org.daisy.pipeline.css.speech;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.ListIterator;
import java.util.List;
import java.util.Optional;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.OutputUtil;
import cz.vutbr.web.csskit.RuleFactoryImpl;
import cz.vutbr.web.domassign.SupportedCSS21;

import org.daisy.braille.css.BrailleCSSParserFactory;
import org.daisy.pipeline.css.CssSerializer;
import org.daisy.pipeline.css.impl.UnmodifiableTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoiceFamilyList extends AbstractList<Term<?>> implements TermList {

	private static final String PROPERTY = "voice-family";

	// using braille-css because of parseDeclaration() method
	private static final BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	private static final RuleFactory ruleFactory = RuleFactoryImpl.getInstance();
	private static final SupportedCSS speechCSS = SupportedCSS21.getInstance();
	private static final Logger logger = LoggerFactory.getLogger(VoiceFamilyList.class);

	public static VoiceFamilyList of(String list) throws IllegalArgumentException {
		Declaration d = parserFactory.parseDeclaration(PROPERTY + ": " + list);
		if (!PROPERTY.equals(d.getProperty()))
			throw new IllegalArgumentException();
		return of(d);
	}

	public static VoiceFamilyList of(List<Term<?>> list) throws IllegalArgumentException {
		List<VoiceFamily> l = new ArrayList<>();
		ListIterator<Term<?>> i = list.listIterator();
		boolean ageWithoutGender = false;
		while (i.hasNext()) {
			VoiceFamily f = VoiceFamily.of(i);
			l.add(f);
			ageWithoutGender = ageWithoutGender || (f.age != null && f.gender == null);
		}

		// Handle old syntax:
		// A comma separated list such as `engine, gender' normally means: "first select voice from
		// engine, and if that doesn't result in any matches, select voice with gender". It is
		// however very unlikely to be used for this purpose. So we could interpret it according to
		// the old syntax, for backwards compatibility.
		if (l.size() > 1 && l.size() <= 3) {
			String name = null;
			Age age = null;
			Gender gender = null;
			boolean extra = false;
			for (VoiceFamily f : l) {
				if (name == null && f.name != null && f.gender == null && f.age == null)
					name = f.name;
				else if (gender == null && f.gender != null && f.name == null && f.age == null)
					gender = f.gender;
				else if (age == null && f.age != null && f.name == null && f.gender == null)
					age = f.age;
				else {
					extra = true;
					break;
				}
			}
			if (!extra) {
				String oldSyntax = CssSerializer.serializeTermList(l);
				l.clear();
				if (age != null && gender == null) {
					// Handle age without gender:
					// (would also be done below, but do it here in order to improve the warning message)
					l.add(new VoiceFamily(name, age, Gender.MALE, null, null));
					l.add(new VoiceFamily(name, age, Gender.FEMALE, null, Operator.COMMA));
					ageWithoutGender = false;
				} else {
					l.add(new VoiceFamily(name, age, gender, null, null));
				}
				logger.warn("Use `font-family: " + CssSerializer.serializeTermList(l)
				            + "` instead of `font-family: " + oldSyntax + "`");
			}
		}

		// Handle age without gender:
		// If an age was not specified as part of a list in the old comma separated syntax (see
		// above), combine it with both male and female in two separate items.
		if (ageWithoutGender) {
			List<VoiceFamily> l2 = new ArrayList<>();
			for (VoiceFamily f : l) {
				if (f.age != null && f.gender == null) {
					logger.warn("Use `font-family: " + f.age + " male, " + f.age + " female` "
					            + "instead of `font-family: " + f.age + "`");
					l2.add(new VoiceFamily(f.name, f.age, Gender.MALE, f.variant, f.comma));
					l2.add(new VoiceFamily(f.name, f.age, Gender.FEMALE, f.variant, Operator.COMMA));
				} else
					l2.add(f);
			}
			l = l2;
		}
		return new VoiceFamilyList(l);
	}

	private List<VoiceFamily> list;

	private VoiceFamilyList(List<VoiceFamily> list) {
		super();
		this.list = list;
	}

	@Override
	public String toString() {
		return CssSerializer.toString(this);
	}

	@Override
	public VoiceFamily get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<Term<?>> getValue() {
		return this;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public VoiceFamilyList shallowClone() {
		try {
			return (VoiceFamilyList)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public VoiceFamilyList clone() {
		VoiceFamilyList clone = shallowClone();
		clone.list = new ArrayList<>(list);
		return clone;
	}

	@Override
	public VoiceFamilyList setValue(List<Term<?>> value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public VoiceFamilyList setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	public static class VoiceFamily extends UnmodifiableTerm<VoiceFamily> {

		private static VoiceFamily of(ListIterator<Term<?>> list) throws IllegalArgumentException {
			if (!list.hasNext())
				throw new IllegalArgumentException();
			Term<?> t = list.next();
			if (t instanceof VoiceFamily)
				return (VoiceFamily)t;
			Operator comma = t.getOperator();
			if (comma == Operator.SPACE)
				comma = null;
			else if (comma != null && comma != Operator.COMMA)
				throw new IllegalArgumentException();
			String name = null;
			Age age = null;
			Integer ageAsInt = null;
			Gender gender = null;
			Integer variant = null;
			do {
				if (name != null)
					throw new IllegalArgumentException();
				if (ageAsInt != null)
					throw new IllegalArgumentException();
				if (gender == null) {
					if (t instanceof TermIdent) {
						if (age == null) {
							try {
								age = Age.of((TermIdent)t);
								if (!age.toString().equals(((TermIdent)t).getValue())) {
									age = null;
									name = ((TermIdent)t).getValue();
								}
							} catch (IllegalArgumentException e) {
								try {
									gender = Gender.of((TermIdent)t);
								} catch (IllegalArgumentException ee) {
									name = ((TermIdent)t).getValue();
								}
							}
						} else {
							try {
								gender = Gender.of((TermIdent)t);
							} catch (IllegalArgumentException e) {
								name = ((TermIdent)t).getValue();
							}
						}
					} else if (t instanceof TermString) {
						name = ((TermString)t).getValue();
					} else if (t instanceof TermInteger) {
						if (age == null) {
							ageAsInt = ((TermInteger)t).getIntValue();
							if (ageAsInt < 0)
								throw new IllegalArgumentException();
						} else
							throw new IllegalArgumentException();
					} else
						throw new IllegalArgumentException();
				} else if (variant == null && t instanceof TermInteger) {
					variant = ((TermInteger)t).getIntValue();
					if (variant <= 0)
						throw new IllegalArgumentException();
				} else if (t instanceof TermIdent || t instanceof TermString) {
					name = ((Term<String>)t).getValue();
				} else
					throw new IllegalArgumentException();
				if (!list.hasNext())
					break;
				t = list.next();
				Operator o = t.getOperator();
				if (o == Operator.COMMA) {
					list.previous();
					break;
				} else if (o != null && o != Operator.SPACE)
					throw new IllegalArgumentException();
			} while (true);
			if (ageAsInt != null) {
				if (ageAsInt <= 16)
					age = Age.CHILD;
				else if (ageAsInt >= 70)
					age = Age.OLD;
				else
					age = Age.YOUNG;
				logger.warn("Specifying age as number is deprecated. "
				            + "Use `font-family: " + age + "` instead of `font-family: " + ageAsInt + "`");
			}
			if (name == null && gender == null && age == null)
				throw new IllegalArgumentException();
			return new VoiceFamily(name, age, gender, variant, comma);
		}

		private final String name;
		private final Age age;
		private final Gender gender;
		private final Integer variant;
		private final Operator comma;

		private VoiceFamily(String name, Age age, Gender gender, Integer variant, Operator comma) {
			this.name = name;
			this.age = age;
			this.gender = gender;
			this.variant = variant;
			this.comma = comma;
		}

		public Optional<String> getFamilyName() {
			return Optional.ofNullable(name);
		}

		public Optional<Age> getAge() {
			return Optional.ofNullable(age);
		}

		public Optional<Gender> getGender() {
			return Optional.ofNullable(gender);
		}

		public Optional<Integer> getVariant() {
			return Optional.ofNullable(variant);
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (comma != null) s.append(", ");
			if (age != null || gender != null) {
				if (age != null)
					s.append(age.toString());
				// gender may be null in a temporary VoiceFamily object (an object that does
				// not leave the VoiceFamilyList class)
				if (gender != null) {
					if (age != null) s.append(" ");
					s.append(gender.toString());
				}
				if (variant != null)
					s.append(" " + variant);
				if (name != null)
					s.append(" ");
			}
			if (name != null)
				s.append("'" + name.replaceAll("\n", "\\\\A ").replaceAll("'", "\\\\27 ") + "'");
			return s.toString();
		}

		@Override
		public Operator getOperator() {
			return comma;
		}
	}

	public static enum Age {
		CHILD,
		YOUNG,
		OLD;

		private static Age of(TermIdent age) throws IllegalArgumentException {
			return of(age.getValue());
		}

		private static Age of(String age) throws IllegalArgumentException {
			if (age == null)
				throw new IllegalArgumentException();
			age = age.toLowerCase();
			for (Age a : EnumSet.allOf(Age.class))
				if (a.toString().equals(age))
					return a;
			throw new IllegalArgumentException("Not an age: " + age);
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public static enum Gender {
		MALE,
		FEMALE,
		NEUTRAL;

		private static Gender of(TermIdent gender) throws IllegalArgumentException {
			return of(gender.getValue());
		}

		private static Gender of(String gender) throws IllegalArgumentException {
			if (gender == null)
				throw new IllegalArgumentException();
			gender = gender.toLowerCase();
			for (Gender g : EnumSet.allOf(Gender.class))
				if (g.toString().equals(gender))
					return g;
			throw new IllegalArgumentException("Not a gender: " + gender);
		}

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
}
