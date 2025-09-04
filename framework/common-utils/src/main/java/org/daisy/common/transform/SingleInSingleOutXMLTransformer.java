package org.daisy.common.transform;

import java.util.HashMap;
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

	/**
	 * Create a {@link SingleInSingleOutXMLTransformer} from a {@link XMLTransformer}. The {@link
	 * XMLTransformer} is assumed to have one XML input, one parameter input, and one XML output.
	 */
	public static SingleInSingleOutXMLTransformer from(XMLTransformer transformer) {
		if (transformer instanceof SingleInSingleOutXMLTransformer)
			return (SingleInSingleOutXMLTransformer)transformer;
		else
			return from(transformer, _SOURCE, _RESULT, _PARAMETERS);
	}

	public static SingleInSingleOutXMLTransformer from(XMLTransformer transformer,
	                                                   QName inputPort, QName outputPort, QName parameterPort) {
		return new SingleInSingleOutXMLTransformer() {
			@Override
			public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
				ImmutableMap.Builder<QName,InputValue<?>> input = new ImmutableMap.Builder<>();
				input.put(inputPort, source);
				if (params != null)
					input.put(parameterPort, params);
				return transformer.transform(input.build(),
				                             ImmutableMap.of(outputPort, result));
			}
		};
	}

	/* Utility functions */

	protected static Map<QName,InputValue<?>> getParameterMap(InputValue<?> parameters) {
		Map<QName,InputValue<?>> parameterMap = new HashMap<>();
		Map<?,?> map; {
			try {
				map = parameters.asObject(Map.class);
			} catch (UnsupportedOperationException e) {
				throw new IllegalArgumentException("unsupported interface for parameter input", e);
			}
		}
		for (Object k : map.keySet()) {
			QName name; {
				if (!(k instanceof QName))
					throw new IllegalArgumentException("unsupported interface for parameter input");
				name = (QName)k;
			}
			Object value = map.get(k);
			if (!(value instanceof InputValue))
				throw new IllegalArgumentException("unsupported interface for parameter input");
			parameterMap.put(name, (InputValue<?>)value);
		}
		return parameterMap;
	}
}
