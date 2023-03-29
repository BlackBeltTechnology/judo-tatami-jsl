package hu.blackbelt.judo.tatami.jsl.jsl2ui;

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

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngine;
import hu.blackbelt.judo.tatami.core.workflow.flow.WorkFlow;
import hu.blackbelt.judo.tatami.core.workflow.work.*;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.SaveArguments.uiSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.calculateJsl2UiTransformationScriptURI;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui.executeJsl2UiTransformation;

@Slf4j
public class Jsl2UiWork extends AbstractTransformationWork {

    @Builder(builderMethodName = "jsl2UiWorkParameter")
    public static final class Jsl2UiWorkParameter {
        @Builder.Default
        Boolean createTrace = false;
        @Builder.Default
        Boolean parallel = true;
    }

    final URI transformationScriptRoot;

    public Jsl2UiWork(TransformationContext transformationContext, URI transformationScriptRoot) {
        super(transformationContext);
        this.transformationScriptRoot = transformationScriptRoot;
    }

    public Jsl2UiWork(TransformationContext transformationContext) {
        this(transformationContext, calculateJsl2UiTransformationScriptURI());
    }

    @Override
    public void execute() throws Exception {
        Optional<JslDslModel> jslModel = getTransformationContext().getByClass(JslDslModel.class);
        jslModel.orElseThrow(() -> new IllegalArgumentException("JSL Model not found in transformation context"));

        UiModel uiModel = getTransformationContext().getByClass(UiModel.class)
                .orElseGet(() -> buildUiModel().name("FallbackUiModelName").build());
        getTransformationContext().put(uiModel);

        Jsl2UiWorkParameter workParam = getTransformationContext().getByClass(Jsl2UiWorkParameter.class)
                .orElseGet(() -> Jsl2UiWorkParameter.jsl2UiWorkParameter().build());

        Jsl2UiTransformationTrace jsl2UiTransformationTrace = executeJsl2UiTransformation(Jsl2Ui.Jsl2UiParameter.jsl2UiParameter()
                .jslModel(jslModel.get())
                .uiModel(uiModel)
                .log(getTransformationContext().getByClass(Log.class).orElse(null))
                .scriptUri(transformationScriptRoot)
                .createTrace(workParam.createTrace)
                .parallel(workParam.parallel));

        getTransformationContext().put(jsl2UiTransformationTrace);
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
//            JslEpsilonValidator.validateJsl(new Slf4jLog(log), jslModel, JslEpsilonValidator.calculateJslValidationScriptURI());
//        }

        transformationContext = new TransformationContext(modelName);
        transformationContext.put(jslModel);

        jsl2UiWork = new Jsl2UiWork(transformationContext, calculateJsl2UiTransformationScriptURI());

        WorkFlow workflow = aNewSequentialFlow().execute(jsl2UiWork).build();
        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        @SuppressWarnings("unused")
        WorkReport workReport = workFlowEngine.run(workflow);

        UiModel uiModel = transformationContext.getByClass(UiModel.class).get();
        uiModel.saveUiModel(uiSaveArgumentsBuilder().file(uiModelFile).build());
    }

}
