package org.daisy.common.transform;

import java.util.function.Function;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.NoSuchElementException;

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

	/**
	 * Check that the expected inputs are present and are of the expected type.
	 */
	public static Map<QName,InputValue<?>> validateInput(Map<QName,InputValue<?>> input,
	                                                     Map<QName,InputType> types)
			throws IllegalArgumentException {
		input = new HashMap<>(input);
		ImmutableMap.Builder<QName,InputValue<?>> map = ImmutableMap.builder();
		if (types != null)
			for (QName expectedPort : types.keySet()) {
				InputType expectedType = types.get(expectedPort);
				try {
					Optional<InputValue<?>> value = Optional.ofNullable(input.remove(expectedPort));
					value = expectedType.validate(value);
					if (value.isPresent())
						map.put(expectedPort, value.get());
				} catch (NoSuchElementException e) {
					throw new IllegalArgumentException(
						"no value specified on input port '" + expectedPort + "'");
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
						"invalid value on input port '" + expectedPort + ": " + e.getMessage());
				}
			}
		for (QName extraPort : input.keySet())
			throw new IllegalArgumentException("no value expected on input port '" + extraPort + "'");
		return map.build();
	};

	/**
	 * Check that the expected outputs are present and are of the expected type.
	 */
	public static Map<QName,OutputValue<?>> validateOutput(Map<QName,OutputValue<?>> output,
	                                                       Map<QName,OutputType> types)
			throws IllegalArgumentException {
		output = new HashMap<>(output);
		ImmutableMap.Builder<QName,OutputValue<?>> map = ImmutableMap.builder();
		if (types != null)
			for (QName expectedPort : types.keySet()) {
				OutputType expectedType = types.get(expectedPort);
				try {
					if (output.containsKey(expectedPort))
						map.put(expectedPort, expectedType.validate(output.remove(expectedPort)));
				} catch (NoSuchElementException e) {
					throw new IllegalArgumentException(
						"no value specified on output port '" + expectedPort + "'");
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
						"invalid value on output port '" + expectedPort + ": " + e.getMessage());
				}
			}
		for (QName extraPort : output.keySet())
			throw new IllegalArgumentException("no value expected on output port '" + extraPort + "'");
		return map.build();
	};

	@FunctionalInterface
	public interface InputType {

		public Optional<InputValue<?>> validate(Optional<InputValue<?>> input);

		public static InputType optional(Function<InputValue<?>,InputValue<?>> type) {
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					if (!input.isPresent())
						return input;
					else
						return Optional.of(type.apply(input.get()));
				}
			};
		}

		public static InputType optional(InputType type) {
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					if (!input.isPresent())
						return input;
					else
						return type.validate(input);
				}
			};
		}

		public static InputType mandatory(Function<InputValue<?>,InputValue<?>> type) {
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					return Optional.of(type.apply(input.get()));
				}
			};
		}

		public static InputType mandatory(InputType type) {
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					return type.validate(Optional.of(input.get()));
				}
			};
		}

		public default InputType and(InputType after) {
			InputType before = this;
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					return after.validate(before.validate(input));
				}
			};
		}

		public default InputType andIfPresent(Function<InputValue<?>,InputValue<?>> type) {
			InputType before = this;
			return new InputType() {
				public Optional<InputValue<?>> validate(Optional<InputValue<?>> input) {
					input = before.validate(input);
					if (!input.isPresent())
						return input;
					else
						return Optional.of(type.apply(input.get()));
				}
			};
		}

		public static final InputType OPTIONAL_ITEM_SEQUENCE = i -> i;

		public static final InputType MANDATORY_ITEM_SEQUENCE = mandatory(OPTIONAL_ITEM_SEQUENCE);

		public static final InputType OPTIONAL_NODE_SEQUENCE
			= OPTIONAL_ITEM_SEQUENCE.andIfPresent(
				i -> {
					if (i instanceof XMLInputValue) return i;
					else throw new IllegalArgumentException("value is not XML"); });

		public static final InputType MANDATORY_NODE_SEQUENCE = mandatory(OPTIONAL_NODE_SEQUENCE);

		public static final InputType OPTIONAL_NODE_SINGLE
			= OPTIONAL_NODE_SEQUENCE.andIfPresent(
				i -> ((XMLInputValue<?>)i).ensureSingleItem());

		public static final InputType MANDATORY_NODE_SINGLE = mandatory(OPTIONAL_NODE_SINGLE);
	}

	@FunctionalInterface
	public interface OutputType {

		public OutputValue<?> validate(OutputValue<?> output);

		public static final OutputType ITEM_SEQUENCE = i -> i;

		public static final OutputType NODE_SEQUENCE = i -> {
			if (i instanceof XMLOutputValue) return i;
			else throw new IllegalArgumentException("value is not XML"); };
	}
}
