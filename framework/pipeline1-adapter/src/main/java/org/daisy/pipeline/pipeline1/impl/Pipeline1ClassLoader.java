package org.daisy.pipeline.pipeline1.impl;

import org.daisy.common.java.JarIsolatedClassLoader;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Pipeline1ClassLoader extends JarIsolatedClassLoader {

	public Pipeline1ClassLoader() {
		super();
		addCurrentJar();
		addJarRecursively("pipeline.jar");
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// to make brailleUtils work with Java > 8 (see https://bugs.openjdk.org/browse/JDK-8068749)
		if ("javax.imageio.spi.ServiceRegistry".equals(name)) {
			Class<?> loadedClass = findLoadedClass(name);
			if (loadedClass == null) {
				try {
					ClassReader cr = new ClassReader(getResourceAsStream(name.replace('.', '/') + ".class"));
					ClassWriter cw = new ClassWriter(cr, 0);
					cr.accept(
						new ClassVisitor(Opcodes.ASM7, cw) {
							@Override
							public MethodVisitor visitMethod(int access,
							                                 String name,
							                                 String desc,
							                                 String signature,
							                                 String[] exceptions) {
								MethodVisitor mw = cw.visitMethod(access, name, desc, signature, exceptions);
								// disable the checkClassAllowed() method
								if ("checkClassAllowed".equals(name)) {
									mw.visitInsn(Opcodes.RETURN);
									mw.visitMaxs(1, 1);
									mw.visitEnd();
									return null;
								}
								return mw;
							}
						}, 0);
					byte[] code = cw.toByteArray();
					loadedClass = defineClass(name, code, 0, code.length);
					if (resolve)
						resolveClass(loadedClass);
					return loadedClass;
				} catch (Throwable e) {
					throw new ClassNotFoundException("Error happened while modifying class " + name, e);
				}
			}
		}
		return super.loadClass(name, resolve);
	}
}
