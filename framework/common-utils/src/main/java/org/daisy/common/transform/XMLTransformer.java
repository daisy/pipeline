package org.daisy.common.transform;

import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

/**
 * Transform a number of input values to output values, at least one input or output being XML or
 * convertable to/from XML.
 *
 * Note that this interface does not extend javax.xml.transform.Transformer.
 */
public interface XMLTransformer {

	/**
	 * @param input Supplier of XDM values as a map from port name ({@link QName}) to {@link
	 *         InputValue} object. If the map returns null for a certain key it means that port is
	 *         not connected. If the map itself is null it means no input ports are connected.
	 * @param output Consumer of XDM values as a map from port name ({@link QName}) to {@link
	 *         OutputValue} object. If the map returns null for a certain key it means that port is
	 *         not connected. If the map itself is null it means no output ports are connected.
	 * @return a {@link Runnable} that executes the transformation. Transformations can be run
	 *         inside threads to keep the buffers between transformers small. If not run inside
	 *         threads, the transformations must be executed in the correct order. {@link
	 *         Runnable#run()} may throw a {@link TransformerException} if an input could not be
	 *         read, the transformation failed, or an output could not be written.
	 */
	public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output);

	public default Runnable transform(InputValue<?> input, OutputValue<?> output) {
		return transform(
			ImmutableMap.of(new QName("source"), input),
			ImmutableMap.of(new QName("result"), output)
		);
	}
}
