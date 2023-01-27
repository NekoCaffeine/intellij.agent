package intellij.agent.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objectweb.asm.commons.ClassRemapper;

import amadeus.maho.lang.AccessLevel;
import amadeus.maho.lang.FieldDefaults;
import amadeus.maho.lang.SneakyThrows;
import amadeus.maho.util.build.Distributive;
import amadeus.maho.util.build.IDEA;
import amadeus.maho.util.build.Jar;
import amadeus.maho.util.build.Javac;
import amadeus.maho.util.build.Module;
import amadeus.maho.util.build.Workspace;
import amadeus.maho.util.bytecode.ASMHelper;
import amadeus.maho.util.bytecode.ClassWriter;
import amadeus.maho.util.bytecode.remap.RemapHandler;
import amadeus.maho.util.depend.Project;
import amadeus.maho.util.depend.Repository;
import amadeus.maho.util.runtime.FileHelper;

@SneakyThrows
public interface Build {
    
    @FieldDefaults(level = AccessLevel.PUBLIC)
    class IntellijConfig {
        
        // install location, usually if you installed IntelliJ using Toolbox, you can find that path inside
        String intellijPath = "missing";
        
        String intellijAgentPath = "missing";
        
        String intellijDebugAgentPath = "missing";
        
    }
    
    Workspace workspace = Workspace.here();
    
    IntellijConfig config = workspace.config().load(new IntellijConfig()).let(it -> {
        {
            if (!Files.isDirectory(Path.of(it.intellijPath)))
                throw new IllegalArgumentException("IntellijConfig.default.cfg # invalid intellijPath: " + it.intellijPath);
        }
    });
    
    Repository maven = Repository.maven();
    
    Set<Module.Dependency> asm = maven.resolveModuleDependencies(new Project.Dependency.Holder().all("org.ow2.asm:asm:+").dependencies());
    
    // avoid parsing unneeded libraries, thus improving compilation speed
    List<String> shouldInCompile = Stream.of("3rd-party-rt", "app", "platform-api", "platform-impl", "util").map(name -> name + Jar.SUFFIX).collect(Collectors.toList());
    
    static Set<Module.Dependency> dependencies() = IDEA.DevKit.attachLocalInstance(Path.of(config.intellijPath), Set.of(), path -> shouldInCompile.contains(path.getFileName().toString())) *= asm;
    
    Module module = { "intellij.agent", dependencies() };
    
    Jar.ClassPath classPath = { asm.stream().flatMap(Module.Dependency::flat).collect(Collectors.toSet()) };
    
    List<Path> useModulePath = classPath.dependencies().stream().map(Module.SingleDependency::classes).collect(Collectors.toList());
    
    static void sync() {
        IDEA.deleteLibraries(workspace);
        IDEA.generateAll(workspace, "17", false, List.of(Module.build(), module));
    }
    
    static Path build() {
        workspace.clean(module).flushMetadata();
        Javac.compile(workspace, module, useModulePath::contains, args -> Javac.addReadsAllUnnamed(args, module));
        final Path classes = workspace.output(Javac.CLASSES_DIR, module), shadow = classes / module.name() / module.name().replace('.', '/');
        final RemapHandler.ASMRemapper remapper = new RemapHandler() {
            @Override
            public String mapInternalName(final String name) = name.startsWith("org/objectweb/asm") ? name.replace("org/objectweb/asm", "intellij/agent/org/objectweb/asm") : name;
        }.remapper();
        final BiConsumer<Path, UnaryOperator<Path>> consumer = (root, mapper) -> Files.walkFileTree(root, FileHelper.visitor(
                (path, _) -> ClassWriter.toBytecode(visitor -> ASMHelper.newClassReader(Files.readAllBytes(path)).accept(new ClassRemapper(visitor, remapper), 0)) >> mapper.apply(path).let(it -> ~-it),
                (path, _) -> path.toString().endsWith(Javac.CLASS_SUFFIX)));
        consumer.accept(classes, UnaryOperator.identity());
        asm.stream().flatMap(Module.Dependency::flat).forEach(dependency -> dependency.classes() | root -> consumer.accept(root, path -> shadow / (root % path).toString()));
        final Map<String, Jar.Result> pack = Jar.pack(workspace, module, Jar.manifest(null, new Jar.Agent("intellij.agent.Agent"), classPath), Jar.WITHOUT_VERSION_FORMATTER);
        return Distributive.zip(workspace, module, root -> {
            classPath.copyTo(root);
            pack.values().forEach(result -> result.modules() >> root);
        });
    }
    
    static void push(final boolean debug = true) {
        final Path build = build();
        final Path agentDir = Path.of(debug ? config.intellijDebugAgentPath : config.intellijAgentPath);
        if (Files.isDirectory(agentDir)) {
            --agentDir;
            build | root -> root >> agentDir;
        } else
            throw new IllegalArgumentException("IntellijAgentConfig.default.cfg # invalid intellijPath: " + config.intellijAgentPath);
    }
    
}
