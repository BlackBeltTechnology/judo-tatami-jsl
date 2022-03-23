package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
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
import hu.blackbelt.judo.tatami.workflow.PsmDefaultWorkflow;
import hu.blackbelt.judo.tatami.workflow.DefaultWorkflowSave;
import hu.blackbelt.judo.tatami.workflow.DefaultWorkflowSetupParameters;

import static com.google.common.base.Strings.isNullOrEmpty;

@Mojo(name = "psm-default-workflow",
		defaultPhase = LifecyclePhase.COMPILE,
		requiresDependencyResolution = ResolutionScope.COMPILE)
public class PsmDefaultWorkflowMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Component
	public RepositorySystem repoSystem;

	@Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
	public RepositorySystemSession repoSession;

	@Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
	public List<RemoteRepository> repositories;

	@Parameter( defaultValue = "${plugin}", readonly = true )
	private PluginDescriptor pluginDescriptor;

	@Parameter(property = "psmModelFile")
	private File psmModelFile;

	@Parameter(property = "destination")
	private File destination;

	@Parameter(property = "modelName")
	private String modelName;
	
	@Parameter(property = "dialectList")
	private List<String> dialectList;

	@Parameter(property = "runInParallel", defaultValue = "true")
	private Boolean runInParallel = true;

	@Parameter(property = "enableMetrics", defaultValue = "true")
	private Boolean enableMetrics = true;

	@Parameter(property = "psmGeneratorClassName")
	private String psmGeneratorClassName;

	@Parameter(property = "psmGeneratorMethodName")
	private String psmGeneratorMethodName;

	@Parameter(property = "validateModels", defaultValue = "false")
	private Boolean validateModels = false;

	Set<URL> classPathUrls = new HashSet<>();

	private void setContextClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
		// Project dependencies
		for (Object mavenCompilePath : project.getCompileClasspathElements()) {
			String currentPathProcessed = (String) mavenCompilePath;
			classPathUrls.add(new File(currentPathProcessed).toURI().toURL());
		}

		// Plugin dependencies
		final ClassRealm classRealm = pluginDescriptor.getClassRealm();
		for (URL url: classRealm.getURLs()) {
			classPathUrls.add(url);
		}

		URL[] urlsForClassLoader = classPathUrls.toArray(new URL[classPathUrls.size()]);
		getLog().debug("Set urls for URLClassLoader: " + Arrays.asList(urlsForClassLoader));

		// need to define parent classloader which knows all dependencies of the plugin
		ClassLoader classLoader = new URLClassLoader(urlsForClassLoader, PsmDefaultWorkflowMojo.class.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		// -------------------------------------------------- //
		// Fetching transformation script roots from manifest //
		// -------------------------------------------------- //

		// Needed for to access project's dependencies.
		// Info: http://blog.chalda.cz/2018/02/17/Maven-plugin-and-fight-with-classloading.html
		try {
			setContextClassLoader();
		} catch (Exception e) {
			throw new MojoExecutionException("Failed to set classloader", e);
		}

		PsmDefaultWorkflow defaultWorkflow;
		try {
			DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder parameters =
					DefaultWorkflowSetupParameters
					.defaultWorkflowSetupParameters()
						.runInParallel(runInParallel)
						.enableMetrics(enableMetrics)
						.validateModels(validateModels)
						.modelName(modelName)
						.dialectList(dialectList);

			//DefaultWorkflowSetupParameters.addTransformerCalculatedUris(parameters);

			if (!isNullOrEmpty(psmGeneratorClassName) && !isNullOrEmpty(psmGeneratorMethodName)) {
				Class generatorClass = Thread.currentThread().getContextClassLoader().loadClass(psmGeneratorClassName);
				Method generatorMethod = generatorClass.getMethod(psmGeneratorMethodName);
				PsmModel psmModel = (PsmModel) generatorMethod.invoke(generatorClass.newInstance());
				parameters.psmModel(psmModel);
			} else {
				parameters.psmModelSourceURI(psmModelFile.toURI());
			}
			defaultWorkflow = new PsmDefaultWorkflow(parameters);
		} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new MojoFailureException("An error occurred during the setup phase of the workflow.", e);
		}

		// ------------ //
		// Run workflow //
		// ------------ //
		try {
			defaultWorkflow.startDefaultWorkflow();
		} catch (IllegalStateException e) {
			try {
				DefaultWorkflowSave.saveModels(defaultWorkflow.getTransformationContext(), destination, dialectList);
			} catch (Exception e2) {
			}
			throw new MojoFailureException("An error occurred during the execution phase of the workflow.", e);
		}

		// ------------------ //
		// Save models/traces //
		// ------------------ //
		destination.mkdirs();
		try {
			DefaultWorkflowSave.saveModels(defaultWorkflow.getTransformationContext(), destination, dialectList);
		} catch (Exception e) {
			throw new MojoFailureException("An error occurred during the saving phase of the workflow.", e);
		}
	}

}
