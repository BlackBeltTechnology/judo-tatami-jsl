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
import hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.asm2keycloak.Asm2KeycloakTransformationTrace;
import hu.blackbelt.judo.tatami.asm2query.QueryModelPersistence;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import hu.blackbelt.judo.tatami.psm2asm.Psm2AsmTransformationTrace;
import hu.blackbelt.judo.tatami.psm2measure.Psm2MeasureTransformationTrace;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.SaveArguments.asmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.SaveArguments.expressionSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.keycloak.runtime.KeycloakModel.SaveArguments.keycloakSaveArgumentsBuilder;
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

@Slf4j
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


        // Digests of the files written in this call: the query model artifact stamps them (see below).
        final AtomicReference<String> asmDigest = new AtomicReference<>();
        final AtomicReference<String> measureDigest = new AtomicReference<>();
        final AtomicReference<String> expressionDigest = new AtomicReference<>();
        final File queryFile = new File(dest, fileName(transformationContext) + QueryModelPersistence.QUERY_SUFFIX);
        final boolean asmPresent = transformationContext.getByClass(AsmModel.class).isPresent();

        transformationContext.getByClass(AsmModel.class).ifPresent(executeWrapper(catchError, (m) -> {
            final File file = deleteFileIfExists(new File(dest, fileName(transformationContext) + "-asm.model"));
            m.saveAsmModel(asmSaveArgumentsBuilder()
                    .validateModel(VALIDATE_MODELS_ON_SAVE)
                    .file(file));
            asmDigest.set(digest(file));
        }));

        transformationContext.getByClass(MeasureModel.class).ifPresent(executeWrapper(catchError, (m) -> {
            final File file = deleteFileIfExists(new File(dest, fileName(transformationContext) + "-measure.model"));
            m.saveMeasureModel(measureSaveArgumentsBuilder()
                    .validateModel(VALIDATE_MODELS_ON_SAVE)
                    .file(file));
            measureDigest.set(digest(file));
        }));

        dialectList.forEach(dialect -> getRdbmsModel(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError, (m) -> m.saveRdbmsModel(rdbmsSaveArgumentsBuilder().validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "rdbms_" + dialect + ".model")))))));

        transformationContext.getByClass(ExpressionModel.class).ifPresent(executeWrapper(catchError, (m) -> {
            final File file = deleteFileIfExists(new File(dest, fileName(transformationContext) + "-expression.model"));
            m.saveExpressionModel(expressionSaveArgumentsBuilder()
                    .validateModel(VALIDATE_MODELS_ON_SAVE)
                    .file(file));
            expressionDigest.set(digest(file));
        }));

        transformationContext.getByClass(KeycloakModel.class).ifPresent(executeWrapper(catchError, (m) ->
                m.saveKeycloakModel(keycloakSaveArgumentsBuilder()
                        .validateModel(VALIDATE_MODELS_ON_SAVE)
                        .file(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-keycloak.model"))))));

        dialectList.forEach(dialect -> getLiquibaseModel(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError,
                        (m) -> saveFixedLiquibaseModel(m, new FileOutputStream(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "liquibase_" + dialect + ".changelog.xml")))))));

        transformationContext.getByClass(Psm2AsmTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "psm2asm.model")))));

        transformationContext.getByClass(Asm2KeycloakTransformationTrace.class).ifPresent(executeWrapper(catchError, (m) ->
                m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "asm2keycloak.model")))));

        dialectList.forEach(dialect -> getAsm2RdbmsTrace(transformationContext, dialect)
                .ifPresent(executeWrapper(catchError, (m) -> m.save(deleteFileIfExists(new File(dest, fileName(transformationContext) + "-" + "asm2rdbms_" + dialect + ".model"))))));

        // The query model is saved LAST: it stamps the digests of the ASM, measure and expression files written above,
        // and refers to the ASM and expression files by name. It is written only next to an expression file saved in this
        // call. A run that rewrites the ASM without writing the query model removes the artifact left by an earlier run,
        // because it would describe the replaced ASM. A run that writes no ASM leaves it alone.
        if (asmPresent) {
            saveQueryModel(catchError, transformationContext, queryFile, asmDigest.get(), measureDigest.get(), expressionDigest.get());
        }
    }

    private static void saveQueryModel(boolean catchError, TransformationContext transformationContext, File queryFile,
                                       String asmDigest, String measureDigest, String expressionDigest) {
        final Optional<QueryModel> queryModel = transformationContext.getByClass(QueryModel.class);
        final boolean measureUnsaved = transformationContext.getByClass(MeasureModel.class).isPresent() && measureDigest == null;
        // the artifact refers to <name>-asm.model / <name>-expression.model by the query model's name: it has to be the files' name
        final boolean nameMismatch = queryModel.isPresent() && !queryModel.get().getName().equals(fileName(transformationContext));
        if (queryModel.isEmpty() || asmDigest == null || expressionDigest == null || measureUnsaved || nameMismatch
                || transformationContext.getByClass(AsmModel.class).isEmpty()
                || transformationContext.getByClass(ExpressionModel.class).isEmpty()) {
            deleteFileIfExists(queryFile);
            if (queryModel.isPresent()) {
                log.warn("Query model {} is not saved: the ASM, measure and expression files of this run were not all written under the model name", queryModel.get().getName());
            }
            return;
        }
        executeWrapper(catchError, (QueryModel m) -> {
            try {
                final QueryModelPersistence.SaveResult result = QueryModelPersistence.save(QueryModelPersistence.SaveArguments.saveArguments()
                        .queryModel(m)
                        .asmModel(transformationContext.getByClass(AsmModel.class).get())
                        .expressionModel(transformationContext.getByClass(ExpressionModel.class).get())
                        .asmDigest(asmDigest)
                        .measureDigest(measureDigest)
                        .expressionDigest(expressionDigest)
                        .file(queryFile)
                        .build());
                if (!result.isWritten()) {
                    // save() already deleted a stale file; make it certain for the next reader
                    deleteFileIfExists(queryFile);
                }
            } catch (Exception e) {
                deleteFileIfExists(queryFile);
                throw e;
            }
        }).accept(queryModel.get());
    }

    private static String digest(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return QueryModelPersistence.sha256Hex(in);
        }
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
