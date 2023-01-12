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

public class LogoOverride implements ClassFileTransformer { // The logo of IntelliJ IDEA 2021.1 is the ugliest work in the world, which is really unacceptable
    
    @Override
    public byte[] transform(final ClassLoader loader, final String name, final Class<?> target, final ProtectionDomain domain, final byte data[]) throws IllegalClassFormatException {
        if ("com/intellij/ui/Splash".equals(name)) {
            System.err.println("Transform -> com.intellij.ui.Splash");
            final ClassWriter writer = { 0 };
            final ClassReader reader = { data };
            reader.accept(new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                    if ("paint".equals(name) && "(Ljava/awt/Graphics;)V".equals(descriptor)) {
                        System.err.println("Transform -> com.intellij.ui.Splash#paint");
                        final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                        visitor.visitCode();
                        visitor.visitVarInsn(ALOAD, 0);
                        visitor.visitVarInsn(ALOAD, 1);
                        visitor.visitMethodInsn(INVOKESTATIC, "intellij/agent/ImageOverride", "paint", "(Ljava/awt/Window;Ljava/awt/Graphics;)V", false);
                        visitor.visitInsn(RETURN);
                        visitor.visitMaxs(2, 2);
                        visitor.visitEnd();
                        return null;
                    } else if ("showProgress".equals(name) && "(D)V".equals(descriptor)) {
                        System.err.println("Transform -> com.intellij.ui.Splash#showProgress");
                        final MethodVisitor visitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                        visitor.visitCode();
                        visitor.visitVarInsn(ALOAD, 0);
                        visitor.visitVarInsn(DLOAD, 1);
                        visitor.visitMethodInsn(INVOKESTATIC, "intellij/agent/ImageOverride", "showProgress", "(Ljava/awt/Window;D)V", false);
                        visitor.visitInsn(RETURN);
                        visitor.visitMaxs(3, 3);
                        visitor.visitEnd();
                        return null;
                    }
                    return super.visitMethod(access, name, descriptor, signature, exceptions);
                }
            }, 0);
            return writer.toByteArray();
        }
        return null;
    }
    
    public static void init(final Instrumentation instrumentation) {
        if (System.getProperty("intellij.logoOverride") != null)
            instrumentation.addTransformer(new LogoOverride(), true);
    }
    
}
