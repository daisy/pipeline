package org.daisy.pipeline.script;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.pipeline.script.impl.StaxXProcScriptParser;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of the scripts defined by the loaded modules.
 */
@Component(
	name = "script-registry",
	service = { ScriptRegistry.class }
)
public class ScriptRegistry {

	private static final Logger logger = LoggerFactory.getLogger(ScriptRegistry.class);

	private final ConcurrentMap<String,ScriptService<?>> scripts = new ConcurrentHashMap<>();
	private final AtomicReference<Future<Boolean>> initialized = new AtomicReference<>();

	public ScriptRegistry() {
	}

	private synchronized Future<Boolean> init() {
		return initialized.updateAndGet(
			prev -> {
				if (prev != null)
					return prev;
				else {
					// this code is called only once
					CompletableFuture<Boolean> initialized = new CompletableFuture<>();
					for (ScriptService<?> script : OSGiHelper.inOSGiContext() ? OSGiHelper.getScripts()
						                                                      : SPIHelper.getScripts())
						registerScript(script);
					List<ScriptServiceProvider> nextScriptProviders
						= Lists.newArrayList(OSGiHelper.inOSGiContext() ? OSGiHelper.getScriptProviders()
						                                                : SPIHelper.getScriptProviders());
					// handle the case where ScriptRegistry.getScript() is called from
					// ScriptServiceProvider.getScripts() or from the returned Iterable/Iterator, by running
					// the ScriptServiceProvider.getScripts() asynchronously
					CompletionService<Boolean> threadPool
						= new ExecutorCompletionService(Executors.newFixedThreadPool(nextScriptProviders.size()));
					AtomicInteger remaining = new AtomicInteger(nextScriptProviders.size());
					for (ScriptServiceProvider provider : nextScriptProviders)
						threadPool.submit(
							() -> {
								if (initialized.isCancelled())
									return;
								logger.debug("Registering scripts from {}", provider);
								for (ScriptService<?> script : provider.getScripts())
									if (initialized.isCancelled())
										return;
									else
										registerScript(script);
								if (remaining.decrementAndGet() == 0)
									initialized.complete(true);
								return;
							},
							true
						);
					return initialized;
				}
			}
		);
	}

	/**
	 * Get all the scripts.
	 */
	public Iterable<ScriptService<?>> getScripts() {
		Future<Boolean> initialized = init();
		try {
			initialized.get(10, SECONDS); // always returns true
		} catch (InterruptedException e) {
			// not likely to happen, but if for some reason next() was
			// interrupted while waiting for ScriptServiceProvider.getScripts()
			// to finish, restore the interrupt status and abort the iteration
			Thread.currentThread().interrupt();
			initialized.cancel(false);
		} catch (TimeoutException e) {
			// either script providers are waiting for each other (deadlock), or take
			// too long to provide the scripts
			initialized.cancel(false);
		} catch (CancellationException e) {
		} catch (ExecutionException e) {
			throw new RuntimeException(e); // should not happen
		}
		return ImmutableList.copyOf(scripts.values());
	}

	/**
	 * Get the script looking it up by its ID
	 */
	// note that this method may be called from ScriptServiceProvider.getScripts(), from one of
	// the threads created in init()
	public ScriptService<?> getScript(String name) {
		Future<Boolean> initialized = init();
		do {
			ScriptService<?> script = scripts.get(name);
			if (script != null)
				return script;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				initialized.cancel(false);
				break;
			}
		} while (!(initialized.isDone() || initialized.isCancelled()));
		// try one last time just in case registerScript() was called from another thread at a
		// bad time
		ScriptService<?> script = scripts.get(name);
		if (script == null)
			logger.warn("Script {} does not exist", name);
		return script;
	}

	private void registerScript(ScriptService<?> script) {
		logger.debug("Registering script {}", script.getId());
		if (script instanceof XProcScriptService)
			((XProcScriptService)script).setParser(parser);
		scripts.put(script.getId(), script);
	}

	/**
	 * The parser to load {@link Script} objects from XProc files.
	 */
	private StaxXProcScriptParser parser;

	@Reference(
		name = "script-parser",
		unbind = "-",
		service = StaxXProcScriptParser.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setParser(StaxXProcScriptParser parser) {
		this.parser = parser;
	}

	// static nested class in order to delay class loading
	private static abstract class SPIHelper {
		static Iterable<ScriptService<?>> getScripts() {
			return Iterables.concat(
				(Iterable<ScriptService<?>>)(Iterable)ServiceLoader.load(ScriptService.class),
				// FIXME: the reason for this second iterator is that most XProc scripts currently
				// implement the XProcScriptService, but not ScriptService. In case there are
				// scripts that implement both, they will be registered (and created?) twice, but
				// will only end up in the map once.
				ServiceLoader.load(XProcScriptService.class)
			);
		}
		static Iterable<ScriptServiceProvider> getScriptProviders() {
			return ServiceLoader.load(ScriptServiceProvider.class);
		}
	}

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {

		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}

		static Iterable<ScriptService<?>> getScripts() {
			BundleContext bc = FrameworkUtil.getBundle(ScriptRegistry.class).getBundleContext();
			List<ScriptService<?>> scripts = new ArrayList<>();
			try {
				for (ServiceReference ref : bc.getServiceReferences(ScriptService.class.getName(), null))
					scripts.add((ScriptService<?>)bc.getService(ref));
			} catch (InvalidSyntaxException e) {
				throw new IllegalStateException(e); // should not happen
			}
			return scripts;
		}

		static Iterable<ScriptServiceProvider> getScriptProviders() {
			BundleContext bc = FrameworkUtil.getBundle(ScriptRegistry.class).getBundleContext();
			List<ScriptServiceProvider> providers = new ArrayList<>();
			try {
				for (ServiceReference ref : bc.getServiceReferences(ScriptServiceProvider.class.getName(), null))
					providers.add((ScriptServiceProvider)bc.getService(ref));
			} catch (InvalidSyntaxException e) {
				throw new IllegalStateException(e); // should not happen
			}
			return providers;
		}
	}
}
