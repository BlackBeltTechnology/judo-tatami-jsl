package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflow;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSave;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSetupParameters;
import hu.blackbelt.judo.tatami.jsl.workflow.WorkflowHelper;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.FileNameUtils;
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
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;

@Mojo(name = "parsed-model-workflow",
		defaultPhase = LifecyclePhase.COMPILE,
		requiresDependencyResolution = ResolutionScope.COMPILE)
public class DefaultWorkflowMojo extends AbstractMojo {

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

	@Parameter(property = "jsl")
	private String jsl;

	@Parameter(property = "psm")
	private String psm;

	@Parameter(property = "measure")
	private String measure;

	@Parameter(property = "asm")
	private String asm;

	@Parameter(property = "psm2AsmTrace")
	private String psm2AsmTrace;

	@Parameter(property = "expression")
	private String expression;

	@Parameter(property = "sdk")
	private String sdk;

	@Parameter(property = "sdkInternal")
	private String sdkInternal;

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

	@Parameter(property = "modelName")
	private String modelName;

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

	@Parameter
	private Map<String, DialectParam> dialects;

	Set<URL> classPathUrls = new TreeSet<>();

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
		ClassLoader classLoader = new URLClassLoader(urlsForClassLoader, DefaultWorkflowMojo.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	@SneakyThrows({MalformedURLException.class})
	private URL getArtifactFile(Artifact artifact) {
		return artifact.getFile().toURI().toURL();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// Needed for to access project's dependencies.
		// Info: http://blog.chalda.cz/2018/02/17/Maven-plugin-and-fight-with-classloading.html
		try {
			setContextClassLoader();
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to set classloader", e);
		}

		JslDslModel jslModel = null;
		URI jslUri = null;

		ArtifactResolver artifactResolver = ArtifactResolver.builder()
				.log(getLog())
				.project(project)
				.repoSession(repoSession)
				.repositories(repositories)
				.repoSystem(repoSystem)
				.build();

		if (jsl != null && !jsl.trim().equals("")) {
			jslUri = artifactResolver.getArtifact(jsl).toURI();

			if (modelName == null || modelName.trim().equals("")) {
				try {
					JslDslModel jslModelForName = JslDslModel.loadJslDslModel(jslDslLoadArgumentsBuilder()
							.inputStream(
									Optional.of(jslUri).orElseThrow(() ->
													new IllegalArgumentException("jslModel or jslModelSourceUri have to be defined"))
											.toURL().openStream())
							.validateModel(false)
							.name("forName"));

					modelName = getStreamOf(jslModelForName.getResourceSet(), ModelDeclaration.class)
							.findFirst().orElseThrow(() -> new IllegalStateException("Cannot find Model element")).getName();
				} catch (IOException | JslDslModel.JslDslValidationException e) {
					throw new MojoExecutionException("Could not load model: ", e);
				}
			}
		}

		DefaultWorkflow defaultWorkflow;

		DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder parameters =
				DefaultWorkflowSetupParameters
						.defaultWorkflowSetupParameters()
						.modelVersion(modelVersion)
						.runInParallel(runInParallel)
						.enableMetrics(enableMetrics)
						.ignorePsm2Asm(ignorePsm2Asm)
						.ignorePsm2Measure(ignoreAsm2Measure)
						.ignoreAsm2Rdbms(ignoreAsm2Rdbms)
						.ignoreAsm2sdk(ignoreAsm2sdk)
						.ignoreAsm2Expression(ignoreAsm2Expression)
						.ignoreRdbms2Liquibase(ignoreRdbms2Liquibase)
						.ignorePsm2AsmTrace(ignorePsm2AsmTrace)
						.ignorePsm2MeasureTrace(ignorePsm2MeasureTrace)
						.ignoreAsm2RdbmsTrace(ignoreAsm2RdbmsTrace)
						.validateModels(validateModels)
						.modelName(modelName)
						.dialectList(dialectList);

		defaultWorkflow = new DefaultWorkflow(parameters);

		WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

		if (jsl != null && !jsl.trim().equals("")) {
			workflowHelper.loadJslModel(modelName, jslModel, jslUri);
		}

		if (psm != null && !psm.trim().equals("")) {
			workflowHelper.loadPsmModel(modelName, null, artifactResolver.getArtifact(psm).toURI());
		}

		if (measure != null && !measure.trim().equals("")) {
			workflowHelper.loadPsmModel(modelName, null, artifactResolver.getArtifact(measure).toURI());
		}

		if (asm != null && (ignorePsm2AsmTrace || psm2AsmTrace != null)) {
			workflowHelper.loadAsmModel(modelName, null, artifactResolver.getArtifact(asm).toURI(),
					null, artifactResolver.getArtifact(psm2AsmTrace).toURI());
		}

		if (expression != null && !expression.trim().equals("")) {
			workflowHelper.loadExpressionModel(modelName, null, artifactResolver.getArtifact(expression).toURI());
		}

		if (sdk != null && !sdk.trim().equals("") && sdkInternal != null && !sdkInternal.trim().equals("")) {
			workflowHelper.loadSdk(null, artifactResolver.getArtifact(sdk).toURI(), null, artifactResolver.getArtifact(sdkInternal).toURI());
		}

		if (dialects != null) {
			for (Map.Entry<String, DialectParam> entry : dialects.entrySet()) {

				if (entry.getValue().getRdbms() != null && entry.getValue().getAsm2rdbmsTrace() != null) {
					workflowHelper.loadRdbmsModel(modelName, entry.getKey(), null, artifactResolver.getArtifact(entry.getValue().getRdbms()).toURI(),
							null, artifactResolver.getArtifact(entry.getValue().getAsm2rdbmsTrace()).toURI());
				}
				if (entry.getValue().getLiquibase() != null) {
					workflowHelper.loadLiquibaseModel(modelName, entry.getKey(), null, artifactResolver.getArtifact(entry.getValue().getLiquibase()).toURI());
				}
			}
		}

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

	@SuppressWarnings("unchecked")
	public <T> Stream<T> getStreamOf(ResourceSet resourceSet, final Class<T> clazz) {
		final Iterable<Notifier> contents = resourceSet::getAllContents;
		return StreamSupport.stream(contents.spliterator(), false)
				.filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
	}

	private static ArrayList<File> listFileTree(File dir, boolean recursive) {
		if (null == dir || !dir.isDirectory()) {
			return new ArrayList<>();
		}
		final Set<File> fileTree = new HashSet<File>();
		FileFilter fileFilter = new FileFilter() {
			private final String[] acceptedExtensions = new String[]{"jsl"};

			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				for (String extension : acceptedExtensions) {
					if (file.getName().toLowerCase().endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
		};
		File[] listed = dir.listFiles(fileFilter);
		if(listed!=null){
			for (File entry : listed) {
				if (entry.isFile()) {
					fileTree.add(entry);
				} else if(recursive){
					fileTree.addAll(listFileTree(entry,true));
				}
			}
		}
		return new ArrayList<>(fileTree);
	}

}
