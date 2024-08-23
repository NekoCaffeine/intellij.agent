package intellij.agent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.AccessibleObject;
import java.security.ProtectionDomain;
import java.util.List;

import jdk.internal.misc.Unsafe;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import amadeus.maho.lang.SneakyThrows;

import static org.objectweb.asm.Opcodes.*;

public class Agent implements ClassFileTransformer {
    
    public static Instrumentation instrumentation;
    
    public static void premain(final String agentArgs, final Instrumentation instrumentation) throws UnmodifiableClassException, IOException {
        if (!instrumentation.isRetransformClassesSupported())
            throw new UnsupportedOperationException("Retransform Class");
        System.setProperty("amadeus.maho.instrumentation.provider", Agent.class.getName());
        System.setProperty("amadeus.maho.skip.acp", "true");
        Agent.instrumentation = instrumentation;
        AccessibleObject.class.getName();
        instrumentation.addTransformer(AllClassesPublic.instance);
        instrumentation.addTransformer(new Agent(), true);
        instrumentation.retransformClasses(AccessibleObject.class);
        ModuleHelper.openAllBootModule();
        LogoOverride.init(instrumentation);
        IconOverride.init(instrumentation);
        PluginOverride.init(instrumentation);
        shareClass(ImageOverride.class);
    }
    
    public static void shareClass(final Class<?> clazz) throws IOException {
        final byte bytecode[] = getBytesFromClass(clazz);
        Unsafe.getUnsafe().defineClass(null, bytecode, 0, bytecode.length, ClassLoader.getPlatformClassLoader(), null);
        for (final Class<?> inner : clazz.getDeclaredClasses())
            shareClass(inner);
    }
    
    private static String path(final Class<?> clazz) = clazz.getName().replace('.', '/') + ".class";
    
    private static byte[] getBytesFromClass(final Class<?> clazz) throws IOException {
        try (final var input = clazz.getClassLoader().getResourceAsStream(path(clazz))) {
            final ByteArrayOutputStream buffer = { };
            int nRead;
            final byte data[] = new byte[1 << 12];
            while ((nRead = input.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, nRead);
            buffer.flush();
            return buffer.toByteArray();
        }
    }
    
    @Override
    @SneakyThrows
    public byte[] transform(final ClassLoader loader, final String name, final Class<?> target, final ProtectionDomain domain, final byte data[]) throws IllegalClassFormatException {
        if (name == null)
            return null;
        if (name.startsWith("com/intellij/")) {
            final InputStream resource = Agent.class.getResourceAsStream("/redirect/" + name + ".class");
            if (resource != null)
                try (resource) {
                    System.err.println("Replace -> " + name.replace('/', '.'));
                    return resource.readAllBytes();
                }
        }
        try {
            switch (name) {
                case "com/intellij/ide/plugins/PluginManagerCore"               -> {
                    System.err.println("Transform -> com.intellij.ide.plugins.PluginManagerCore");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                            if ("checkBuildNumberCompatibility".equals(name) && (
                                    "(Lcom/intellij/ide/plugins/IdeaPluginDescriptor;Lcom/intellij/openapi/util/BuildNumber;Ljava/lang/Runnable;)Lcom/intellij/ide/plugins/PluginLoadingError;".equals(descriptor) ||
                                    "(Lcom/intellij/ide/plugins/IdeaPluginDescriptor;Lcom/intellij/openapi/util/BuildNumber;)Lcom/intellij/ide/plugins/PluginLoadingError;".equals(descriptor))) {
                                System.err.println("Transform -> com.intellij.ide.plugins.PluginManagerCore#checkBuildNumberCompatibility");
                                final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(ACONST_NULL);
                                visitor.visitInsn(ARETURN);
                                visitor.visitMaxs(1, 3);
                                visitor.visitEnd();
                                return null;
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/ide/plugins/IdeaPluginDescriptorImpl"        -> {
                    System.err.println("Transform -> com.intellij.ide.plugins.IdeaPluginDescriptorImpl");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                            if ("isImplementationDetail".equals(name) && "()Z".equals(descriptor)) {
                                System.err.println("Transform -> com.intellij.ide.plugins.IdeaPluginDescriptorImpl#isImplementationDetail");
                                final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(ICONST_0);
                                visitor.visitInsn(IRETURN);
                                visitor.visitMaxs(1, 1);
                                visitor.visitEnd();
                                return null;
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "java/lang/reflect/AccessibleObject"                       -> {
                    System.err.println("Transform -> java.lang.reflect.AccessibleObject");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                            if ("checkCanSetAccessible".equals(name) && "(Ljava/lang/Class;Ljava/lang/Class;Z)Z".equals(descriptor)) {
                                System.err.println("Transform -> java.lang.reflect.AccessibleObject#checkCanSetAccessible");
                                final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(ICONST_1);
                                visitor.visitInsn(IRETURN);
                                visitor.visitMaxs(2, 4);
                                visitor.visitEnd();
                                return null;
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "jdk/internal/reflect/Reflection"                          -> {
                    System.err.println("Transform -> jdk.internal.reflect.Reflection");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                            if ("verifyMemberAccess".equals(name) && "(Ljava/lang/Class;Ljava/lang/Class;Ljava/lang/Class;I)Z".equals(descriptor)) {
                                System.err.println("Transform -> jdk.internal.reflect.Reflection#verifyMemberAccess");
                                final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(ICONST_1);
                                visitor.visitInsn(IRETURN);
                                visitor.visitMaxs(1, 4);
                                visitor.visitEnd();
                                return null;
                            } else if ("verifyModuleAccess".equals(name) && "(Ljava/lang/Module;Ljava/lang/Class;)Z".equals(descriptor)) {
                                System.err.println("Transform -> jdk.internal.reflect.Reflection#verifyModuleAccess");
                                final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(ICONST_1);
                                visitor.visitInsn(IRETURN);
                                visitor.visitMaxs(1, 2);
                                visitor.visitEnd();
                                return null;
                            }
                            return super.visitMethod(access, name, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/openapi/actionSystem/impl/ActionManagerImpl" -> {
                    System.err.println("Transform -> com.intellij.openapi.actionSystem.impl.ActionManagerImpl");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String methodName, final String descriptor, final String signature, final String exceptions[]) {
                            if ("processActionElement".equals(methodName) || "processGroupElement".equals(methodName)) {
                                return new MethodVisitor(ASM9, super.visitMethod(access, methodName, descriptor, signature, exceptions)) {
                                    
                                    @Override
                                    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                                        if ("isInternal".equals(name)) {
                                            System.err.println("Transform -> com.intellij.openapi.actionSystem.impl.ActionManagerImpl#" + methodName);
                                            visitInsn(POP);
                                            visitInsn(ICONST_1);
                                        } else
                                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                    }
                                    
                                };
                            }
                            return super.visitMethod(access, methodName, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/psi/impl/source/tree/CompositeElement"       -> {
                    System.err.println("Transform -> com.intellij.psi.impl.source.tree.CompositeElement");
                    final List<String> volatileNames = List.of("firstChild", "lastChild");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value)
                                = super.visitField(volatileNames.contains(name) ? access | ACC_VOLATILE : access, name, descriptor, signature, value);
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/psi/impl/source/tree/TreeElement"            -> {
                    System.err.println("Transform -> com.intellij.psi.impl.source.tree.TreeElement");
                    final List<String> volatileNames = List.of("myNextSibling", "myPrevSibling", "myParent");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public FieldVisitor visitField(final int access, final String name, final String descriptor, final String signature, final Object value)
                                = super.visitField(volatileNames.contains(name) ? access | ACC_VOLATILE : access, name, descriptor, signature, value);
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/openapi/editor/impl/FontFamilyServiceImpl"            -> {
                    System.err.println("Transform -> com.intellij.openapi.editor.impl.FontFamilyServiceImpl");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    final List<String> names = List.of("<init>", "<clinit>", "getFont2dMethod", "getDescriptorByFontImpl");
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String methodName, final String descriptor, final String signature, final String exceptions[]) {
                            if (names.contains(methodName)) {
                                return new MethodVisitor(ASM9, super.visitMethod(access, methodName, descriptor, signature, exceptions)) {
                                    
                                    @Override
                                    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                                        if ("warn".equals(name)) {
                                            System.err.println("Transform -> com.intellij.openapi.editor.impl.FontFamilyServiceImpl#" + methodName);
                                            visitInsn(POP);
                                            visitInsn(POP);
                                        } else
                                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                    }
                                    
                                };
                            }
                            return super.visitMethod(access, methodName, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
                case "com/intellij/openapi/actionSystem/impl/ActionManagerImplKt"            -> {
                    System.err.println("Transform -> com.intellij.openapi.actionSystem.impl.ActionManagerImplKt");
                    final ClassWriter writer = { 0 };
                    final ClassReader reader = { data };
                    reader.accept(new ClassVisitor(ASM9, writer) {
                        @Override
                        public MethodVisitor visitMethod(final int access, final String methodName, final String descriptor, final String signature, final String exceptions[]) {
                            if ("reportKeymapNotFoundWarning".equals(methodName)) {
                                System.err.println("Transform -> com.intellij.openapi.actionSystem.impl.ActionManagerImplKt#" + methodName);
                                final MethodVisitor visitor = super.visitMethod(access, methodName, descriptor, signature, exceptions);
                                visitor.visitCode();
                                visitor.visitInsn(RETURN);
                                visitor.visitMaxs(0, 2);
                                visitor.visitEnd();
                                return new MethodVisitor(ASM9) { };
                            }
                            return super.visitMethod(access, methodName, descriptor, signature, exceptions);
                        }
                    }, 0);
                    return writer.toByteArray();
                }
            }
        } catch (Throwable t) { t.printStackTrace(); }
        return null;
    }
    
}
