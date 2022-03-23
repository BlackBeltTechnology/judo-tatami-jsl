package hu.blackbelt.judo.tatami.workflow;

import com.pivovarit.function.ThrowingSupplier;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.tatami.psm.validation.PsmValidationWork;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReportPredicate;
import hu.blackbelt.judo.tatami.core.workflow.work.NoOpWork;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.Work;
import hu.blackbelt.judo.tatami.jsl2psm.Jsl2PsmWork;

import java.net.URI;
import java.util.function.Supplier;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.LoadArguments.psmLoadArgumentsBuilder;
import static hu.blackbelt.judo.tatami.core.workflow.flow.ConditionalFlow.Builder.aNewConditionalFlow;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class WorkflowHelper {
    final private TransformationContext transformationContext;
    final private WorkflowMetrics workflowMetrics;

    public WorkflowHelper(AbstractTatamiPipelineWorkflow workflow) {
        this.transformationContext = workflow.getTransformationContext();
        this.workflowMetrics = workflow.getMetrics();
    }

    public WorkflowHelper(TransformationContext transformationContext, WorkflowMetrics workflowMetrics) {
        this.transformationContext = transformationContext;
        this.workflowMetrics = workflowMetrics;
    }

    public void loadJslModel(final String modelName,
                             final JslDslModel jslModel,
                             final URI jslModelSourceURI) {

        if (jslModel == null && jslModelSourceURI == null) {
            return;
        }
        transformationContext.put(ofNullable(jslModel).orElseGet(
                ThrowingSupplier.sneaky(() -> JslDslModel.loadJslDslModel(jslDslLoadArgumentsBuilder()
                        .inputStream(
                                of(jslModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("jslModel or jslModelSourceUri have to be defined"))
                                        .toURL().openStream())
                        .name(modelName)))));
    }

    public void loadPsmModel(final String modelName,
                             final PsmModel psmModel,
                             final URI psmModelSourceURI) {

        if (psmModel == null && psmModelSourceURI == null) {
            return;
        }
        transformationContext.put(ofNullable(psmModel).orElseGet(
                ThrowingSupplier.sneaky(() -> PsmModel.loadPsmModel(psmLoadArgumentsBuilder()
                        .inputStream(
                                of(psmModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("psmModel or psmModelSourceUri have to be defined"))
                                        .toURL().openStream())
                        .name(modelName)))));
    }

//    public Work createJslValidateWork() {
//        return aNewConditionalFlow()
//                .named("Conditional when Jsl model exists then Execute JslValidation")
//                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(JslDslModel.class)))
//                .when(WorkReportPredicate.COMPLETED)
//                .then(
//                        new JslDslValidationWork(transformationContext).withMetricsCollector(workflowMetrics))
//                .otherwise(new NoOpWork())
//                .build();
//    }

    public Work createPsmValidateWork() {
        return aNewConditionalFlow()
                .named("Conditional when Psm model exists then Execute PsmValidation")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(PsmModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        new PsmValidationWork(transformationContext).withMetricsCollector(workflowMetrics))
                .otherwise(new NoOpWork())
                .build();
    }

    public Supplier<Boolean> jsl2PsmOutputPredicate() {
        return () -> transformationContext.transformationContextVerifier.verifyClassPresent(PsmModel.class);
    }

    public Work createJsl2PsmWork() {
        return aNewConditionalFlow()
                .named("Conditional when Jsl model exists then Execute Jsl2Psm")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(JslDslModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Jsl2Psm")
                                .execute(
                                        new Jsl2PsmWork(transformationContext).withMetricsCollector(workflowMetrics),
                                        new CheckWork(jsl2PsmOutputPredicate())
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }
}
