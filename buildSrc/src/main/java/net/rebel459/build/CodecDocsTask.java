package net.rebel459.build;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public abstract class CodecDocsTask extends DefaultTask {
    @InputDirectory @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getSourceDirectory();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Classpath
    public abstract ConfigurableFileCollection getEnumClasspath();

    @TaskAction
    public void generate() throws IOException {
        Generator generator = new Generator(getSourceDirectory().get().getAsFile().toPath(), getEnumClasspath().getFiles());
        List<Registration> registrations;
        try (generator) { registrations = generator.registrations(); }
        if (registrations.isEmpty()) throw new GradleException("No codec registrations found under " + getSourceDirectory().get().getAsFile());
        Map<String, List<Registration>> categories = new TreeMap<>();
        for (Registration registration : registrations) {
            categories.computeIfAbsent(registration.category, ignored -> new ArrayList<>()).add(registration);
        }
        Path output = getOutputDirectory().get().getAsFile().toPath();
        if (Files.exists(output)) {
            try (Stream<Path> paths = Files.walk(output)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.delete(path); } catch (IOException e) { throw new RuntimeException(e); }
                });
            }
        }
        Files.createDirectories(output);
        for (Map.Entry<String, List<Registration>> category : categories.entrySet()) {
            category.getValue().sort(Comparator.comparing(registration -> registration.id));
            StringBuilder markdown = new StringBuilder();
            for (Registration registration : category.getValue()) {
                markdown.append("### `").append(registration.id).append("`\n\n");
                List<List<Field>> examples = fieldVariants(registration.fields);
                for (int exampleIndex = 0; exampleIndex < examples.size(); exampleIndex++) {
                    if (exampleIndex > 0) markdown.append('\n');
                    markdown.append("```json5\n");
                    List<Field> fields = examples.get(exampleIndex);
                    markdown.append("{\n  \"type\": \"").append(registration.id).append("\"");
                    if (!fields.isEmpty()) markdown.append(",\n");
                    for (int index = 0; index < fields.size(); index++) {
                        markdown.append(fields.get(index).render(2, index < fields.size() - 1));
                        if (index < fields.size() - 1) markdown.append('\n');
                    }
                    markdown.append("\n}\n```\n");
                }
                List<String> enumFields = new ArrayList<>();
                for (Field field : registration.fields) collectEnumFields(field, field.name, enumFields);
                if (!enumFields.isEmpty()) markdown.append("\n<details>\n<summary>Values</summary>\n\n")
                        .append(String.join("\n", enumFields)).append("\n\n</details>\n");
                markdown.append('\n');
            }
            Files.writeString(output.resolve(category.getKey() + ".md"), markdown.toString());
        }
        getLogger().lifecycle("Generated {} codec documentation entries in {} categories at {}", registrations.size(), categories.size(), output);
    }

    static final class Generator implements AutoCloseable {
        private final Path root;
        private final URLClassLoader enumClassLoader;
        private final List<CompilationUnit> units = new ArrayList<>();
        private final Map<String, Expression> declarations = new HashMap<>();
        private final Map<String, List<Expression>> simpleDeclarations = new HashMap<>();
        private final Map<String, Expression> fileDeclarations = new HashMap<>();
        private final Map<String, String> importedTypes = new HashMap<>();
        private final List<Registration> found = new ArrayList<>();

        Generator(Path root) throws IOException { this(root, Set.of()); }

        Generator(Path root, Set<File> enumClasspath) throws IOException {
            this.root = root;
            URL[] classpath = enumClasspath.stream().map(file -> {
                try { return file.toURI().toURL(); }
                catch (java.net.MalformedURLException e) { throw new GradleException("Invalid enum classpath entry " + file, e); }
            }).toArray(URL[]::new);
            this.enumClassLoader = new URLClassLoader(classpath, getClass().getClassLoader());
            StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    try { units.add(StaticJavaParser.parse(file)); }
                    catch (RuntimeException error) { throw new GradleException("Could not parse Java source " + file + ": " + error.getMessage(), error); }
                }
            }
            for (CompilationUnit unit : units) for (var imported : unit.getImports()) {
                if (imported.isAsterisk() || imported.isStatic()) continue;
                String qualified = imported.getNameAsString();
                importedTypes.put(qualified.substring(qualified.lastIndexOf('.') + 1), qualified);
            }
            indexDeclarations();
        }

        @Override
        public void close() throws IOException { enumClassLoader.close(); }

        List<Registration> registrations() {
            for (CompilationUnit unit : units) {
                String packageName = unit.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
                if (!packageName.equals("net.rebel459.unified.api.registry")) continue;
                Path source = unit.getStorage().map(storage -> storage.getPath()).orElse(root.resolve("Unknown.java"));
                if (!source.getFileName().toString().endsWith("Codecs.java")) continue;
                String category = category(source.getFileName().toString());
                for (FieldDeclaration declaration : unit.findAll(FieldDeclaration.class)) {
                    for (var variable : declaration.getVariables()) {
                        if (variable.getInitializer().isEmpty()) continue;
                        registration(variable.getInitializer().get(), source, category).ifPresent(found::add);
                    }
                }
                for (VariableDeclarationExpr declaration : unit.findAll(VariableDeclarationExpr.class)) {
                    for (var variable : declaration.getVariables()) {
                        if (variable.getInitializer().isEmpty()) continue;
                        Optional<Registration> registration = registration(variable.getInitializer().get(), source, category);
                        registration.ifPresent(found::add);
                    }
                }
            }
            found.sort(Comparator.comparing((Registration r) -> r.category).thenComparing(r -> r.id));
            return found;
        }

        private void indexDeclarations() {
            for (CompilationUnit unit : units) {
                for (FieldDeclaration field : unit.findAll(FieldDeclaration.class)) {
                    String owner = field.findAncestor(TypeDeclaration.class).map(TypeDeclaration::getNameAsString).orElse("");
                    String file = unit.getStorage().map(storage -> storage.getPath().toString()).orElse("");
                    for (var variable : field.getVariables()) variable.getInitializer().ifPresent(expression -> {
                        declarations.put(owner + "." + variable.getNameAsString(), expression);
                        fileDeclarations.put(file + "::" + variable.getNameAsString(), expression);
                        simpleDeclarations.computeIfAbsent(variable.getNameAsString(), ignored -> new ArrayList<>()).add(expression);
                    });
                }
            }
        }

        private Optional<Registration> registration(Expression initializer, Path source, String category) {
            if (!(initializer instanceof MethodCallExpr call)) return Optional.empty();
            String method = call.getNameAsString();
            if (!Set.of("simple", "simpleBlock", "simpleEntity", "complex", "complexBlock", "register").contains(method)) return Optional.empty();
            List<Expression> args = call.getArguments();
            if (args.isEmpty()) return Optional.empty();
            Expression pathExpression = args.get(0);
            String path;
            String namespace;
            if (pathExpression instanceof StringLiteralExpr literal) {
                path = literal.asString();
                namespace = source.getFileName().toString().startsWith("Vanilla") ? "minecraft" : "unified";
            } else if (pathExpression instanceof MethodCallExpr idCall && !idCall.getArguments().isEmpty() && idCall.getArgument(0) instanceof StringLiteralExpr literal) {
                path = literal.asString();
                String idExpression = idCall.toString();
                namespace = idExpression.startsWith("Identifier.withDefaultNamespace") ? "minecraft" : "unified";
            } else return Optional.empty();
            String id = namespace + ":" + path;
            List<Field> fields = List.of();
            boolean blockPredicateRegister = call.getScope().map(scope -> scope.toString().endsWith("ExtensibleBlockPredicateCodec")).orElse(false);
            boolean directComplex = method.equals("register") && (blockPredicateRegister ? args.size() >= 6 : args.size() >= 3);
            if (method.equals("complex") || method.equals("complexBlock") || directComplex) {
                if (args.size() < 2) return Optional.empty();
                fields = resolve(args.get(1), new LinkedHashSet<>(), source, id);
            }
            return Optional.of(new Registration(id, category, fields, source));
        }

        private List<Field> resolve(Expression expression, Set<String> visiting, Path source, String id) {
            if (expression instanceof MethodCallExpr call) {
                String method = call.getNameAsString();
                List<Expression> args = call.getArguments();
                if ((method.equals("fieldOf") || method.equals("optionalFieldOf")) && !args.isEmpty() && args.get(0) instanceof StringLiteralExpr name) {
                    Value value = value(call.getScope().orElseThrow(() -> unresolved(id, source, call)), visiting, source, id);
                    boolean optional = method.equals("optionalFieldOf");
                    String detail = "";
                    if (args.size() > 1) detail = (optional ? "optional, " : "") + "defaults to `" + defaultValue(call.getScope().orElseThrow(), args.get(1)) + "`";
                    else if (optional) detail = "optional";
                    if (!value.constraint.isEmpty()) detail = detail.isEmpty() ? value.constraint : detail + ", " + value.constraint;
                    return List.of(new Field(name.asString(), value.type, detail, value.children, value.alternatives, value.enumValues));
                }
                if (method.equals("forGetter")) return resolve(call.getScope().orElseThrow(), visiting, source, id);
                if (method.equals("group")) {
                    List<Field> fields = new ArrayList<>();
                    for (Expression argument : args) fields.addAll(resolve(argument, visiting, source, id));
                    return fields;
                }
                if (method.equals("mapCodec") || method.equals("create")) {
                    Optional<MethodCallExpr> group = call.findFirst(MethodCallExpr.class, nested -> nested.getNameAsString().equals("group"));
                    if (group.isPresent()) return resolve(group.get(), visiting, source, id);
                }
                if (method.equals("listOf")) {
                    Value element = value(call.getScope().orElseThrow(), visiting, source, id);
                    return List.of(new Field("", "[]", "", List.of(), element.alternatives.isEmpty() ? List.of(element) : element.alternatives));
                }
            }
            Value value = value(expression, visiting, source, id);
            if (!value.children.isEmpty()) return value.children;
            throw unresolved(id, source, expression);
        }

        private String defaultValue(Expression codec, Expression expression) {
            boolean namedCodec = codec.findFirst(MethodCallExpr.class, call -> call.getNameAsString().equals("named")
                    && call.getScope().map(scope -> scope.toString().equals("UnifiedCodecs")).orElse(false)).isPresent();
            if (namedCodec && expression instanceof com.github.javaparser.ast.expr.FieldAccessExpr constant) {
                return "\"" + constant.getNameAsString().toLowerCase(java.util.Locale.ROOT) + "\"";
            }
            return expression.toString();
        }

        private String codecType(Expression expression) {
            String name = expression.toString();
            if (name.startsWith("ExtensibleCodecs.")) name = name.substring("ExtensibleCodecs.".length());
            return name.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
        }

        private List<String> enumValues(String className) {
            try {
                Class<?> type = loadClass(className);
                List<String> names = new ArrayList<>();
                for (java.lang.reflect.Field field : type.getFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && type.isAssignableFrom(field.getType())) {
                        names.add(field.getName().toLowerCase(java.util.Locale.ROOT));
                    }
                }
                return names.stream().distinct().sorted().toList();
            } catch (ReflectiveOperationException | LinkageError error) {
                throw new GradleException("Could not read named codec values from " + className + ". Ensure the Minecraft compile classpath is supplied to generateCodecDocs.", error);
            }
        }

        private Class<?> loadClass(String className) throws ClassNotFoundException {
            String candidate = className;
            ClassNotFoundException failure = null;
            for (int separator = candidate.lastIndexOf('.'); separator >= 0; separator = candidate.lastIndexOf('.', separator - 1)) {
                try { return Class.forName(candidate, false, enumClassLoader); }
                catch (ClassNotFoundException error) { failure = error; }
                candidate = candidate.substring(0, separator) + '$' + candidate.substring(separator + 1);
            }
            try { return Class.forName(candidate, false, enumClassLoader); }
            catch (ClassNotFoundException error) { if (failure != null) error.addSuppressed(failure); throw error; }
        }

        private String resolveTypeName(String typeName) {
            int separator = typeName.indexOf('.');
            String first = separator < 0 ? typeName : typeName.substring(0, separator);
            String imported = importedTypes.get(first);
            return imported == null ? typeName : imported + (separator < 0 ? "" : typeName.substring(separator));
        }

        private Value value(Expression expression, Set<String> visiting, Path source, String id) {
            String text = expression.toString();
            if (expression instanceof NameExpr name) {
                String key = name.getNameAsString();
                return reference(key, visiting, source, id, expression);
            }
            if (expression instanceof MethodCallExpr call) {
                String method = call.getNameAsString();
                List<Expression> args = call.getArguments();
                if (method.equals("byNameCodec") || method.equals("supplied") || method.equals("codec") && args.size() == 1 && args.get(0).toString().startsWith("Registries.")) return Value.of("<identifier>");
                if (method.equals("named")) return Value.stringEnum(enumValues(resolveTypeName(args.get(0).asClassExpr().getType().asString())));
                if (method.equals("codec") && text.startsWith("MultiColored.codec") && args.size() == 2) return Value.object(List.of(
                        new Field(((StringLiteralExpr) args.get(0)).asString(), "<string>", "", List.of()),
                        new Field(((StringLiteralExpr) args.get(1)).asString(), "<string>", "", List.of())));
                if (method.equals("codec") && text.startsWith("IntProviders.")) {
                    String range = "range: `" + args.get(0) + "` to `" + args.get(1) + "`";
                    return Value.choice(Value.withConstraint("<int>", range), Value.object(List.of(
                            new Field("type", "\"minecraft:uniform\"", "", List.of()),
                            new Field("min_inclusive", "<int>", "", List.of()),
                            new Field("max_inclusive", "<int>", "", List.of()))), range);
                }
                if (method.equals("conditional") && text.startsWith("UnifiedCodecs.")) {
                    String predicateType = "<" + codecType(args.get(0)) + " codec>";
                    String valueType = "<" + codecType(args.get(1)) + " codec>";
                    return Value.object(List.of(
                            new Field("predicate", predicateType, "", List.of()),
                            new Field("if_true", valueType, "", List.of()),
                            new Field("if_false", valueType, "", List.of())));
                }
                if (method.equals("lazyInitialized")) return Value.of("<codec>");
                if (method.equals("homogeneousList") || method.equals("holderSet")) return Value.choice(
                        Value.of("\"#<identifier>\""), Value.of("<identifier>"), Value.of("[<identifier>]")
                );
                if (method.equals("list") || method.equals("listOf")) {
                    Expression element = method.equals("list") ? args.get(0) : call.getScope().orElseThrow();
                    Value elementValue = value(element, visiting, source, id);
                    return Value.list(elementValue);
                }
                if (method.equals("unboundedMap")) return Value.of("{" + value(args.get(0), visiting, source, id).type + ": " + value(args.get(1), visiting, source, id).type + "}");
                if (method.equals("intRange")) return Value.range("<int>", args.get(0) + "` to `" + args.get(1));
                if (method.equals("floatRange")) return Value.range("<float>", args.get(0) + "` to `" + args.get(1));
                if (method.equals("either")) {
                    return Value.choice(value(args.get(0), visiting, source, id), value(args.get(1), visiting, source, id));
                }
                if (method.equals("codec") && text.startsWith("TagKey.")) return Value.of("<identifier>");
                if (method.equals("codec") && text.startsWith("ResourceKey.")) return Value.of("<identifier>");
                if (method.equals("flatXmap") || method.equals("xmap") || method.equals("validate") || method.equals("comapFlatMap")) return value(call.getScope().orElseThrow(), visiting, source, id);
                if (method.equals("codec") && args.isEmpty() && call.getScope().isPresent()) return value(call.getScope().get(), visiting, source, id);
                if (method.equals("codec") && args.size() == 1 && args.get(0).toString().startsWith("Registries.")) return Value.of("<identifier>");
                if (call.getScope().isPresent()) {
                    String scope = call.getScope().get().toString();
                    if (scope.endsWith(".CODEC")) return reference(scope, visiting, source, id, expression);
                    if (method.equals("codec") && scope.equals("TagKey")) return Value.of("<identifier>");
                }
            }
            if (expression instanceof com.github.javaparser.ast.expr.FieldAccessExpr access) {
                String name = access.getNameAsString();
                String owner = access.getScope().toString();
                if (!(owner.endsWith("Codec") || owner.endsWith("ExtraCodecs"))) return reference(text, visiting, source, id, expression);
                if (name.equals("BOOL")) return Value.of("<bool>");
                if (name.equals("INT") || name.equals("NON_NEGATIVE_INT") || name.equals("POSITIVE_INT")) return Value.withConstraint("<int>", name.equals("NON_NEGATIVE_INT") ? "range: `0` or greater" : name.equals("POSITIVE_INT") ? "range: `1` or greater" : "");
                if (name.equals("LONG")) return Value.of("<long>");
                if (name.equals("FLOAT")) return Value.of("<float>");
                if (name.equals("POSITIVE_FLOAT")) return Value.withConstraint("<float>", "range: `0` or greater");
                if (name.equals("DOUBLE")) return Value.of("<double>");
                if (name.equals("STRING") || name.equals("NON_EMPTY_STRING")) return Value.of("<string>");
                return reference(text, visiting, source, id, expression);
            }
            if (expression instanceof ObjectCreationExpr) return Value.of("<object>");
            throw unresolved(id, source, expression);
        }

        private Value reference(String key, Set<String> visiting, Path source, String id, Expression site) {
            Expression initializer = key.contains(".") ? declarations.get(key) : fileDeclarations.get(source.toString() + "::" + key);
            if (initializer == null && key.endsWith(".CODEC")) {
                String owner = key.substring(0, key.length() - 6);
                List<Expression> candidates = simpleDeclarations.get("CODEC");
                if (candidates != null && candidates.size() == 1) initializer = candidates.get(0);
                if (initializer == null && owner.equals("BlockState")) return Value.object(List.of(
                        new Field("Name", "<identifier>", "", List.of()),
                        new Field("Properties", "{<string>: <string>}", "", List.of())));
            }
            if (initializer == null && !key.contains(".")) {
                List<Expression> candidates = simpleDeclarations.get(key);
                if (candidates != null && candidates.size() == 1) initializer = candidates.get(0);
            }
            if (initializer == null) {
                String leaf = key.substring(key.lastIndexOf('.') + 1);
                String owner = key.contains(".") ? key.substring(0, key.lastIndexOf('.')) : "";
                if (leaf.equals("CODEC") && owner.equals("Identifier")) return Value.of("<identifier>");
                if (leaf.equals("CODEC") && owner.equals("ColorRGBA")) return Value.of("<int>");
                if (leaf.equals("CODEC") && owner.equals("ItemStack")) return Value.object(List.of(
                        new Field("id", "<identifier>", "", List.of()),
                        new Field("count", "<int>", "optional, defaults to `1`, range: `1` to `99`", List.of()),
                        new Field("components", "{<identifier>: <value>}", "optional, defaults to `{}`", List.of())));
                if (leaf.equals("CODEC") && Set.of("Direction", "DyeColor", "Biome.Precipitation", "CreativeModeTab.TabVisibility", "WeatheringCopper.WeatherState", "SkullBlock.Type").contains(owner)) {
                    String packageName = owner.equals("Direction") ? "net.minecraft.core." : "net.minecraft.world.level.block.";
                    if (owner.equals("DyeColor")) packageName = "net.minecraft.world.item.";
                    if (owner.equals("CreativeModeTab.TabVisibility")) packageName = "net.minecraft.world.item.";
                    if (owner.equals("Biome.Precipitation")) packageName = "net.minecraft.world.level.biome.";
                    if (owner.equals("WeatheringCopper.WeatherState")) packageName = "net.minecraft.world.level.block.";
                    return Value.stringEnum(enumValues(packageName + owner));
                }
                if (leaf.equals("CODEC") && owner.equals("TreeGrower")) return Value.stringEnum(enumValues("net.minecraft.world.level.block.grower.TreeGrower"));
                if (leaf.equals("CODEC") && owner.equals("AmbientLeavesBlockSoundPlayer")) return Value.object(List.of(
                        new Field("ambient_sound", "", "optional", List.of(), List.of(
                                Value.of("<identifier>"),
                                Value.object(List.of(
                                        new Field("sound_id", "<identifier>", "", List.of()),
                                        new Field("range", "<float>", "optional", List.of()))))),
                        new Field("chance", "<int>", "optional, defaults to `300`, range: `0` or greater", List.of()),
                        new Field("satisfying_blocks", "<identifier>", "optional", List.of()),
                        new Field("nearby_satisfying_blocks_required", "<int>", "optional, defaults to `1`, range: `0` or greater", List.of()),
                        new Field("nearby_same_leaves_required", "<int>", "optional, defaults to `3`, range: `0` or greater", List.of())));
                if (leaf.equals("CODEC") && owner.equals("ParticleTypes")) return Value.of("<identifier>");
                if (leaf.equals("CODEC") && owner.equals("SuspiciousStewEffects")) return Value.list(Value.object(List.of(
                        new Field("id", "<identifier>", "", List.of()),
                        new Field("duration", "<int>", "optional, defaults to `160`", List.of()))));
                if (leaf.equals("CODEC") && owner.equals("CauldronInteractions")) return Value.of("<string>");
                if (Set.of("CODEC", "VALUE_MAP_CODEC").contains(leaf)) {
                    if (key.contains("DataComponent")) return Value.of("{<identifier>: <value>}");
                }
                throw unresolved(id, source, site);
            }
            if (!visiting.add(key)) throw new GradleException("Codec alias cycle while resolving " + id + ": " + String.join(" -> ", visiting) + " -> " + key);
            try {
                try {
                    List<Field> fields = resolve(initializer, visiting, source, id);
                    return Value.object(fields);
                } catch (GradleException notAFieldCodec) {
                    if (initializer.findFirst(MethodCallExpr.class, call -> call.getNameAsString().equals("group")).isPresent()) throw notAFieldCodec;
                    return value(initializer, visiting, source, id);
                }
            } finally { visiting.remove(key); }
        }

        private GradleException unresolved(String id, Path source, Node expression) {
            int line = expression.getBegin().map(position -> position.line).orElse(1);
            return new GradleException("Unable to resolve codec for " + id + "\nSource: " + source.getFileName() + ":" + line + "\nCodec: " + expression + "\nAdd a resolver rule for this Java AST expression.");
        }

        private static String category(String file) {
            String category = file.replaceFirst("^(Vanilla|Unified)", "").replaceFirst("Codecs\\.java$", "");
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < category.length(); i++) {
                char c = category.charAt(i);
                if (Character.isUpperCase(c) && i > 0) out.append('-');
                out.append(Character.toLowerCase(c));
            }
            return out.toString();
        }
    }

    record Registration(String id, String category, List<Field> fields, Path source) {}

    private static List<List<Field>> fieldVariants(List<Field> fields) {
        List<List<Field>> variants = List.of(List.of());
        for (Field field : fields) {
            List<Field> options = field.type.equals("[]") ? List.of(field) : fieldVariants(field);
            List<List<Field>> expanded = new ArrayList<>();
            for (List<Field> variant : variants) for (Field option : options) {
                List<Field> copy = new ArrayList<>(variant);
                copy.add(option);
                expanded.add(copy);
            }
            variants = expanded;
        }
        return variants;
    }

    private static List<Field> fieldVariants(Field field) {
        if (!field.alternatives.isEmpty()) {
            List<Field> variants = new ArrayList<>();
            for (Value value : field.alternatives) {
                Field alternative = new Field(field.name, value.type, field.detail, value.children, value.alternatives, value.enumValues);
                variants.addAll(fieldVariants(alternative));
            }
            return variants;
        }
        if (field.type.equals("<object>")) {
            List<Field> variants = new ArrayList<>();
            for (List<Field> children : fieldVariants(field.children)) {
                variants.add(new Field(field.name, field.type, field.detail, children));
            }
            return variants;
        }
        return List.of(field);
    }

    private static void collectEnumFields(Field field, String path, List<String> result) {
        if (!field.enumValues.isEmpty()) result.add("- `" + path + "`: " + field.enumValues.stream().map(value -> "`" + value + "`").collect(java.util.stream.Collectors.joining(", ")));
        for (Field child : field.children) collectEnumFields(child, path + "." + child.name, result);
        for (Value alternative : field.alternatives) for (Field child : alternative.children) collectEnumFields(child, path + "." + child.name, result);
    }

    record Field(String name, String type, String detail, List<Field> children, List<Value> alternatives, List<String> enumValues) {
        Field(String name, String type, String detail, List<Field> children) { this(name, type, detail, children, List.of(), List.of()); }
        Field(String name, String type, String detail, List<Field> children, List<Value> alternatives) { this(name, type, detail, children, alternatives, List.of()); }

        String render(int indent, boolean trailingComma) {
            String padding = " ".repeat(indent);
            if (name.isEmpty()) return padding + type + (trailingComma ? "," : "");
            StringBuilder result = new StringBuilder(padding).append("\"").append(name).append("\": ");
            if (!children.isEmpty() && type.equals("<object>")) {
                result.append("{\n");
                for (int index = 0; index < children.size(); index++) {
                    result.append(children.get(index).render(indent + 2, index < children.size() - 1));
                    if (index < children.size() - 1) result.append('\n');
                }
                result.append("\n").append(padding).append("}");
            } else if (type.equals("[]")) {
                result.append("[\n");
                List<String> values = new ArrayList<>();
                for (Value alternative : alternatives) values.addAll(renderValues(alternative, indent + 2));
                for (int index = 0; index < values.size(); index++) {
                    result.append(values.get(index));
                    if (index < values.size() - 1) result.append(',');
                    result.append('\n');
                }
                result.append(padding).append(']');
            } else {
                result.append(type);
            }
            if (trailingComma) result.append(',');
            if (!detail.isEmpty()) result.append(" // ").append(detail);
            return result.toString();
        }

        private static List<String> renderValues(Value value, int indent) {
            if (!value.alternatives.isEmpty()) {
                List<String> rendered = new ArrayList<>();
                for (Value alternative : value.alternatives) rendered.addAll(renderValues(alternative, indent));
                return rendered;
            }
            if (value.type.equals("<object>")) {
                List<List<Field>> variants = List.of(List.of());
                for (Field field : value.children) {
                    List<Field> options = field.alternatives.isEmpty()
                            ? List.of(field)
                            : field.alternatives.stream().map(option -> new Field(field.name, option.type, field.detail, option.children, option.alternatives, option.enumValues)).toList();
                    List<List<Field>> expanded = new ArrayList<>();
                    for (List<Field> variant : variants) for (Field option : options) {
                        List<Field> copy = new ArrayList<>(variant);
                        copy.add(option);
                        expanded.add(copy);
                    }
                    variants = expanded;
                }
                List<String> rendered = new ArrayList<>();
                for (List<Field> fields : variants) {
                    StringBuilder object = new StringBuilder(" ".repeat(indent)).append("{\n");
                    for (int i = 0; i < fields.size(); i++) {
                        object.append(fields.get(i).render(indent + 2, i < fields.size() - 1));
                        if (i < fields.size() - 1) object.append('\n');
                    }
                    object.append('\n').append(" ".repeat(indent)).append('}');
                    rendered.add(object.toString());
                }
                return rendered;
            }
            return List.of(" ".repeat(indent) + value.type);
        }
    }

    record Value(String type, String constraint, List<Field> children, List<Value> alternatives, List<String> enumValues) {
        static Value of(String type) { return new Value(type, "", List.of(), List.of(), List.of()); }
        static Value withConstraint(String type, String constraint) { return new Value(type, constraint, List.of(), List.of(), List.of()); }
        static Value range(String type, String range) { return new Value(type, "range: `" + range + "`", List.of(), List.of(), List.of()); }
        static Value object(List<Field> children) { return new Value("<object>", "", children, List.of(), List.of()); }
        static Value stringEnum(List<String> values) { return new Value("<string>", "", List.of(), List.of(), values); }
        static Value list(Value element) { return new Value("[]", "", List.of(), element.alternatives.isEmpty() ? List.of(element) : element.alternatives, List.of()); }
        static Value choice(Value first, Value second) {
            List<Value> choices = new ArrayList<>();
            choices.addAll(first.alternatives.isEmpty() ? List.of(first) : first.alternatives);
            choices.addAll(second.alternatives.isEmpty() ? List.of(second) : second.alternatives);
            return new Value("", "", List.of(), choices, List.of());
        }
        static Value choice(Value first, Value second, String constraint) {
            Value choice = choice(first, second);
            return new Value(choice.type, constraint, choice.children, choice.alternatives, choice.enumValues);
        }
        static Value choice(Value first, Value second, Value third) {
            return choice(choice(first, second), third);
        }
    }
}
