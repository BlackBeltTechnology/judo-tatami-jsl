package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.namespace.Model;
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
import static java.util.Optional.of;

@Mojo(name = "default-workflow",
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

//	@Parameter
//	private Map<String, DialectParam> dialects;

	@Parameter(property = "destination")
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
		ClassLoader classLoader = new URLClassLoader(urlsForClassLoader, DefaultWorkflowMojo.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	@SneakyThrows({MalformedURLException.class})
	private URL getArtifactFile(Artifact artifact) {
		return artifact.getFile().toURI().toURL();
	}


	/**
	 * Get the artifact file from the given url.
	 * @param url
	 * @return
	 * @throws MojoExecutionException
	 */
	public File getArtifact(String url) throws MojoExecutionException {
		if (url.startsWith("mvn:")) {
			String mvnUrl = url;
			String subUrl = "";
			if (mvnUrl.contains("!")) {
				subUrl = mvnUrl.substring(mvnUrl.lastIndexOf("!") + 1);
				mvnUrl = mvnUrl.substring(0, mvnUrl.lastIndexOf("!"));
			}
			ArtifactResult resolutionResult = getArtifactResult(mvnUrl);
			// The file should exists, but we never know.
			File file = resolutionResult.getArtifact().getFile();
			if (file == null || !file.exists()) {
				getLog().warn("Artifact " + url.toString() + " has no attached file. Its content will not be copied in the target model directory.");
			}

			if (subUrl.equals("")) {
				return file;
			} else {
				// Extract file from JAR or ZIP
				File fileFromArchive = null;
				try {
					fileFromArchive = getFileFromArchive(file, subUrl);
				} catch (IOException e) {
					throw new MojoExecutionException("Could not decompress: " + fileFromArchive.getAbsolutePath() + " file: " + fileFromArchive);
				}
				if (fileFromArchive == null || !fileFromArchive.exists()) {
					throw new MojoExecutionException("File " + subUrl + " does not exists in " + file.getAbsolutePath());
				}
				return fileFromArchive;
			}
		} else {
			File file = new File(url);
			if (file == null || !file.exists()) {
				getLog().warn("File " + url.toString() + " does not exists.");
			}
			return file;
		}
	}

	public File getFileFromArchive(File archive, String path) throws IOException {
		CompressorInputStream compressorInputStream = null;
		ArchiveInputStream archiveInputStream = null;
		File outFile = null;
		try {
			if (archive.getName().toLowerCase().endsWith(".tgz") || archive.getName().toLowerCase().endsWith(".tar.gz")) {
				compressorInputStream = new GzipCompressorInputStream(new FileInputStream(archive));
				archiveInputStream = new TarArchiveInputStream(compressorInputStream);
			} else if (archive.getName().toLowerCase().endsWith(".zip") || archive.getName().toLowerCase().endsWith(".jar")) {
				archiveInputStream = new ZipArchiveInputStream(new FileInputStream(archive));
			} else if (archive.getName().toLowerCase().endsWith(".bz2") || archive.getName().toLowerCase().endsWith(".tar.bzip2")) {
				compressorInputStream = new BZip2CompressorInputStream(new FileInputStream(archive));
				archiveInputStream = new TarArchiveInputStream(compressorInputStream);
			}

			if (archiveInputStream == null) {
				throw new IOException("Could not open: " + archive.getAbsolutePath());
			}
			if (archiveInputStream != null) {
				ArchiveEntry entry;
				while ((entry = archiveInputStream.getNextEntry()) != null) {
					if (!entry.isDirectory() && entry.getName().equals(path)) {
						FileNameUtils.getExtension(path);
						outFile = File.createTempFile("artifacthandler", entry.getName().replaceAll("/", "_"));
						int count;
						byte data[] = new byte[BUFFER_SIZE];
						FileOutputStream fos = new FileOutputStream(outFile, false);
						try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
							while ((count = archiveInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
								dest.write(data, 0, count);
							}
						}
						return outFile;
					}
				}
			}
		} finally {
			if (compressorInputStream != null) {
				try {
					compressorInputStream.close();
				} catch (Exception e) {
				}
			}

			if (archiveInputStream != null) {
				try {
					archiveInputStream.close();
				} catch (Exception e) {
				}
			}
		}
		return null;
	}

	/**
	 * Get the artifact result from the given url.
	 * @param url
	 * @return
	 * @throws MojoExecutionException
	 */
	public ArtifactResult getArtifactResult(String url) throws MojoExecutionException {
		org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(url.toString().substring(4));
		ArtifactRequest req = new ArtifactRequest().setRepositories(this.repositories).setArtifact(artifact);
		ArtifactResult resolutionResult;
		try {
			resolutionResult = repoSystem.resolveArtifact(repoSession, req);

		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException("Artifact " + url.toString() + " could not be resolved.", e);
		}
		return resolutionResult;
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

		if (jsl != null && !jsl.trim().equals("")) {
			jslUri = getArtifact(jsl).toURI();

			if (modelName == null || modelName.trim().equals("")) {
				try {
					JslDslModel jslModelForName = JslDslModel.loadJslDslModel(jslDslLoadArgumentsBuilder()
							.inputStream(
									of(jslUri).orElseThrow(() ->
													new IllegalArgumentException("jslModel or jslModelSourceUri have to be defined"))
											.toURL().openStream())
							.validateModel(false)
							.name("forName"));

					modelName = getStreamOf(jslModelForName.getResourceSet(), Model.class)
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
						.ignoreJsl2Psm(ignoreJsl2Psm)
						.ignoreJsl2PsmTrace(ignoreJsl2PsmTrace)
						.validateModels(validateModels)
						.modelName(modelName);

		defaultWorkflow = new DefaultWorkflow(parameters);

		WorkflowHelper workflowHelper = new WorkflowHelper(defaultWorkflow);

		if (jsl != null && !jsl.trim().equals("")) {
			workflowHelper.loadJslModel(modelName, jslModel, jslUri);
		}

		if (psm != null) {
			workflowHelper.loadPsmModel(modelName, null, getArtifact(psm).toURI());
		}

		Exception error = null;
		try {
			defaultWorkflow.startDefaultWorkflow();
		} catch (IllegalStateException e) {
			error = e;
		}

		if (error != null || saveModels) {
			destination.mkdirs();
			try {
				DefaultWorkflowSave.saveModels(defaultWorkflow.getTransformationContext(), destination);
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

	public <T> Stream<T> getStreamOf(ResourceSet resourceSet, final Class<T> clazz) {
		final Iterable<Notifier> contents = resourceSet::getAllContents;
		return StreamSupport.stream(contents.spliterator(), false)
				.filter(e -> clazz.isAssignableFrom(e.getClass())).map(e -> (T) e);
	}
}
