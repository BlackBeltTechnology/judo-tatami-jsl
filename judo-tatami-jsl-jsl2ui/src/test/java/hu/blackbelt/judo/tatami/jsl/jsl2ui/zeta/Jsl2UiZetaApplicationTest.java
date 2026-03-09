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
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.data.*;
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

    @Test
    void testStructureRules() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings("StructureTestModel", List.of("""
                model StructureTestModel;

                type string String min-size:0 max-size:255;

                entity User {
                    identifier String email required;
                    field String firstName;
                }

                transfer UserTransfer(User u) {
                    field String email <=> u.email;
                    field String firstName <= u.firstName;
                    field String transientField;

                    event create onCreate;
                    event update onUpdate;
                    event delete onDelete;
                }

                actor TestActor realm:"COMPANY" claim:"email" identity:UserTransfer::email {
                    access UserTransfer[] users <= User.all() create delete update;
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

        // Verify ClassTypes were created for exposed transfer objects
        List<ClassType> classTypes = uiWrapper.getStreamOfUiDataClassType().toList();
        log.info("ClassTypes found: {}", classTypes.size());
        for (ClassType ct : classTypes) {
            log.info("  ClassType: name={}, simpleName={}, isMapped={}, attrs={}, rels={}",
                    ct.getName(), ct.getSimpleName(), ct.isIsMapped(),
                    ct.getAttributes().size(), ct.getRelations().size());
        }

        // UserTransfer ClassType should exist
        ClassType userCt = classTypes.stream()
                .filter(ct -> "UserTransfer".equals(ct.getSimpleName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("UserTransfer ClassType not found"));
        assertTrue(userCt.isIsMapped(), "UserTransfer should be mapped");

        // Verify attributes
        List<AttributeType> attributes = userCt.getAttributes();
        log.info("Attributes for UserTransfer: {}", attributes.size());
        for (AttributeType attr : attributes) {
            log.info("  Attribute: name={}, memberType={}, isReadOnly={}",
                    attr.getName(), attr.getMemberType(), attr.isIsReadOnly());
        }

        assertTrue(attributes.stream().anyMatch(a -> "email".equals(a.getName())
                        && a.getMemberType() == MemberType.MAPPED),
                "email attribute should be mapped");
        assertTrue(attributes.stream().anyMatch(a -> "firstName".equals(a.getName())
                        && a.getMemberType() == MemberType.DERIVED),
                "firstName attribute should be derived (reads)");
        assertTrue(attributes.stream().anyMatch(a -> "transientField".equals(a.getName())
                        && a.getMemberType() == MemberType.TRANSIENT),
                "transientField attribute should be transient");

        // Verify relations (access 'users' should be a RelationType)
        ClassType actorCt = classTypes.stream()
                .filter(ClassType::isIsActor)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Actor ClassType not found"));

        List<RelationType> relations = actorCt.getRelations();
        log.info("Relations for actor: {}", relations.size());
        for (RelationType rel : relations) {
            log.info("  Relation: name={}, isAccess={}, isCollection={}, behaviours={}",
                    rel.getName(), rel.isIsAccess(), rel.isIsCollection(), rel.getBehaviours());
        }

        assertTrue(relations.stream().anyMatch(r -> "users".equals(r.getName()) && r.isIsAccess()),
                "users relation should be an access relation");
    }

    @Test
    void testTypeRules() throws Exception {
        JslDslModel jslModel = JslParser.getModelFromStrings("TypeTestModel", List.of("""
                model TypeTestModel;

                type string String min-size:0 max-size:255;
                type numeric Decimal precision:10 scale:2;
                type boolean Bool;
                type date Date;
                type time Time;
                type timestamp Timestamp;

                enum Status {
                    ACTIVE = 0;
                    INACTIVE = 1;
                }

                entity TestEntity {
                    identifier String name required;
                }

                transfer TestTransfer(TestEntity e) {
                    field String name <=> e.name;
                }

                actor TestActor realm:"REALM" claim:"email" identity:TestTransfer::name {
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

        // Verify Application has dataTypes
        List<Application> apps = uiWrapper.getStreamOfUiApplication().toList();
        log.info("Applications found: {}", apps.size());
        for (Application a : apps) {
            log.info("  Application: name={}, dataTypes={}", a.getName(), a.getDataTypes().size());
        }
        assertTrue(apps.size() >= 1, "Should have at least one Application");

        Application app = apps.get(0);
        List<DataType> dataTypes = app.getDataTypes();
        log.info("DataTypes found: {}", dataTypes.size());
        for (DataType dt : dataTypes) {
            log.info("  DataType: {} ({})", dt.getName(), dt.getClass().getSimpleName());
        }

        // Verify specific type instances
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof StringType
                        && "String".equals(dt.getName())),
                "String type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof NumericType
                        && "Decimal".equals(dt.getName())),
                "Numeric type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof BooleanType
                        && "Bool".equals(dt.getName())),
                "Boolean type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof DateType
                        && "Date".equals(dt.getName())),
                "Date type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof TimeType
                        && "Time".equals(dt.getName())),
                "Time type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof TimestampType
                        && "Timestamp".equals(dt.getName())),
                "Timestamp type should exist");

        // Verify enumeration type
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof EnumerationType
                        && "Status".equals(dt.getName())),
                "Status enum type should exist");

        // Verify NumericType precision/scale
        NumericType numericType = dataTypes.stream()
                .filter(dt -> dt instanceof NumericType)
                .map(dt -> (NumericType) dt)
                .findFirst().orElseThrow();
        assertEquals(10, numericType.getPrecision(), "Decimal precision should be 10");
        assertEquals(2, numericType.getScale(), "Decimal scale should be 2");

        // Verify StringType maxLength
        StringType stringType = dataTypes.stream()
                .filter(dt -> dt instanceof StringType)
                .map(dt -> (StringType) dt)
                .findFirst().orElseThrow();
        assertEquals(255, stringType.getMaxLength(), "String maxLength should be 255");

        // Verify operator EnumerationTypes exist (BooleanOperation, NumericOperation, etc.)
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof EnumerationType
                        && "BooleanOperation".equals(dt.getName())),
                "BooleanOperation type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof EnumerationType
                        && "NumericOperation".equals(dt.getName())),
                "NumericOperation type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof EnumerationType
                        && "StringOperation".equals(dt.getName())),
                "StringOperation type should exist");
        assertTrue(dataTypes.stream().anyMatch(dt -> dt instanceof EnumerationType
                        && "EnumerationOperation".equals(dt.getName())),
                "EnumerationOperation type should exist");

        // Verify operators are linked
        assertNotNull(numericType.getOperator(), "NumericType should have operator");
        assertEquals("NumericOperation", numericType.getOperator().getName());
        assertNotNull(stringType.getOperator(), "StringType should have operator");
        assertEquals("StringOperation", stringType.getOperator().getName());

        // Verify NumericOperation has members
        EnumerationType numericOp = (EnumerationType) dataTypes.stream()
                .filter(dt -> "NumericOperation".equals(dt.getName()))
                .findFirst().orElseThrow();
        assertEquals(6, numericOp.getMembers().size(),
                "NumericOperation should have 6 members");

        // Verify Status enum has members
        EnumerationType statusEnum = (EnumerationType) dataTypes.stream()
                .filter(dt -> "Status".equals(dt.getName()))
                .findFirst().orElseThrow();
        assertEquals(2, statusEnum.getMembers().size(),
                "Status enum should have 2 members");
        assertTrue(statusEnum.getMembers().stream().anyMatch(m -> "ACTIVE".equals(m.getName())),
                "Status should have ACTIVE member");
        assertTrue(statusEnum.getMembers().stream().anyMatch(m -> "INACTIVE".equals(m.getName())),
                "Status should have INACTIVE member");
    }
}
