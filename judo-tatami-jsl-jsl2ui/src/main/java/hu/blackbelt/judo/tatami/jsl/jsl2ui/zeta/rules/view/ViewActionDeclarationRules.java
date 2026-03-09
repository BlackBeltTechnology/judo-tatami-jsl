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
 * Ported from viewActionDeclaration.etl:
 * Creates Buttons and ActionDefinitions for UIActionDeclaration in view context,
 * plus operation input/output page definitions and actions.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewActionDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewActionDeclarationRules.class);

    // =========================================================================
    // ViewActionDeclarationButton (@greedy)
    // =========================================================================

    @TransformRule(name = VIEW_ACTION_DECLARATION_BUTTON, description = "Create Button for view action declaration")
    @Greedy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIActionDeclaration, Button> viewActionDeclarationButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            // Guard: not in row or action group context
            if (source.eContainer() instanceof UIRowDeclaration) return null;
            if (source.eContainer() instanceof ActionGroupModifier) return null;

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewActionDeclarationButton");
            target.setName(source.getName());
            target.setButtonStyle("text");

            LabelModifier labelMod = getLabelModifier(source);
            if (labelMod != null) {
                target.setLabel(labelMod.getValue().getValue());
            }
            if (target.getLabel() == null && target.getIcon() == null) {
                target.setLabel(target.getName());
            }

            HelpModifier helpMod = getHelpModifier(source);
            if (helpMod != null) {
                target.setTooltipText(helpMod.getValue().getValue());
            }

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            Integer sourcePos = posMap.get(source);
            posMap.put(target, sourcePos != null ? sourcePos : 0);

            if (isOpenOperationFormAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputFormActionDefinition.class,
                        VIEW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
            } else if (isOpenOperationSelectorAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputSelectorActionDefinition.class,
                        VIEW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
            } else {
                target.setActionDefinition(ctx.equivalent(source,
                        ParameterlessCallOperationActionDefinition.class,
                        VIEW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
            }

            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));

            // Add to parent container
            VisualElement uiContainer = resolveUiContainer(source.eContainer(), ctx);
            if (uiContainer instanceof ButtonGroup) {
                ((ButtonGroup) uiContainer).getButtons().add(target);
            } else if (uiContainer instanceof Flex) {
                ((Flex) uiContainer).getChildren().add(target);
            }

            LOG.debug("ViewActionDeclarationButton: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // View action definition rules (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION, description = "Create call operation action definition")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = ParameterlessCallOperationActionDefinition.class)
    public TransformFunction<UIActionDeclaration, ParameterlessCallOperationActionDefinition> viewActionDeclarationCallOperationActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ParameterlessCallOperationActionDefinition target = ctx.createTarget(ParameterlessCallOperationActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewActionDeclarationCallOperationActionDefinition");
            target.setName(getFqName(source) + "::Call");
            target.setOperation(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            LOG.debug("ViewActionDeclarationCallOperationActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION, description = "Create open selector action definition")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputSelectorActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputSelectorActionDefinition> viewActionDeclarationOpenSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputSelectorActionDefinition target = ctx.createTarget(OpenOperationInputSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewActionDeclarationOpenSelectorActionDefinition");
            target.setName(getFqName(source) + "::Open::Selector");
            target.setSelectorFor(ctx.equivalent(source,
                    InputSelectorCallOperationActionDefinition.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION));
            LOG.debug("ViewActionDeclarationOpenSelectorActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION, description = "Create open form action definition")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputFormActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputFormActionDefinition> viewActionDeclarationOpenFormActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputFormActionDefinition target = ctx.createTarget(OpenOperationInputFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewActionDeclarationOpenFormActionDefinition");
            target.setName(getFqName(source) + "::Open::Operation::Form");
            LOG.debug("ViewActionDeclarationOpenFormActionDefinition: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // ViewAction (@lazy) - creates Action with routing logic
    // =========================================================================

    @TransformRule(name = VIEW_ACTION, description = "Create Action for view action declaration")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> viewAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewAction");
            target.setName(getFqName(source) + "::Action");

            if (isOpenOperationFormAction(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_INPUT_FORM_PAGE_DEFINITION));
                // Use row/actionGroup form defs vs view form defs based on container
                if (source.eContainer() instanceof UIRowDeclaration
                        || source.eContainer() instanceof ActionGroupModifier) {
                    target.setActionDefinition(ctx.equivalent(source,
                            OpenOperationInputFormActionDefinition.class,
                            VIEW_TABLE_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
                } else {
                    target.setActionDefinition(ctx.equivalent(source,
                            OpenOperationInputFormActionDefinition.class,
                            VIEW_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
                }
                if (target.getActionDefinition() instanceof OpenOperationInputFormActionDefinition) {
                    ((OpenOperationInputFormActionDefinition) target.getActionDefinition())
                            .setFormFor(ctx.equivalent(source,
                                    InputFormCallOperationActionDefinition.class,
                                    OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION));
                }
            } else if (isOpenOperationSelectorAction(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_INPUT_SELECTOR_PAGE_DEFINITION));
                if (source.eContainer() instanceof UIRowDeclaration
                        || source.eContainer() instanceof ActionGroupModifier) {
                    target.setActionDefinition(ctx.equivalent(source,
                            OpenOperationInputSelectorActionDefinition.class,
                            VIEW_TABLE_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
                } else {
                    target.setActionDefinition(ctx.equivalent(source,
                            OpenOperationInputSelectorActionDefinition.class,
                            VIEW_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
                }
            } else {
                if (hasActionOutput(source)) {
                    target.setTargetPageDefinition(ctx.equivalent(source,
                            PageDefinition.class, OPERATION_OUTPUT_PAGE_DEFINITION));
                }
                if (source.eContainer() instanceof UIRowDeclaration
                        || source.eContainer() instanceof ActionGroupModifier) {
                    target.setActionDefinition(ctx.equivalent(source,
                            ParameterlessCallOperationActionDefinition.class,
                            VIEW_TABLE_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
                } else {
                    target.setActionDefinition(ctx.equivalent(source,
                            ParameterlessCallOperationActionDefinition.class,
                            VIEW_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
                }
            }

            LOG.debug("ViewAction: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Operation Input Form rules
    // =========================================================================

    @TransformRule(name = OPERATION_INPUT_FORM_PAGE_DEFINITION, description = "Create PageDefinition for operation input form")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIActionDeclaration, PageDefinition> operationInputFormPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormPageDefinition");
            target.setOpenInDialog(true);
            target.setName(getFqName(source) + "::OperationInputForm");
            target.setContainer(ctx.equivalent(source.getParameterType(),
                    PageContainer.class, FORM_PAGE_CONTAINER));
            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_INPUT_PARAMETER_TYPE));

            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_FORM_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_FORM_CALL_ACTION));
            if (isGetTemplateSupported(source.getTransferAction().getTarget().getParameterType())) {
                target.getActions().add(ctx.equivalent(source, Action.class,
                        OPERATION_INPUT_FORM_GET_TEMPLATE_ACTION));
            }

            // Add call action button to the form page container's action button group
            target.getContainer().getActionButtonGroups().get(0).getButtons()
                    .add(ctx.equivalent(source, Button.class,
                            OPERATION_INPUT_FORM_CALL_ACTION_BUTTON));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("OperationInputFormPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_BACK_ACTION, description = "Create back action for input form")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputFormBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormBackAction");
            target.setName(source.getName() + "::Back");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getParameterType(),
                    BackActionDefinition.class, FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("OperationInputFormBackAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_GET_TEMPLATE_ACTION, description = "Create get-template action for input form")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputFormGetTemplateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormGetTemplateAction");
            target.setName(source.getName() + "::GetTemplate");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getParameterType(),
                    GetTemplateActionDefinition.class, FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION));
            LOG.debug("OperationInputFormGetTemplateAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_CALL_ACTION_BUTTON, description = "Create call action button for input form")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIActionDeclaration, Button> operationInputFormCallActionButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormCallActionButton");
            target.setName(getFqName(source) + "::CallAction");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source,
                    InputFormCallOperationActionDefinition.class,
                    OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION));
            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class,
                        OPERATION_INPUT_FORM_CALL_ACTION_BUTTON_ICON));
            }

            LabelModifier labelMod = getLabelModifier(source);
            if (labelMod != null) {
                target.setLabel(labelMod.getValue().getValue());
            }
            if (target.getLabel() == null && target.getIcon() == null) {
                target.setLabel(target.getName());
            }

            HelpModifier helpMod = getHelpModifier(source);
            if (helpMod != null) {
                target.setTooltipText(helpMod.getValue().getValue());
            }

            LOG.debug("OperationInputFormCallActionButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_CALL_ACTION_BUTTON_ICON, description = "Create Icon for form call action button")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIActionDeclaration, Icon> operationInputFormCallActionButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormCallActionButtonIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "Icon");
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION, description = "Create InputFormCallOperationActionDefinition")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = InputFormCallOperationActionDefinition.class)
    public TransformFunction<UIActionDeclaration, InputFormCallOperationActionDefinition> operationInputFormCallActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            InputFormCallOperationActionDefinition target = ctx.createTarget(InputFormCallOperationActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormCallActionDefinition");
            target.setName(getFqName(source) + "::FormCall");
            target.setOperation(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            LOG.debug("OperationInputFormCallActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_FORM_CALL_ACTION, description = "Create form call Action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputFormCallAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputFormCallAction");
            target.setName(source.getName() + "::CallOperation");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setTargetDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source,
                    InputFormCallOperationActionDefinition.class,
                    OPERATION_INPUT_FORM_CALL_ACTION_DEFINITION));
            if (hasActionOutput(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_OUTPUT_PAGE_DEFINITION));
            }
            LOG.debug("OperationInputFormCallAction: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Operation Input Selector rules
    // =========================================================================

    @TransformRule(name = OPERATION_INPUT_SELECTOR_PAGE_DEFINITION, description = "Create PageDefinition for operation input selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIActionDeclaration, PageDefinition> operationInputSelectorPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod == null || selectorMod.getRow() == null) return null;

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorPageDefinition");
            target.setName(getFqName(source) + "::OperationInputSelector");
            target.setOpenInDialog(true);
            target.setContainer(ctx.equivalent(selectorMod.getRow(),
                    PageContainer.class, TABLE_PAGE_CONTAINER));
            target.setIsSelector(true);

            // Add call action button to selector's action button group
            target.getContainer().getActionButtonGroups().get(0).getButtons()
                    .add(ctx.equivalent(source, Button.class,
                            OPERATION_INPUT_SELECTOR_CALL_ACTION_BUTTON));

            OperationType opType = ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE);
            target.setDataElement(opType.getInput());

            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_SELECTOR_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_SELECTOR_RANGE_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_SELECTOR_FILTER_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION));

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("OperationInputSelectorPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_CALL_ACTION_BUTTON, description = "Create call action button for selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIActionDeclaration, Button> operationInputSelectorCallActionButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorCallActionButton");
            target.setName(getFqName(source) + "::CallAction");
            target.setButtonStyle("contained");
            target.setActionDefinition(ctx.equivalent(source,
                    InputSelectorCallOperationActionDefinition.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION));
            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));

            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(iconMod, Icon.class, ICON_MODIFIER_ICON));
            }

            LabelModifier labelMod = getLabelModifier(source);
            if (labelMod != null) {
                target.setLabel(labelMod.getValue().getValue());
            }
            if (target.getLabel() == null && target.getIcon() == null) {
                target.setLabel(target.getName());
            }

            HelpModifier helpMod = getHelpModifier(source);
            if (helpMod != null) {
                target.setTooltipText(helpMod.getValue().getValue());
            }

            LOG.debug("OperationInputSelectorCallActionButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION, description = "Create InputSelectorCallOperationActionDefinition")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = InputSelectorCallOperationActionDefinition.class)
    public TransformFunction<UIActionDeclaration, InputSelectorCallOperationActionDefinition> operationInputSelectorCallActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            InputSelectorCallOperationActionDefinition target = ctx.createTarget(InputSelectorCallOperationActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorCallActionDefinition");
            target.setName(getFqName(source) + "::SelectorCall");
            target.setOperation(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            LOG.debug("OperationInputSelectorCallActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_BACK_ACTION, description = "Create back action for selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputSelectorBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod == null || selectorMod.getRow() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorBackAction");
            target.setName(source.getName() + "::Back");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(selectorMod.getRow(),
                    BackActionDefinition.class, TABLE_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("OperationInputSelectorBackAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_CALL_ACTION, description = "Create call action for selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputSelectorCallAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorCallAction");
            target.setName(source.getName() + "::CallOperation");
            OperationType opType = ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE);
            target.setTargetDataElement(opType);
            target.setActionDefinition(ctx.equivalent(source,
                    InputSelectorCallOperationActionDefinition.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION));
            if (hasActionOutput(source)) {
                target.setTargetPageDefinition(ctx.equivalent(source,
                        PageDefinition.class, OPERATION_OUTPUT_PAGE_DEFINITION));
            }
            LOG.debug("OperationInputSelectorCallAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_RANGE_ACTION, description = "Create range action for selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputSelectorRangeAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod == null || selectorMod.getRow() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorRangeAction");
            target.setName(source.getName() + "::Refresh");
            OperationType opType = ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE);
            target.setOwnerDataElement(opType);
            target.setTargetDataElement(opType.getInput());
            target.setActionDefinition(ctx.equivalent(selectorMod.getRow(),
                    RefreshActionDefinition.class, TABLE_TABLE_REFRESH_ACTION_DEFINITION));
            LOG.debug("OperationInputSelectorRangeAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_INPUT_SELECTOR_FILTER_ACTION, description = "Create filter action for selector")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationInputSelectorFilterAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod == null || selectorMod.getRow() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationInputSelectorFilterAction");
            target.setName(source.getName() + "::Filter");
            OperationType opType = ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE);
            target.setOwnerDataElement(opType);
            target.setTargetDataElement(opType.getInput());
            target.setActionDefinition(ctx.equivalent(selectorMod.getRow(),
                    FilterActionDefinition.class, TABLE_TABLE_FILTER_ACTION_DEFINITION));
            LOG.debug("OperationInputSelectorFilterAction: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // Operation Output rules
    // =========================================================================

    @TransformRule(name = OPERATION_OUTPUT_PAGE_DEFINITION, description = "Create PageDefinition for operation output")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIActionDeclaration, PageDefinition> operationOutputPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source.getReturn() == null) return null;

            OperationType opType = ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE);

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputPageDefinition");
            target.setName(getFqName(source) + "::OperationOutput");
            target.setContainer(ctx.equivalent(source.getReturn(),
                    PageContainer.class, VIEW_PAGE_CONTAINER));
            target.setDataElement(opType.getOutput());
            target.setOpenInDialog(true);
            target.setDialogSize(DialogSize.LG);

            target.getActions().add(ctx.equivalent(source, Action.class,
                    OPERATION_OUTPUT_BACK_ACTION));

            if (source.getReturn().getMap() != null) {
                if (isRefreshSupported(source.getReturn().getMap().getTransfer())) {
                    target.getActions().add(ctx.equivalent(source, Action.class,
                            OPERATION_OUTPUT_REFRESH_ACTION));
                }
                if (isUpdateSupported(source.getReturn().getMap().getTransfer())) {
                    target.getActions().add(ctx.equivalent(source, Action.class,
                            OPERATION_OUTPUT_UPDATE_ACTION));
                }
                if (isDeleteSupported(source.getReturn().getMap().getTransfer())) {
                    target.getActions().add(ctx.equivalent(source, Action.class,
                            OPERATION_OUTPUT_DELETE_ACTION));
                }
            }

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("OperationOutputPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_OUTPUT_BACK_ACTION, description = "Create back action for output page")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationOutputBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source.getReturn() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputBackAction");
            target.setName(source.getName() + "::Back");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReturn(),
                    BackActionDefinition.class, VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            LOG.debug("OperationOutputBackAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_OUTPUT_REFRESH_ACTION, description = "Create refresh action for output page")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationOutputRefreshAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source.getReturn() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputRefreshAction");
            target.setName(source.getName() + "::Refresh");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReturn(),
                    RefreshActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            LOG.debug("OperationOutputRefreshAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_OUTPUT_UPDATE_ACTION, description = "Create update action for output page")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationOutputUpdateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source.getReturn() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputUpdateAction");
            target.setName(source.getName() + "::Update");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReturn(),
                    UpdateActionDefinition.class, VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            LOG.debug("OperationOutputUpdateAction: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = OPERATION_OUTPUT_DELETE_ACTION, description = "Create delete action for output page")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIActionDeclaration, Action> operationOutputDeleteAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source.getReturn() == null) return null;

            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/OperationOutputDeleteAction");
            target.setName(getFqName(source) + "::Delete");
            target.setOwnerDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setActionDefinition(ctx.equivalent(source.getReturn(),
                    DeleteActionDefinition.class, VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            LOG.debug("OperationOutputDeleteAction: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // ViewTableActionDeclaration rules (for inline table row/table action buttons)
    // =========================================================================

    @TransformRule(name = VIEW_TABLE_ACTION_DECLARATION_BUTTON, description = "Create Button for view table action declaration")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIActionDeclaration, Button> viewTableActionDeclarationButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");

            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableActionDeclarationButton");
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

            Integer sourcePos = posMap.get(source);
            posMap.put(target, sourcePos != null ? sourcePos : 0);

            if (isOpenOperationFormAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputFormActionDefinition.class,
                        VIEW_TABLE_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION));
            } else if (isOpenOperationSelectorAction(source)) {
                target.setActionDefinition(ctx.equivalent(source,
                        OpenOperationInputSelectorActionDefinition.class,
                        VIEW_TABLE_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION));
            } else {
                target.setActionDefinition(ctx.equivalent(source,
                        ParameterlessCallOperationActionDefinition.class,
                        VIEW_TABLE_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION));
            }

            target.setDataElement(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));

            LOG.debug("ViewTableActionDeclarationButton: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_ACTION_DECLARATION_CALL_OPERATION_ACTION_DEFINITION, description = "Create call operation action definition for table action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = ParameterlessCallOperationActionDefinition.class)
    public TransformFunction<UIActionDeclaration, ParameterlessCallOperationActionDefinition> viewTableActionDeclarationCallOperationActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ParameterlessCallOperationActionDefinition target = ctx.createTarget(ParameterlessCallOperationActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableActionDeclarationCallOperationActionDefinition");
            target.setName(getFqName(source) + "::Call");
            target.setOperation(ctx.equivalent(source.getTransferAction().getTarget(),
                    OperationType.class, OPERATION_TYPE));
            target.setTargetType(ctx.equivalent(
                    source.getTransferAction().getTarget().getParameterType(),
                    ClassType.class, CLASS_TYPE));
            target.setIsContainedRelationAction(!(source.eContainer() instanceof ActionGroupModifier));
            LOG.debug("ViewTableActionDeclarationCallOperationActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_ACTION_DECLARATION_OPEN_SELECTOR_ACTION_DEFINITION, description = "Create open selector action definition for table action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputSelectorActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputSelectorActionDefinition> viewTableActionDeclarationOpenSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputSelectorActionDefinition target = ctx.createTarget(OpenOperationInputSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableActionDeclarationOpenSelectorActionDefinition");
            target.setName(getFqName(source) + "::Open::Selector");
            target.setIsContainedRelationAction(true);

            SelectorTableModifier selectorMod = getSelectorTableModifier(source);
            if (selectorMod != null && selectorMod.getRow() != null) {
                target.setTargetType(ctx.equivalent(
                        selectorMod.getRow().getMap().getTransfer(),
                        ClassType.class, CLASS_TYPE));
            }
            target.setSelectorFor(ctx.equivalent(source,
                    InputSelectorCallOperationActionDefinition.class,
                    OPERATION_INPUT_SELECTOR_CALL_ACTION_DEFINITION));
            LOG.debug("ViewTableActionDeclarationOpenSelectorActionDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_TABLE_ACTION_DECLARATION_OPEN_FORM_ACTION_DEFINITION, description = "Create open form action definition for table action")
    @Lazy
    @Transform(type = UIActionDeclaration.class)
    @To(type = OpenOperationInputFormActionDefinition.class)
    public TransformFunction<UIActionDeclaration, OpenOperationInputFormActionDefinition> viewTableActionDeclarationOpenFormActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenOperationInputFormActionDefinition target = ctx.createTarget(OpenOperationInputFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewTableActionDeclarationOpenFormActionDefinition");
            target.setName(getFqName(source) + "::Open::Operation::Form");
            target.setIsContainedRelationAction(true);
            LOG.debug("ViewTableActionDeclarationOpenFormActionDefinition: {}", target.getName());
            return target;
        };
    }
}
