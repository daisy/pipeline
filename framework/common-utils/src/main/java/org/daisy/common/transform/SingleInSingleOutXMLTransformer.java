package org.daisy.common.transform;

import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

/**
 * {@link XMLTransformer} with one XML input (named "source"), one XML output (named "result"), and
 * a "parameters" port with QName/String pairs.
 */
public abstract class SingleInSingleOutXMLTransformer implements XMLTransformer {

	private static final QName _SOURCE = new QName("source");
	private static final QName _RESULT = new QName("result");
	private static final QName _PARAMETERS = new QName("parameters");

	public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
		input = XMLTransformer.validateInput(input,
		                                     ImmutableMap.of(_SOURCE,     InputType.MANDATORY_NODE_SEQUENCE,
		                                                     _PARAMETERS, InputType.OPTIONAL_ITEM_SEQUENCE));
		output = XMLTransformer.validateOutput(output,
		                                       ImmutableMap.of(_RESULT,   OutputType.NODE_SEQUENCE));
		return transform((XMLInputValue<?>)input.get(_SOURCE),
		                 (XMLOutputValue<?>)output.get(_RESULT),
		                 input.get(_PARAMETERS));
	}

	public abstract Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params);

	/**
	 * Returns a composed transformer that runs this transformation with as input the output of the
	 * <code>before</code> transformation, and the parameters being passed to both.
	 *
	 * @param buffer The {@link Buffer} instance to use for connecting the transformers.
	 * @param concurrent Whether to run the transformations concurrently, or to run the
	 *     <code>after</code> transformation only when this transformation has completed.
	 */
	public SingleInSingleOutXMLTransformer andThen(SingleInSingleOutXMLTransformer after, Buffer<?,?> buffer, boolean concurrent) {
		SingleInSingleOutXMLTransformer thiz = this;
		return new SingleInSingleOutXMLTransformer() {
			public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
				if (concurrent)
					return () -> {
						Thread afterThread = new Thread(after.transform(buffer.asInput(), result, params));
						afterThread.setUncaughtExceptionHandler(
							(_t, e) -> buffer.error(TransformerException.wrap(e)));
						afterThread.start();
						thiz.transform(source, buffer.asOutput(), params).run();
						buffer.done();
					};
				else
					return () -> {
						thiz.transform(source, buffer.asOutput(), params).run();
						buffer.done();
						after.transform(buffer.asInput(), result, params).run();
					};
			}
		};
	}
}
