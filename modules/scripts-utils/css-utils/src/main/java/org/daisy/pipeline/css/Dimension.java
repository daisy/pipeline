package org.daisy.pipeline.css;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dimension implements Comparable<Dimension> {

	public enum Unit {
		MM,
		CM,
		IN,
		PX,
		EM,
		CH,
		VW,
		VH;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public static Unit parse(String s) throws IllegalArgumentException {
			try {
				return Unit.valueOf(Unit.class, s.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("not a valid unit: " + s);
			}
		}
	}

	public interface RelativeDimensionBase {

		/**
		 * The length of a 'ch', in millimeters
		 */
		default double getCh() {
			throw new UnsupportedOperationException("character width is not known");
		}

		/**
		 * The length of a 'em', in millimeters
		 */
		default double getEm() {
			throw new UnsupportedOperationException("character height is not known");
		}

		/**
		 * The width of the viewport, in millimeters
		 *
		 * Used to convert to and from {@code vw}.
		 */
		default double getViewportWidth() {
			throw new UnsupportedOperationException("viewport width is not known");
		}

		/**
		 * The height of the viewport, in millimeters
		 *
		 * Used to convert to and from {@code vh}.
		 */
		default double getViewportHeight() {
			throw new UnsupportedOperationException("viewport height is not known");
		}
	}

	private final BigDecimal value;
	private final Unit unit;
	private final Dimension original;
	private final boolean rounded;

	public Dimension(BigDecimal value, Unit unit) {
		this(value, unit, null, false);
	}

	public Dimension(double value, Unit unit) {
		this(new BigDecimal(value), unit, null, false);
	}

	private Dimension(BigDecimal value, Unit unit, Dimension original, boolean rounded) {
		if (value == null)
			throw new IllegalArgumentException();
		this.value = value;
		this.unit = unit;
		this.original = original;
		this.rounded = rounded;
	}

	public BigDecimal getValue() {
		return value;
	}

	public Unit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return "" + value + (unit != null ? unit : "");
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Dimension))
			return false;
		Dimension that = (Dimension)o;
		if (this.unit == that.unit && !this.rounded && !that.rounded)
			return this.value.equals(that.value);
		else if (this.original != null && this.original.unit == that.unit)
			return this.original.equals(that);
		else if (that.original != null && that.original.unit == this.unit)
			return this.equals(that.original);
		else if (this.original != null)
			return this.original.equals(that);
		else if (that.original != null)
			return this.equals(that.original);
		else
			try {
				Dimension d = this.toUnit(that.unit);
				if (!d.rounded) // only return true if exactly equal
					return d.value.equals(that.value);
				d = that.toUnit(this.unit);
				return !d.rounded
					&& d.value.equals(this.value);
			} catch (UnsupportedOperationException e) {
				return false;
			}
	}

	private static Comparator<Dimension> comparator = comparator(null);

	@Override
	public int compareTo(Dimension o) {
		return comparator.compare(this, o);
	}

	private static final BigDecimal IN_TO_MM = new BigDecimal(25.4);
	private static final BigDecimal IN_TO_PX = new BigDecimal(96.0);

	public Dimension toUnit(Unit unit) throws UnsupportedOperationException {
		return toUnit(unit, null);
	}

	private static final RelativeDimensionBase defaultBase = new RelativeDimensionBase() {};

	public Dimension toUnit(Unit unit, RelativeDimensionBase base) throws UnsupportedOperationException {
		return toUnit(unit, base, false);
	}

	private Dimension toUnit(Unit unit, RelativeDimensionBase base, boolean round)
			throws UnsupportedOperationException {

		Unit toUnit = unit;
		Dimension fromDimension = this;
		Unit fromUnit = this.unit; {
			for (;;) { // we use a loop even though original never has an original
				if (fromUnit == toUnit)
					return fromDimension;
				if (fromDimension.original == null)
					break;
				fromDimension = fromDimension.original;
				fromUnit = fromDimension.unit;
			}
		}
		if (fromUnit == null)
			throw new UnsupportedOperationException("Can not convert from no unit to " + toUnit);
		else if (toUnit == null)
			throw new UnsupportedOperationException("Can not convert from " + fromUnit + " to no unit");
		else {
			if (base == null) base = defaultBase;
			BigDecimal value = fromDimension.value;
			MathContext precision = round ? MathContext.DECIMAL128 : MathContext.UNLIMITED;
			try {
				switch (fromUnit) {
				case MM:
					break;
				case CM:
					value = value.multiply(BigDecimal.TEN);
					break;
				case IN:
					value = value.multiply(IN_TO_MM);
					break;
				case PX:
					value = value.multiply(IN_TO_MM)
					             .divide(IN_TO_PX, precision);
					break;
				case CH:
					value = value.multiply(new BigDecimal(base.getCh()));
					break;
				case EM:
					value = value.multiply(new BigDecimal(base.getEm()));
					break;
				case VW:
					value = value.multiply(new BigDecimal(base.getViewportWidth()))
					             .divide(HUNDRED);
					break;
				case VH:
					value = value.multiply(new BigDecimal(base.getViewportHeight()))
					             .divide(HUNDRED);
					break;
				default:
					throw new RuntimeException("unknown unit: " + fromUnit);
				}
				switch (toUnit) {
				case MM:
					break;
				case CM:
					value = value.divide(BigDecimal.TEN);
					break;
				case IN:
					value = value.divide(IN_TO_MM, precision);
					break;
				case PX:
					value = value.multiply(IN_TO_PX)
					             .divide(IN_TO_MM, precision);
					break;
				case CH:
					value = value.divide(new BigDecimal(base.getCh()), precision);
					break;
				case EM:
					value = value.divide(new BigDecimal(base.getEm()), precision);
					break;
				case VW:
					value = value.multiply(HUNDRED)
					             .divide(new BigDecimal(base.getViewportWidth()), precision);
					break;
				case VH:
					value = value.multiply(HUNDRED)
					             .divide(new BigDecimal(base.getViewportHeight()), precision);
					break;
				default:
					throw new RuntimeException("unknown unit: " + toUnit);
				}
			} catch (ArithmeticException e) {
				if (round)
					throw new IllegalStateException(); // coding error
				return fromDimension.toUnit(toUnit, base, true);
			} catch (RuntimeException e) {
				throw new UnsupportedOperationException("Can not convert from " + fromUnit + " to " + toUnit, e);
			}
			return new Dimension(value, toUnit, fromDimension, round);
		}
	}

	private static final BigDecimal HUNDRED = BigDecimal.TEN.multiply(BigDecimal.TEN);

	public static Comparator<Dimension> comparator(RelativeDimensionBase base) {
		return new Comparator<Dimension>() {
			@Override
			public int compare(Dimension a, Dimension b) throws UnsupportedOperationException {
				if (a == null || b == null)
					throw new NullPointerException();
				if (a.unit == b.unit && !a.rounded && !b.rounded)
					return a.value.compareTo(b.value);
				else if (a.original != null && a.original.unit == b.unit)
					return compare(a.original, b);
				else if (b.original != null && b.original.unit == a.unit)
					return compare(a, b.original);
				else if (a.original != null)
					return compare(a.original, b);
				else if (b.original != null)
					return compare(a, b.original);
				else
					// this may compare rounded values
					return a.toUnit(b.unit, base).value.compareTo(b.value);
			}
		};
	}

	private static final Pattern DIMENSION_REGEX = Pattern.compile("(?<value>[+-]?([0-9]*\\.)?[0-9]+([eE][+-]?[0-9]+)?)(?<unit>[a-zA-Z]+)");

	public static Dimension parse(String s) throws IllegalArgumentException {
		Matcher m = DIMENSION_REGEX.matcher(s);
		if (m.matches())
			return new Dimension(new BigDecimal(m.group("value")),
			                     Unit.parse(m.group("unit")));
		else
			throw new IllegalArgumentException("Not a valid dimension: " + s);
	}
}
