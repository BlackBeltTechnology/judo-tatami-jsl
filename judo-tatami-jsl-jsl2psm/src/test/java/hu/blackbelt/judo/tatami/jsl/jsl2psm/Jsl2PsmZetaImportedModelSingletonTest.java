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

import com.google.common.collect.ImmutableList;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.namespace.NamedElement;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.tatami.core.TransformationMode;
import hu.blackbelt.judo.tatami.core.workflow.work.TransformationContext;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression: an imported model with a mapped transfer gets query customizers whose attributes must reference the
 * single shared "extensions" types (ETL semantics), not an orphaned per-model copy outside the PSM resource.
 * Found when the JSL Maven plugin default switched to ZETA (FunctionsModels itest). See change: split-etl-artifacts.
 */
class Jsl2PsmZetaImportedModelSingletonTest {

    @Test
    void importedModelQueryCustomizerTypesResolveToSharedExtensionsTypes() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings("Main", ImmutableList.of(
                "model Externals;\n"
                        + "type string String min-size:0 max-size:128;\n"
                        + "entity ExternalEntity {\n"
                        + "    field String name;\n"
                        + "}\n"
                        + "transfer TransferExternalEntity (ExternalEntity mapped) {\n"
                        + "    field String name <=> mapped.name;\n"
                        + "}\n",
                "model Main;\n"
                        + "import Externals as X;\n"
                        + "type string String min-size:0 max-size:128;\n"
                        + "entity MainEntity {\n"
                        + "    field String name;\n"
                        + "}\n"
                        + "transfer TransferMainEntity (MainEntity mapped) {\n"
                        + "    field String name <=> mapped.name;\n"
                        + "}\n"));

        TransformationContext context = new TransformationContext("Main");
        context.put(jslModel);
        context.put(Jsl2PsmWork.Jsl2PsmWorkParameter.jsl2PsmWorkParameter()
                .transformationMode(TransformationMode.ZETA).generateBehaviours(true).build());
        new Jsl2PsmWork(context).execute();

        PsmModel psmModel = context.getByClass(PsmModel.class).orElseThrow();
        List<TransferAttribute> attributes = new ArrayList<>();
        psmModel.getResource().getAllContents().forEachRemaining(o -> {
            if (o instanceof TransferAttribute) {
                attributes.add((TransferAttribute) o);
            }
        });
        assertFalse(attributes.isEmpty());

        List<String> dangling = attributes.stream()
                .filter(a -> a.getDataType() == null || a.getDataType().eResource() != psmModel.getResource())
                .map(a -> ((NamedElement) a.eContainer()).getName() + "." + a.getName())
                .collect(Collectors.toList());
        assertTrue(dangling.isEmpty(), "attributes whose dataType is not in the PSM resource: " + dangling);

        // query customizers of BOTH models exist ...
        assertTrue(attributes.stream().anyMatch(a -> ((NamedElement) a.eContainer()).getName().contains("TransferExternalEntity")
                && "_mask".equals(a.getName())), "imported model has query customizer attributes");
        // ... and share one instance of each extensions singleton type
        Map<String, Long> singletonCounts = new java.util.HashMap<>();
        psmModel.getResource().getAllContents().forEachRemaining(o -> {
            if (o instanceof NamedElement && ((NamedElement) o).getName() != null
                    && ((NamedElement) o).getName().startsWith("QueryCustomizer") && ((NamedElement) o).getName().endsWith("Type")) {
                singletonCounts.merge(((NamedElement) o).getName(), 1L, Long::sum);
            }
        });
        assertFalse(singletonCounts.isEmpty());
        singletonCounts.forEach((name, count) -> assertEquals(1L, count, name + " must exist once"));
    }
}
