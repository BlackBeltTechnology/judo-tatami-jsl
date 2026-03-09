package hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application;

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
import hu.blackbelt.judo.meta.jsl.jsldsl.ApplicationTitleModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.UIFrontendDeclaration;
import hu.blackbelt.judo.meta.ui.*;
import hu.blackbelt.judo.meta.ui.data.ClassType;
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

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from frontendDeclaration.etl:
 * - Application: creates UI Application from UIFrontendDeclaration
 * - Theme: @lazy, creates Theme with default colors
 * - NavigationController: @lazy, creates NavigationController
 */
@TransformationContext(
        source = UIFrontendDeclaration.class,
        target = EObject.class
)
public class FrontendDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(FrontendDeclarationRules.class);

    @TransformRule(name = APPLICATION, description = "Create Application for UIFrontendDeclaration")
    @Lazy
    @Transform(type = UIFrontendDeclaration.class)
    @To(type = Application.class)
    public TransformFunction<UIFrontendDeclaration, Application> application() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");

            Application target = ctx.createTarget(Application.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/Application");

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            target.setName(actorDeclaration.getName());

            String defaultModelName = ctx.getAttribute("defaultModelName");
            target.setModelName(defaultModelName);

            ClassType actorCt = ctx.equivalent(actorDeclaration, ClassType.class, ACTOR);
            target.setActor(actorCt);
            target.getDataElements().add(actorCt);

            TransferDeclaration identity = getIdentityTransferDeclaration(actorDeclaration);
            if (identity != null) {
                ClassType principalCt = ctx.equivalent(identity, ClassType.class, CLASS_TYPE);
                target.setPrincipal(principalCt);
            }

            target.setDefaultLanguage("en-US");
            target.setLogo("judo-color-logo.png");

            ApplicationTitleModifier titleMod = getApplicationTitleModifier(source);
            if (titleMod != null) {
                target.setTitle(titleMod.getTitle().getValue());
            }

            NavigationController navController = ctx.equivalent(source,
                    NavigationController.class, NAVIGATION_CONTROLLER);
            target.setNavigationController(navController);

            Theme theme = ctx.equivalent(source, Theme.class, THEME);
            target.setTheme(theme);

            ctx.addToResource(target);

            LOG.debug("Created Application: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = THEME, description = "Create Theme (lazy)")
    @Lazy
    @Transform(type = UIFrontendDeclaration.class)
    @To(type = Theme.class)
    public TransformFunction<UIFrontendDeclaration, Theme> theme() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Theme target = ctx.createTarget(Theme.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/Theme");

            target.setTextPrimaryColor("#17191DFF");
            target.setTextSecondaryColor("#434448FF");
            target.setPrimaryColor("#3C4166FF");
            target.setSecondaryColor("#E7501DFF");
            target.setSubtitleColor("#8C8C8C");
            target.setBackgroundColor("#FAFAFAFF");

            ctx.addToResource(target);

            LOG.debug("Created Theme");
            return target;
        };
    }

    @TransformRule(name = NAVIGATION_CONTROLLER, description = "Create NavigationController (lazy)")
    @Lazy
    @Transform(type = UIFrontendDeclaration.class)
    @To(type = NavigationController.class)
    public TransformFunction<UIFrontendDeclaration, NavigationController> navigationController() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            NavigationController target = ctx.createTarget(NavigationController.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/NavigationController");

            target.setName(getFqName(source) + "::NavigationController");

            ctx.addToResource(target);

            LOG.debug("Created NavigationController: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = EMPTY_DASHBOARD_PAGE_DEFINITION, description = "Create empty DashboardPageDefinition when no dashboard items")
    @Greedy
    @Transform(type = UIFrontendDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIFrontendDeclaration, PageDefinition> emptyDashboardPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (source != frontend || hasDashboard(source)) {
                return null;
            }

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/EmptyDashboardPageDefinition");

            target.setName(getFqName(source) + "::DashboardPage");
            target.setDashboard(true);

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ClassType actorCt = ctx.equivalent(actorDeclaration, ClassType.class, ACTOR);
            target.setDataElement(actorCt);

            PageContainer container = ctx.equivalent(source,
                    PageContainer.class, EMPTY_DASHBOARD_PAGE_CONTAINER);
            target.setContainer(container);

            Application app = ctx.equivalent(source, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("EmptyDashboardPageDefinition [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(name = EMPTY_DASHBOARD_PAGE_CONTAINER, description = "Create empty DashboardPageContainer when no dashboard items")
    @Lazy
    @Transform(type = UIFrontendDeclaration.class)
    @To(type = PageContainer.class)
    public TransformFunction<UIFrontendDeclaration, PageContainer> emptyDashboardPageContainer() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");

            PageContainer target = ctx.createTarget(PageContainer.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/EmptyDashboardPageContainer");

            target.setName(getFqName(source) + "::Dashboard");
            target.setLabel("Dashboard");
            target.setType(PageContainerType.VIEW);

            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            ClassType actorCt = ctx.equivalent(actorDeclaration, ClassType.class, ACTOR);
            target.setDataElement(actorCt);

            Application app = ctx.equivalent(source, Application.class, APPLICATION);
            app.getPageContainers().add(target);

            LOG.debug("EmptyDashboardPageContainer [{}]", target.getName());
            return target;
        };
    }
}
