package org.daisy.dotify.common.xml;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Provides a transformer environment.
 *
 * @param <T> the type of throwable that this environment prefers
 * @author Joel Håkansson
 */
public final class TransformerEnvironment<T extends Throwable> {
    private final TransformerFactory factory;
    private final Function<? super Throwable, T> throwableProcessor;
    private final Map<String, Object> params;

    /**
     * Provides a builder for a {@link TransformerEnvironment}.
     */
    public static class Builder {
        private TransformerFactory factory = null;
        private Map<String, Object> params = Collections.emptyMap();

        private Builder() {
        }

        /**
         * Sets the transformer factory for this environment.
         *
         * @param factory the transformer factory
         * @return this builder
         */
        public Builder transformerFactory(TransformerFactory factory) {
            this.factory = factory;
            return this;
        }

        /**
         * Sets the xslt parameters for this environment.
         *
         * @param params the xslt parameters
         * @return this builder
         */
        public Builder parameters(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        /**
         * Builds a new environment using the current configuration of this builder.
         *
         * @return a new {@link TransformerEnvironment}
         */
        public TransformerEnvironment<XMLToolsException> build() {
            return build(th -> {
                if (th instanceof XMLToolsException) {
                    return (XMLToolsException) th;
                } else {
                    return new XMLToolsException(th);
                }
            });
        }

        /**
         * Builds a new environment with the specified type of throwable that can be thrown when
         * using the environment.
         *
         * @param <Y>                the type of throwable to throw in case of an error
         * @param throwableProcessor a function that processes a throwable and returns another throwable
         * @return a new {@link TransformerEnvironment}
         */
        public <Y extends Throwable> TransformerEnvironment<Y> build(
            Function<? super Throwable, Y> throwableProcessor
        ) {
            return new TransformerEnvironment<Y>(this, throwableProcessor);
        }
    }

    /**
     * Creates a new {@link TransformerEnvironment.Builder}.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private TransformerEnvironment(Builder builder, Function<? super Throwable, T> throwableProcessor) {
        this.throwableProcessor = throwableProcessor;
        this.params = builder.params;
        this.factory = Optional.ofNullable(builder.factory).orElse(TransformerFactory.newInstance());
    }

    /**
     * Processes the supplied throwable and returns another throwable.
     *
     * @param cause the throwable
     * @return another throwable
     */
    T toThrowable(Throwable cause) {
        return throwableProcessor.apply(cause);
    }

    Transformer newTransformer(Source xslt) throws T {
        try {
            return factory.newTransformer(xslt);
        } catch (TransformerConfigurationException e) {
            throw toThrowable(e);
        }
    }

    Map<String, Object> getParameters() {
        return params;
    }

    Source asSource(Object source) throws T {
        try {
            return TransformerTools.toSource(source);
        } catch (XMLToolsException e) {
            throw toThrowable(e);
        }
    }

    Result asResult(Object result) throws T {
        try {
            return TransformerTools.toResult(result);
        } catch (XMLToolsException e) {
            throw toThrowable(e);
        }
    }

}
