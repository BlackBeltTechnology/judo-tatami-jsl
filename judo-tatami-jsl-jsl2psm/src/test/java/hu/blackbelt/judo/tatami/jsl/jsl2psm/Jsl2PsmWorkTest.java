package hu.blackbelt.judo.tatami.jsl.jsl2psm;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngine;
import hu.blackbelt.judo.tatami.core.workflow.flow.WorkFlow;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.calculateJsl2PsmTransformationScriptURI;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class Jsl2PsmWorkTest {

	public static final String MODEL_NAME = "test";
	public static final String TARGET_TEST_CLASSES = "target/test-classes";
	public static final String JSL_FILE_LOCATION = TARGET_TEST_CLASSES + "/jsl/" + MODEL_NAME + "-jsl.model";
	public static final String PSM_FILE_LOCATION = TARGET_TEST_CLASSES + "/jsl/" + MODEL_NAME + "-psm.model";

	Jsl2PsmWork jsl2PsmWork;
	TransformationContext transformationContext;

	@BeforeEach
	void setUp() throws JslDslModel.JslDslValidationException, IOException {
		JslDslModel jslModel = buildJslDslModel().uri(URI.createURI(JSL_FILE_LOCATION)).name(MODEL_NAME).build();

		final EntityDeclaration entityA = EntityDeclarationBuilder.create().withName("A").build();
		final EntityDeclaration entityB = EntityDeclarationBuilder.create().withName("B").build();

		final ModelDeclaration model = ModelDeclarationBuilder.create()
				.withName(MODEL_NAME)
				.withDeclarations(asList(entityA, entityB))
				.build();

		jslModel.addContent(model);
		jslModel.saveJslDslModel();

		transformationContext = new TransformationContext(MODEL_NAME);
		transformationContext.put(jslModel);

		jsl2PsmWork = new Jsl2PsmWork(transformationContext, calculateJsl2PsmTransformationScriptURI());
		jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().file(new File(JSL_FILE_LOCATION)).build());
	}

	@Test
	void testSimpleWorkflow() throws IOException, PsmModel.PsmValidationException {
		WorkFlow workflow = aNewSequentialFlow().execute(jsl2PsmWork).build();

		WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
		WorkReport workReport = workFlowEngine.run(workflow);

		log.info("Workflow completed with status {}", workReport.getStatus(), workReport.getError());

		assertThat(workReport.getStatus(), equalTo(WorkStatus.COMPLETED));

		Optional<PsmModel> psmModel = transformationContext.getByClass(PsmModel.class);
		assertTrue(psmModel.isPresent());
		psmModel.get().savePsmModel(psmSaveArgumentsBuilder().file(new File(PSM_FILE_LOCATION)));


	}

}
