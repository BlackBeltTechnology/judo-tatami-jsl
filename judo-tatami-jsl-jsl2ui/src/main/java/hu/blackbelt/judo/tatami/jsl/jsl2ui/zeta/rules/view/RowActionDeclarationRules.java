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
import hu.blackbelt.judo.meta.ui.data.ClassType;
import hu.blackbelt.judo.meta.ui.data.OperationType;
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
 * Ported from rowActionDeclaration.etl:
 * Creates Button and ActionDefinition rules for UIActionDeclaration within rows.
 * These are all @Lazy rules invoked from RowDeclarationRules.tableRowButtonGroup().
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class RowActionDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(RowActionDeclarationRules.class);

    @TransformRule(name = ROW_ACTION_DECLARATION_BUTTON, description = "Create Button for row action declaration")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIActionDeclaration, Button> rowActionDeclarationButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowActionDeclarationButton");
            target.setName(source.getName());
            target.setButtonStyle("text");

            LabelModifier labelMod = getLabelModifier(source);
            if (labelMod != null) {
                target.setLabel(labelMod.getValue().getValue());
            }
            if (target.getLabel() == null && target.getIcon() == null) {
                target.setLabel(target.getName());
            }

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            HelpModifier helpMod = getHelpModifier(source);
            if (helpMod != null) {
                target.setTooltipText(helpMod.getValue().getValue());
            }

            Integer sourcePos = posMap.get(source);
            posMap.put(target, sourcePos != null ? sourcePos : 0);

            if (isOpenOperationFormAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputFormActionDefinition.class,
                        ROW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
            } else if (isOpenOperationSelectorAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputSelectorActionDefinition.class,
                        ROW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
            } else {
                target.setActionDefinition(ctx.equivalent(source,
                        ParameterlessCallOperationActionDefinition.class,
                        ROW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
            }

            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));

            LOG.debug("RowActionDeclarationButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ROW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION, description = "Create call operation action definition for row action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = ParameterlessCallOperationActionDefinition.class)
    public TransformFunction<UIActionDeclaration, ParameterlessCallOperationActionDefinition> rowActionDeclarationCallOperationActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ParameterlessCallOperationActionDefinition target = ctx.createTarget(ParameterlessCallOperationActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowActionDeclarationCallOperationActionDefinition");
            target.setName(getFqName(source) + "::Call");
            target.setOperation(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setTargetType(ctx.equivalent(
                    source.getTransferAction().getTarget().getParameterType(),
                    ClassType.class, CLASS_TYPE));
            LOG.debug("RowActionDeclarationCallOperationActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ROW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION, description = "Create open selector action definition for row action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputSelectorActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputSelectorActionDefinition> rowActionDeclarationOpenSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputSelectorActionDefinition target = ctx.createTarget(OpenOperationInputSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowActionDeclarationOpenSelectorActionDefinition");
            target.setName(getFqName(source) + "::Open::Selector");

            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod != null && selectorMod.getRow() != null) {
                target.setTargetType(ctx.equivalent(
                        selectorMod.getRow().getMap().getTransfer(),
                        ClassType.class, CLASS_TYPE));
            }
            target.setSelectorFor(ctx.equivalent(source,
                    InputSelectorCallOperationActionDefinition.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION));
            LOG.debug("RowActionDeclarationOpenSelectorActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ROW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION, description = "Create open form action definition for row action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputFormActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputFormActionDefinition> rowActionDeclarationOpenFormActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputFormActionDefinition target = ctx.createTarget(OpenOperationInputFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowActionDeclarationOpenFormActionDefinition");
            target.setName(getFqName(source) + "::Open::Operation::Form");
            LOG.debug("RowActionDeclarationOpenFormActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = ROW_ACTION, description = "Create Action for row action declaration")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> rowAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/RowAction");
            target.setName(getFqName(source) + "::Action");

            if (isOpenOperationFormAction(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_INPUT_FORM_PAGE_DEFINITION));
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputFormActionDefinition.class,
                        ROW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
                if (target.getActionDefinition() instanceof OpenOperationInputFormActionDefinition) {
                    ((OpenOperationInputFormActionDefinition) target.getActionDefinition())
                            .setFormFor(ctx.equivalent(source,
                                    InputFormCallOperationActionDefinition.class,
                                    OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION));
                }
            } else if (isOpenOperationSelectorAction(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_INPUT_SELECTOR_PAGE_DEFINITION));
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputSelectorActionDefinition.class,
                        ROW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
            } else {
                if (hasActionOutput(source)) {
                    target.setTargetPageDefinition(ctx.equivalent(source,
                            PageDefinition.class, OPERATION_OUTPUT_PAGE_DEFINITION));
                }
                target.setActionDefinition(ctx.equivalent(source,
                        ParameterlessCallOperationActionDefinition.class,
                        ROW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
            }

            LOG.debug("RowAction: {}", target.getName());
            return target;
        };
    }
}
