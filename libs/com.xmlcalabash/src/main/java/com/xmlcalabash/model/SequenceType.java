package com.xmlcalabash.model;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;

import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.InscopeNamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmEmptySequence;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;

/**
 * @author bert
 */
public class SequenceType {

	public static final SequenceType XS_STRING = new SequenceType(ItemType.STRING, OccurrenceIndicator.ONE);

	private final net.sf.saxon.s9api.SequenceType underlyingType;

	private static Processor proc = null;

	private SequenceType(net.sf.saxon.s9api.SequenceType sequenceType) {
		this.underlyingType = sequenceType;
	}

	private SequenceType(ItemType itemType, OccurrenceIndicator cardinality) {
		this(net.sf.saxon.s9api.SequenceType.makeSequenceType(itemType, cardinality));
	}

	/**
	 * @param namespaceResolver The node whose in-scope namespaces are to be used
	 *                          to resolve any prefix parts in the type name.
	 */
	public static SequenceType parse(String type, XdmNode namespaceResolver) throws IllegalArgumentException {
		OccurrenceIndicator cardinality;
		if (type.matches(".+[*?+]")) {
			switch (type.charAt(type.length() - 1)) {
			case '*': cardinality = OccurrenceIndicator.ZERO_OR_MORE; break;
			case '?': cardinality = OccurrenceIndicator.ZERO_OR_ONE; break;
			default:  cardinality = OccurrenceIndicator.ONE_OR_MORE;
			}
			type = type.substring(0, type.length() - 1);
		} else {
			cardinality = OccurrenceIndicator.ONE;
		}
		if (type.equals("map(*)"))
			return new SequenceType(ItemType.ANY_MAP, cardinality);
		if (type.matches("map\\(.+,.+\\)")) {
			SequenceType keyType = parse(type.substring(4, type.indexOf(",")), namespaceResolver);
			if (keyType.underlyingType.getOccurrenceIndicator() != OccurrenceIndicator.ONE)
				throw new IllegalArgumentException("Key type of a map must be atomic but got " + keyType);
			SequenceType valueType = parse(type.substring(type.indexOf(",") + 1, type.length() - 1), namespaceResolver);
			if (proc == null)
				// doesn't matter if no user-defined types or types that reference element or attributes names are used
				proc = new Processor(false);
			return new SequenceType(
				new ItemTypeFactory(proc).getMapType(keyType.underlyingType.getItemType(), valueType.underlyingType),
				cardinality);
		}
		if (type.equals("array(*)"))
			return new SequenceType(ItemType.ANY_ARRAY, cardinality);
		if (type.matches("array\\(.+\\)")) {
			SequenceType memberType = parse(type.substring(6, type.length() - 1), namespaceResolver);
			if (proc == null)
				proc = new Processor(false);
			return new SequenceType(
				new ItemTypeFactory(proc).getArrayType(memberType.underlyingType),
				cardinality);
		}
		if (type.contains(":")) {
			QName qname = new QName(type, namespaceResolver);
			if (XProcConstants.xs_string.equals(qname) && cardinality == OccurrenceIndicator.ONE)
				return XS_STRING;
			else {
				if (proc == null) proc = new Processor(false);
				try {
					return new SequenceType(new ItemTypeFactory(proc).getAtomicType(qname), cardinality);
				} catch (SaxonApiException e) {
					throw new IllegalArgumentException("Unsupported type: " + type, e);
				}
			}
		} else {
			throw new IllegalArgumentException("Unsupported type: " + type);
		}
	}

	/**
	 * @param namespaceResolver The node whose in-scope namespaces are to be used
	 *                          to resolve any prefix parts if the value is a string
	 *                          that needs to be cast to a QName.
	 */
	public XdmValue cast(XdmValue value, XdmNode namespaceResolver) throws XProcException {
		ItemType itemType = underlyingType.getItemType();
		OccurrenceIndicator cardinality = underlyingType.getOccurrenceIndicator();
		switch (value.size()) {
		case 0:
			switch (cardinality) {
			case ZERO:
			case ZERO_OR_ONE:
			case ZERO_OR_MORE:
				return XdmEmptySequence.getInstance();
			default:
				throw new XProcException("Empty sequence can not be cast to " + this);
			}
		case 1:
			XdmItem item = value.iterator().next();
			if (itemType.matches(item)) {
				return value;
			} else if (itemType.getUnderlyingItemType().isAtomicType()
			           && (item.isAtomicValue() || item instanceof XdmNode)) {
				AtomicValue sourceValue;
				if (item.isAtomicValue()) {
					sourceValue = (AtomicValue)item.getUnderlyingValue();
				} else {
					try {
						AtomicSequence atomizedNode = ((XdmNode)item).getUnderlyingValue().atomize();
						if (atomizedNode.getLength() != 1) throw new RuntimeException("???");
						sourceValue = atomizedNode.head();
					} catch (XPathException e) {
						throw new XProcException(
							new RuntimeException("Value can not be cast to " + this + ": " + value, e));
					}
				}
				AtomicType sourceType = sourceValue.getItemType();
				AtomicType targetType = (AtomicType)itemType.getUnderlyingItemType();
				Converter converter = itemType.getConversionRules().getConverter(sourceType, targetType);
				if (converter == null)
					throw new XProcException(sourceType + " value can not be cast to " + this + ": " + value);
				if (targetType.isNamespaceSensitive()) {
					converter = converter.setNamespaceResolver(
						new InscopeNamespaceResolver((NodeInfo)namespaceResolver.getUnderlyingValue()));
				}
				ConversionResult result = converter.convert(sourceValue);
				if (result instanceof ValidationFailure) {
					throw new XProcException(
						new RuntimeException(sourceType + " value can not be cast to " + this + ": " + value,
						                     ((ValidationFailure)result).makeException()));
				} else {
					return XdmValue.wrap((AtomicValue)result);
				}
			} else {
				throw new XProcException("Value can not be cast to " + this + ": " + value);
			}
		default:
			switch (cardinality) {
			case ZERO:
			case ONE:
			case ZERO_OR_ONE:
				throw new XProcException("Sequence of more than one item can not be cast to " + this);
			default:
				for (XdmItem i : value)
					if (!itemType.matches(i))
						throw new XProcException("Value can not be cast to " + this + ": " + value);
				return value;
			}
		}
	}

	@Override
	public String toString() {
		return underlyingType.getItemType().toString() + underlyingType.getOccurrenceIndicator().toString();
	}

	@Override
	public int hashCode() {
		return underlyingType.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SequenceType))
			return false;
		return underlyingType.equals(((SequenceType)o).underlyingType);
	}
}
