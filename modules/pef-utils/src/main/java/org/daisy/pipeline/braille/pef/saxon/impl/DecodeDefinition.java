package org.daisy.pipeline.braille.pef.saxon.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.TableRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pef:decode",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class DecodeDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "pef-decode");
	
	@Reference(
		name = "TableRegistry",
		unbind = "-",
		service = TableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(TableRegistry registry) {
		tableRegistry = registry;
	}
	
	private TableRegistry tableRegistry;
	private final Query fallbackQuery = mutableQuery()
		.add("id", "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US");
	
	@Override
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING,
				SequenceType.SINGLE_STRING};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					Query tableQuery = query(((AtomicSequence)arguments[0]).getStringValue());
					String text = ((AtomicSequence)arguments[1]).getStringValue();
					Table table;
					try {
						table = tableRegistry.get(tableQuery).iterator().next(); }
					catch (NoSuchElementException e) {
						try {
							table = tableRegistry.get(fallbackQuery).iterator().next(); }
						catch (NoSuchElementException e2) {
							throw new XPathException("Could not find a table for query: " + tableQuery); }}
					return new StringValue(table.newBrailleConverter().toBraille(text)); }
				catch (XPathException e) {
					throw e; }
				catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:pef-decode", e); }
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(EncodeDefinition.class);
	
}
