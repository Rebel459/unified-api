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
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
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

    @TaskAction
    public void generate() throws IOException {
        Generator generator = new Generator(getSourceDirectory().get().getAsFile().toPath());
        List<Registration> registrations = generator.registrations();
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
                markdown.append("### `").append(registration.id).append("`\n\n```json5\n");
                markdown.append("{\n  \"type\": \"").append(registration.id).append("\"");
                for (Field field : registration.fields) markdown.append(",\n  ").append(field.render());
                markdown.append("\n}\n```\n\n");
            }
            Files.writeString(output.resolve(category.getKey() + ".md"), markdown.toString());
        }
        getLogger().lifecycle("Generated {} codec documentation entries in {} categories at {}", registrations.size(), categories.size(), output);
    }

    static final class Generator {
        private final Path root;
        private final List<CompilationUnit> units = new ArrayList<>();
        private final Map<String, Expression> declarations = new HashMap<>();
        private final Map<String, List<Expression>> simpleDeclarations = new HashMap<>();
        private final Map<String, Expression> fileDeclarations = new HashMap<>();
        private final List<Registration> found = new ArrayList<>();

        Generator(Path root) throws IOException {
            this.root = root;
            StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
            try (Stream<Path> files = Files.walk(root)) {
                for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                    try { units.add(StaticJavaParser.parse(file)); }
                    catch (RuntimeException error) { throw new GradleException("Could not parse Java source " + file + ": " + error.getMessage(), error); }
                }
            }
            indexDeclarations();
        }

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
                    if (args.size() > 1) detail = (optional ? "optional, " : "") + "defaults to `" + args.get(1) + "`";
                    else if (optional) detail = "optional";
                    if (!value.constraint.isEmpty()) detail = detail.isEmpty() ? value.constraint : detail + ", " + value.constraint;
                    return List.of(new Field(name.asString(), value.type, detail, value.children));
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
                    return List.of(new Field("", "[" + element.type + "]", "", element.children));
                }
            }
            Value value = value(expression, visiting, source, id);
            if (!value.children.isEmpty()) return value.children;
            throw unresolved(id, source, expression);
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
                if (method.equals("named")) return Value.of("<string>");
                if (method.equals("codec") && text.startsWith("MultiColored.codec") && args.size() == 2) return Value.object(List.of(
                        new Field(((StringLiteralExpr) args.get(0)).asString(), "<string>", "", List.of()),
                        new Field(((StringLiteralExpr) args.get(1)).asString(), "<string>", "", List.of())));
                if (method.equals("codec") && text.startsWith("IntProviders.")) return Value.range("<int>", args.get(0) + "` to `" + args.get(1));
                if (method.equals("conditional") && text.startsWith("UnifiedCodecs.")) return Value.object(List.of(
                        new Field("predicate", "<codec>", "", List.of()),
                        new Field("if_true", "<codec>", "", List.of()),
                        new Field("if_false", "<codec>", "", List.of())));
                if (method.equals("lazyInitialized")) return Value.of("<codec>");
                if (method.equals("homogeneousList")) return Value.of("[<identifier>]");
                if (method.equals("list") || method.equals("listOf")) {
                    Expression element = method.equals("list") ? args.get(0) : call.getScope().orElseThrow();
                    return Value.of("[" + value(element, visiting, source, id).type + "]");
                }
                if (method.equals("unboundedMap")) return Value.of("{" + value(args.get(0), visiting, source, id).type + ": " + value(args.get(1), visiting, source, id).type + "}");
                if (method.equals("intRange")) return Value.range("<int>", args.get(0) + "` to `" + args.get(1));
                if (method.equals("floatRange")) return Value.range("<float>", args.get(0) + "` to `" + args.get(1));
                if (method.equals("either")) {
                    String first = value(args.get(0), visiting, source, id).type;
                    String second = value(args.get(1), visiting, source, id).type;
                    Set<String> alternatives = new LinkedHashSet<>();
                    for (String alternative : List.of(first, second)) {
                        String flattened = alternative.replace("<", "").replace(">", "");
                        for (String member : flattened.split(" or ")) alternatives.add(member.trim());
                    }
                    return Value.of("<" + String.join(" or ", alternatives) + ">");
                }
                if (method.equals("codec") && text.startsWith("TagKey.")) return Value.of("<identifier>");
                if (method.equals("codec") && text.startsWith("ResourceKey.")) return Value.of("<identifier>");
                if (method.equals("flatXmap") || method.equals("xmap") || method.equals("validate") || method.equals("comapFlatMap")) return value(call.getScope().orElseThrow(), visiting, source, id);
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
                if (initializer == null && owner.equals("BlockState")) return Value.of("<object>");
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
                if (leaf.equals("CODEC") && owner.equals("ItemStack")) return Value.of("<object>");
                if (leaf.equals("CODEC") && Set.of("Direction", "DyeColor", "Biome.Precipitation", "CreativeModeTab.TabVisibility", "WeatheringCopper.WeatherState").contains(owner)) return Value.of("<string>");
                if (leaf.equals("CODEC") && owner.equals("SkullBlock.Type")) return Value.of("<string>");
                if (leaf.equals("CODEC") && owner.equals("TreeGrower")) return Value.of("<string>");
                if (leaf.equals("CODEC") && owner.equals("ParticleTypes")) return Value.of("<identifier>");
                if (leaf.equals("CODEC") && owner.equals("SuspiciousStewEffects")) return Value.of("[<object>]");
                if (leaf.equals("CODEC") && owner.equals("CauldronInteractions")) return Value.of("{<string>: <object>}");
                if (Set.of("CODEC", "VALUE_MAP_CODEC").contains(leaf)) {
                    if (key.contains("DataComponent")) return Value.of("{<identifier>: <object>}");
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

    record Field(String name, String type, String detail, List<Field> children) {
        String render() {
            if (name.isEmpty()) return type;
            StringBuilder result = new StringBuilder("\"").append(name).append("\": ").append(type);
            if (!detail.isEmpty()) result.append(" // ").append(detail);
            return result.toString();
        }
    }

    record Value(String type, String constraint, List<Field> children) {
        static Value of(String type) { return new Value(type, "", List.of()); }
        static Value withConstraint(String type, String constraint) { return new Value(type, constraint, List.of()); }
        static Value range(String type, String range) { return new Value(type, "range: `" + range + "`", List.of()); }
        static Value object(List<Field> children) { return new Value("<object>", "", children); }
    }
}
