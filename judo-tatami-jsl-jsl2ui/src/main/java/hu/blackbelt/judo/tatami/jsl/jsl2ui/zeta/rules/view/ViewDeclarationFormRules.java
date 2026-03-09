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

import hu.blackbelt.judo.meta.jsl.jsldsl.UIFrontendDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.UIViewDeclaration;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
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
 * Ported from viewDeclarationForm.etl:
 * - FormPageContainer: creates PageContainer (FORM type) for form UIViewDeclaration
 * - FormPageContainerVisualElement (@lazy): root Flex
 * - FormPageContainerButtonGroup (@lazy): Back + Create buttons
 * - Back/Create buttons, icons, action definitions (@lazy)
 * - FormPageContainerGetTemplateActionDefinition (@lazy)
 */
@TransformationContext(
        source = UIViewDeclaration.class,
        target = EObject.class
)
public class ViewDeclarationFormRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDeclarationFormRules.class);

    @TransformRule(name = FORM_PAGE_CONTAINER, description = "Create FormPageContainer for form UIViewDeclaration")
    @Transform(type = UIViewDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UIViewDeclaration, PageContainer> formPageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!source.isForm()) return null;

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainer");

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            if (!posMap.containsKey(target)) {
                posMap.put(target, 0);
            }

            target.setLabel(getLabelWithNameFallback(source));
            target.setName(getFqName(source) + "::Create::PageContainer");

            Flex visualElement = ctx.equivalent(source, Flex.class, FORM_PAGE_CONTAINER_VISUAL_ELEMENT);
            target.getChildren().add(visualElement);

            ButtonGroup buttonGroup = ctx.equivalent(source, ButtonGroup.class, FORM_PAGE_CONTAINER_BUTTON_GROUP);
            target.getActionButtonGroups().add(buttonGroup);

            ClassType dataElement = ctx.equivalent(source.getMap().getTransfer(), ClassType.class, CLASS_TYPE);
            target.setDataElement(dataElement);

            target.setType(PageContainerType.FORM);

            GetTemplateActionDefinition getTemplate = ctx.equivalent(source,
                    GetTemplateActionDefinition.class, FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION);
            target.setOnInit(getTemplate);
            target.setTemplateAction(getTemplate);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("Created FormPageContainer: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_VISUAL_ELEMENT, description = "Create Flex for FormPageContainer")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UIViewDeclaration, Flex> formPageContainerVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!source.isForm()) return null;

            Flex target = ctx.createTarget(Flex.class);
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            posMap.put(target, 0);

            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerVisualElement");
            target.setName(source.getName());
            target.setDirection(Axis.VERTICAL);
            target.setMainAxisAlignment(MainAxisAlignment.START);
            target.setCrossAxisAlignment(CrossAxisAlignment.STRETCH);
            target.setCol(12.0);

            LOG.debug("FormPageContainerVisualElement: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_BUTTON_GROUP, description = "Create ButtonGroup for FormPageContainer")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewDeclaration, ButtonGroup> formPageContainerButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerButtonGroup");
            target.setName(getFqName(source) + "::PageActions");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class, FORM_PAGE_CONTAINER_BACK_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, FORM_PAGE_CONTAINER_CREATE_BUTTON));

            LOG.debug("FormPageContainerButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION, description = "Create BackActionDefinition for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = BackActionDefinition.class)
    public TransformFunction<UIViewDeclaration, BackActionDefinition> formPageContainerBackActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BackActionDefinition target = ctx.createTarget(BackActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerBackActionDefinition");
            target.setName(getFqName(source) + "::Back");
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_BACK_BUTTON_ICON, description = "Create back button icon for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> formPageContainerBackButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerBackButtonIcon");
            target.setIconName("arrow-left");
            target.setName(source.getName() + "BackButtonIcon");
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_BACK_BUTTON, description = "Create back button for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> formPageContainerBackButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerBackButton");
            target.setName(getFqName(source) + "::Back");
            target.setLabel("Back");
            target.setButtonStyle("text");
            target.setIcon(ctx.equivalent(source, Icon.class, FORM_PAGE_CONTAINER_BACK_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, BackActionDefinition.class,
                    FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_CREATE_BUTTON_ICON, description = "Create button icon for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> formPageContainerCreateButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerCreateButtonIcon");
            target.setName(source.getName() + "CreateButtonIcon");
            target.setIconName("content-save");
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_CREATE_BUTTON, description = "Create button for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> formPageContainerCreateButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerCreateButton");
            target.setName(getFqName(source) + "::Create");
            target.setLabel("Create");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class, FORM_PAGE_CONTAINER_CREATE_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, CreateActionDefinition.class,
                    FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION, description = "Create CreateActionDefinition for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = CreateActionDefinition.class)
    public TransformFunction<UIViewDeclaration, CreateActionDefinition> formPageContainerCreateActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            CreateActionDefinition target = ctx.createTarget(CreateActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerCreateActionDefinition");
            target.setName(getFqName(source) + "::Create");
            return target;
        };
    }

    @TransformRule(name = FORM_PAGE_CONTAINER_GET_TEMPLATE_ACTION_DEFINITION, description = "Create GetTemplateActionDefinition for form")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = GetTemplateActionDefinition.class)
    public TransformFunction<UIViewDeclaration, GetTemplateActionDefinition> formPageContainerGetTemplateActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            GetTemplateActionDefinition target = ctx.createTarget(GetTemplateActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/FormPageContainerGetTemplateActionDefinition");
            target.setName(getFqName(source) + "::GetTemplate");
            target.setTargetType(ctx.equivalent(source.getMap().getTransfer(), ClassType.class, CLASS_TYPE));
            return target;
        };
    }
}
