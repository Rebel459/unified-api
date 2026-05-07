import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

import java.nio.file.Files
import java.nio.file.Path

abstract class LegacySupplierBridgeTask extends DefaultTask {

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    final ConfigurableFileCollection classRoots = project.objects.fileCollection()

    private static final String SUPPLIED_DESC = 'Lnet/rebel459/unified/util/registry/Supplied;'
    private static final String SUPPLIED_ITEM_DESC = 'Lnet/rebel459/unified/util/registry/SuppliedItem;'
    private static final String LEGACY_SUPPLIED_ITEM_DESC = 'Lnet/rebel459/unified/util/SuppliedItem;'
    private static final String SUPPLIED_BLOCK_DESC = 'Lnet/rebel459/unified/util/registry/SuppliedBlock;'
    private static final String LEGACY_SUPPLIED_BLOCK_DESC = 'Lnet/rebel459/unified/util/SuppliedBlock;'
    private static final String SUPPLIER_DESC = 'Ljava/util/function/Supplier;'

    private static final Map<String, List<BridgeSpec>> BRIDGES = Collections.unmodifiableMap([
            'net/rebel459/unified/platform/UnifiedRegistries$DeferredRegistry': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Supplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$Items': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Ljava/lang/String;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Lnet/rebel459/unified/util/SuppliedBlock;Ljava/util/function/Supplier;)', '(Lnet/rebel459/unified/util/registry/SuppliedBlock;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$Blocks': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$DataComponentTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/UnaryOperator;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$EntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/entity/EntityType$Builder;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$BlockEntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/rebel459/unified/util/BlockLike;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/UnifiedRegistries$SoundEvents': [
                    new BridgeSpec('register', '(Ljava/lang/String;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;F)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$DeferredRegistry': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Supplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$Items': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Ljava/lang/String;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Lnet/rebel459/unified/util/SuppliedBlock;Ljava/util/function/Supplier;)', '(Lnet/rebel459/unified/util/registry/SuppliedBlock;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$Blocks': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$DataComponentTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/UnaryOperator;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$EntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/entity/EntityType$Builder;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$BlockEntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/rebel459/unified/util/BlockLike;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/FabricUnifiedRegistries$SoundEvents': [
                    new BridgeSpec('register', '(Ljava/lang/String;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;F)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$DeferredRegistry': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Supplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$Items': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Ljava/lang/String;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
                    new BridgeSpec('registerBlockItem', '(Lnet/rebel459/unified/util/SuppliedBlock;Ljava/util/function/Supplier;)', '(Lnet/rebel459/unified/util/registry/SuppliedBlock;Ljava/util/function/Supplier;)', SUPPLIED_ITEM_DESC, LEGACY_SUPPLIED_ITEM_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$Blocks': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('registerWithoutItem', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Supplier;Ljava/util/function/Function;Ljava/util/function/Supplier;Lnet/minecraft/world/level/block/entity/BlockEntityType;)', SUPPLIED_BLOCK_DESC, LEGACY_SUPPLIED_BLOCK_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$DataComponentTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Ljava/util/function/UnaryOperator;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$EntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/entity/EntityType$Builder;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$BlockEntityTypes': [
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;Lnet/minecraft/world/level/block/entity/BlockEntityType$BlockEntitySupplier;[Lnet/rebel459/unified/util/BlockLike;)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
            'net/rebel459/unified/platform/NeoForgeUnifiedRegistries$SoundEvents': [
                    new BridgeSpec('register', '(Ljava/lang/String;)', SUPPLIED_DESC, SUPPLIER_DESC),
                    new BridgeSpec('register', '(Ljava/lang/String;F)', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
    ] as Map<String, List<BridgeSpec>>)

    private static final Map<String, List<FieldBridgeSpec>> FIELD_BRIDGES = Collections.unmodifiableMap([
            'net/rebel459/unified/registry/UnifiedDataComponents': [
                    new FieldBridgeSpec('FURNACE_FUEL', SUPPLIED_DESC, SUPPLIER_DESC),
                    new FieldBridgeSpec('COMPOST', SUPPLIED_DESC, SUPPLIER_DESC),
            ],
    ] as Map<String, List<FieldBridgeSpec>>)

    @TaskAction
    void injectBridges() {
        for (File root : classRoots.files.findAll { it.isDirectory() }) {
            for (Map.Entry<String, List<BridgeSpec>> entry : BRIDGES.entrySet()) {
                String internalName = entry.key
                Path classFile = root.toPath().resolve("${internalName}.class")
                if (Files.exists(classFile)) {
                    patchClass(classFile, entry.value, FIELD_BRIDGES.getOrDefault(internalName, List.of()))
                }
            }
            for (Map.Entry<String, List<FieldBridgeSpec>> entry : FIELD_BRIDGES.entrySet()) {
                String internalName = entry.key
                if (BRIDGES.containsKey(internalName)) {
                    continue
                }
                Path classFile = root.toPath().resolve("${internalName}.class")
                if (Files.exists(classFile)) {
                    patchClass(classFile, List.of(), entry.value)
                }
            }
        }
    }

    private void patchClass(Path classFile, List<BridgeSpec> specs, List<FieldBridgeSpec> fieldSpecs) {
        byte[] original = Files.readAllBytes(classFile)
        ClassReader reader = new ClassReader(original)
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
        BridgeClassVisitor visitor = new BridgeClassVisitor(writer, specs, fieldSpecs)
        reader.accept(visitor, 0)
        if (visitor.modified) {
            Files.write(classFile, writer.toByteArray())
            logger.info("Injected legacy Supplier bridges into ${classFile}")
        }
    }

    private static final class BridgeClassVisitor extends ClassVisitor {
        private final List<BridgeSpec> specs
        private final List<FieldBridgeSpec> fieldSpecs
        private final Set<String> methods = new HashSet<>()
        private final Set<String> fields = new HashSet<>()
        private String owner
        private boolean isInterface
        boolean modified

        BridgeClassVisitor(ClassVisitor classVisitor, List<BridgeSpec> specs, List<FieldBridgeSpec> fieldSpecs) {
            super(Opcodes.ASM9, classVisitor)
            this.specs = specs
            this.fieldSpecs = fieldSpecs
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            owner = name
            isInterface = (access & Opcodes.ACC_INTERFACE) != 0
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            fields.add(name + descriptor)
            return super.visitField(access, name, descriptor, signature, value)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            methods.add(name + descriptor)
            MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if (name == '<clinit>' && !fieldSpecs.isEmpty()) {
                return new FieldBridgeInitializerVisitor(mv, owner, fields, fieldSpecs, this)
            }
            return mv
        }

        @Override
        void visitEnd() {
            for (BridgeSpec spec : specs) {
                String newDescriptor = spec.newArgumentsDescriptor + spec.newReturnDescriptor
                String oldDescriptor = spec.oldArgumentsDescriptor + spec.oldReturnDescriptor
                if (!methods.contains(spec.name + newDescriptor) || methods.contains(spec.name + oldDescriptor)) {
                    continue
                }

                MethodVisitor mv = super.visitMethod(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC,
                        spec.name,
                        oldDescriptor,
                        null,
                        null
                )
                mv.visitCode()
                int local = 0
                mv.visitVarInsn(Opcodes.ALOAD, local++)
                Type[] oldArgumentTypes = Type.getArgumentTypes(spec.oldArgumentsDescriptor + 'V')
                Type[] newArgumentTypes = Type.getArgumentTypes(spec.newArgumentsDescriptor + 'V')
                for (int i = 0; i < oldArgumentTypes.length; i++) {
                    Type argumentType = oldArgumentTypes[i]
                    mv.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), local)
                    Type newArgumentType = newArgumentTypes[i]
                    if (argumentType.descriptor != newArgumentType.descriptor && (newArgumentType.sort == Type.OBJECT || newArgumentType.sort == Type.ARRAY)) {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, newArgumentType.internalName)
                    }
                    local += argumentType.size
                }
                mv.visitMethodInsn(
                        isInterface ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                        owner,
                        spec.name,
                        newDescriptor,
                        isInterface
                )
                Type oldReturnType = Type.getReturnType(oldDescriptor)
                if (oldReturnType.sort == Type.OBJECT && spec.oldReturnDescriptor != spec.newReturnDescriptor && spec.oldReturnDescriptor != SUPPLIER_DESC) {
                    mv.visitTypeInsn(Opcodes.CHECKCAST, oldReturnType.internalName)
                }
                mv.visitInsn(Opcodes.ARETURN)
                mv.visitMaxs(0, 0)
                mv.visitEnd()
                modified = true
            }

            for (FieldBridgeSpec spec : fieldSpecs) {
                if (!fields.contains(spec.name + spec.newDescriptor) || fields.contains(spec.name + spec.oldDescriptor)) {
                    continue
                }
                super.visitField(
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                        spec.name,
                        spec.oldDescriptor,
                        null,
                        null
                )?.visitEnd()
                modified = true
            }
            super.visitEnd()
        }
    }

    private static final class FieldBridgeInitializerVisitor extends MethodVisitor {
        private final String owner
        private final Set<String> fields
        private final List<FieldBridgeSpec> fieldSpecs
        private final BridgeClassVisitor parent

        FieldBridgeInitializerVisitor(MethodVisitor methodVisitor, String owner, Set<String> fields, List<FieldBridgeSpec> fieldSpecs, BridgeClassVisitor parent) {
            super(Opcodes.ASM9, methodVisitor)
            this.owner = owner
            this.fields = fields
            this.fieldSpecs = fieldSpecs
            this.parent = parent
        }

        @Override
        void visitFieldInsn(int opcode, String fieldOwner, String name, String descriptor) {
            if (opcode == Opcodes.PUTSTATIC && fieldOwner == owner) {
                for (FieldBridgeSpec spec : fieldSpecs) {
                    if (spec.name == name && spec.newDescriptor == descriptor && !fields.contains(spec.name + spec.oldDescriptor)) {
                        super.visitInsn(Opcodes.DUP)
                        super.visitFieldInsn(opcode, fieldOwner, name, descriptor)
                        super.visitFieldInsn(opcode, fieldOwner, name, spec.oldDescriptor)
                        parent.modified = true
                        return
                    }
                }
            }
            super.visitFieldInsn(opcode, fieldOwner, name, descriptor)
        }
    }

    private static final class BridgeSpec {
        final String name
        final String oldArgumentsDescriptor
        final String newArgumentsDescriptor
        final String newReturnDescriptor
        final String oldReturnDescriptor

        BridgeSpec(String name, String argumentsDescriptor, String newReturnDescriptor, String oldReturnDescriptor) {
            this(name, argumentsDescriptor, argumentsDescriptor, newReturnDescriptor, oldReturnDescriptor)
        }

        BridgeSpec(String name, String oldArgumentsDescriptor, String newArgumentsDescriptor, String newReturnDescriptor, String oldReturnDescriptor) {
            this.name = name
            this.oldArgumentsDescriptor = oldArgumentsDescriptor
            this.newArgumentsDescriptor = newArgumentsDescriptor
            this.newReturnDescriptor = newReturnDescriptor
            this.oldReturnDescriptor = oldReturnDescriptor
        }
    }

    private static final class FieldBridgeSpec {
        final String name
        final String newDescriptor
        final String oldDescriptor

        FieldBridgeSpec(String name, String newDescriptor, String oldDescriptor) {
            this.name = name
            this.newDescriptor = newDescriptor
            this.oldDescriptor = oldDescriptor
        }
    }
}
