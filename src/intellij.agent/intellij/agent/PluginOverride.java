package intellij.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class PluginOverride implements ClassFileTransformer { // Disable forced loading of plug-ins
    
    @Override
    public byte[] transform(final ClassLoader loader, final String name, final Class<?> target, final ProtectionDomain domain, final byte data[]) throws IllegalClassFormatException {
        if ("com/intellij/ide/plugins/IdeaPluginDescriptorImpl".equals(name)) {
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
        if ("com/intellij/ide/plugins/PluginManagerCore".equals(name)) {
            System.err.println("Transform -> com.intellij.ide.plugins.PluginManagerCore");
            final ClassWriter writer = { 0 };
            final ClassReader reader = { data };
            reader.accept(new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                    if ("scheduleDescriptorLoading".equals(name) && "()V".equals(descriptor)) {
                        return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            
                            @Override
                            public void visitInsn(final int opcode) {
                                if (opcode == POP) {
                                    System.err.println("Transform -> com.intellij.ide.plugins.PluginManagerCore#scheduleDescriptorLoading");
                                    super.visitMethodInsn(INVOKESTATIC, "intellij/agent/MahoInit", "awaitPluginDescriptorLoaded", "(Ljava/util/concurrent/CompletableFuture;)V", false);
                                } else
                                    super.visitInsn(opcode);
                            }
                            
                        };
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }, 0);
            return writer.toByteArray();
        }
        return null;
    }
    
    public static void init(final Instrumentation instrumentation) {
        if (System.getProperty("disable.intellij.pluginOverride") == null)
            instrumentation.addTransformer(new PluginOverride(), true);
    }
    
}
