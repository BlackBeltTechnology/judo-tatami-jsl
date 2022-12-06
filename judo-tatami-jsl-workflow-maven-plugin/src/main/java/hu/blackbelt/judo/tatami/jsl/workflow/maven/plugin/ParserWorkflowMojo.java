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
import hu.blackbelt.judo.meta.jsl.runtime.JslStreamSource;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflow;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSave;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSetupParameters;
import hu.blackbelt.judo.tatami.jsl.workflow.WorkflowHelper;
import lombok.SneakyThrows;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.*;
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mojo(name = "parser-workflow",
		defaultPhase = LifecyclePhase.COMPILE,
		requiresDependencyResolution = ResolutionScope.COMPILE)
public class ParserWorkflowMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	public MavenProject project;

	@Component
	public RepositorySystem repoSystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	public RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
	public List<RemoteRepository> repositories;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	public PluginDescriptor pluginDescriptor;

	@Parameter(property = "sources", defaultValue = "${project.basedir}/src/main/resources/model")
	public List<String> sources;

	@Parameter(property = "modelNames")
	public List<String> modelNames;

	@Parameter(property = "boolean", defaultValue = "false")
	public Boolean useDependencies = false;

	@Parameter(property = "createSdkJar", defaultValue = "false")
	public Boolean createSdkJar = false;

	@Parameter(property = "compileSdk", defaultValue = "false")
	public Boolean compileSdk = false;

	@Parameter(property = "sdkOutputDirectory")
	private File sdkOutputDirectory = null;

	@Parameter(property = "sdkPackagePrefix")
	private String sdkPackagePrefix = null;

	@Parameter(property = "sdkAddSourceToJar", defaultValue = "true")
	private Boolean sdkAddSourceToJar = true;

	@Parameter(property = "generateSdk", defaultValue = "true")
	private Boolean generateSdk = true;

	@Parameter(property = "generateSdkInternal", defaultValue = "true")
	private Boolean generateSdkInternal = true;

	@Parameter(property = "generateSdkGuice", defaultValue = "false")
	private Boolean generateSdkGuice = false;

	@Parameter(property = "generateSdkSpring", defaultValue = "false")
	private Boolean generateSdkSpring = false;

	@Parameter(property = "generateOptionalTypes", defaultValue = "true")
	private Boolean generateOptionalTypes = true;

	@Parameter(property = "ignorePsm2Asm", defaultValue = "false")
	public Boolean ignorePsm2Asm = false;

	@Parameter(property = "ignorePsm2AsmTrace", defaultValue = "true")
	public Boolean ignorePsm2AsmTrace = true;

	@Parameter(property = "ignorePsm2Measure", defaultValue = "false")
	public Boolean ignorePsm2Measure = false;

	@Parameter(property = "ignorePsm2MeasureTrace", defaultValue = "true")
	public Boolean ignorePsm2MeasureTrace = true;

	@Parameter(property = "ignoreAsm2Rdbms", defaultValue = "false")
	public Boolean ignoreAsm2Rdbms = false;

	@Parameter(property = "ignoreAsm2RdbmsTrace", defaultValue = "false")
	public Boolean ignoreAsm2RdbmsTrace = false;

	@Parameter(property = "ignoreRdbms2Liquibase", defaultValue = "false")
	public Boolean ignoreRdbms2Liquibase = false;

	@Parameter(property = "ignoreAsm2sdk", defaultValue = "false")
	public Boolean ignoreAsm2sdk = false;

	@Parameter(property = "ignoreAsm2Expression", defaultValue = "false")
	public Boolean ignoreAsm2Expression = false;

	@Parameter(property = "destination", defaultValue = "${project.basedir}/target/generated-sources/model")
	public File destination;

	@Parameter(property = "modelVersion", defaultValue = "${project.version}")
	public String modelVersion;

	@Parameter(property = "dialects", defaultValue = "hsqldb,postgresql")
	public List<String> dialects;

	@Parameter(property = "runInParallel", defaultValue = "true")
	public Boolean runInParallel = true;

	@Parameter(property = "enableMetrics", defaultValue = "true")
	public Boolean enableMetrics = true;

	@Parameter(property = "ignoreJsl2Psm", defaultValue = "false")
	public Boolean ignoreJsl2Psm = false;

	@Parameter(property = "ignoreJsl2PsmTrace", defaultValue = "true")
	public Boolean ignoreJsl2PsmTrace = true;

	@Parameter(property = "validateModels", defaultValue = "false")
	public Boolean validateModels = false;

	@Parameter(property = "saveModels", defaultValue = "true")
	public Boolean saveModels = true;

	Set<URL> classPathUrls = new HashSet<>();

	public ParserWorkflowMojo() {
	}

	private void setContextClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
		// Project dependencies
		for (Object mavenCompilePath : project.getCompileClasspathElements()) {
			String currentPathProcessed = (String) mavenCompilePath;
			classPathUrls.add(new File(currentPathProcessed).toURI().toURL());
		}

		// Add plugin defined dependencies
		for (Artifact artifact : pluginDescriptor.getArtifacts()) {
			classPathUrls.add(getArtifactFile(artifact));
		}

		// Plugin dependencies
		final ClassRealm classRealm = pluginDescriptor.getClassRealm();
		classPathUrls.addAll(Arrays.asList(classRealm.getURLs()));

		URL[] urlsForClassLoader = classPathUrls.toArray(new URL[0]);
		getLog().debug("Set urls for URLClassLoader: " + Arrays.asList(urlsForClassLoader));

		// need to define parent classloader which knows all dependencies of the plugin
		ClassLoader classLoader = new URLClassLoader(urlsForClassLoader, ParserWorkflowMojo.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	@SneakyThrows({MalformedURLException.class})
	private URL getArtifactFile(Artifact artifact) {
		return artifact.getFile().toURI().toURL();
	}


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		ArtifactResolver artifactResolver = ArtifactResolver.builder()
				.log(getLog())
				.project(project)
				.repoSession(repoSession)
				.repositories(repositories)
				.repoSystem(repoSystem)
				.build();

		// Needed for to access project's dependencies.
		// Info: http://blog.chalda.cz/2018/02/17/Maven-plugin-and-fight-with-classloading.html
		try {
			setContextClassLoader();
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to set classloader", e);
		}

		Collection<String> sourceFiles = new ArrayList<>();

		Pattern searchPattern = Pattern.compile(".*\\.jsl$");
		if (useDependencies) {
			sourceFiles.addAll(ResourceList.getResources(getLog(), classPathUrls, searchPattern ));
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

		List<File> jslFiles = new ArrayList<>();

		for (String sourceUrl : sourceFiles) {
			try {
				jslFiles.add(artifactResolver.getArtifact(sourceUrl));
			} catch (Exception e) {
				getLog().error("Could not add JSL file: " + sourceUrl);
			}
		}

		if (jslFiles.isEmpty()) {
			getLog().warn("No JSL files presented to process");
		}

		XtextResourceSet resourceSet = JslParser.loadJslFromFile(jslFiles);
		Collection<ModelDeclaration> allModelDeclcarations = JslParser.getAllModelDeclarationFromXtextResourceSet(resourceSet);
		final List<Issue> errors = new ArrayList<>();

		for (ModelDeclaration modelDeclaration : allModelDeclcarations) {
			boolean process = true;
			if (modelNames != null && modelNames.size() > 0) {
				process = modelNames.contains(modelDeclaration.getName());
			}
			if (process) {
				XtextResource jslResource = (XtextResource) modelDeclaration.eResource();
				final IResourceValidator validator = jslResource.getResourceServiceProvider().getResourceValidator();
				errors.addAll(validator.validate(jslResource, CheckMode.ALL, CancelIndicator.NullImpl)
						.stream().filter(i -> i.getSeverity() == Severity.ERROR).collect(Collectors.toList()));
			}
		}

		try {
			if (errors.size() > 0) {
				throw new JslParseException(errors);
			}
		} catch (JslParseException e) {
			throw new MojoExecutionException("Model errors", e);
		}

		for (ModelDeclaration modelDeclaration : allModelDeclcarations) {
			boolean process = true;
			if (modelNames != null && modelNames.size() > 0) {
				process = modelNames.contains(modelDeclaration.getName());
			}

			if (process) {
				String modelName = modelDeclaration.getName();
				JslDslModel jslDslModel = JslParser.getModelFromStreamSources(
						modelName,
						JslParser.collectReferencedModelDeclarations(modelDeclaration, allModelDeclcarations).stream().map(d -> d.eResource().getURI())
								.map(s -> new File(s.toString()).toURI())
								.map(s -> {
									try {
										return new JslStreamSource(s.toURL().openStream(), org.eclipse.emf.common.util.URI.createURI("platform:/" + s.getPath()));
									} catch (IOException e) {
										throw new RuntimeException("Could not open stream: " + s);
									}
								})
								.collect(Collectors.toList()));

				DefaultWorkflow defaultWorkflow;
				File sdkOutputDirectory = this.sdkOutputDirectory;
				if (destination != null && this.sdkOutputDirectory == null) {
					sdkOutputDirectory = new File(new File(destination, "sdk"), modelName.replaceAll("::", "_"));
					sdkOutputDirectory.mkdirs();
				}

				String packagePrefix = sdkPackagePrefix;
				if (packagePrefix == null) {
					packagePrefix = "";
				} else if (!packagePrefix.endsWith(".")) {
					packagePrefix = packagePrefix + ".";
				}
				packagePrefix = packagePrefix + modelName.replaceAll("::", ".").toLowerCase() + ".";

				DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder parameters =
						DefaultWorkflowSetupParameters
								.defaultWorkflowSetupParameters()
								.modelVersion(modelVersion)
								.runInParallel(runInParallel)
								.enableMetrics(enableMetrics)
								.ignoreJsl2Psm(ignoreJsl2Psm)
								.ignorePsm2Asm(ignorePsm2Asm)
								.ignorePsm2Measure(ignorePsm2Measure)
								.ignoreAsm2Rdbms(ignoreAsm2Rdbms)
								.ignoreAsm2sdk(ignoreAsm2sdk)
								.ignoreAsm2Expression(ignoreAsm2Expression)
								.ignoreRdbms2Liquibase(ignoreRdbms2Liquibase)
								.ignorePsm2AsmTrace(ignorePsm2AsmTrace)
								.ignoreAsm2RdbmsTrace(ignoreAsm2RdbmsTrace)
								.ignorePsm2MeasureTrace(ignorePsm2MeasureTrace)
								.ignoreJsl2PsmTrace(ignoreJsl2PsmTrace)
								.validateModels(validateModels)
								.modelName(modelName)
								.dialectList(dialects)
								.compileSdk(compileSdk)
								.createSdkJar(createSdkJar)
								.sdkOutputDirectory(sdkOutputDirectory)
								.sdkPackagePrefix(packagePrefix)
								.addSourceToJar(sdkAddSourceToJar)
								.generateSdk(generateSdk)
								.generateInternal(generateSdkInternal)
								.generateGuice(generateSdkGuice)
								.generateSpring(generateSdkSpring)
								.generateOptionalTypes(generateOptionalTypes);

				defaultWorkflow = new DefaultWorkflow(parameters);

				WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

				workflowHelper.loadJslModel(modelName, jslDslModel, null);

				Exception error = null;
				try {
					defaultWorkflow.startDefaultWorkflow();
				} catch (IllegalStateException e) {
					error = e;
				}

				if (destination != null && (error != null || saveModels)) {
					destination.mkdirs();
					try {
						DefaultWorkflowSave.saveModels(defaultWorkflow.getTransformationContext(), destination, dialects);
					} catch (Exception e) {
						if (error != null) {
							throw new MojoFailureException("An error occurred during the execution phase of the workflow.", error);
						}
						throw new MojoFailureException("An error occurred during the saving phase of the workflow.", e);
					}
				}
				if (error != null) {
					throw new MojoFailureException("An error occurred during the execution phase of the workflow.", error);
				}
			}
		}
	}
}
