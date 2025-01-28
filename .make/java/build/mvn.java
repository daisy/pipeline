package build;

import java.io.*;
import java.util.function.*;

import static lib.util.*;

public class mvn {
	private mvn() {}

	private static final String MY_DIR = System.getenv("MY_DIR");

	public static VarArgsFunction<String,Void> releaseModulesInDir(String dir) {
		return modules -> {
			try {
				exec("bash", "-c", String.format("%s/mvn-release.sh %s %s", MY_DIR,
				                                                            dir,
				                                                            String.join(" ", modules)));
				return null;
			} catch (IOException|InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@FunctionalInterface
	public interface VarArgsFunction<T, U> extends Function<T[], U> {
		@Override
		U apply(T... args);
	}
}
