package org.daisy.common.transform;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * {@link XMLTransformer} with one XML input (named "source"), one XML output (named "result"), and
 * a "parameters" port with QName/String pairs.
 */
public abstract class SingleInSingleOutXMLTransformer implements XMLTransformer {

	public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
		QName _source = new QName("source");
		QName _result = new QName("result");
		QName _parameters = new QName("parameters");
		for (QName n : input.keySet())
			if (!n.equals(_source) && !n.equals(_parameters))
				throw new IllegalArgumentException("unexpected value on input port " + n);
		for (QName n : output.keySet())
			if (!n.equals(_result))
				throw new IllegalArgumentException("unexpected value on output port " + n);
		InputValue<?> source = input.get(_source);
		if (source != null && !(source instanceof XMLInputValue))
			throw new IllegalArgumentException("input on 'source' port is not XML");
		InputValue<?> params = input.get(_parameters);
		OutputValue<?> result = output.get(_result);
		if (result != null && !(result instanceof XMLOutputValue))
			throw new IllegalArgumentException("output on 'result' port is not XML");
		return transform((XMLInputValue<?>)source, (XMLOutputValue<?>)result, params);
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
