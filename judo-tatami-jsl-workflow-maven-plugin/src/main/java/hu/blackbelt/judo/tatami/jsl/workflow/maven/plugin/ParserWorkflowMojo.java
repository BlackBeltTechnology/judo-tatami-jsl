package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

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

	final int BUFFER_SIZE = 4096;

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Component
	public RepositorySystem repoSystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	public RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
	public List<RemoteRepository> repositories;

	@Parameter(defaultValue = "${plugin}", readonly = true)
	private PluginDescriptor pluginDescriptor;

	@Parameter(property = "sources")
	private List<String> sources;

	@Parameter(property = "modelNames")
	private List<String> modelNames;

	@Parameter(property = "sdkPackagePrefix")
	private String sdkPackagePrefix;

	@Parameter(property = "boolean", defaultValue = "false")
	private Boolean useDependencies = false;

	@Parameter(property = "parseOnly", defaultValue = "false")
	private Boolean parseOnly = false;

	@Parameter(property = "boolean", defaultValue = "false")
	private Boolean compressSdk = false;

	@Parameter(property = "boolean", defaultValue = "false")
	private Boolean compileSdk = false;

	@Parameter(property = "ignorePsm2Asm", defaultValue = "false")
	private Boolean ignorePsm2Asm = false;

	@Parameter(property = "ignorePsm2AsmTrace", defaultValue = "true")
	private Boolean ignorePsm2AsmTrace = true;

	@Parameter(property = "ignorePsm2Measure", defaultValue = "false")
	private Boolean ignoreAsm2Measure = false;

	@Parameter(property = "ignorePsm2MeasureTrace", defaultValue = "true")
	private Boolean ignorePsm2MeasureTrace = true;

	@Parameter(property = "ignoreAsm2Rdbms", defaultValue = "false")
	private Boolean ignoreAsm2Rdbms = false;

	@Parameter(property = "ignoreAsm2RdbmsTrace", defaultValue = "false")
	private Boolean ignoreAsm2RdbmsTrace = false;

	@Parameter(property = "ignoreRdbms2Liquibase", defaultValue = "false")
	private Boolean ignoreRdbms2Liquibase = false;

	@Parameter(property = "ignoreAsm2sdk", defaultValue = "false")
	private Boolean ignoreAsm2sdk = false;

	@Parameter(property = "ignoreAsm2Expression", defaultValue = "false")
	private Boolean ignoreAsm2Expression = false;

	@Parameter(property = "destination", defaultValue = "${project.basedir}/target/model")
	private File destination;

	@Parameter(property = "modelVersion", defaultValue = "${project.version}")
	private String modelVersion;

	@Parameter(property = "dialectList")
	private List<String> dialectList;

	@Parameter(property = "runInParallel", defaultValue = "true")
	private Boolean runInParallel = true;

	@Parameter(property = "enableMetrics", defaultValue = "true")
	private Boolean enableMetrics = true;

	@Parameter(property = "ignoreJsl2Psm", defaultValue = "false")
	private Boolean ignoreJsl2Psm = false;

	@Parameter(property = "ignoreJsl2PsmTrace", defaultValue = "true")
	private Boolean ignoreJsl2PsmTrace = true;

	@Parameter(property = "validateModels", defaultValue = "false")
	private Boolean validateModels = false;

	@Parameter(property = "saveModels", defaultValue = "true")
	private Boolean saveModels = true;

	Set<URL> classPathUrls = new HashSet<>();

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
		for (URL url : classRealm.getURLs()) {
			classPathUrls.add(url);
		}

		URL[] urlsForClassLoader = classPathUrls.toArray(new URL[classPathUrls.size()]);
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
				} catch (MalformedURLException e) {
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

		JslParser jslParser = new JslParser();
		XtextResourceSet resourceSet = jslParser.loadJslFromFile(jslFiles);
		Collection<ModelDeclaration> allModelDeclcarations = jslParser.getAllModelDeclarationFromXtextResourceSet(resourceSet);
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
				JslDslModel jslDslModel = jslParser.getModelFromStreamSources(
						modelName,
						jslFiles.stream()
								.map(f -> f.toURI())
								.map(s -> {
									try {
										return new JslStreamSource(s.toURL().openStream(), org.eclipse.emf.common.util.URI.createURI("platform:/" + s.getPath()));
									} catch (IOException e) {
										throw new RuntimeException("Could not open stream: " + s.toString());
									}
								})
								.collect(Collectors.toList()));

				DefaultWorkflow defaultWorkflow;
				File sdkOutputDirectory = null;
				if (destination != null) {
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
								.compileSdk(compileSdk)
								.compressSdk(compressSdk)
								.sdkOutputDirectory(sdkOutputDirectory)
								.sdkPackagePrefix(packagePrefix)
								.runInParallel(runInParallel)
								.enableMetrics(enableMetrics)
								.ignoreJsl2Psm(ignoreJsl2Psm || parseOnly)
								.ignorePsm2Asm(ignorePsm2Asm)
								.ignoreAsm2Rdbms(ignoreAsm2Rdbms)
								.ignoreAsm2sdk(ignoreAsm2sdk)
								.ignoreAsm2Expression(ignoreAsm2Expression)
								.ignoreRdbms2Liquibase(ignoreRdbms2Liquibase)
								.ignorePsm2AsmTrace(ignorePsm2AsmTrace)
								.ignoreAsm2RdbmsTrace(ignoreAsm2RdbmsTrace)
								.ignorePsm2MeasureTrace(ignorePsm2MeasureTrace)
								.validateModels(validateModels)
								.modelName(modelName)
								.dialectList(dialectList);

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
						DefaultWorkflowSave.saveModels(defaultWorkflow.getTransformationContext(), destination, dialectList);
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
