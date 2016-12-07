package org.daisy.dotify.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EmptyStackException;

/**
 * <p>Provides a simple unsynchronized stack based on ArrayList instead of Vector.
 * This class implements the exact same methods as {@link java.util.Stack} and is
 * therefore easily interchangeable with the former.
 * However, {@link java.util.ArrayDeque} should for most purposes
 * be used instead of this class.</p>
 * 
 * @author Joel Håkansson
 * @param <E> the type of elements in the stack
 * @see java.util.Deque
 */
public class ArrayStack<E> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7549882261530382669L;

    /**
     * Constructs an empty stack with an initial capacity of ten.
     */
	public ArrayStack() {
		super();
	}

    /**
     * Constructs a stack containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this stack
     * @throws NullPointerException if the specified collection is null
     */
	public ArrayStack(Collection<? extends E> c) {
		super(c);
	}

    /**
     * Constructs an empty stack with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the stack
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
	public ArrayStack(int initialCapacity) {
		super(initialCapacity);
	}
	
	
	   /**
     * Pushes an item onto the top of this stack. This has exactly 
     * the same effect as:
     * <blockquote><pre>add(item)</pre></blockquote>
     *
     * @param   item   the item to be pushed onto this stack.
     * @return  the <code>item</code> argument.
     * @see     java.util.ArrayList#add
     */
    public E push(E item) {
    	add(item);
    	return item;
    }

    /**
     * Removes the object at the top of this stack and returns that 
     * object as the value of this function. 
     *
     * @return     The object at the top of this stack (the last item 
     *             of the <tt>ArrayList</tt> object).
     * @exception  EmptyStackException  if this stack is empty.
     */
    public E pop() {
    	E	obj;
    	int	len = size();

    	if (len == 0) {
    		throw new EmptyStackException();
    	}
    	obj = get(len -1);
    	remove(len - 1);

    	return obj;
    }

    /**
     * Looks at the object at the top of this stack without removing it 
     * from the stack. 
     *
     * @return     the object at the top of this stack (the last item 
     *             of the <tt>ArrayList</tt> object). 
     * @exception  EmptyStackException  if this stack is empty.
     */
    public E peek() {
    	int	len = size();
    	if (len == 0) {
    		throw new EmptyStackException();
    	}
    	return get(len - 1);
    }

    /**
     * Tests if this stack is empty.
     *
     * @return  <code>true</code> if and only if this stack contains 
     *          no items; <code>false</code> otherwise.
     */
    public boolean empty() {
    	return size() == 0;
    }

    /**
     * Returns the 1-based position where an object is on this stack. 
     * If the object <tt>o</tt> occurs as an item in this stack, this 
     * method returns the distance from the top of the stack of the 
     * occurrence nearest the top of the stack; the topmost item on the 
     * stack is considered to be at distance <tt>1</tt>. The <tt>equals</tt> 
     * method is used to compare <tt>o</tt> to the 
     * items in this stack.
     *
     * @param   o   the desired object.
     * @return  the 1-based position from the top of the stack where 
     *          the object is located; the return value <code>-1</code>
     *          indicates that the object is not on the stack.
     */
    public int search(Object o) {
		int i = lastIndexOf(o);
	
		if (i >= 0) {
		    return size() - i;
		}
		return -1;
    }
	

}
