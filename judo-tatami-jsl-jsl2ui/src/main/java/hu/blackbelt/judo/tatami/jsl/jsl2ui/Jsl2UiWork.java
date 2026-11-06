package hu.blackbelt.judo.tatami.jsl.jsl2ui;

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.core.etl.EtlEngineRegistry;
import hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngine;
import hu.blackbelt.judo.tatami.core.workflow.flow.WorkFlow;
import hu.blackbelt.judo.tatami.core.workflow.work.AbstractTransformationWork;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiZetaTransformation;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.SaveArguments.uiSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;

/**
 * JSL to UI work. Zeta runs in this (Epsilon-free) artifact; ETL (and the ETL half of DUAL) delegates to
 * the optional {@link Jsl2UiEtlEngine} add-on ({@code judo-tatami-jsl-jsl2ui-etl}). See change: split-etl-artifacts.
 */
@Slf4j
public class Jsl2UiWork extends AbstractTransformationWork {
    @Builder(builderMethodName = "jsl2UiWorkParameter")
    public static final class Jsl2UiWorkParameter {
        @Builder.Default
        Boolean createTrace = false;
        @Builder.Default
        Boolean parallel = true;
        @Builder.Default
        TransformationMode transformationMode = TransformationMode.fromSystemProperty();
    }


    public Jsl2UiWork(TransformationContext transformationContext) {
        super(transformationContext);
    }

    @Override
    public void execute() throws Exception {
        Optional<JslDslModel> jslModel = getTransformationContext().getByClass(JslDslModel.class);
        jslModel.orElseThrow(() -> new IllegalArgumentException("JSL Model not found in transformation context"));

        UiModel uiModel = getTransformationContext().getByClass(UiModel.class)
                .orElseGet(() -> buildUiModel().name(jslModel.get().getName()).build());
        getTransformationContext().put(uiModel);

        Jsl2UiWorkParameter workParam = getTransformationContext().getByClass(Jsl2UiWorkParameter.class)
                .orElseGet(() -> Jsl2UiWorkParameter.jsl2UiWorkParameter().build());

        TransformationMode mode = workParam.transformationMode;
        log.info("JSL2UI transformation mode: {}", mode);

        if (mode.isEtl()) {
            EtlEngineRegistry.require(Jsl2UiEtlEngine.class, Jsl2UiEtlEngine.ARTIFACT_COORDINATES)
                    .execute(jslModel.get(), uiModel,
                            getTransformationContext().getByClass(Logger.class).orElse(null),
                            workParam.createTrace, workParam.parallel);
        }

        if (mode.isZeta()) {
            Jsl2UiZetaTransformation.builder()
                    .jslModel(jslModel.get())
                    .uiModel(uiModel)
                    .defaultModelName(jslModel.get().getName())
                    .build()
                    .execute();
        }
    }

    public static void main(String[] args) throws IOException, JslDslModel.JslDslValidationException, UiModel.UiValidationException {

        File jslModelFile = new File(args[0]);
        String modelName = args[1];
        File uiModelFile = new File(args[2]);
        @SuppressWarnings("unused")
        Boolean validate = true;

        if (args.length >= 4) {
            try {
                validate = Boolean.parseBoolean(args[3]);
            } catch (Exception e) {
            }
        }

        Jsl2UiWork jsl2UiWork;
        TransformationContext transformationContext;

        JslDslModel jslModel = JslDslModel.loadJslDslModel(
                jslDslLoadArgumentsBuilder().file(jslModelFile).name(modelName));

//        if (validate) {
//            JslEpsilonValidator.validateJsl(log, jslModel, JslEpsilonValidator.calculateJslValidationScriptURI());
//        }

        transformationContext = new TransformationContext(modelName);
        transformationContext.put(jslModel);

        jsl2UiWork = new Jsl2UiWork(transformationContext);

        WorkFlow workflow = aNewSequentialFlow().execute(jsl2UiWork).build();
        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        @SuppressWarnings("unused")
        WorkReport workReport = workFlowEngine.run(workflow);

        UiModel uiModel = transformationContext.getByClass(UiModel.class).get();
        uiModel.saveUiModel(uiSaveArgumentsBuilder().file(uiModelFile).build());
    }
}
