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
        if ("com/intellij/ui/AppUIUtilKt".equals(name)) {
            System.err.println("Transform -> com.intellij.ui.AppUIUtilKt");
            final ClassWriter writer = { 0 };
            final ClassReader reader = { data };
            reader.accept(new ClassVisitor(ASM9, writer) {
                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String exceptions[]) {
                    if ("loadAppIconImage".equals(name) && "(Ljava/lang/String;Lcom/intellij/ui/scale/ScaleContext;I)Ljava/awt/Image;".equals(descriptor) ||
                        "loadApplicationIconImage".equals(name) && "(Ljava/lang/String;Lcom/intellij/ui/scale/ScaleContext;ILjava/lang/String;)Ljava/awt/Image;".equals(descriptor)) {
                        System.err.println("Transform -> com.intellij.ui.AppUIUtilKt#" + name);
                        return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            
                            @Override
                            public void visitInsn(final int opcode) {
                                if (opcode == ARETURN) {
                                    visitVarInsn(ILOAD, 2);
                                    visitMethodInsn(INVOKESTATIC, "intellij/agent/ImageOverride", "icon", "(Ljava/awt/Image;I)Ljava/awt/Image;", false);
                                }
                                super.visitInsn(opcode);
                            }
                            
                        };
                    }
                    if ("loadSmallApplicationIcon".equals(name) && "(Lcom/intellij/ui/scale/ScaleContext;IZ)Ljavax/swing/Icon;".equals(descriptor)) {
                        System.err.println("Transform -> com.intellij.ui.AppUIUtilKt#loadSmallApplicationIcon");
                        return new MethodVisitor(ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                            
                            @Override
                            public void visitInsn(final int opcode) {
                                if (opcode == ARETURN) {
                                    visitVarInsn(ILOAD, 1);
                                    visitMethodInsn(INVOKESTATIC, "intellij/agent/ImageOverride", "icon", "(Ljavax/swing/Icon;I)Ljavax/swing/Icon;", false);
                                }
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
        if (System.getProperty("intellij.iconOverride") != null)
            instrumentation.addTransformer(new IconOverride(), true);
    }
    
}
