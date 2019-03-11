package org.daisy.pipeline.junit;

import java.lang.reflect.Field;
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
	
	public OSGiLessRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}
	
	// Overriding "withBefores" and not the more logical "methodInvoker" because with
	// "methodInvoker" it is not possible to inject the services *before* "@Before"
	// methods are invoked.
	@Override
	protected Statement withBefores(FrameworkMethod method, final Object test, Statement statement) {
		final Statement invoker = super.withBefores(method, test, statement);
		final List<FrameworkMethod> config = getTestClass().getAnnotatedMethods(OSGiLessConfiguration.class);
		final List<FrameworkField> injectFields = getTestClass().getAnnotatedFields(Inject.class);
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				for (FrameworkMethod c : config)
					c.invokeExplosively(test);
				for (FrameworkField f : injectFields) {
					Field field = f.getField();
					Class<?> type = field.getType();
					Object o; {
						try {
							for (CreateOnStart c : ServiceLoader.load(CreateOnStart.class));
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
				invoker.evaluate();
			}
		};
	}
}
