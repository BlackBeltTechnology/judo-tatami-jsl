package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.type;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.DataTypeDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EnumDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EnumLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.MaxFileSizeModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MimeTypesModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.RegexModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.UIFrontendDeclaration;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.data.BinaryType;
import hu.blackbelt.judo.meta.ui.data.BooleanType;
import hu.blackbelt.judo.meta.ui.data.DateType;
import hu.blackbelt.judo.meta.ui.data.EnumerationMember;
import hu.blackbelt.judo.meta.ui.data.EnumerationType;
import hu.blackbelt.judo.meta.ui.data.NumericType;
import hu.blackbelt.judo.meta.ui.data.StringType;
import hu.blackbelt.judo.meta.ui.data.TimeType;
import hu.blackbelt.judo.meta.ui.data.TimestampType;
import hu.blackbelt.judo.zeta.annotation.Greedy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from type.etl:
 * - CreateNumericType, CreateDateType, CreateTimeType, CreateTimestampType
 * - CreateBooleanType, CreateStringType, CreateBinaryType
 * - CreateEnumerationType, CreateEnumerationMember
 * - MimeType (@lazy)
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class TypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(TypeRules.class);

    @TransformRule(name = CREATE_NUMERIC_TYPE, description = "Create NumericType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = NumericType.class)
    public TransformFunction<DataTypeDeclaration, NumericType> createNumericType() {
        return (source, ctx) -> {
            if (!"numeric".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            NumericType target = ctx.createTarget(NumericType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateNumericType");

            target.setName(source.getName());
            target.setPrecision(getPrecision(source).getValue().intValue());
            target.setScale(getScale(source).getValue().intValue());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, NUMERIC_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created NumericType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_DATE_TYPE, description = "Create DateType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = DateType.class)
    public TransformFunction<DataTypeDeclaration, DateType> createDateType() {
        return (source, ctx) -> {
            if (!"date".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            DateType target = ctx.createTarget(DateType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateDateType");

            target.setName(source.getName());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, NUMERIC_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created DateType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_TIME_TYPE, description = "Create TimeType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = TimeType.class)
    public TransformFunction<DataTypeDeclaration, TimeType> createTimeType() {
        return (source, ctx) -> {
            if (!"time".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            TimeType target = ctx.createTarget(TimeType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateTimeType");

            target.setName(source.getName());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, NUMERIC_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created TimeType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_TIMESTAMP_TYPE, description = "Create TimestampType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = TimestampType.class)
    public TransformFunction<DataTypeDeclaration, TimestampType> createTimestampType() {
        return (source, ctx) -> {
            if (!"timestamp".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            TimestampType target = ctx.createTarget(TimestampType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateTimestampType");

            target.setName(source.getName());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, NUMERIC_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created TimestampType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_BOOLEAN_TYPE, description = "Create BooleanType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = BooleanType.class)
    public TransformFunction<DataTypeDeclaration, BooleanType> createBooleanType() {
        return (source, ctx) -> {
            if (!"boolean".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BooleanType target = ctx.createTarget(BooleanType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateBooleanType");

            target.setName(source.getName());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, BOOLEAN_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created BooleanType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_STRING_TYPE, description = "Create StringType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = StringType.class)
    public TransformFunction<DataTypeDeclaration, StringType> createStringType() {
        return (source, ctx) -> {
            if (!"string".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            StringType target = ctx.createTarget(StringType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateStringType");

            target.setName(source.getName());
            target.setMaxLength(getMaxSize(source).getValue().intValue());

            RegexModifier regex = getRegex(source);
            if (regex != null && regex.getRegex() != null && regex.getRegex().getValue() != null) {
                target.setRegExp(regex.getRegex().getValue());
            }

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, STRING_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created StringType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_BINARY_TYPE, description = "Create BinaryType for DataTypeDeclaration")
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = BinaryType.class)
    public TransformFunction<DataTypeDeclaration, BinaryType> createBinaryType() {
        return (source, ctx) -> {
            if (!"binary".equals(source.getPrimitive())) return null;

            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BinaryType target = ctx.createTarget(BinaryType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateBinaryType");

            target.setName(source.getName());

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);

            MimeTypesModifier mimeTypeMod = getMimeType(source);
            if (mimeTypeMod != null) {
                for (var mimeValue : mimeTypeMod.getValues()) {
                    String mimeStr = mimeValue.getValue().getValue();
                    hu.blackbelt.judo.meta.ui.data.MimeType uiMimeType =
                            ctx.createTarget(hu.blackbelt.judo.meta.ui.data.MimeType.class);
                    ctx.setElementId(uiMimeType, frontend.getName()
                            + "/(jsl/" + mimeStr + ")/MimeType");
                    String[] split = mimeStr.split("/");
                    uiMimeType.setType(split[0]);
                    uiMimeType.setSubType(split[1]);
                    app.getMimeTypes().add(uiMimeType);
                    target.getMimeTypes().add(uiMimeType);
                }
            }

            MaxFileSizeModifier maxFileSize = getMaxFileSize(source);
            if (maxFileSize != null) {
                target.setMaxFileSize(getMaxFileSizeValue(maxFileSize));
            }

            app.getDataTypes().add(target);

            LOG.debug("Created BinaryType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_ENUMERATION_TYPE, description = "Create EnumerationType for EnumDeclaration")
    @Greedy
    @Transform(type = EnumDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<EnumDeclaration, EnumerationType> createEnumerationType() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            EnumerationType target = ctx.createTarget(EnumerationType.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateEnumerationType");

            target.setName(source.getName());

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            EnumerationType operator = ctx.equivalent(actorDeclaration,
                    EnumerationType.class, ENUMERATION_OPERATION);
            target.setOperator(operator);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getDataTypes().add(target);

            LOG.debug("Created EnumerationType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = CREATE_ENUMERATION_MEMBER, description = "Create EnumerationMember for EnumLiteral")
    @Greedy
    @Transform(type = EnumLiteral.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<EnumLiteral, EnumerationMember> createEnumerationMember() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            EnumerationMember target = ctx.createTarget(EnumerationMember.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/CreateEnumerationMember");

            target.setName(source.getName());
            target.setOrdinal(source.getValue().intValue());

            EnumerationType parentEnum = ctx.equivalent(source.eContainer(),
                    EnumerationType.class, CREATE_ENUMERATION_TYPE);
            parentEnum.getMembers().add(target);

            LOG.debug("Created EnumerationMember: {}", target.getName());
            return target;
        };
    }

}
