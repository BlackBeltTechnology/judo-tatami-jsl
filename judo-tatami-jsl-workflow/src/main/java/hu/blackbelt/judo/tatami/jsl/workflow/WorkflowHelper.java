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

import com.pivovarit.function.ThrowingSupplier;
import hu.blackbelt.judo.meta.asm.runtime.AsmModel;
import hu.blackbelt.judo.meta.expression.runtime.ExpressionModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel;
import hu.blackbelt.judo.meta.measure.runtime.MeasureModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.rdbms.runtime.RdbmsModel;
import hu.blackbelt.judo.meta.rdbms.support.RdbmsModelResourceSupport;
import hu.blackbelt.judo.tatami.asm2expression.Asm2ExpressionWork;
import hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsTransformationTrace;
import hu.blackbelt.judo.tatami.asm2rdbms.Asm2RdbmsWork;
import hu.blackbelt.judo.tatami.expression.asm.validation.ExpressionValidationOnAsmWork;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmWork;
import hu.blackbelt.judo.tatami.psm.validation.PsmValidationWork;
import hu.blackbelt.judo.tatami.core.workflow.work.WorkReportPredicate;
import hu.blackbelt.judo.tatami.core.workflow.work.NoOpWork;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import hu.blackbelt.judo.tatami.core.workflow.work.Work;
import hu.blackbelt.judo.tatami.psm2asm.Psm2AsmTransformationTrace;
import hu.blackbelt.judo.tatami.psm2asm.Psm2AsmWork;
import hu.blackbelt.judo.tatami.psm2measure.Psm2MeasureTransformationTrace;
import hu.blackbelt.judo.tatami.psm2measure.Psm2MeasureWork;
import hu.blackbelt.judo.tatami.rdbms2liquibase.Rdbms2LiquibaseWork;
import org.eclipse.emf.ecore.resource.ResourceSet;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static hu.blackbelt.judo.meta.asm.runtime.AsmModel.LoadArguments.asmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.expression.runtime.ExpressionModel.LoadArguments.expressionLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.LoadArguments.jslDslLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.liquibase.runtime.LiquibaseModel.LoadArguments.liquibaseLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.measure.runtime.MeasureModel.LoadArguments.measureLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.LoadArguments.psmLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.rdbms.runtime.RdbmsModel.LoadArguments.rdbmsLoadArgumentsBuilder;
import static hu.blackbelt.judo.meta.rdbmsDataTypes.support.RdbmsDataTypesModelResourceSupport.registerRdbmsDataTypesMetamodel;
import static hu.blackbelt.judo.meta.rdbmsNameMapping.support.RdbmsNameMappingModelResourceSupport.registerRdbmsNameMappingMetamodel;
import static hu.blackbelt.judo.meta.rdbmsRules.support.RdbmsTableMappingRulesModelResourceSupport.registerRdbmsTableMappingRulesMetamodel;
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
                                        .toURL().openStream())))));
    }

    public void loadAsmModel(final String modelName,
                             final AsmModel asmModel,
                             final URI asmModelSourceURI,
                             final Psm2AsmTransformationTrace psm2AsmTransformationTrace,
                             final URI psm2AsmTransformationTraceSourceURI) {

        if (asmModel == null && asmModelSourceURI == null) {
            return;
        }

        transformationContext.put(ofNullable(asmModel).orElseGet(
                ThrowingSupplier.sneaky(() -> AsmModel.loadAsmModel(asmLoadArgumentsBuilder()
                        .inputStream(
                                of(asmModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("asmModel or asmModelSourceUri have to be defined"))
                                        .toURL().openStream())))));

        Optional<PsmModel> psmModelFromContext = transformationContext.getByClass(PsmModel.class);

        AsmModel asmModelFromContext = transformationContext.getByClass(AsmModel.class).get();

        transformationContext.put(ofNullable(psm2AsmTransformationTrace).orElseGet(
                ThrowingSupplier.sneaky(() -> Psm2AsmTransformationTrace.fromModelsAndTrace(
                        modelName,
                        psmModelFromContext.orElseThrow(() ->
                                new IllegalArgumentException("psmModel have to be defined")),
                        asmModelFromContext,
                        of(psm2AsmTransformationTraceSourceURI).orElseThrow(() ->
                                        new IllegalArgumentException("psm2AsmTransformationTrace or psm2AsmTransformationTraceSourceURI have to be defined"))
                                .toURL().openStream()))));
    }

    public void loadMeasureModel(final String modelName,
                                 final MeasureModel measureModel,
                                 final URI measureModelSourceURI,
                                 final Psm2MeasureTransformationTrace psm2MeasureTransformationTrace,
                                 final URI psm2MeasureTransformationTraceSourceURI) {

        if (measureModel == null && measureModelSourceURI == null) {
            return;
        }

        transformationContext.put(ofNullable(measureModel).orElseGet(
                ThrowingSupplier.sneaky(() -> MeasureModel.loadMeasureModel(measureLoadArgumentsBuilder()
                        .inputStream(
                                of(measureModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("measureModel or measureModelSourceURI have to be defined"))
                                        .toURL().openStream())
                        .name(modelName)))));

        Optional<PsmModel> psmModelFromContext = transformationContext.getByClass(PsmModel.class);

        MeasureModel measureModelFromContext = transformationContext.getByClass(MeasureModel.class).get();

        transformationContext.put(ofNullable(psm2MeasureTransformationTrace).orElseGet(
                ThrowingSupplier.sneaky(() -> Psm2MeasureTransformationTrace.fromModelsAndTrace(
                        modelName,
                        psmModelFromContext.orElseThrow(() ->
                                new IllegalArgumentException("psmModel have to be defined")),
                        measureModelFromContext,
                        of(psm2MeasureTransformationTraceSourceURI).orElseThrow(() ->
                                        new IllegalArgumentException("psm2MeasureTransformationTrace or psm2MeasureTransformationTraceSourceURI have to be defined"))
                                .toURL().openStream()))));
    }

    public void loadExpressionModel(final String modelName,
                                    final ExpressionModel expressionModel,
                                    final URI expressionModelSourceURI) {

        if (expressionModel == null && expressionModelSourceURI == null) {
            return;
        }
        transformationContext.put(ofNullable(expressionModel).orElseGet(
                ThrowingSupplier.sneaky(() -> ExpressionModel.loadExpressionModel(expressionLoadArgumentsBuilder()
                        .inputStream(
                                of(expressionModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("expressionModel or expressionModelSourceUri have to be defined"))
                                        .toURL().openStream())
                        .name(modelName)))));
    }

    public void loadRdbmsModel(final String modelName,
                               final String dialect,
                               final RdbmsModel rdbmsModel,
                               final URI rdbmsModelSourceURI,
                               final Asm2RdbmsTransformationTrace asm2RdbmsTransformationTrace,
                               final URI asm2RdbmsTransformationTraceSourceURI) {

        if (rdbmsModel == null && rdbmsModelSourceURI == null) {
            return;
        }

        ResourceSet resourceSet = RdbmsModelResourceSupport.createRdbmsResourceSet();
        registerRdbmsNameMappingMetamodel(resourceSet);
        registerRdbmsDataTypesMetamodel(resourceSet);
        registerRdbmsTableMappingRulesMetamodel(resourceSet);
        transformationContext.put("rdbms:" + dialect, ofNullable(rdbmsModel).orElseGet(
                ThrowingSupplier.sneaky(() -> RdbmsModel.loadRdbmsModel(rdbmsLoadArgumentsBuilder()
                        .resourceSet(resourceSet)
                        .inputStream(
                                of(rdbmsModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("asmModel or asmModelSourceUri have to be defined"))
                                        .toURL().openStream())))));

        Optional<AsmModel> asmModelFromContext = transformationContext.getByClass(AsmModel.class);

        RdbmsModel rdbmsModelFromContext = transformationContext.get(RdbmsModel.class, "rdbms:" + dialect).get();

        transformationContext.put("asm2rdbmstrace:" + dialect, ofNullable(asm2RdbmsTransformationTrace).orElseGet(
                ThrowingSupplier.sneaky(() -> Asm2RdbmsTransformationTrace.fromModelsAndTrace(
                        modelName,
                        asmModelFromContext.orElseThrow(() ->
                                new IllegalArgumentException("asmModel have to be defined")),
                        rdbmsModelFromContext,
                        of(asm2RdbmsTransformationTraceSourceURI).orElseThrow(() ->
                                        new IllegalArgumentException("asm2RdbmsTransformationTrace or asm2RdbmsTransformationTraceSourceURI have to be defined"))
                                .toURL().openStream()))));
    }

    public void loadLiquibaseModel(final String modelName,
                                   final String dialect,
                                   final LiquibaseModel liquibaseModel,
                                   final URI liquibaseModelSourceURI) {

        if (liquibaseModel == null && liquibaseModelSourceURI == null) {
            return;
        }
        transformationContext.put("liquibase:" + dialect, ofNullable(liquibaseModel).orElseGet(
                ThrowingSupplier.sneaky(() -> LiquibaseModel.loadLiquibaseModel(liquibaseLoadArgumentsBuilder()
                        .inputStream(
                                of(liquibaseModelSourceURI).orElseThrow(() ->
                                                new IllegalArgumentException("liquibaseModel or liquibaseModelSourceURI have to be defined"))
                                        .toURL().openStream())
                        .name(modelName)))));
    }

    public void loadSdk(final InputStream sdk,
                        final URI sdkSourceURI,
                        final InputStream sdkInternal,
                        final URI sdkInternalSourceURI,
                        final InputStream sdkGuice,
                        final URI sdkGuiceSourceURI,
                        final InputStream sdkSpring,
                        final URI sdkSpringSourceURI

    ) {

        if (sdk == null && sdkSourceURI == null || sdkInternal == null && sdkInternalSourceURI == null ) {
            return;
        }
    }

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

    public Supplier<Boolean> psm2MeasureOutputPredicate() {
        return () -> transformationContext.transformationContextVerifier.verifyClassPresent(MeasureModel.class) &&
                transformationContext.transformationContextVerifier.verifyClassPresent(Psm2MeasureTransformationTrace.class);
    }

    public Work createPsm2MeasureWork() {
        return     aNewConditionalFlow()
                .named("Conditional when Psm model exists then Execute Psm2Measure")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(PsmModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Psm2Measure")
                                .execute(
                                        new Psm2MeasureWork(transformationContext).withMetricsCollector(workflowMetrics),
                                        new CheckWork(psm2MeasureOutputPredicate())
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }

    public Supplier<Boolean> psm2AsmOutputPredicate() {
        return () -> transformationContext.transformationContextVerifier.verifyClassPresent(AsmModel.class) &&
                transformationContext.transformationContextVerifier.verifyClassPresent(Psm2AsmTransformationTrace.class);
    }

    public Work createPsm2AsmWork() {
        return     aNewConditionalFlow()
                .named("Conditional when Psm model exists then Execute Psm2Asm")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(PsmModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Psm2Asm")
                                .execute(
                                        new Psm2AsmWork(transformationContext).withMetricsCollector(workflowMetrics),
                                        new CheckWork(psm2AsmOutputPredicate())
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }

    public Supplier<Boolean> asm2ExpressionOutputPredicate() {
        return () -> transformationContext.transformationContextVerifier.verifyClassPresent(ExpressionModel.class);
    }

    public Work createAsm2ExpressionWork(boolean validateExpression) {
        return     aNewConditionalFlow()
                .named("Conditional when Asm model exists then Execute Asm2Expression")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(AsmModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Asm2Expression")
                                .execute(
                                        aNewSequentialFlow().named("").execute(
                                                Stream.of(
                                                        Optional.of(new Asm2ExpressionWork(transformationContext).withMetricsCollector(workflowMetrics)),
                                                        validateExpression ? Optional.of(new ExpressionValidationOnAsmWork(transformationContext).withMetricsCollector(workflowMetrics)) : Optional.empty()
                                                ).filter(Optional::isPresent).map(Optional::get).toArray(Work[]::new)
                                        ).build(),
                                        new CheckWork(asm2ExpressionOutputPredicate())
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }

    public Supplier<Boolean> asm2RdbmsOutputPredicate(String dialect) {
        return () -> transformationContext.transformationContextVerifier.verifyKeyPresent(RdbmsModel.class, "rdbms:" + dialect) &&
                transformationContext.transformationContextVerifier.verifyKeyPresent(Asm2RdbmsTransformationTrace.class, "asm2rdbmstrace:" + dialect);
    }

    public Work createAsm2RdbmsWork(String dialect, boolean ignoreLiquibase) {
        return     aNewConditionalFlow()
                .named("Conditional when Asm model exists then Execute Asm2Rdbms")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyClassPresent(AsmModel.class)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Asm2Rdbms")
                                .execute(
                                        new Asm2RdbmsWork(transformationContext, dialect).withMetricsCollector(workflowMetrics),
                                        new CheckWork(asm2RdbmsOutputPredicate(dialect))
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }

    public Supplier<Boolean> rdbms2LiquibaseOutputPredicate(String dialect) {
        return () -> transformationContext.transformationContextVerifier.verifyKeyPresent(LiquibaseModel.class, "liquibase:" + dialect);
    }

    public Work createRdbms2LiquibaseWork(String dialect) {
        return     aNewConditionalFlow()
                .named("Conditional when Rdbms model exists then Execute Rdbms2Liquibase")
                .execute(new CheckWork(() -> transformationContext.transformationContextVerifier.verifyKeyPresent(RdbmsModel.class, "rdbms:" + dialect)))
                .when(WorkReportPredicate.COMPLETED)
                .then(
                        aNewSequentialFlow()
                                .named("Execute Rdbms2Liquibase")
                                .execute(
                                        new Rdbms2LiquibaseWork(transformationContext, dialect).withMetricsCollector(workflowMetrics),
                                        new CheckWork(rdbms2LiquibaseOutputPredicate(dialect))
                                )
                                .build()
                )
                .otherwise(new NoOpWork())
                .build();
    }

}
