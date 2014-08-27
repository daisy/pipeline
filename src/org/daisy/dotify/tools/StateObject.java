package org.daisy.dotify.tools;

/**
 * The StateObject is a convenience object that can be used
 * to avoid certain programming errors. By setting this object
 * when the state changes and asserting that it has the correct
 * state before engaging in state dependent activity, 
 * the programmer can get proper feedback whenever the object 
 * is in the wrong state for a particular request.
 * 
 * @author Joel Håkansson
 */
public class StateObject {
	/**
	 * Possible states for a StateObject
	 */
	public enum State {
		/**
		 * Indicates that the StateObject has not yet been opened
		 */
		UNOPENED,
		/**
		 * Indicates that the StateObject is open
		 */
		OPEN,
		/**
		 * Indicates that the StateObject has been open, but is now closed
		 */
		CLOSED
	}
	private State state;
	private String type;
	
	/**
	 * Create a new StateObject with the specified type.
	 * @param type the type name of this StateObject, e.g. a class name
	 */
	public StateObject(String type) {
		this.type = type;
		state = State.UNOPENED;
	}
	
	/**
	 * Create a new StateObject with the default type, which is "Object"
	 */
	public StateObject() {
		this("Object");
	}
	
	/**
	 * Open the StateObject
	 */
	public void open() {
		state = State.OPEN;
	}
	
	/**
	 * Close the StateObject
	 */
	public void close() {
		state = State.CLOSED;
	}
	
	/**
	 * Check if the StateObject has been closed
	 * @return returns true if the object is closed
	 */
	public boolean isClosed() {
		return state == State.CLOSED;
	}
	
	/**
	 * Check if the StateObject has been opened
	 * @return returns true if the object is opened
	 */
	public boolean isOpen() {
		return state == State.OPEN;
	}

	/**
	 * Assert that the object is open
	 * @throws throws IllegalStateException if the object is not open
	 */
	public void assertOpen() throws IllegalStateException {
		if (state != State.OPEN) {
			throw new IllegalStateException(type + " is not open.");
		}
	}
	
	/**
	 * Assert that the object is not open
	 * @throws throws IllegalStateException if the object is open
	 */
	public void assertNotOpen() throws IllegalStateException {
		if (state == State.OPEN) {
			throw new IllegalStateException(type + " is already open.");
		}
	}

	/**
	 * Assert that the object has been closed
	 * @throws throws IllegalStateException if the object is not closed
	 */
	public void assertClosed() throws IllegalStateException {
		if (state != State.CLOSED) {
			throw new IllegalStateException(type + " is not closed.");
		}
	}
	
	/**
	 * Assert that the object has never been opened
	 * @throws throws IllegalStateException if the object is not unopened
	 */
	public void assertUnopened() throws IllegalStateException {
		if (state != State.UNOPENED) {
			throw new IllegalStateException(type + " has already been opened.");
		}
	}
}