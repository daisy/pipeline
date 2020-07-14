package org.daisy.pipeline.junit;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class OSGiLessRunner extends BlockJUnit4ClassRunner {

	/* class loader for creating and injecting services unique to this runner */
	private final ClassLoader classLoader;
	private boolean configurationDone = false;

	public OSGiLessRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
		classLoader = new URLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
	}
	
	// Overriding "withBefores" and not the more logical "methodInvoker" because with
	// "methodInvoker" it is not possible to inject the services *before* "@Before" methods are
	// invoked. Because of caching, the same services are injected for every test method in the
	// class.
	@Override
	protected Statement withBefores(FrameworkMethod method, final Object test, Statement statement) {
		final Statement invoker = super.withBefores(method, test, statement);
		final List<FrameworkField> injectFields = getTestClass().getAnnotatedFields(Inject.class);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
				try {
					if (!configurationDone) {
						for (FrameworkMethod c : getTestClass().getAnnotatedMethods(OSGiLessConfiguration.class))
							c.invokeExplosively(test);
						configurationDone = true;
					}
					Thread.currentThread().setContextClassLoader(classLoader);
					for (CreateOnStart c : ServiceLoader.load(CreateOnStart.class));
					for (FrameworkField f : injectFields) {
						Field field = f.getField();
						Class<?> type = field.getType();
						Object o; {
							try {
								o = ServiceLoader.load(type).iterator().next();
							} catch (NoSuchElementException e) {
								throw new RuntimeException("Failed to inject a " + type.getCanonicalName());
							}
						}
						try {
							field.set(test, o);
						} catch (IllegalAccessException e) {
							throw new RuntimeException("Failed to inject a " + type.getCanonicalName() + "; "
							                           + field.getName() + " field is not visible.");
						}
					}
				} finally {
					Thread.currentThread().setContextClassLoader(savedContextClassLoader);
				}
				invoker.evaluate();
			}
		};
	}

	@Override
	protected void collectInitializationErrors(List<Throwable> errors) {
		super.collectInitializationErrors(errors);
		validatePublicVoidNoArgMethods(OSGiLessConfiguration.class, false, errors);
	}
}
