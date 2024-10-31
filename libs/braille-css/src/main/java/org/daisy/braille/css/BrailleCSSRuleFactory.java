package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.css.Selector;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.csskit.RuleFactoryImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrailleCSSRuleFactory extends RuleFactoryImpl {
	
	public BrailleCSSRuleFactory() {
		this(Collections.emptyList(), true);
	}

	private final List<BrailleCSSExtension> extensions;
	private final boolean allowUnknownVendorExtensions;

	public BrailleCSSRuleFactory(Collection<BrailleCSSExtension> extensions,
	                             boolean allowUnknownVendorExtensions) {
		this.extensions = new ArrayList<>();
		for (BrailleCSSExtension x : extensions)
			if (x.getPrefix() == null || !x.getPrefix().matches("-.+-"))
				log.warn("CSS extension without prefix ignored: " + x);
			else
				this.extensions.add(x);
		this.allowUnknownVendorExtensions = allowUnknownVendorExtensions;
	}

	@Override
	public RuleMargin createMargin(String area) {
		return new RuleMarginImpl(area);
	}
	
	@Override
	public Selector createSelector() {
		return new SelectorImpl();
	}
	
	@Override
	public CombinedSelector createCombinedSelector() {
		return new CombinedSelectorImpl();
	}
	
	@Override
	public PseudoClass createPseudoClass(String name) throws IllegalArgumentException {
		if (name.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (name.startsWith(x.getPrefix()))
					return x.createPseudoClass(name);
			if (!allowUnknownVendorExtensions)
				throw new IllegalArgumentException(name + " is not a valid pseudo-class name");
		}
		try {
			return new SelectorImpl.PseudoClassImpl(name);
		} catch (IllegalArgumentException e) {
			if (!name.startsWith("-"))
				// might be an unprefixed extension pseudo-class (which the extension's parser may or may not support)
				for (BrailleCSSExtension x : extensions)
					try {
						return x.createPseudoClass(name);
					} catch (IllegalArgumentException ee) {
						// ignore
					}
			throw e;
		}
	}
	
	@Override
	public PseudoClass createPseudoClassFunction(String name, String... args) throws IllegalArgumentException {
		if (name.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (name.startsWith(x.getPrefix()))
					return x.createPseudoClassFunction(name, args);
			if (!allowUnknownVendorExtensions)
				throw new IllegalArgumentException(name + " is not a valid pseudo-class name");
		}
		try {
			return new SelectorImpl.PseudoClassImpl(name, args);
		} catch (IllegalArgumentException e) {
			if (!name.startsWith("-"))
				// might be an unprefixed extension pseudo-class (which the extension's parser may or may not support)
				for (BrailleCSSExtension x : extensions)
					try {
						return x.createPseudoClassFunction(name, args);
					} catch (IllegalArgumentException ee) {
						// ignore
					}
			throw e;
		}
	}
	
	@Override
	public PseudoElement createPseudoElement(String name) throws IllegalArgumentException {
		String n = name;
		if (n.startsWith(":"))
			n = n.substring(1);
		if (n.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (n.startsWith(x.getPrefix()))
					return x.createPseudoElement(name);
			if (!allowUnknownVendorExtensions)
				throw new IllegalArgumentException(n + " is not a valid pseudo-element name");
		}
		try {
			return new SelectorImpl.PseudoElementImpl(name);
		} catch (IllegalArgumentException e) {
			if (!n.startsWith("-"))
				// might be an unprefixed extension pseudo-element (which the extension's parser may or may not support)
				for (BrailleCSSExtension x : extensions)
					try {
						return x.createPseudoElement(name);
					} catch (IllegalArgumentException ee) {
						// ignore
					}
			throw e;
		}
	}
	
	@Override
	public PseudoElement createPseudoElementFunction(String name, String... args) throws IllegalArgumentException {
		String n = name;
		if (n.startsWith(":"))
			n = n.substring(1);
		if (n.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (n.startsWith(x.getPrefix()))
					return x.createPseudoElementFunction(name, args);
			if (!allowUnknownVendorExtensions)
				throw new IllegalArgumentException(n + " is not a valid pseudo-element name");
		}
		try {
			return new SelectorImpl.PseudoElementImpl(name, args);
		} catch (IllegalArgumentException e) {
			if (!n.startsWith("-"))
				// might be an unprefixed extension pseudo-element (which the extension's parser may or may not support)
				for (BrailleCSSExtension x : extensions)
					try {
						return x.createPseudoElementFunction(name, args);
					} catch (IllegalArgumentException ee) {
						// ignore
					}
			throw e;
		}
	}

	public VendorAtRule<? extends Rule<?>> createRuleVendor(String name, List<Rule<?>> content)
			throws IllegalArgumentException {
		if (name.startsWith("-")) {
			for (BrailleCSSExtension x : extensions)
				if (name.startsWith(x.getPrefix()))
					return x.createAtRule(name, content);
			if (!allowUnknownVendorExtensions)
				throw new IllegalArgumentException(name + " is not a valid at-rule name");
		}
		if (!name.startsWith("-"))
			// might be an unprefixed extension pseudo-element (which the extension's parser may or may not support)
			for (BrailleCSSExtension x : extensions)
				try {
					return x.createAtRule(name, content);
				} catch (IllegalArgumentException ee) {
					// ignore
				}
		return new VendorAtRule<Rule<?>>(name, content);
	}

	private static final Logger log = LoggerFactory.getLogger(BrailleCSSRuleFactory.class);
}
