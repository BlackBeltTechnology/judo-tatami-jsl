package hu.blackbelt.judo.tatami.jsl.workflow;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.net.URI;
import java.util.List;

@Builder(builderMethodName = "defaultWorkflowSetupParameters")
@Getter
public class DefaultWorkflowSetupParameters {

	private String modelVersion;

	/**
	 * When psmModel is defined psmModelSourceURI is ignored.
	 */
	private PsmModel psmModel;

	private URI psmModelSourceURI;

	/**
	 * When jslModel is defined jslModelSourceURI is ignored.
	 */
	private JslDslModel jslModel;

	private URI jslModelSourceURI;

	@NonNull
	private String modelName;

	@NonNull
	private List<String> dialectList;

	@Builder.Default
	private Boolean createSdkJar = true;

	@Builder.Default
	private Boolean compileSdk = true;

	@Builder.Default
	private File sdkOutputDirectory = null;

	@Builder.Default
	private String sdkPackagePrefix = null;

	@Builder.Default
	private Boolean runInParallel = true;

	@Builder.Default
	private Boolean enableMetrics = true;

	@Builder.Default
	private Boolean ignoreJsl2Psm = false;

	@Builder.Default
	private Boolean ignoreJsl2PsmTrace = false;

	@Builder.Default
	private Boolean ignorePsm2Asm = false;

	@Builder.Default
	private Boolean ignorePsm2Measure = false;

	@Builder.Default
	private Boolean ignorePsm2AsmTrace = false;

	@Builder.Default
	private Boolean ignorePsm2MeasureTrace = false;

	@Builder.Default
	private Boolean ignoreAsm2Rdbms = false;

	@Builder.Default
	private Boolean ignoreAsm2RdbmsTrace = false;

	@Builder.Default
	private Boolean ignoreRdbms2Liquibase = false;

	@Builder.Default
	private Boolean ignoreAsm2sdk = false;

	@Builder.Default
	private Boolean ignoreAsm2Expression = false;

	@Builder.Default
	private Boolean validateModels = false;

}
