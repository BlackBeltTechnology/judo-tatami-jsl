package org.judo.tatami.workflow;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.EntityDeclarationBuilder;
import hu.blackbelt.judo.meta.jsl.jsldsl.util.builder.ModelDeclarationBuilder;
import org.eclipse.emf.common.util.URI;

import java.io.IOException;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.buildJslDslModel;
import static java.util.Arrays.asList;

public class JslTestModel {

    public static final String MODEL_NAME = "test";

    public static final String FILE_LOCATION = "target/test-classes/jsl/" + MODEL_NAME + "-jsl.model";


    public static void createJslModelAndSave() throws JslDslModel.JslDslValidationException, IOException {
        JslDslModel jslModel = buildJslDslModel().uri(URI.createURI(FILE_LOCATION)).name(MODEL_NAME).build();

        final EntityDeclaration entityA = EntityDeclarationBuilder.create().withName("A").build();
        final EntityDeclaration entityB = EntityDeclarationBuilder.create().withName("B").build();

        final ModelDeclaration model = ModelDeclarationBuilder.create()
                .withName(MODEL_NAME)
                .withDeclarations(asList(entityA, entityB))
                .build();

        jslModel.addContent(model);
        jslModel.saveJslDslModel();
    }

}
