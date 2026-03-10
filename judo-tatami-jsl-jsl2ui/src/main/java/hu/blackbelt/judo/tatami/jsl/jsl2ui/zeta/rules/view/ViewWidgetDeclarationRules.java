package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view;

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

import hu.blackbelt.judo.meta.jsl.jsldsl.*;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import hu.blackbelt.judo.meta.ui.data.EnumerationMember;
import hu.blackbelt.judo.zeta.annotation.Greedy;
import hu.blackbelt.judo.zeta.annotation.Lazy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from viewWidgetDeclaration.etl:
 * Widget rules for each primitive type - creates appropriate UI input elements.
 * Each widget rule extends AbstractViewWidgetDeclaration (applied inline).
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewWidgetDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewWidgetDeclarationRules.class);

    @TransformRule(name = VIEW_WIDGET_ICON, description = "Create Icon for widget")
    @Lazy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewWidgetDeclaration, Icon> viewWidgetIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewWidgetIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "WidgetIcon");
            return target;
        };
    }

    @TransformRule(name = STRING_TYPE_TEXT_INPUT, description = "Create TextInput for string widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = TextInput.class)
    public TransformFunction<UIViewWidgetDeclaration, TextInput> stringTypeTextInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            String primitive = getWidgetPrimitive(source);
            if (!"string".equals(primitive)) return null;

            Modifier linesMod = getLines(source);
            if (linesMod != null && getModifierIntValue(linesMod) > 1) return null;

            TextInput target = ctx.createTarget(TextInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/StringTypeTextInput");

            addToParentContainer(source, target, ctx);

            if (isPredictive(source)) {
                target.setIsTypeAheadField(true);
            }

            LOG.debug("StringTypeTextInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = STRING_TYPE_TEXT_AREA, description = "Create TextArea for string widget with lines > 1")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = TextArea.class)
    public TransformFunction<UIViewWidgetDeclaration, TextArea> stringTypeTextArea() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            String primitive = getWidgetPrimitive(source);
            if (!"string".equals(primitive)) return null;

            Modifier linesMod = getLines(source);
            if (linesMod == null || getModifierIntValue(linesMod) <= 1) return null;

            TextArea target = ctx.createTarget(TextArea.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/StringTypeTextArea");

            int lines = getModifierIntValue(linesMod);
            target.setLines(lines);
            target.setRow((double) lines);

            addToParentContainer(source, target, ctx);

            LOG.debug("StringTypeTextArea: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = NUMERIC_TYPE_VISUAL_INPUT, description = "Create NumericInput for numeric widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = NumericInput.class)
    public TransformFunction<UIViewWidgetDeclaration, NumericInput> numericTypeVisualInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"numeric".equals(getWidgetPrimitive(source))) return null;

            NumericInput target = ctx.createTarget(NumericInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/NumericTypeVisualInput");

            addToParentContainer(source, target, ctx);

            LOG.debug("NumericTypeVisualInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = BOOLEAN_TYPE_TRINARY_LOGIC_COMBO, description = "Create TrinaryLogicCombo for boolean widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = TrinaryLogicCombo.class)
    public TransformFunction<UIViewWidgetDeclaration, TrinaryLogicCombo> booleanTypeTrinaryLogicCombo() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"boolean".equals(getWidgetPrimitive(source))) return null;

            TrinaryLogicCombo target = ctx.createTarget(TrinaryLogicCombo.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/BooleanTypeTrinaryLogicCombo");

            addToParentContainer(source, target, ctx);

            LOG.debug("BooleanTypeTrinaryLogicCombo: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = DATE_TYPE_INPUT, description = "Create DateInput for date widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = DateInput.class)
    public TransformFunction<UIViewWidgetDeclaration, DateInput> dateTypeInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"date".equals(getWidgetPrimitive(source))) return null;

            DateInput target = ctx.createTarget(DateInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/DateTypeInput");

            addToParentContainer(source, target, ctx);

            LOG.debug("DateTypeInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TIME_TYPE_TIME_INPUT, description = "Create TimeInput for time widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = TimeInput.class)
    public TransformFunction<UIViewWidgetDeclaration, TimeInput> timeTypeTimeInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"time".equals(getWidgetPrimitive(source))) return null;

            TimeInput target = ctx.createTarget(TimeInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TimeTypeTypeTimeInput");

            addToParentContainer(source, target, ctx);

            LOG.debug("TimeTypeTypeTimeInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = TIMESTAMP_TYPE_DATE_TIME_INPUT, description = "Create DateTimeInput for timestamp widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = DateTimeInput.class)
    public TransformFunction<UIViewWidgetDeclaration, DateTimeInput> timestampTypeDateTimeInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"timestamp".equals(getWidgetPrimitive(source))) return null;

            DateTimeInput target = ctx.createTarget(DateTimeInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/TimestampTypeDateTimeInput");

            addToParentContainer(source, target, ctx);

            LOG.debug("TimestampTypeDateTimeInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = BINARY_TYPE_INPUT, description = "Create BinaryTypeInput for binary widget")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = BinaryTypeInput.class)
    public TransformFunction<UIViewWidgetDeclaration, BinaryTypeInput> binaryTypeInput() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"binary".equals(getWidgetPrimitive(source))) return null;

            BinaryTypeInput target = ctx.createTarget(BinaryTypeInput.class);
            applyAbstractWidget(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/BinaryTypeInput");

            addToParentContainer(source, target, ctx);

            LOG.debug("BinaryTypeInput: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ENUMERATION_TYPE_COMBO, description = "Create EnumerationCombo for enum widget (combo)")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = EnumerationCombo.class)
    public TransformFunction<UIViewWidgetDeclaration, EnumerationCombo> enumerationTypeCombo() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"enum".equals(getWidgetPrimitive(source))) return null;
            if (source.getType() != null && "RadioWidget".equals(source.getType().getName())) return null;

            EnumerationCombo target = ctx.createTarget(EnumerationCombo.class);
            applyAbstractWidget(source, target, ctx);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/EnumerationTypeCombo";
            ctx.setElementId(target, id);

            addToParentContainer(source, target, ctx);

            EnumDeclaration enumDecl = (EnumDeclaration) source.getTransferField().getTarget().getReferenceType();
            for (EnumLiteral literal : enumDecl.getLiterals()) {
                target.getOptions().add(ctx.equivalentDiscriminated(literal, Option.class,
                        ENUMERATION_MEMBER_OPTION, id));
            }

            LOG.debug("EnumerationTypeCombo: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ENUMERATION_TYPE_RADIO, description = "Create EnumerationRadio for enum widget (radio)")
    @Greedy
    @Transform(type = UIViewWidgetDeclaration.class)
    @To(type = EnumerationRadio.class)
    public TransformFunction<UIViewWidgetDeclaration, EnumerationRadio> enumerationTypeRadio() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.eContainer() instanceof UICardDeclaration) return null;
            if (!"enum".equals(getWidgetPrimitive(source))) return null;
            if (source.getType() == null || !"RadioWidget".equals(source.getType().getName())) return null;

            EnumerationRadio target = ctx.createTarget(EnumerationRadio.class);
            applyAbstractWidget(source, target, ctx);
            String id = frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/EnumerationTypeRadio";
            ctx.setElementId(target, id);

            addToParentContainer(source, target, ctx);

            EnumDeclaration enumDecl = (EnumDeclaration) source.getTransferField().getTarget().getReferenceType();
            for (EnumLiteral literal : enumDecl.getLiterals()) {
                target.getOptions().add(ctx.equivalentDiscriminated(literal, Option.class,
                        ENUMERATION_MEMBER_OPTION, id));
            }

            LOG.debug("EnumerationTypeRadio: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // EnumerationMemberOption (ported from enumLiteral.etl)
    // =========================================================================

    @TransformRule(name = ENUMERATION_MEMBER_OPTION, description = "Create Option for EnumLiteral")
    @Lazy
    @Transform(type = EnumLiteral.class)
    @To(type = Option.class)
    public TransformFunction<EnumLiteral, Option> enumerationMemberOption() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            Option target = ctx.createTarget(Option.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/EnumerationMemberOption");
            target.setSelected(false);
            target.setName(source.getName());
            target.setEnumerationMember(ctx.equivalent(source, EnumerationMember.class,
                    CREATE_ENUMERATION_MEMBER));
            return target;
        };
    }

    // =========================================================================
    // Shared logic (AbstractViewWidgetDeclaration)
    // =========================================================================

    private void applyAbstractWidget(UIViewWidgetDeclaration source, Input target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());
        target.setLabel(getLabelWithNameFallback(source));

        IconModifier iconMod = getIconModifier(source);
        if (iconMod != null) {
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_WIDGET_ICON));
        }

        HelpModifier helpMod = getHelpModifier(source);
        if (helpMod != null) {
            target.setTooltipText(helpMod.getValue().getValue());
        }

        Modifier widthMod = getWidth(source);
        target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

        AttributeType attrType = getTransferFieldDeclarationEquivalent(source, ctx);
        target.setAttributeType(attrType);

        target.setIsReadOnly(source.getBind() == null);

        ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
        posMap.put(target, getPos(source));
    }

    private void addToParentContainer(UIViewWidgetDeclaration source, VisualElement target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        VisualElement container = resolveUiContainer(source.eContainer(), ctx);
        if (container instanceof Flex) {
            ((Flex) container).getChildren().add(target);
        }
    }
}
