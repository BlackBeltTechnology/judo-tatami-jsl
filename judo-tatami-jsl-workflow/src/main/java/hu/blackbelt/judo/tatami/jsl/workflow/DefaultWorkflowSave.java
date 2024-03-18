package hu.blackbelt.judo.tatami.jsl.workflow;

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

import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import hu.blackbelt.judo.tatami.psm2asm.Psm2AsmTransformationTrace;
import hu.blackbelt.judo.tatami.psm2measure.Psm2MeasureTransformationTrace;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.SaveArguments.asmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.SaveArguments.expressionSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.SaveArguments.liquibaseSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseNamespaceFixUriHandler.fixUriOutputStream;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.SaveArguments.measureSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.rdbms.runtime.RdbmsModel.SaveArguments.rdbmsSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.ui.runtime.UiModel.SaveArguments.uiSaveArgumentsBuilder;
import static hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsWork.getAsm2RdbmsTrace;
import static hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsWork.getRdbmsModel;
import static hu.blackbelt.judo.tatami.jsl.workflow.ThrowingCosumerWrapper.executeWrapper;
import static hu.blackbelt.judo.tatami.rdbms2liquibase.Rdbms2LiquibaseWork.getLiquibaseModel;

public class DefaultWorkflowSave {

    private static final boolean VALIDATE_MODELS_ON_SAVE = false; // do not validate models on save

    public static void saveModels(TransformationContext transformationContext, File dest, List<String> dialectList) {
        saveModels(true, transformationContext, dest, dialectList);
    }

    public static void saveModels(boolean catchError, TransformationContext transformationContext, File dest, List<String> dialectList) {

        if (!dest.exists()) {
            throw new IllegalArgumentException("Destination doesn't exist!");
        }
        if (!dest.isDirectory()) {
            throw new IllegalArgumentException("Destination is not a directory!");
        }

        transformationContext.getByClass(JslDslModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveJslDslModel(jslDslSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-jsl.model"))))));

        transformationContext.getByClass(PsmModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.savePsmModel(psmSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-psm.model"))))));

        transformationContext.getByClass(UiModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveUiModel(uiSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-ui.model"))))));

        transformationContext.getByClass(Jsl2PsmTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "jsl2psm.model")))));

        transformationContext.getByClass(Psm2MeasureTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "psm2measure.model")))));

        transformationContext.getByClass(Psm2AsmTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "psm2asm.model")))));


        transformationContext.getByClass(AsmModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveAsmModel(asmSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-asm.model"))))));

        transformationContext.getByClass(MeasureModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveMeasureModel(measureSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-measure.model"))))));

        dialectList.forEach(dialect -> getRdbmsModel(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError, (m) -> m.saveRdbmsModel(rdbmsSaveArgumentsBuilder().validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "rdbms_" + dialect + ".model")))))));

        transformationContext.getByClass(ExpressionModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveExpressionModel(expressionSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-expression.model"))))));

        dialectList.forEach(dialect -> getLiquibaseModel(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError,
                        (m) -> saveFixedLiquibaseModel(m, new FileOutputStream(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "liquibase_" + dialect + ".changelog.xml")))))));

        transformationContext.getByClass(Psm2AsmTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "psm2asm.model")))));

        dialectList.forEach(dialect -> getAsm2RdbmsTrace(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError, (m) -> m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "asm2rdbms_" + dialect + ".model"))))));

    }

    private static String fileName(TransformationContext transformationContext) {
        return transformationContext.getModelName().replaceAll(":", "_");
    }

    private static File deleteFileIfExists(File file) {
        if (!file.isDirectory() && file.exists()) {
            file.delete();
        }
        return file;
    }

    public static void saveFixedLiquibaseModel(LiquibaseModel liquibaseModel, OutputStream outputStream) throws IOException, LiquibaseModel.LiquibaseValidationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        liquibaseModel.saveLiquibaseModel(liquibaseSaveArgumentsBuilder()
                .validateModel(VALIDATE_MODELS_ON_SAVE)
                .outputStream(fixUriOutputStream(byteArrayOutputStream)));
        byteArrayOutputStream.writeTo(outputStream);
    }

}
