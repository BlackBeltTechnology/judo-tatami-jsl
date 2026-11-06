package hu.judo.tatami.jsl.workflow;

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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.query.QueryModelInfo;
import hu.blackbelt.judo.meta.query.runtime.QueryModel;
import hu.blackbelt.judo.tatami.asm2query.Asm2QueryWork;
import hu.blackbelt.judo.tatami.asm2query.QueryModelPersistence;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReport;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkStatus;
import hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSetupParameters;
import hu.blackbelt.judo.tatami.jsl.workflow.JslDefaultWorkflow;
import hu.blackbelt.judo.tatami.jsl.workflow.WorkflowHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.LoadArguments.asmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static hu.blackbelt.judo.tatami.jsl.workflow.DefaultWorkflowSave.saveModels;
import static hu.judo.tatami.jsl.workflow.JslTestModel.FILE_LOCATION;
import static hu.judo.tatami.jsl.workflow.JslTestModel.MODEL_NAME;
import static hu.judo.tatami.jsl.workflow.JslTestModel.createJslModelAndSave;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The query stage of the JSL workflow and its save, over the JSL fixture model (TP-2): default run, both execution
 * modes, the opt-out flag, the stale file rules, the skip rules and a reload of the saved files.
 */
public class QueryModelWorkflowTest {

    private static final List<String> HSQLDB = ImmutableList.of("hsqldb");
    private static final String QUERY_FILE = MODEL_NAME + "-query.model";

    @TempDir
    File temp;

    @BeforeEach
    void createJsl() throws Exception {
        createJslModelAndSave();
    }

    private static JslDefaultWorkflow workflow(final Consumer<DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder> customizer) {
        final DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder builder = DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters()
                .jslModelSourceURI(new File(FILE_LOCATION).toURI())
                .dialectList(HSQLDB)
                .modelName(MODEL_NAME);
        customizer.accept(builder);
        return new JslDefaultWorkflow(builder);
    }

    /** Runs the workflow and saves its models into {@code dir}. */
    private static JslDefaultWorkflow run(final File dir, final Consumer<DefaultWorkflowSetupParameters.DefaultWorkflowSetupParametersBuilder> customizer) {
        final JslDefaultWorkflow workflow = workflow(customizer);
        final WorkReport report = workflow.startDefaultWorkflow();
        assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));
        saveModels(false, workflow.getTransformationContext(), dir, HSQLDB);
        return workflow;
    }

    private File dir(final String name) {
        final File dir = new File(temp, name);
        assertTrue(dir.mkdirs(), "could not create " + dir);
        return dir;
    }

    @Test
    void ignoreAsm2QueryDefaultsToFalse() {
        assertThat(DefaultWorkflowSetupParameters.defaultWorkflowSetupParameters().modelName(MODEL_NAME).dialectList(HSQLDB).build().getIgnoreAsm2Query(), equalTo(false));
    }

    @Test
    void defaultRunWritesTheQueryFileNextToAsmAndExpressionFiles() {
        final File dir = dir("default");
        final JslDefaultWorkflow workflow = run(dir, b -> { });

        assertTrue(new File(dir, MODEL_NAME + "-asm.model").isFile(), "asm file");
        assertTrue(new File(dir, MODEL_NAME + "-expression.model").isFile(), "expression file");
        assertTrue(new File(dir, QUERY_FILE).isFile(), "query file");
        assertThat(workflow.getTransformationContext().getByClass(QueryModel.class).isPresent(), equalTo(true));
    }

    @Test
    void sequentialRunWritesTheQueryFile() {
        final File dir = dir("sequential");
        final JslDefaultWorkflow workflow = run(dir, b -> b.runInParallel(false));

        assertTrue(new File(dir, QUERY_FILE).isFile(), "query file");
        assertThat(workflow.getTransformationContext().getByClass(QueryModel.class).isPresent(), equalTo(true));
    }

    @Test
    void optOutProducesNoQueryModelAndNoFile() {
        final File dir = dir("opt-out");
        final JslDefaultWorkflow workflow = run(dir, b -> b.ignoreAsm2Query(true));

        assertThat(workflow.getTransformationContext().getByClass(QueryModel.class).isPresent(), equalTo(false));
        assertTrue(new File(dir, MODEL_NAME + "-asm.model").isFile(), "asm file");
        assertThat(new File(dir, QUERY_FILE).exists(), equalTo(false));
    }

    /** A run that rewrites the ASM without the stage removes the artifact an earlier run left. */
    @Test
    void staleQueryFileIsDeletedWhenTheAsmIsRewrittenWithoutTheStage() {
        final File dir = dir("stale");
        run(dir, b -> { });
        assertTrue(new File(dir, QUERY_FILE).isFile());

        run(dir, b -> b.ignoreAsm2Query(true));
        assertTrue(new File(dir, MODEL_NAME + "-asm.model").isFile(), "the ASM was rewritten");
        assertThat(new File(dir, QUERY_FILE).exists(), equalTo(false));
    }

    /** A run that writes no ASM leaves the artifact alone. */
    @Test
    void queryFileIsUntouchedWhenNoAsmIsWritten() {
        final File dir = dir("untouched");
        run(dir, b -> { });
        final File query = new File(dir, QUERY_FILE);
        assertTrue(query.isFile());
        assertTrue(query.setLastModified(query.lastModified() - 3_600_000L));
        final long mtime = query.lastModified();
        final long size = query.length();

        final JslDefaultWorkflow second = run(dir, b -> b.ignorePsm2Asm(true));
        assertThat(second.getTransformationContext().getByClass(AsmModel.class).isPresent(), equalTo(false));

        assertTrue(query.isFile());
        assertThat(query.lastModified(), equalTo(mtime));
        assertThat(query.length(), equalTo(size));
    }

    /** Without the expression stage the run succeeds, builds no query model and says why. */
    @Test
    void stageIsSkippedWithAWarningWithoutAnExpressionModel() {
        final Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Asm2QueryWork.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        final File dir = dir("no-expression");
        try {
            final JslDefaultWorkflow workflow = run(dir, b -> b.ignoreAsm2Expression(true));
            assertThat(workflow.getTransformationContext().getByClass(ExpressionModel.class).isPresent(), equalTo(false));
            assertThat(workflow.getTransformationContext().getByClass(QueryModel.class).isPresent(), equalTo(false));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
        final List<String> warnings = appender.list.stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.WARN))
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.toList());
        assertThat(warnings.stream().anyMatch(m -> m.contains("expression model not found")), equalTo(true));
        assertTrue(new File(dir, MODEL_NAME + "-asm.model").isFile());
        assertThat(new File(dir, QUERY_FILE).exists(), equalTo(false));
    }

    /**
     * D4 A1: an expression model preloaded into the context (the {@code expression} parameter of the Maven plugin) was
     * not extracted in this run: no query model is built, the run succeeds, the save removes the stale artifact an
     * earlier run left next to the rewritten ASM, and the skip is logged as a WARN.
     */
    @Test
    void preloadedExpressionModelSkipsTheStageAndDeletesTheStaleQueryFile() {
        final File dir = dir("preloaded-expression");
        run(dir, b -> { });
        final File query = new File(dir, QUERY_FILE);
        assertTrue(query.isFile(), "the earlier run left a query file");
        final File expressionFile = new File(dir, MODEL_NAME + "-expression.model");
        assertTrue(expressionFile.isFile(), "expression file of the earlier run");

        final Logger logger = (Logger) org.slf4j.LoggerFactory.getLogger(Asm2QueryWork.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        final JslDefaultWorkflow workflow = workflow(b -> { });
        final WorkReport report;
        try {
            new WorkflowHelper(workflow).loadExpressionModel(MODEL_NAME, null, expressionFile.toURI());
            report = workflow.startDefaultWorkflow();
            saveModels(false, workflow.getTransformationContext(), dir, HSQLDB);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }

        assertThat(report.getStatus(), equalTo(WorkStatus.COMPLETED));
        assertThat(workflow.getTransformationContext().getByClass(ExpressionModel.class).isPresent(), equalTo(true));
        assertThat(workflow.getTransformationContext().getByClass(QueryModel.class).isPresent(), equalTo(false));
        assertTrue(new File(dir, MODEL_NAME + "-asm.model").isFile(), "the ASM was rewritten");
        assertTrue(expressionFile.isFile(), "the preloaded expression model was saved");
        assertThat(query.exists(), equalTo(false));
        assertThat(appender.list.stream()
                .filter(e -> e.getLevel().isGreaterOrEqual(Level.WARN))
                .map(ILoggingEvent::getFormattedMessage)
                .anyMatch(m -> m.contains("expression model not extracted in this run")), equalTo(true));
    }

    /** A QueryModel already in the context is not built again. */
    @Test
    void existingQueryModelInTheContextIsKept() {
        final QueryModel existing = run(dir("first"), b -> { }).getTransformationContext().getByClass(QueryModel.class).orElseThrow();

        final JslDefaultWorkflow second = workflow(b -> { });
        second.getTransformationContext().put(existing);
        assertThat(new WorkflowHelper(second).asm2QueryOutputPredicate().get(), equalTo(true));
        assertThat(second.startDefaultWorkflow().getStatus(), equalTo(WorkStatus.COMPLETED));

        assertThat(second.getTransformationContext().getByClass(QueryModel.class).orElseThrow(), sameInstance(existing));
    }

    /** The files of one run reload from disk with every reference resolved. */
    @Test
    void savedQueryFileReloadsResolved() throws Exception {
        final File dir = dir("reload");
        run(dir, b -> { });

        final AsmModel asm = AsmModel.loadAsmModel(asmLoadArgumentsBuilder().file(new File(dir, MODEL_NAME + "-asm.model")));
        final ExpressionModel expression = loadExpression(new File(dir, MODEL_NAME + "-expression.model"));
        final QueryModelPersistence.LoadResult loaded;
        try (InputStream in = new FileInputStream(new File(dir, QUERY_FILE))) {
            loaded = QueryModelPersistence.load(MODEL_NAME, in, asm.getResource(), expression.getResource());
        }
        assertThat(loaded.getUnresolvedProxyCount(), equalTo(0));
        final QueryModelInfo info = loaded.getInfo();
        assertThat(info, notNullValue());
        assertThat(info.getModelName(), equalTo(MODEL_NAME));
    }

    private static ExpressionModel loadExpression(final File file) throws IOException {
        try {
            return ExpressionModel.loadExpressionModel(expressionLoadArgumentsBuilder().file(file).name(MODEL_NAME));
        } catch (ExpressionModel.ExpressionValidationException e) {
            throw new IOException(e);
        }
    }
}
