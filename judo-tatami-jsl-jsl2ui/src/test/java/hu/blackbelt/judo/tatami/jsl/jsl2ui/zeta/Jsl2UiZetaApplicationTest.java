package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.meta.ui.support.UiModelResourceSupport;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static hu.blackbelt.judo.meta.ui.runtime.UiModel.buildUiModel;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD tests for JSL2UI Zeta application rules.
 */
public class Jsl2UiZetaApplicationTest {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaApplicationTest.class);

    @Test
    void testActorClassType() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings("TestModel", List.of("""
                model TestModel;

                type string String min-size:0 max-size:255;

                entity TestEntity {
                    identifier String name;
                }

                transfer TestTransfer(TestEntity e) {
                    field String name <=> e.name;
                }

                actor TestActor realm:"TEST" claim:"email" identity:TestTransfer::name {
                    access TestTransfer[] items <= TestEntity.all();
                }

                frontend TestApp(TestActor usr);
                """));

        assertTrue(jslModel.isValid(), "JSL model should be valid");

        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();

        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();

        UiModelResourceSupport uiWrapper = UiModelResourceSupport
                .uiModelResourceSupportBuilder()
                .resourceSet(uiModel.getResourceSet())
                .build();

        // Verify ClassType for actor was created
        List<ClassType> classTypes = uiWrapper.getStreamOfUiDataClassType().toList();
        log.info("ClassTypes found: {}", classTypes.size());
        for (ClassType ct : classTypes) {
            log.info("  ClassType: name={}, simpleName={}, isActor={}", ct.getName(), ct.getSimpleName(), ct.isIsActor());
        }

        assertTrue(classTypes.stream().anyMatch(ct -> ct.isIsActor()),
                "At least one ClassType should be an actor");

        ClassType actorCt = classTypes.stream()
                .filter(ClassType::isIsActor)
                .findFirst()
                .orElseThrow();
        assertEquals("TestActor", actorCt.getSimpleName());
        assertTrue(actorCt.getName().contains("TestActor"));
        assertFalse(actorCt.getPackageNameTokens().isEmpty());
        assertEquals("TestModel", actorCt.getPackageNameTokens().get(0));
    }

    @Test
    void testAuthenticationCreation() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings("TestModel", List.of("""
                model TestModel;

                type string String min-size:0 max-size:255;

                entity TestEntity {
                    identifier String name;
                }

                transfer TestTransfer(TestEntity e) {
                    field String name <=> e.name;
                }

                actor TestActor realm:"MY_REALM" claim:"email" identity:TestTransfer::name {
                    access TestTransfer[] items <= TestEntity.all();
                }

                frontend TestApp(TestActor usr);
                """));

        assertTrue(jslModel.isValid(), "JSL model should be valid");

        UiModel uiModel = buildUiModel().name(jslModel.getName()).build();

        Jsl2UiZetaTransformation.builder()
                .jslModel(jslModel)
                .uiModel(uiModel)
                .defaultModelName(jslModel.getName())
                .build()
                .execute();

        UiModelResourceSupport uiWrapper = UiModelResourceSupport
                .uiModelResourceSupportBuilder()
                .resourceSet(uiModel.getResourceSet())
                .build();

        // Verify Authentication was created
        var authentications = uiWrapper.getStreamOfUiAuthentication().toList();
        log.info("Authentications found: {}", authentications.size());
        for (var auth : authentications) {
            log.info("  Authentication: realm={}, claims={}", auth.getRealm(), auth.getClaims().size());
        }

        assertEquals(1, authentications.size(), "Exactly one Authentication should be created");
        assertEquals("MY_REALM", authentications.get(0).getRealm());
    }
}
