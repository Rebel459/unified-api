package net.rebel459.build;

import org.gradle.api.GradleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CodecDocsTaskTest {
    @TempDir Path temporaryDirectory;

    @Test
    void resolvesSimpleAliasesRecordsOptionalDefaultsAndConstraints() throws Exception {
        Path source = temporaryDirectory.resolve("ExampleCodecs.java");
        Files.writeString(source, """
                package net.rebel459.unified.api.registry;
                import com.mojang.serialization.Codec;
                import com.mojang.serialization.MapCodec;
                import com.mojang.serialization.codecs.RecordCodecBuilder;
                class ExampleCodecs {
                    static MapCodec<String> BASE = Codec.STRING.fieldOf("name");
                    static MapCodec<Integer> COUNT = Codec.intRange(1, 12).optionalFieldOf("count", DEFAULT_COUNT);
                    static final int DEFAULT_COUNT = 4;
                    static Object ONE = simple("one", null);
                    static Object EXAMPLE = complex("example", RecordCodecBuilder.mapCodec(i -> i.group(
                        BASE.forGetter(x -> null),
                        COUNT.forGetter(x -> null),
                        Codec.either(Codec.STRING, Codec.INT).listOf().fieldOf("choices").forGetter(x -> null),
                        Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("weights").forGetter(x -> null),
                        Codec.BOOL.fieldOf("enabled").forGetter(x -> null),
                        RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("entities").forGetter(x -> null),
                        IntProviders.codec(0, 10).fieldOf("experience").forGetter(x -> null)
                    ).apply(i, X::new)), null);
                    static Object simple(String id, Object factory) { return null; }
                    static Object complex(String id, MapCodec<?> codec, Object factory) { return null; }
                    static class X {}
                }
                """);
        CodecDocsTask.Generator generator = new CodecDocsTask.Generator(temporaryDirectory);
        List<CodecDocsTask.Registration> entries = generator.registrations();

        assertEquals(2, entries.size());
        assertEquals("unified:example", entries.get(0).id());
        assertEquals("unified:one", entries.get(1).id());
        assertEquals(List.of("name", "count", "choices", "weights", "enabled", "entities", "experience"), entries.get(0).fields().stream().map(CodecDocsTask.Field::name).toList());
        assertEquals("<string>", entries.get(0).fields().get(0).type());
        assertEquals("optional, defaults to `DEFAULT_COUNT`, range: `1` to `12`", entries.get(0).fields().get(1).detail());
        assertEquals("[]", entries.get(0).fields().get(2).type());
        assertEquals(List.of("<string>", "<int>"), entries.get(0).fields().get(2).alternatives().stream().map(CodecDocsTask.Value::type).toList());
        assertEquals("{<string>: <int>}", entries.get(0).fields().get(3).type());
        assertEquals("<bool>", entries.get(0).fields().get(4).type());
        assertEquals(List.of("\"#<identifier>\"", "<identifier>", "[<identifier>]"), entries.get(0).fields().get(5).alternatives().stream().map(CodecDocsTask.Value::type).toList());
        assertEquals(List.of("<int>", "<object>"), entries.get(0).fields().get(6).alternatives().stream().map(CodecDocsTask.Value::type).toList());
    }

    @Test
    void refusesUnrecognizedCodecExpressionsWithSourceContext() throws Exception {
        Path source = temporaryDirectory.resolve("UnknownCodecs.java");
        Files.writeString(source, """
                package net.rebel459.unified.api.registry;
                class UnknownCodecs {
                    static Object BAD = complex("bad", SomeLibrary.magicCodec().fieldOf("value"), null);
                    static Object complex(String id, Object codec, Object factory) { return null; }
                }
                """);
        CodecDocsTask.Generator generator = new CodecDocsTask.Generator(temporaryDirectory);
        GradleException failure = assertThrows(GradleException.class, generator::registrations);
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("unified:bad"));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("UnknownCodecs.java"));
        org.junit.jupiter.api.Assertions.assertTrue(failure.getMessage().contains("SomeLibrary.magicCodec()"));
    }
}
