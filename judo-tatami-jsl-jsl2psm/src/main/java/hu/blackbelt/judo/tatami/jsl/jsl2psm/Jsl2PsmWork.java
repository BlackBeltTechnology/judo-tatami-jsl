package hu.blackbelt.judo.tatami.jsl.jsl2psm;

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

import org.slf4j.Logger;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
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
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.core.workflow.engine.WorkFlowEngineBuilder.aNewWorkFlowEngine;
import static hu.blackbelt.judo.tatami.core.workflow.flow.SequentialFlow.Builder.aNewSequentialFlow;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.calculateJsl2PsmTransformationScriptURI;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;

@Slf4j
public class Jsl2PsmWork extends AbstractTransformationWork {

    @Builder(builderMethodName = "jsl2PsmWorkParameter")
    public static final class Jsl2PsmWorkParameter {
        @Builder.Default
        Boolean createTrace = false;
        @Builder.Default
        Boolean parallel = true;
    }

    final URI transformationScriptRoot;

    public Jsl2PsmWork(TransformationContext transformationContext, URI transformationScriptRoot) {
        super(transformationContext);
        this.transformationScriptRoot = transformationScriptRoot;
    }

    public Jsl2PsmWork(TransformationContext transformationContext) {
        this(transformationContext, calculateJsl2PsmTransformationScriptURI());
    }

    @Override
    public void execute() throws Exception {
        Optional<JslDslModel> jslModel = getTransformationContext().getByClass(JslDslModel.class);
        jslModel.orElseThrow(() -> new IllegalArgumentException("JSL Model not found in transformation context"));

        PsmModel psmModel = getTransformationContext().getByClass(PsmModel.class)
                .orElseGet(() -> buildPsmModel().build());
        getTransformationContext().put(psmModel);

        Jsl2PsmWorkParameter workParam = getTransformationContext().getByClass(Jsl2PsmWorkParameter.class)
                .orElseGet(() -> Jsl2PsmWorkParameter.jsl2PsmWorkParameter().build());

        Jsl2PsmTransformationTrace jsl2PsmTransformationTrace = executeJsl2PsmTransformation(Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
                .jslModel(jslModel.get())
                .psmModel(psmModel)
                .log(getTransformationContext().getByClass(Logger.class).orElse(null))
                .scriptUri(transformationScriptRoot)
                .createTrace(workParam.createTrace)
                .parallel(workParam.parallel));

        getTransformationContext().put(jsl2PsmTransformationTrace);
    }

    public static void main(String[] args) throws IOException, JslDslModel.JslDslValidationException, PsmModel.PsmValidationException {

        File jslModelFile = new File(args[0]);
        String modelName = args[1];
        File psmModelFile = new File(args[2]);
        @SuppressWarnings("unused")
        Boolean validate = true;

        if (args.length >= 4) {
            try {
                validate = Boolean.parseBoolean(args[3]);
            } catch (Exception e) {
            }
        }

        Jsl2PsmWork jsl2PsmWork;
        TransformationContext transformationContext;

        JslDslModel jslModel = JslDslModel.loadJslDslModel(
                jslDslLoadArgumentsBuilder().file(jslModelFile).name(modelName));

//        if (validate) {
//            JslEpsilonValidator.validateJsl(log, jslModel, JslEpsilonValidator.calculateJslValidationScriptURI());
//        }

        transformationContext = new TransformationContext(modelName);
        transformationContext.put(jslModel);

        jsl2PsmWork = new Jsl2PsmWork(transformationContext, calculateJsl2PsmTransformationScriptURI());

        WorkFlow workflow = aNewSequentialFlow().execute(jsl2PsmWork).build();
        WorkFlowEngine workFlowEngine = aNewWorkFlowEngine().build();
        @SuppressWarnings("unused")
        WorkReport workReport = workFlowEngine.run(workflow);

        PsmModel psmModel = transformationContext.getByClass(PsmModel.class).get();
        psmModel.savePsmModel(psmSaveArgumentsBuilder().file(psmModelFile).build());
    }

}
