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
 * Ported from viewDeclaration.etl:
 * - ViewPageContainer: creates PageContainer (VIEW type) for non-form UIViewDeclaration
 * - ViewPageContainerVisualElement (@lazy): root Flex
 * - ViewPageContainerButtonGroup (@lazy): action button group (Back, Refresh, Delete, Update)
 * - ViewPageContainerBack/Refresh/Delete/Update buttons, icons, action definitions (@lazy)
 */
@TransformationContext(
        source = UIViewDeclaration.class,
        target = EObject.class
)
public class ViewDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewDeclarationRules.class);

    @TransformRule(name = VIEW_PAGE_CONTAINER, description = "Create ViewPageContainer for UIViewDeclaration")
    @Transform(type = UIViewDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UIViewDeclaration, PageContainer> viewPageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.isForm()) return null;

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainer");

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            if (!posMap.containsKey(target)) {
                posMap.put(target, 0);
            }

            target.setName(getFqName(source) + "::View::PageContainer");
            target.setLabel(getLabelWithNameFallback(source));

            Flex visualElement = ctx.equivalent(source, Flex.class, VIEW_PAGE_CONTAINER_VISUAL_ELEMENT);
            target.getChildren().add(visualElement);

            ButtonGroup buttonGroup = ctx.equivalent(source, ButtonGroup.class, VIEW_PAGE_CONTAINER_BUTTON_GROUP);
            target.getActionButtonGroups().add(buttonGroup);

            ClassType dataElement = ctx.equivalent(source.getMap().getTransfer(), ClassType.class, CLASS_TYPE);
            target.setDataElement(dataElement);

            target.setType(PageContainerType.VIEW);

            RefreshActionDefinition onInit = ctx.equivalent(source,
                    RefreshActionDefinition.class, VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION);
            target.setOnInit(onInit);

            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("Created ViewPageContainer: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_VISUAL_ELEMENT, description = "Create Flex for ViewPageContainer")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Flex.class)
    public TransformFunction<UIViewDeclaration, Flex> viewPageContainerVisualElement() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.isForm()) return null;

            Flex target = ctx.createTarget(Flex.class);
            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            posMap.put(target, 0);

            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerVisualElement");
            target.setName(source.getName());
            target.setDirection(Axis.VERTICAL);
            target.setMainAxisAlignment(MainAxisAlignment.START);
            target.setCrossAxisAlignment(CrossAxisAlignment.STRETCH);
            target.setCol(12.0);

            LOG.debug("ViewPageContainerVisualElement: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_BUTTON_GROUP, description = "Create ButtonGroup for ViewPageContainer")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewDeclaration, ButtonGroup> viewPageContainerButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerButtonGroup");
            target.setName(getFqName(source) + "::PageActions");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class, VIEW_PAGE_CONTAINER_BACK_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, VIEW_PAGE_CONTAINER_REFRESH_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, VIEW_PAGE_CONTAINER_DELETE_BUTTON));
            target.getButtons().add(ctx.equivalent(source, Button.class, VIEW_PAGE_CONTAINER_UPDATE_BUTTON));

            LOG.debug("ViewPageContainerButtonGroup: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION, description = "Create BackActionDefinition")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = BackActionDefinition.class)
    public TransformFunction<UIViewDeclaration, BackActionDefinition> viewPageContainerBackActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            BackActionDefinition target = ctx.createTarget(BackActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerBackActionDefinition");
            target.setName(getFqName(source) + "::Back");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_BACK_BUTTON_ICON, description = "Create back button icon")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> viewPageContainerBackButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerBackButtonIcon");
            target.setName(source.getName() + "BackButtonIcon");
            target.setIconName("arrow-left");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_BACK_BUTTON, description = "Create back button")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> viewPageContainerBackButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerBackButton");
            target.setName(getFqName(source) + "::Back");
            target.setLabel("Back");
            target.setButtonStyle("text");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_PAGE_CONTAINER_BACK_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, BackActionDefinition.class,
                    VIEW_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIViewDeclaration, RefreshActionDefinition> viewPageContainerRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerRefreshActionDefinition");
            target.setName(getFqName(source) + "::Refresh");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_REFRESH_BUTTON_ICON, description = "Create refresh button icon")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> viewPageContainerRefreshButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerRefreshButtonIcon");
            target.setName(source.getName() + "RefreshButtonIcon");
            target.setIconName("refresh");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_REFRESH_BUTTON, description = "Create refresh button")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> viewPageContainerRefreshButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerRefreshButton");
            target.setName(getFqName(source) + "::Refresh");
            target.setLabel("Refresh");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_PAGE_CONTAINER_REFRESH_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, RefreshActionDefinition.class,
                    VIEW_PAGE_CONTAINER_REFRESH_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION, description = "Create DeleteActionDefinition")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = DeleteActionDefinition.class)
    public TransformFunction<UIViewDeclaration, DeleteActionDefinition> viewPageContainerDeleteActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            DeleteActionDefinition target = ctx.createTarget(DeleteActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerDeleteActionDefinition");
            target.setName(getFqName(source) + "::Delete");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_DELETE_BUTTON_ICON, description = "Create delete button icon")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> viewPageContainerDeleteButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerDeleteButtonIcon");
            target.setName(source.getName() + "DeleteButtonIcon");
            target.setIconName("delete_forever");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_DELETE_BUTTON, description = "Create delete button")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> viewPageContainerDeleteButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerDeleteButton");
            target.setName(getFqName(source) + "::Delete");
            target.setLabel("Delete");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_PAGE_CONTAINER_DELETE_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, DeleteActionDefinition.class,
                    VIEW_PAGE_CONTAINER_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION, description = "Create UpdateActionDefinition")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = UpdateActionDefinition.class)
    public TransformFunction<UIViewDeclaration, UpdateActionDefinition> viewPageContainerUpdateActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            UpdateActionDefinition target = ctx.createTarget(UpdateActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerUpdateActionDefinition");
            target.setName(getFqName(source) + "::Update");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_UPDATE_BUTTON_ICON, description = "Create update button icon")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewDeclaration, Icon> viewPageContainerUpdateButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerUpdateButtonIcon");
            target.setName(source.getName() + "UpdateButtonIcon");
            target.setIconName("content-save");
            return target;
        };
    }

    @TransformRule(name = VIEW_PAGE_CONTAINER_UPDATE_BUTTON, description = "Create update button")
    @Lazy
    @Transform(type = UIViewDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewDeclaration, Button> viewPageContainerUpdateButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewPageContainerUpdateButton");
            target.setName(getFqName(source) + "::Update");
            target.setLabel("Save");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class, VIEW_PAGE_CONTAINER_UPDATE_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source, UpdateActionDefinition.class,
                    VIEW_PAGE_CONTAINER_UPDATE_ACTION_DEFINITION));
            return target;
        };
    }
}
