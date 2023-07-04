package com.yolt.service.starter;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.ModelBase;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ArchTest {

    @Test
    void only_yolt_starters_may_have_spring_starters() throws IOException, XmlPullParserException {
        List<String> projectsWithStarters = allProjects()
                .stream()
                .filter(excludeArtifactIds(
                        "yolt-service-spring-boot-starter",
                        "yolt-service-spring-boot-starter-test",
                        "local-team-starter",
                        "cassandra-starter",
                        "cassandra-starter-test",
                        "client-tokens-starter",
                        "sample-app-cassandra-with-v3-client",
                        "sample-app-cassandra-with-spring-data",
                        "sample-app-cassandra-with-combined-spring-data-and-v3-client",
                        "sample-app-kafka",
                        "sample-app-kafka-client-tokens",
                        "sample-app-postgres",
                        "sample-app-reactive",
                        "sample-app-web",
                        "sample-app-web-client-tokens"
                ))
                .filter(dependsOn("-starter"))
                .map(Model::getArtifactId)
                .sorted()
                .collect(Collectors.toList());

        assertThat(projectsWithStarters).isEmpty();
    }

    @Test
    void do_not_add_to_testsupport_follow_conventions_in_readme() throws IOException, XmlPullParserException {
        List<String> testSupportModules = allProjects()
                .stream()
                .filter(model -> model.getArtifactId().equals("testsupport"))
                .map(ModelBase::getModules)
                .flatMap(Collection::stream)
                .sorted()
                .collect(Collectors.toList());

        assertThat(testSupportModules).containsExactly("testsupport-cassandra");
    }

    @Test
    void please_dont_depend_on_guava() throws IOException, XmlPullParserException {
        List<String> projectsWithGuava = allProjects()
                .stream()
                .filter(dependsOn("guava"))
                .map(Model::getArtifactId)
                .sorted()
                .collect(Collectors.toList());

        assertThat(projectsWithGuava).isEmpty();
    }

    @Test
    void please_dont_depend_on_commons_lang3() throws IOException, XmlPullParserException {
        List<String> projectsWithCommonsLang = allProjects()
                .stream()
                .filter(dependsOn("commons-lang3"))
                .map(Model::getArtifactId)
                .sorted()
                .collect(Collectors.toList());

        assertThat(projectsWithCommonsLang).isEmpty();
    }

    @Test
    void render_dependency_graph() throws IOException, XmlPullParserException {
        String dependencies = allProjects()
                .stream()
                .flatMap(model -> model.getDependencies()
                        .stream()
                        .filter(usableInProduction())
                        .map(dependencyArrow(model)))
                .sorted()
                .collect(plantUml());

        requireUptoDateGraph(dependencies, "../dependency-graph.puml");
    }

    @Test
    void render_module_graph() throws IOException, XmlPullParserException {
        String dependencies = allProjects()
                .stream()
                .flatMap(model -> model.getDependencies()
                        .stream()
                        .filter(usableInProduction())
                        .filter(isLovebirdOrYoltProject())
                        .map(dependencyArrow(model)))
                .sorted()
                .collect(plantUml());

        requireUptoDateGraph(dependencies, "../module-graph.puml");
    }

    @Test
    void render_commons_module_graph() throws IOException, XmlPullParserException {
        // only works with all projects checked out
        if (!Files.exists(Paths.get("../../../code-search"))) {
            return;
        }

        String dependencies = allProjectsIn(
                Paths.get(".."),
                Paths.get("../../../code-search/backend/yolt-shared-dtos")
        )
                .stream()
                .flatMap(model -> model.getDependencies()
                        .stream()
                        .filter(usableInProduction())
                        .filter(isLovebirdOrYoltProject())
                        .map(dependencyArrow(model)))
                .sorted()
                .collect(plantUml());

        requireUptoDateGraph(dependencies, "../commons-module-graph.puml");
    }


    @Test
    void render_providers_module_graph() throws IOException, XmlPullParserException {
        // only works with all projects checked out
        if (!Files.exists(Paths.get("../../../code-search"))) {
            return;
        }

        String dependencies = allProjectsIn(
                Paths.get("../../../code-search/backend/yolt-shared-dtos"),
                Paths.get("../../../code-search/providers")
        )
                .stream()
                .flatMap(model -> model.getDependencies()
                        .stream()
                        .filter(usableInProduction())
                        .filter(isLovebirdOrYoltProject())
                        .map(dependencyArrow(model)))
                .sorted()
                .collect(plantUml());

        requireUptoDateGraph(dependencies, "../providers-module-graph.puml");
    }

    @Test
    void render_starter_module_graph() throws IOException, XmlPullParserException {
        String dependencies = allProjectsIn(
                Paths.get("")
        )
                .stream()
                .flatMap(model -> model.getDependencies()
                        .stream()
                        .filter(usableInProduction())
//                        .filter(isLovebirdOrYoltProject())
                        .map(dependencyArrow(model)))
                .sorted()
                .collect(plantUml());

        requireUptoDateGraph(dependencies, "../starter-module-graph.puml");
    }

    private Predicate<Dependency> isLovebirdOrYoltProject() {
        return dependency -> "nl.ing.lovebird".equals(dependency.getGroupId()) || "com.yolt".equals(dependency.getGroupId());
    }

    private Collector<CharSequence, ?, String> plantUml() {
        return Collectors.joining("\n", "@startuml\n", "\n@enduml\n");
    }

    private Predicate<Model> dependsOn(String pattern) {
        return model -> model.getDependencies()
                .stream()
                .filter(usableInProduction())
                .anyMatch(dependency -> dependency.getArtifactId().contains(pattern));
    }


    private Function<Dependency, String> dependencyArrow(Model model) {
        return dependency -> "[" + fqn(model) + "] " + arrow(dependency) + " [" + fqn(dependency) + "]";
    }

    private void requireUptoDateGraph(String currentDependencies, String path) {
        Path file = Paths.get(path);
        // Updates graph and fails if different.
        // Ensures that an up to date version is always checked in.
        // This test will pass on re-run
        assertAll(
                () -> assertDoesNotThrow(() -> {
                    Files.write(file, currentDependencies.getBytes(UTF_8));
                }),
                () -> {
                    if (!Files.exists(file)) {
                        return;
                    }
                    byte[] bytes = Files.readAllBytes(file);
                    String dependencies = new String(bytes, UTF_8);
                    assertEquals(dependencies, currentDependencies);
                });
    }

    private String arrow(Dependency dependency) {
        if ("provided".equals(dependency.getScope())) {
            return "...up...>";
        }
        return "--up-->";
    }

    private String fqn(Dependency dependency) {
        return dependency.getArtifactId();
    }

    private String fqn(Model model) {
        return model.getArtifactId();
    }


    private Predicate<Dependency> usableInProduction() {
        return dependency -> !"test".equals(dependency.getScope());
    }

    private Predicate<Model> excludeArtifactIds(String... artifactIds) {
        List<String> artifacts = Arrays.asList(artifactIds);
        return model -> !artifacts.contains(model.getArtifactId());
    }

    private List<Model> allProjects() throws IOException, XmlPullParserException {
        return allProjectsIn(Paths.get(".."));
    }

    private List<Model> allProjectsIn(Path... start) throws IOException, XmlPullParserException {
        List<Model> models = new ArrayList<>();
        Finder finder = new Finder("pom.xml");

        for (Path path : start) {
            Files.walkFileTree(path, finder);
        }
        for (Path path : finder.getFiles()) {
            try (Reader r = Files.newBufferedReader(path)) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(r);
                models.add(model);

            }
        }
        return models;
    }

    static class Finder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final List<Path> files = new ArrayList<>();

        Finder(String pattern) {
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                files.add(file);
            }
            return CONTINUE;
        }

        List<Path> getFiles() {
            return files;
        }
    }
}
