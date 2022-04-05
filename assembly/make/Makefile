all : eval_java.class java/lib/util.class eval-java.exe

eval_java.class java/lib/util.class : %.class : %.java
	javac $<

eval-java.exe : eval-java.c
	i686-w64-mingw32-gcc $< -o $@

clean :
	rm -f eval_java.class java/lib/util.class java/lib/util\$$OS.class eval-java.exe
