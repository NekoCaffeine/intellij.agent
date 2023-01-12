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

public class IconOverride implements ClassFileTransformer { // The logo of IntelliJ IDEA 2021.1 is the ugliest work in the world, which is really unacceptable
    
    @Override
    public byte[] transform(final ClassLoader loader, final String name, final Class<?> target, final ProtectionDomain domain, final byte data[]) throws IllegalClassFormatException {
        if ("com/intellij/ui/AppUIUtil".equals(name)) {
            System.err.println("Transform -> com.intellij.ui.AppUIUtil");
            final ClassWriter writer = { 0 };
            final ClassReader reader = { data };
            reader.accept(new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                    if ("loadApplicationIcon".equals(name) && "(Ljava/lang/String;Lcom/intellij/ui/scale/ScaleContext;I)Ljavax/swing/Icon;".equals(descriptor)) {
                        System.err.println("Transform -> com.intellij.ui.AppUIUtil#loadApplicationIcon");
                        return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            @Override
                            public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                                if (name.equals("findIcon"))
                                    visitMethodInsn(INVOKESTATIC, "intellij/agent/ImageOverride", "icon", "(Ljavax/swing/Icon;)Ljavax/swing/Icon;", false);
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
        if (System.getProperty("intellij.iconOverride") != null)
            instrumentation.addTransformer(new IconOverride(), true);
    }
    
}
