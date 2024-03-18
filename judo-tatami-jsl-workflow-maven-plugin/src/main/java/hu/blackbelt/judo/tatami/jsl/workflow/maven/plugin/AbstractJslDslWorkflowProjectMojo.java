package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParseException;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.loadJslDslModel;

public abstract class AbstractJslDslWorkflowProjectMojo extends AbstractMojo {

    public static final String TEMPLATES_BACKEND_PROJECT = "templates/backend-project";

    final int BUFFER_SIZE = 4096;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    public MavenProject project;

    @Component
    public RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    public RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    public List<RemoteRepository> repositories;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor pluginDescriptor;

    @Parameter(property = "actors")
    private List<String> actors;

    @Parameter(property = "scanSources", defaultValue = "false")
    public Boolean scanSources = false;

    @Parameter(property = "srcModelTarget", defaultValue = "${project.basedir}/target/classes/model")
    public File srcModelTarget;

    @Parameter(property = "sources", defaultValue = "${project.basedir}/src/main/model")
    public List<String> sources;

    @Parameter(property = "modelNames")
    public List<String> modelNames;

    @Parameter(property = "jsl")
    public String jsl;

    Set<URL> classPathUrls = new HashSet<>();

    ArtifactResolver artifactResolver;


    public abstract void performExecution(JslDslModel jslModel) throws MojoExecutionException, MojoFailureException;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        artifactResolver = ArtifactResolver.builder()
                .log(getLog())
                .project(project)
                .repoSession(repoSession)
                .repositories(repositories)
                .repoSystem(repoSystem)
                .build();

        // Needed for to access project's dependencies.
        // Info: http://blog.chalda.cz/2018/02/17/Maven-plugin-and-fight-with-classloading.html
        try {
            setContextClassLoader(artifactResolver);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to set classloader", e);
        }
        JslDslModel jslDslModel;

        if (jsl == null || jsl.trim().isBlank()) {
            List<File> jslFiles = resolveJslFiles(artifactResolver);
            if (jslFiles.isEmpty()) {
                getLog().warn("No JSL files presented to process");
            }

            XtextResourceSet resourceSet = JslParser.loadJslFromFile(jslFiles);
            Collection<ModelDeclaration> allModelDeclcarations = JslParser.getAllModelDeclarationFromXtextResourceSet(resourceSet);

            Collection<String> effectiveModelsNames = calculateEffectiveModelDeclarations(allModelDeclcarations);
            Collection<ModelDeclaration> effectiveModels = allModelDeclcarations.stream().filter(m -> effectiveModelsNames.contains(m.getName())).collect(Collectors.toList());

            if (effectiveModels.size() > 1) {
                throw new MojoExecutionException("Multiple root models are defined, please remove or define the correct ones in 'modelNames' -  "
                        + effectiveModels.stream().map(m -> m.getName()).collect(Collectors.joining()));
            }

            if (effectiveModels.size() == 0) {
                throw new MojoExecutionException("No model found");
            }

            checkErrors(effectiveModels);

            ModelDeclaration modelDeclaration = effectiveModels.stream().findFirst().get();
            jslDslModel = JslParser.getModelFromXtextResourceSet(modelDeclaration.getName(), resourceSet);

            if (srcModelTarget != null) {
                srcModelTarget.mkdirs();
                try {
                    jslDslModel.saveJslDslModel(JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder()
                                    .file(new File(srcModelTarget, modelDeclaration.getName() + "-jsl.model"))
                            .build());
                } catch (IOException e) {
                    throw new MojoExecutionException("Could not save model: ", e);
                } catch (JslDslModel.JslDslValidationException e) {
                    throw new MojoExecutionException("Model validation error", e);
                }
            }
        } else {
            try {
                jslDslModel = loadJslDslModel(jslDslLoadArgumentsBuilder()
                                .file(artifactResolver.getArtifact(jsl))
                        .build());

            } catch (IOException e) {
                throw new MojoExecutionException("IO Error: ", e);
            } catch (JslDslModel.JslDslValidationException e) {
                throw new MojoExecutionException("Model validation error", e);
            }
        }
        performExecution(jslDslModel);
    }

    private void checkErrors(Collection<ModelDeclaration> modulesToProcess) throws MojoExecutionException {
        final List<Issue> errors = new ArrayList<>();

        for (ModelDeclaration modelDeclaration : modulesToProcess) {
            XtextResource jslResource = (XtextResource) modelDeclaration.eResource();
            final IResourceValidator validator = jslResource.getResourceServiceProvider().getResourceValidator();
            errors.addAll(validator.validate(jslResource, CheckMode.ALL, CancelIndicator.NullImpl)
                    .stream().filter(i -> i.getSeverity() == Severity.ERROR).collect(Collectors.toList()));
        }

        try {
            if (errors.size() > 0) {
                throw new JslParseException(errors);
            }
        } catch (JslParseException e) {
            throw new MojoExecutionException("Model errors", e);
        }
    }


    private void setContextClassLoader(ArtifactResolver artifactResolver) throws DependencyResolutionRequiredException, MalformedURLException, MojoExecutionException {
        // Project dependencies
        for (Object mavenCompilePath : project.getCompileClasspathElements()) {
            String currentPathProcessed = (String) mavenCompilePath;
            classPathUrls.add(new File(currentPathProcessed).toURI().toURL());
        }

        // Add plugin defined dependencies
        for (Artifact artifact : pluginDescriptor.getArtifacts()) {
            classPathUrls.add(artifactResolver.getArtifactFile(artifact));
        }

        // Plugin dependencies
        final ClassRealm classRealm = pluginDescriptor.getClassRealm();
        for (URL url : classRealm.getURLs()) {
            classPathUrls.add(url);
        }

        URL[] urlsForClassLoader = classPathUrls.toArray(new URL[classPathUrls.size()]);
        getLog().debug("Set urls for URLClassLoader: " + Arrays.asList(urlsForClassLoader));

        // need to define parent classloader which knows all dependencies of the plugin
        ClassLoader classLoader = new URLClassLoader(urlsForClassLoader, AbstractJslDslWorkflowProjectMojo.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);
    }


    private List<File> resolveJslFiles(ArtifactResolver artifactResolver) throws MojoExecutionException {
        List<File> jslFiles = new ArrayList<>();

        for (String sourceUrl : getJslSourceFiles(artifactResolver)) {
            try {
                jslFiles.add(artifactResolver.getArtifact(sourceUrl));
            } catch (Exception e) {
                getLog().error("Could not add JSL file: " + sourceUrl);
            }
        }
        return jslFiles;
    }

    private Collection<String> getJslSourceFiles(ArtifactResolver artifactResolver) throws MojoExecutionException {
        Collection<String> sourceFiles = new ArrayList<>();

        Pattern searchPattern = Pattern.compile(".*\\.jsl$");
        if (scanSources) {
            sourceFiles.addAll(ResourceList.getResources(getLog(), classPathUrls, searchPattern )
                    .stream().filter(r -> !r.startsWith(srcModelTarget.getAbsolutePath()))
                    .collect(Collectors.toList()));
        }

        if (sources != null && sources.size() > 0) {
            for (String source : sources) {
                try {
                    URL artifactUrl = artifactResolver.getArtifact(source).toURI().toURL();
                    sourceFiles.addAll(ResourceList.getResources(getLog(), Set.of(artifactUrl), searchPattern));
                } catch (MalformedURLException ignored) {
                }
            }
        }
        return sourceFiles;
    }

    private Collection<String> calculateEffectiveModelDeclarations(Collection<ModelDeclaration> allModelDeclcarations) {
        List<String> effectiveModelNames = modelNames;

        if (modelNames == null || modelNames.size() == 0) {
            effectiveModelNames = JslParser.getRootModelDeclarations(allModelDeclcarations)
                    .stream().map(m -> m.getName())
                    .collect(Collectors.toList());

            getLog().info("Model names have not been defined, possible candidates: "
                    + effectiveModelNames.stream().collect(Collectors.joining(", ")));
        }

        if (!project.getProperties().containsKey("judo-model-name")) {
            getLog().info("judo-model-name property is not defined, trying to calculate.");
            if (effectiveModelNames.size() == 1) {
                project.getProperties().put("judo-model-name", effectiveModelNames.get(0));
                getLog().info("judo-model-name property set to: " + effectiveModelNames.get(0));
            } else if (effectiveModelNames.size() > 1) {
                getLog().warn("Multiple model is defined as base model: " + effectiveModelNames.stream().collect(Collectors.joining(", "))
                        + "\nPlease select one and define in src/main/resources/application.yaml with judo.modelName property");
            }
        }
        return effectiveModelNames;
    }
}
