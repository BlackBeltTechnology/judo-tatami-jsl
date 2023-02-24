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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.measure.Measure;
import hu.blackbelt.judo.meta.psm.measure.MeasuredType;
import hu.blackbelt.judo.meta.psm.measure.Unit;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.type.BooleanType;
import hu.blackbelt.judo.meta.psm.type.CustomType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import org.eclipse.emf.common.util.URI;

import java.io.IOException;

import static hu.blackbelt.judo.meta.psm.data.util.builder.DataBuilders.*;
import static hu.blackbelt.judo.meta.psm.measure.util.builder.MeasureBuilders.*;
import static hu.blackbelt.judo.meta.psm.namespace.util.builder.NamespaceBuilders.newModelBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.meta.psm.type.util.builder.TypeBuilders.*;

public class PsmTestModel {

    public static final String MODEL_NAME = "test";
    public static final String FILE_LOCATION = "target/test-classes/psm/" + MODEL_NAME + "-psm.model";

    public static void createPsmModelelAndSave() throws IOException, PsmModel.PsmValidationException {
        PsmModel psmModel = buildPsmModel().uri(URI.createURI(FILE_LOCATION)).build();

        StringType strType = newStringTypeBuilder().withName("string").withMaxLength(256).build();
        NumericType intType = newNumericTypeBuilder().withName("int").withPrecision(6).withScale(0).build();
        BooleanType boolType = newBooleanTypeBuilder().withName("bool").build();
        CustomType custom = newCustomTypeBuilder().withName("object").build();

        Unit unit = newUnitBuilder().withName("u").build();
        Measure m = newMeasureBuilder().withName("measure").withUnits(unit).build();
        MeasuredType measuredType = newMeasuredTypeBuilder().withName("measuredType").withStoreUnit(unit)
                .withPrecision(5).withScale(3).build();

        EntityType abstractEntity1 = newEntityTypeBuilder().withName("abstractEntity1").withAbstract_(true).build();
        EntityType abstractEntity2 = newEntityTypeBuilder().withName("abstractEntity2").withAbstract_(true).build();
        EntityType entity1 = newEntityTypeBuilder().withName("entity1")
                .withSuperEntityTypes(ImmutableList.of(abstractEntity1, abstractEntity2)).build();
        EntityType entity2 = newEntityTypeBuilder().withName("entity2").build();
        EntityType entity3 = newEntityTypeBuilder().withName("entity3").withSuperEntityTypes(ImmutableList.of(entity2))
                .build();
        EntityType entity4 = newEntityTypeBuilder().withName("entity4").withSuperEntityTypes(ImmutableList.of(entity3))
                .build();

        Attribute stringAttr = newAttributeBuilder().withName("a1").withDataType(strType).withRequired(true).build();
        Attribute customAttr = newAttributeBuilder().withName("a2").withDataType(custom).build();
        Attribute boolAttr = newAttributeBuilder().withName("a3").withDataType(boolType).build();
        Attribute intAttr = newAttributeBuilder().withName("a4").withDataType(intType).withRequired(true)
                .withIdentifier(true).build();
        Attribute measuredAttr = newAttributeBuilder().withName("a5").withDataType(measuredType).build();

        entity1.getAttributes().addAll(ImmutableList.of(stringAttr, customAttr, boolAttr, intAttr, measuredAttr));

        AssociationEnd association = newAssociationEndBuilder().withName("association")
                .withCardinality(newCardinalityBuilder().withLower(1).withUpper(1)).withTarget(entity4).build();
        AssociationEnd associationPartner1 = newAssociationEndBuilder().withName("associationPartner1")
                .withCardinality(newCardinalityBuilder().withLower(0).withUpper(-1)).withTarget(entity4).build();
        AssociationEnd associationPartner2 = newAssociationEndBuilder().withName("associationPartner2")
                .withCardinality(newCardinalityBuilder().withLower(0).withUpper(1)).withTarget(entity1).build();

        Containment containment = newContainmentBuilder().withName("containment")
                .withCardinality(newCardinalityBuilder().withLower(0).withUpper(-1)).withTarget(entity3).build();

        associationPartner1.setPartner(associationPartner2);
        associationPartner2.setPartner(associationPartner1);

        entity1.getRelations().addAll(ImmutableList.of(association, associationPartner1, containment));
        entity4.getRelations().addAll(ImmutableList.of(associationPartner2));

        Model model = newModelBuilder().withName("M").withElements(ImmutableList.of(abstractEntity1, abstractEntity2,
                entity1, entity2, entity3, entity4, strType, intType, boolType, custom, measuredType, m))
                .build();

        psmModel.addContent(model);
        psmModel.savePsmModel();
    }

}
