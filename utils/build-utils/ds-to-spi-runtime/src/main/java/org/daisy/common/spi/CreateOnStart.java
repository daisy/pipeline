package org.daisy.common.spi;

/**
 * Tag interface for indicating that a component is to be created immediately
 * when the program starts.
 *
 * This can be accomplished by making the component available through
 * META-INF/services and executing the following code in the program's main
 * method:
 *
 * <pre>
 * {@code
 * for (CreateOnStart c : ServiceLoader.load(CreateOnStart.class));
 * }
 * </pre>
 */
public interface CreateOnStart {}
