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

import hu.blackbelt.judo.meta.jsl.jsldsl.*;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.meta.ui.Container;
import hu.blackbelt.judo.meta.ui.NavigationController;
import hu.blackbelt.judo.meta.ui.NavigationItem;
import hu.blackbelt.judo.meta.ui.VisualElement;
import hu.blackbelt.judo.meta.ui.runtime.UiModel;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.common.ModelProvider;
import hu.blackbelt.judo.zeta.transformation.core.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformationExecutor;
import hu.blackbelt.judo.zeta.transformation.core.TransformationRegistry;
import lombok.Builder;
import lombok.NonNull;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zeta-based JSL to UI transformation.
 *
 * <p>Iterates over each {@link UIFrontendDeclaration} in the JSL model and executes
 * the registered Zeta rules per-frontend (matching ETL behaviour in {@link hu.blackbelt.judo.tatami.jsl.jsl2ui.Jsl2Ui}).
 *
 * <p>Pre-execution: initializes a position map ({@code __pos}) from JSL source model ordering.
 * Post-execution: sorts NavigationController items, NavigationItem groups, and Container children
 * by their stored position.
 */
public class Jsl2UiZetaTransformation {

    private static final Logger log = LoggerFactory.getLogger(Jsl2UiZetaTransformation.class);

    private final JslDslModel jslModel;
    private final UiModel uiModel;
    private final String defaultModelName;

    @Builder
    public Jsl2UiZetaTransformation(
            @NonNull JslDslModel jslModel,
            @NonNull UiModel uiModel,
            @NonNull String defaultModelName) {
        this.jslModel = jslModel;
        this.uiModel = uiModel;
        this.defaultModelName = defaultModelName;
    }

    public void execute() {
        log.info("Starting JSL to UI Zeta transformation for model: {}", defaultModelName);
        long startTime = System.currentTimeMillis();

        // Pre-execution: initialize position map from JSL source ordering
        ConcurrentHashMap<EObject, Integer> positionMap = initPositionMap();

        JslDslModelResourceSupport jslDslModelResourceSupport = JslDslModelResourceSupport
                .jslDslModelResourceSupportBuilder()
                .resourceSet(jslModel.getResourceSet())
                .uri(jslModel.getUri())
                .build();

        List<UIFrontendDeclaration> frontends = jslDslModelResourceSupport
                .getStreamOfJsldslUIFrontendDeclaration().toList();

        log.info("Found {} frontend declarations", frontends.size());

        for (UIFrontendDeclaration frontendDeclaration : frontends) {
            ActorDeclaration actorDeclaration = frontendDeclaration.getMap().getActor();
            log.info("Processing frontend for actor: {}", actorDeclaration.getName());

            long frontendStart = System.currentTimeMillis();

            // Phase 1: Create registry and register all rule classes
            TransformationRegistry registry = createRegistry();

            // Phase 2: Create transformation context for this frontend
            TransformationContext context = createContext(registry, frontendDeclaration, actorDeclaration, positionMap);

            // Phase 3: Create executor
            TransformationExecutor executor = TransformationExecutor.builder()
                    .registry(registry)
                    .context(context)
                    .parallel(false)
                    .etlCompatibilityMode(true)
                    .build();

            // Phase 4: Execute transformation
            executor.transform();

            // Phase 5: Unwrap deferred proxies so EMF references point to real objects
            context.unwrapAllProxiesInModel();

            // Phase 6: Flush pending XMI IDs for elements added via containment
            context.applyAllPendingXmiIds();

            log.info("Frontend '{}' transformation completed in {}ms",
                    actorDeclaration.getName(), System.currentTimeMillis() - frontendStart);
        }

        // Post-execution: sort UI elements by position
        postProcess(positionMap);

        long duration = System.currentTimeMillis() - startTime;
        log.info("JSL to UI Zeta transformation completed in {}ms", duration);
    }

    /**
     * Creates a position map from JSL source model element ordering.
     * Matches the ETL pre-block that stores 1-indexed positions.
     */
    private ConcurrentHashMap<EObject, Integer> initPositionMap() {
        ConcurrentHashMap<EObject, Integer> posMap = new ConcurrentHashMap<>();
        Jsl2UiModelProvider modelProvider = new Jsl2UiModelProvider();
        ResourceSet sourceResourceSet = jslModel.getResourceSet();

        // UIViewGroupDeclaration members
        for (UIViewPanelDeclaration container : modelProvider.getAllContents(sourceResourceSet, UIViewPanelDeclaration.class)) {
            if (container instanceof UIViewGroupDeclaration) {
                UIViewGroupDeclaration group = (UIViewGroupDeclaration) container;
                for (int i = 0; i < group.getMembers().size(); i++) {
                    posMap.put(group.getMembers().get(i), i + 1);
                }
            } else if (container instanceof UIViewTabsDeclaration) {
                UIViewTabsDeclaration tabs = (UIViewTabsDeclaration) container;
                for (int i = 0; i < tabs.getPanels().size(); i++) {
                    posMap.put(tabs.getPanels().get(i), i + 1);
                }
            }
        }

        // UIRowDeclaration members
        for (UIRowDeclaration row : modelProvider.getAllContents(sourceResourceSet, UIRowDeclaration.class)) {
            for (int i = 0; i < row.getMembers().size(); i++) {
                posMap.put(row.getMembers().get(i), i + 1);
            }
        }

        // UIViewDeclaration members
        for (UIViewDeclaration view : modelProvider.getAllContents(sourceResourceSet, UIViewDeclaration.class)) {
            for (int i = 0; i < view.getMembers().size(); i++) {
                posMap.put(view.getMembers().get(i), i + 1);
            }
        }

        log.debug("Initialized position map with {} entries", posMap.size());
        return posMap;
    }

    private TransformationRegistry createRegistry() {
        TransformationRegistry registry = new TransformationRegistry();

        // Application rules (frontend, actor, authentication, menu groups, modifiers)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application.FrontendDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application.ActorDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application.ActorGroupDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.application.ModifiableRules.class);

        // Structure rules (transfer declarations, fields, relations, actions)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure.TransferDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure.TransferFieldDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure.TransferRelationDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.structure.TransferActionDeclarationRules.class);

        // Type rules (data types, operator enumerations)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.type.TypeRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.type.DataTypeOperationRules.class);

        // View rules (view declarations, forms, groups, tabs, widgets)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewDeclarationFormRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewGroupDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewTabsDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewWidgetDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewTableDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewLinkDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewLinkDeclarationViewPageRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewTableDeclarationViewPageRules.class);

        // Menu rules (menu tables, menu links)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.MenuTableDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.MenuLinkDeclarationRules.class);

        // Row, card, tag rules (table page containers)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.RowDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.CardDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.TagDeclarationRules.class);

        // Action rules (action groups, row actions, view actions)
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ActionGroupDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.RowActionDeclarationRules.class);
        registry.register(hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.rules.view.ViewActionDeclarationRules.class);

        return registry;
    }

    private TransformationContext createContext(
            TransformationRegistry registry,
            UIFrontendDeclaration frontend,
            ActorDeclaration actorDeclaration,
            ConcurrentHashMap<EObject, Integer> positionMap) {

        ResourceSet sourceResourceSet = jslModel.getResourceSet();
        ResourceSet targetResourceSet = uiModel.getResourceSet();

        ModelProvider modelProvider = new Jsl2UiModelProvider();
        ExtensionMethodRegistry extensionRegistry = new ExtensionMethodRegistry();

        TransformationContext context = new TransformationContext(
                modelProvider,
                sourceResourceSet,
                targetResourceSet,
                extensionRegistry
        );

        context.setTransformationRegistry(registry);
        context.setUseStructuredIds(true);
        context.setEtlCompatibilityMode(true);
        context.setEquivalentDiscriminatedStrategy(
                hu.blackbelt.judo.zeta.transformation.core.EquivalentDiscriminatedStrategy.CLONE_CURRENT_STATE);

        context.registerResource("jsl", sourceResourceSet);
        context.registerResource("ui", targetResourceSet);
        context.setPreferredSourceAlias("jsl");

        // Per-frontend context variables (matching ETL inject contexts)
        context.setAttribute("frontend", frontend);
        context.setAttribute("actorDeclaration", actorDeclaration);
        context.setAttribute("defaultModelName", defaultModelName);
        context.setAttribute("__pos", positionMap);

        return context;
    }

    /**
     * Post-processing: sort UI elements by their JSL source ordering.
     * Matches the ETL post-block that sorts NavigationController items,
     * NavigationItem groups, and Container children.
     */
    private void postProcess(ConcurrentHashMap<EObject, Integer> positionMap) {
        Jsl2UiModelProvider modelProvider = new Jsl2UiModelProvider();
        ResourceSet targetResourceSet = uiModel.getResourceSet();

        // Sort NavigationController items
        for (NavigationController navController : modelProvider.getAllContents(targetResourceSet, NavigationController.class)) {
            sortByPosition(navController.getItems(), positionMap);
        }

        // Sort NavigationItem groups (nested items)
        for (NavigationItem navItem : modelProvider.getAllContents(targetResourceSet, NavigationItem.class)) {
            sortByPosition(navItem.getItems(), positionMap);
        }

        // Sort Container children with validation
        for (Container container : modelProvider.getAllContents(targetResourceSet, Container.class)) {
            boolean missingPos = false;
            for (VisualElement child : container.getChildren()) {
                if (!positionMap.containsKey(child)) {
                    missingPos = true;
                    log.error("Position is not defined for child: {}", child);
                }
            }
            if (missingPos) {
                log.warn("Missing position parameter(s) under Container: {}", container);
            }
            sortVisualElementsByPosition(container.getChildren(), positionMap);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends EObject> void sortByPosition(
            org.eclipse.emf.common.util.EList<T> list,
            ConcurrentHashMap<EObject, Integer> positionMap) {
        if (list.isEmpty()) return;
        List<T> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(e -> positionMap.getOrDefault(e, Integer.MAX_VALUE)));
        list.clear();
        list.addAll(sorted);
    }

    private void sortVisualElementsByPosition(
            org.eclipse.emf.common.util.EList<VisualElement> list,
            ConcurrentHashMap<EObject, Integer> positionMap) {
        if (list.isEmpty()) return;
        List<VisualElement> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(e -> positionMap.getOrDefault(e, Integer.MAX_VALUE)));
        list.clear();
        list.addAll(sorted);
    }

    static class Jsl2UiModelProvider implements ModelProvider {
        @Override
        public <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
            List<T> result = new ArrayList<>();
            var iterator = resourceSet.getAllContents();
            while (iterator.hasNext()) {
                var next = iterator.next();
                if (type.isInstance(next)) {
                    result.add(type.cast(next));
                }
            }
            return result;
        }

        @Override
        public String getName(EObject element) {
            try {
                var method = element.getClass().getMethod("getName");
                return (String) method.invoke(element);
            } catch (Exception e) {
                return element.eClass().getName();
            }
        }

        @Override
        public String getTypeName(EObject element) {
            return element.eClass().getName();
        }
    }
}
