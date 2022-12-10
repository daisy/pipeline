all : eval_java.class java/lib/util.class eval-java.exe eval-java

# Java >= 8 is required to compile lib.util
eval_java.class java/lib/util.class : %.class : %.java
	javac -source 1.8 -target 1.8 -bootclasspath rt8.jar -extdirs '' $<

eval-java.exe : eval-java.c
	i686-w64-mingw32-gcc $< -o $@

clean :
	rm -f eval_java.class java/lib/util.class java/lib/util\$$OS.class eval-java.exe eval-java
