package org.daisy.common.transform;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.SettableFuture;

import org.w3c.dom.Node;

/**
 * Buffer to connect two XML transformer ports, one that pushes items and one that pulls items.
 *
 * The buffer is both a {@link XMLInputValue} and a {@link XMLOutputValue}. It is thread-safe in the
 * sense that one thread can push and another thread can pull. The {@code hasNext} and {@code next}
 * methods are blocking if a next item is not immediately available but the buffer is not closed
 * yet.
 */
public class Buffer<I,O> {

	private XMLInputValue<I> input = null;
	private XMLOutputValue<O> output = null;
	private ItemBuffer<Node> nodeBuffer = null;

	/**
	 * Should be called by the input if all items have been pushed.
	 */
	public void done() {
		if (nodeBuffer == null)
			nodeBuffer = new ItemBuffer<>();
		nodeBuffer.done();
	}

	/**
	 * Can be called by the input if no more items can be pushed due to an error.
	 */
	public void error(TransformerException e) {
		if (nodeBuffer == null)
			nodeBuffer = new ItemBuffer<>();
		nodeBuffer.error(e);
	}

	public XMLInputValue<I> asInput() {
		if (input == null) {
			if (nodeBuffer == null)
				nodeBuffer = new ItemBuffer<>();
			input = new XMLInputValue<I>(nodeBuffer);
		}
		return input;
	}

	public XMLOutputValue<O> asOutput() {
		if (output == null) {
			if (nodeBuffer == null)
				nodeBuffer = new ItemBuffer<>();
			output = new XMLOutputValue<O>(nodeBuffer);
		}
		return output;
	}

	protected static class ItemBuffer<I> implements Iterator<I>, Consumer<I> {

		private final Queue<Future<Optional<I>>> queue;
		private SettableFuture<Optional<I>> lastItem;

		public ItemBuffer() {
			queue = new ConcurrentLinkedQueue<>();
			lastItem = SettableFuture.create();
			queue.add(lastItem);
		}

		public void accept(I i) {
			SettableFuture<Optional<I>> nextItem = SettableFuture.create();
			queue.add(nextItem);
			lastItem.set(Optional.of(i));
			lastItem = nextItem;
		}

		public void done() {
			lastItem.set(Optional.absent());
		}

		/**
		 * Can be called by the input if no more items can be pushed due to an error.
		 */
		public void error(TransformerException e) {
			lastItem.setException(e);
		}

		public boolean hasNext() throws TransformerException {
			if (queue.isEmpty())
				return false;
			else
				try {
					return queue.peek().get().isPresent();
				} catch (ExecutionException e) {
					// if Buffer.setException() was previously called
					try {
						throw e.getCause();
					} catch (TransformerException ee) {
						throw ee;
					} catch (Throwable ee) {
						throw new RuntimeException(ee); // should not happen
					}
				} catch (InterruptedException e) {
					// if "pulling" thread was interrupted during call
					throw new RuntimeException(e);
				}
		}

		public I next() throws TransformerException {
			if (queue.isEmpty())
				throw new NoSuchElementException();
			else
				try {
					return queue.remove().get().get();
				} catch (ExecutionException e) {
					// if Buffer.setException() was previously called
					try {
						throw e.getCause();
					} catch (TransformerException ee) {
						throw ee;
					} catch (Throwable ee) {
						throw new RuntimeException(ee); // should not happen
					}
				} catch (InterruptedException e) {
					// if "pulling" thread was interrupted during the call
					throw new RuntimeException(e);
				}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
