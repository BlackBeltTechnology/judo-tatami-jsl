package hu.blackbelt.judo.tatami.jsl.jsl2ui;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.core.etl.EtlEngineMissingException;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core artifact without the judo-tatami-jsl-jsl2ui-etl add-on: ETL fails fast naming the artifact, ZETA runs.
 * See change: split-etl-artifacts.
 */
class Jsl2UiWorkEtlMissingTest {

    private TransformationContext context(TransformationMode mode) {
        JslDslModel jslModel = JslDslModel.buildJslDslModel().uri(URI.createURI("urn:test-jsl")).name("test").build();
        ModelDeclaration model = ModelDeclarationBuilder.create()
                .withName("test")
                .withDeclarations(asList(EntityDeclarationBuilder.create().withName("A").build()))
                .build();
        jslModel.addContent(model);
        TransformationContext context = new TransformationContext("test");
        context.put(jslModel);
        context.put(Jsl2UiWork.Jsl2UiWorkParameter.jsl2UiWorkParameter().transformationMode(mode).build());
        return context;
    }

    @Test
    void etlModeWithoutAddOnFailsNamingTheArtifact() {
        EtlEngineMissingException e = assertThrows(EtlEngineMissingException.class,
                () -> new Jsl2UiWork(context(TransformationMode.ETL)).execute());
        assertEquals(Jsl2UiEtlEngine.class, e.getEngineType());
        assertTrue(e.getMessage().contains("hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2ui-etl"), e.getMessage());
    }

    @Test
    void zetaModeRunsWithoutAddOn() {
        TransformationContext context = context(TransformationMode.ZETA);
        assertDoesNotThrow(() -> new Jsl2UiWork(context).execute());
        assertTrue(context.getByClass(UiModel.class).isPresent());
    }
}
