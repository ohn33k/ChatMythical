import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import jdk.internal.org.objectweb.asm.*;

public final class PatchBridgeJars {
    private static final String OCULUS_COMMON = "net/irisshaders/iris/uniforms/CommonUniforms.class";
    private static final String POSEABLE_MODEL = "com/cobblemon/mod/common/client/render/models/blockbench/PoseableEntityModel.class";

    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            System.err.println("Usage: PatchBridgeJars <oculusIn> <oculusOut> <cobblemonIn> <cobblemonOut> <helperClassesDir>");
            System.exit(2);
        }
        Path oculusIn = Path.of(args[0]);
        Path oculusOut = Path.of(args[1]);
        Path cobblemonIn = Path.of(args[2]);
        Path cobblemonOut = Path.of(args[3]);
        Path helpers = Path.of(args[4]);

        Map<String, byte[]> oculusReplacements = new LinkedHashMap<>();
        oculusReplacements.put(OCULUS_COMMON, patchOculusCommonUniforms(readJarEntry(oculusIn, OCULUS_COMMON)));
        oculusReplacements.put("net/irisshaders/iris/uniforms/CobblemonBridge.class",
            Files.readAllBytes(helpers.resolve("net/irisshaders/iris/uniforms/CobblemonBridge.class")));
        oculusReplacements.put("com/openai/mythicaloculus/OculusUniformHook.class",
            Files.readAllBytes(helpers.resolve("com/openai/mythicaloculus/OculusUniformHook.class")));
        oculusReplacements.put("SUNLIT_SKINS_OCULUS_PATCH.txt", oculusReadme().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        rewriteJar(oculusIn, oculusOut, oculusReplacements);

        Map<String, byte[]> cobbleReplacements = new LinkedHashMap<>();
        cobbleReplacements.put(POSEABLE_MODEL, patchPoseableEntityModel(readJarEntry(cobblemonIn, POSEABLE_MODEL)));
        for (String helper : List.of(
            "com/openai/mythicalbridge/OculusEffectState.class",
            "com/openai/mythicalbridge/EffectTexturingState.class",
            "com/openai/mythicalbridge/EffectRenderTypeFactory.class",
            "com/openai/mythicalbridge/RenderTypeBridge.class",
            "com/openai/mythicalbridge/RenderTypeBridge$MethodLookupFailure.class")) {
            cobbleReplacements.put(helper, Files.readAllBytes(helpers.resolve(helper)));
        }
        cobbleReplacements.put("SUNLIT_SKINS_COBBLEMON_RENDER_PATCH.txt", cobblemonReadme().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        rewriteJar(cobblemonIn, cobblemonOut, cobbleReplacements);

        System.out.println("Patched Oculus: " + oculusOut);
        System.out.println("Patched Cobblemon: " + cobblemonOut);
    }

    private static byte[] patchOculusCommonUniforms(byte[] original) {
        ClassReader cr = new ClassReader(original);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        final int[] patchedMethods = {0};
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM8, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("addDynamicUniforms") && desc.equals("(Lnet/irisshaders/iris/gl/uniform/DynamicUniformHolder;Lnet/irisshaders/iris/gl/state/FogMode;)V")) {
                    patchedMethods[0]++;
                    return new MethodVisitor(Opcodes.ASM8, mv) {
                        @Override public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                super.visitVarInsn(Opcodes.ALOAD, 0);
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/openai/mythicaloculus/OculusUniformHook", "register", "(Ljava/lang/Object;)V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        if (patchedMethods[0] != 1) throw new IllegalStateException("Expected one Oculus addDynamicUniforms method, patched " + patchedMethods[0]);
        return cw.toByteArray();
    }

    private static byte[] patchPoseableEntityModel(byte[] original) {
        ClassReader cr = new ClassReader(original);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        final int[] lambdaMethods = {0};
        final int[] lambdaCalls = {0};
        final int[] setupMethods = {0};
        final int[] renderMethods = {0};
        final int[] clearReturns = {0};

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM8, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("_init_$lambda$0") && desc.equals("(Lkotlin/jvm/functions/Function1;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;")) {
                    lambdaMethods[0]++;
                    return new MethodVisitor(Opcodes.ASM8, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String methodName, String methodDesc, boolean isInterface) {
                            if (opcode == Opcodes.INVOKEINTERFACE
                                && owner.equals("kotlin/jvm/functions/Function1")
                                && methodName.equals("invoke")
                                && methodDesc.equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
                                lambdaCalls[0]++;
                                // Stack is [Function1, ResourceLocation]. It matches our bridge parameters.
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/openai/mythicalbridge/RenderTypeBridge", "choose",
                                    "(Ljava/lang/Object;Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", false);
                                return;
                            }
                            super.visitMethodInsn(opcode, owner, methodName, methodDesc, isInterface);
                        }
                    };
                }
                if (name.equals("m_6973_") && desc.equals("(Lnet/minecraft/world/entity/Entity;FFFFF)V")) {
                    setupMethods[0]++;
                    return new MethodVisitor(Opcodes.ASM8, mv) {
                        @Override public void visitCode() {
                            super.visitCode();
                            super.visitVarInsn(Opcodes.ALOAD, 1);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                "com/openai/mythicalbridge/RenderTypeBridge", "setCurrentEntity", "(Ljava/lang/Object;)V", false);
                        }
                    };
                }
                if (name.equals("m_7695_") && desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")) {
                    renderMethods[0]++;
                    return new MethodVisitor(Opcodes.ASM8, mv) {
                        @Override public void visitInsn(int opcode) {
                            if (opcode == Opcodes.RETURN) {
                                clearReturns[0]++;
                                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                    "com/openai/mythicalbridge/RenderTypeBridge", "clearCurrentEntity", "()V", false);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        if (lambdaMethods[0] != 1 || lambdaCalls[0] != 1) {
            throw new IllegalStateException("RenderType selector patch mismatch: methods=" + lambdaMethods[0] + " calls=" + lambdaCalls[0]);
        }
        if (setupMethods[0] != 1) throw new IllegalStateException("Expected one setupAnim method, patched " + setupMethods[0]);
        if (renderMethods[0] != 1 || clearReturns[0] < 1) {
            throw new IllegalStateException("Render cleanup patch mismatch: methods=" + renderMethods[0] + " returns=" + clearReturns[0]);
        }
        return cw.toByteArray();
    }

    private static byte[] readJarEntry(Path jar, String entryName) throws IOException {
        try (JarFile jf = new JarFile(jar.toFile())) {
            JarEntry e = jf.getJarEntry(entryName);
            if (e == null) throw new FileNotFoundException(entryName + " not found in " + jar);
            try (InputStream in = jf.getInputStream(e)) { return in.readAllBytes(); }
        }
    }

    private static void rewriteJar(Path input, Path output, Map<String, byte[]> replacements) throws IOException {
        Files.deleteIfExists(output);
        Set<String> written = new HashSet<>();
        try (JarFile jf = new JarFile(input.toFile()); OutputStream fos = Files.newOutputStream(output); JarOutputStream jos = new JarOutputStream(fos)) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry old = entries.nextElement();
                String name = old.getName();
                String upper = name.toUpperCase(Locale.ROOT);
                if (upper.startsWith("META-INF/") && (upper.endsWith(".SF") || upper.endsWith(".RSA") || upper.endsWith(".DSA"))) continue;
                byte[] replacement = replacements.get(name);
                JarEntry next = new JarEntry(name); next.setTime(0L); jos.putNextEntry(next);
                if (!old.isDirectory()) {
                    if (replacement != null) jos.write(replacement);
                    else try (InputStream in = jf.getInputStream(old)) { in.transferTo(jos); }
                }
                jos.closeEntry(); written.add(name);
            }
            for (Map.Entry<String, byte[]> e : replacements.entrySet()) {
                if (written.contains(e.getKey())) continue;
                JarEntry next = new JarEntry(e.getKey()); next.setTime(0L); jos.putNextEntry(next); jos.write(e.getValue()); jos.closeEntry();
            }
        }
    }

    private static String oculusReadme() {
        return "Sunlit Compatible Skins: registers cobblemon_effectType with Oculus 1.8.0.\n"
            + "The value is changed by a custom Cobblemon RenderType at actual batch draw time.\n"
            + "Client side only; built specifically for Society Sunlit's Oculus version.\n";
    }

    private static String cobblemonReadme() {
        return "Sunlit Compatible Skins: selects a custom effect RenderType from Mythical aspects.\n"
            + "Unlike V2/V3, it does not flush global buffers; setup/clear occurs when each batch is drawn.\n"
            + "Client side only; server must retain the original Cobblemon 1.5.2 jar.\n";
    }
}
