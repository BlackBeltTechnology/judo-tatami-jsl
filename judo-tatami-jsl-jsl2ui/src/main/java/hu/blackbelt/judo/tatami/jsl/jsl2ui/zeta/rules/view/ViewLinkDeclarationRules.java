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
import hu.blackbelt.judo.meta.ui.data.RelationType;
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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2ui.zeta.Jsl2UiRuleNames.*;

/**
 * Ported from viewLinkDeclaration.etl:
 * Link/Button rules for UIViewLinkDeclaration - creates Link (non-button) or Button (button mode)
 * with associated action definitions, button groups, icons, and autocomplete support.
 */
@TransformationContext(
        source = EObject.class,
        target = EObject.class
)
public class ViewLinkDeclarationRules {

    private static final Logger LOG = LoggerFactory.getLogger(ViewLinkDeclarationRules.class);

    // =========================================================================
    // LinkIcon (@lazy)
    // =========================================================================

    @TransformRule(name = LINK_ICON, description = "Create Icon for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> linkIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/LinkIcon");
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIconName(iconMod.getValue().getValue());
            }
            target.setName(source.getName() + "ViewLinkIcon");
            return target;
        };
    }

    // =========================================================================
    // InlineViewLink (@greedy, guard: not button)
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_LINK, description = "Create Link for non-button UIViewLinkDeclaration")
    @Greedy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Link.class)
    public TransformFunction<UIViewLinkDeclaration, Link> inlineViewLink() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (source.isButton()) return null;

            Link target = ctx.createTarget(Link.class);
            applyAbstractLink(source, target, ctx);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewLink");

            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();
            target.setDataElement(ctx.equivalent(relation, RelationType.class, RELATION_TYPE));

            // Add to parent container
            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            // Representation column
            Column col = ctx.equivalent(source, Column.class, VIEW_LINK_DECLARATION_REPRESENTATION_COLUMN);
            target.getParts().add(col);

            target.setSelectorRowsPerPage(10);
            target.setAutoCompleteRows(10);

            target.setActionButtonGroup(ctx.equivalent(source, ButtonGroup.class,
                    INLINE_VIEW_LINK_BUTTON_GROUP));

            target.setAutocompleteRangeActionDefinition(ctx.equivalent(source,
                    AutocompleteRangeActionDefinition.class,
                    VIEW_LINK_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION));
            target.setAutocompleteSetActionDefinition(ctx.equivalent(source,
                    AutocompleteSetActionDefinition.class,
                    VIEW_LINK_DECLARATION_AUTOCOMPLETE_SET_ACTION_DEFINITION));

            if (isRefreshAllowed(relation) && !isEager(relation)) {
                target.setRefreshActionDefinition(ctx.equivalent(source,
                        RefreshActionDefinition.class,
                        VIEW_LINK_DECLARATION_REFRESH_ACTION_DEFINITION));
            }

            target.setIsEager(isEager(relation));

            LOG.debug("InlineViewLink: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // InlineViewButton (@greedy, guard: button)
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_BUTTON, description = "Create Button for button-style UIViewLinkDeclaration")
    @Greedy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> inlineViewButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (!source.isButton()) return null;

            Button target = ctx.createTarget(Button.class);
            // Apply AbstractViewLinkDeclaration shared properties
            target.setName(source.getName());
            target.setLabel(getLabelWithNameFallback(source));
            IconModifier iconMod = getIconModifier(source);
            if (iconMod != null) {
                target.setIcon(ctx.equivalent(source, Icon.class, LINK_ICON));
            }
            target.setRow(1.0);
            Modifier widthMod = getWidth(source);
            target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

            ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
            posMap.put(target, getPos(source));

            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewButton");

            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();
            target.setDataElement(ctx.equivalent(relation, RelationType.class, RELATION_TYPE));
            target.setRelationName(target.getDataElement().getName());

            target.setActionDefinition(ctx.equivalent(source,
                    OpenPageActionDefinition.class,
                    VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION_DEFINITION));
            target.setPreFetchActionDefinition(ctx.equivalent(source,
                    PreFetchActionDefinition.class,
                    INLINE_VIEW_BUTTON_PRE_FETCH_ACTION_DEFINITION));

            // Add to parent container
            VisualElement container = resolveUiContainer(source.eContainer(), ctx);
            if (container instanceof Flex) {
                ((Flex) container).getChildren().add(target);
            }

            LOG.debug("InlineViewButton: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // InlineViewButton action definitions (@lazy)
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_BUTTON_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for button link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, OpenPageActionDefinition> inlineViewButtonOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewButtonOpenPageActionDefinition");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            target.setName(source.getName() + "::OpenPage");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    @TransformRule(name = INLINE_VIEW_BUTTON_PRE_FETCH_ACTION_DEFINITION, description = "Create PreFetchActionDefinition for button link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = PreFetchActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, PreFetchActionDefinition> inlineViewButtonPreFetchActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            PreFetchActionDefinition target = ctx.createTarget(PreFetchActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewButtonPreFetchActionDefinition");
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            target.setName(source.getName() + "::PreFetch");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // ViewLinkDeclarationRefreshActionDefinition (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_REFRESH_ACTION_DEFINITION, description = "Create RefreshActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = RefreshActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, RefreshActionDefinition> viewLinkDeclarationRefreshActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RefreshActionDefinition target = ctx.createTarget(RefreshActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationRefreshActionDefinition");
            target.setName(source.getName() + "::Refresh");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // InlineViewLinkButtonGroup (@lazy)
    // =========================================================================

    @TransformRule(name = INLINE_VIEW_LINK_BUTTON_GROUP, description = "Create ButtonGroup for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = ButtonGroup.class)
    public TransformFunction<UIViewLinkDeclaration, ButtonGroup> inlineViewLinkButtonGroup() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            ButtonGroup target = ctx.createTarget(ButtonGroup.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/InlineViewLinkButtonGroup");
            target.setName(source.getName() + "::Actions");
            target.setLabel("Actions");

            target.getButtons().add(ctx.equivalent(source, Button.class,
                    VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON));

            if (getCreateFormModifier(source) != null) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON));
            }
            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();
            if (isDeleteAllowed(relation)) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_LINK_DECLARATION_DELETE_BUTTON));
            }
            if (getSelectorTableModifier(source) != null) {
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON));
                target.getButtons().add(ctx.equivalent(source, Button.class,
                        VIEW_LINK_DECLARATION_UNSET_BUTTON));
            }

            LOG.debug("InlineViewLinkButtonGroup: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // ViewLinkDeclarationRepresentationColumn (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_REPRESENTATION_COLUMN, description = "Create representation Column for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Column.class)
    public TransformFunction<UIViewLinkDeclaration, Column> viewLinkDeclarationRepresentationColumn() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Column target = ctx.createTarget(Column.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationRepresentationColumn");

            // Find first string field from the relation target's transfer declaration
            TransferDeclaration transferDecl = source.getReferenceType().getMap().getTransfer();
            Set<TransferFieldDeclaration> fields = getAllPrimitiveFields(transferDecl);
            TransferFieldDeclaration firstStringField = fields.stream()
                    .filter(f -> f.getReferenceType() instanceof DataTypeDeclaration
                            && "string".equals(((DataTypeDeclaration) f.getReferenceType()).getPrimitive()))
                    .findFirst()
                    .orElse(null);

            if (firstStringField == null) {
                LOG.error("Could not get string type field from relation target for representation: {}",
                        source.getReferenceType().getName());
                return null;
            }

            AttributeType attrType = getTransferFieldDeclarationEquivalentForField(firstStringField, ctx);
            target.setAttributeType(attrType);
            target.setName(attrType != null ? attrType.getName() : source.getName());
            target.setLabel("");
            target.setCol(12.0);
            target.setRow(1.0);
            target.setFormat("%s");
            target.setSort(Sort.ASC);

            LOG.debug("ViewLinkDeclarationRepresentationColumn: {}", target.getName());
            return target;
        };
    }

    // =========================================================================
    // OpenPage button/icon/action (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON, description = "Create View button for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> viewLinkDeclarationOpenPageButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenPageButton");
            target.setName(source.getName() + "::View");
            target.setButtonStyle("contained");
            target.setLabel("View");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source,
                    OpenPageActionDefinition.class,
                    VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_PAGE_BUTTON_ICON, description = "Create View button icon")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> viewLinkDeclarationOpenPageButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenPageButtonIcon");
            target.setIconName("eye");
            target.setName(source.getName() + "OpenPageIcon");
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION_DEFINITION, description = "Create OpenPageActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = OpenPageActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, OpenPageActionDefinition> viewLinkDeclarationOpenPageActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenPageActionDefinition target = ctx.createTarget(OpenPageActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenPageActionDefinition");
            target.setName(source.getName() + "::View");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // OpenForm (Create) button/icon/action (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON, description = "Create Create button for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> viewLinkDeclarationOpenFormButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenFormButton");
            target.setName(source.getName() + "::Create::Open");
            target.setButtonStyle("contained");
            target.setLabel("Create");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source,
                    OpenCreateFormActionDefinition.class,
                    VIEW_LINK_DECLARATION_OPEN_CREATE_FORM_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_FORM_BUTTON_ICON, description = "Create Create button icon")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> viewLinkDeclarationOpenFormButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenFormButtonIcon");
            target.setIconName("note-add");
            target.setName(source.getName() + "OpenFormIcon");
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_CREATE_FORM_ACTION_DEFINITION, description = "Create OpenCreateFormActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = OpenCreateFormActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, OpenCreateFormActionDefinition> viewLinkDeclarationOpenCreateFormActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenCreateFormActionDefinition target = ctx.createTarget(OpenCreateFormActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenCreateFormActionDefinition");
            target.setName(source.getName() + "::OpenCreate");
            target.setIsContainedRelationAction(true);
            return target;
        };
    }

    // =========================================================================
    // Delete button/icon/action (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_DELETE_BUTTON, description = "Create Delete button for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> viewLinkDeclarationDeleteButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationDeleteButton");
            target.setName(source.getName() + "::Delete");
            target.setButtonStyle("contained");
            target.setLabel("Delete");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    VIEW_LINK_DECLARATION_DELETE_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source,
                    RowDeleteActionDefinition.class,
                    VIEW_LINK_DECLARATION_ROW_DELETE_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_DELETE_BUTTON_ICON, description = "Create Delete button icon")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> viewLinkDeclarationDeleteButtonIcon() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationDeleteButtonIcon");
            target.setIconName("delete_forever");
            target.setName(source.getName() + "DeleteIcon");
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_ROW_DELETE_ACTION_DEFINITION, description = "Create RowDeleteActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = RowDeleteActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, RowDeleteActionDefinition> viewLinkDeclarationRowDeleteActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            RowDeleteActionDefinition target = ctx.createTarget(RowDeleteActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationRowDeleteActionDefinition");
            target.setName(source.getName() + "::Delete");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // Unset button/icon/action (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_UNSET_BUTTON, description = "Create Unset button for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> viewLinkDeclarationUnsetButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationUnsetButton");
            target.setName(source.getName() + "::Unset");
            target.setLabel("Unset");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    VIEW_LINK_DECLARATION_UNSET_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source,
                    UnsetActionDefinition.class,
                    VIEW_LINK_DECLARATION_UNSET_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_UNSET_BUTTON_ICON, description = "Create Unset button icon")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> viewLinkDeclarationUnsetButtonIcon() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationUnsetButtonIcon");
            target.setName(getFqName(source) + "::link-off::Icon");
            target.setIconName("link-off");
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_UNSET_ACTION_DEFINITION, description = "Create UnsetActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = UnsetActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, UnsetActionDefinition> viewLinkDeclarationUnsetActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            UnsetActionDefinition target = ctx.createTarget(UnsetActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationUnsetActionDefinition");
            target.setName(source.getName() + "::Unset");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // OpenSetSelector button/icon/action (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON, description = "Create Set button for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Button.class)
    public TransformFunction<UIViewLinkDeclaration, Button> viewLinkDeclarationOpenSetSelectorButton() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Button target = ctx.createTarget(Button.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenSetSelectorButton");
            target.setName(source.getName() + "::OpenSetSelector");
            target.setLabel("Set");
            target.setButtonStyle("contained");
            target.setIcon(ctx.equivalent(source, Icon.class,
                    VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON_ICON));
            target.setActionDefinition(ctx.equivalent(source,
                    OpenSetSelectorActionDefinition.class,
                    VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_ACTION_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_BUTTON_ICON, description = "Create Set button icon")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Icon.class)
    public TransformFunction<UIViewLinkDeclaration, Icon> viewLinkDeclarationOpenSetSelectorButtonIcon() {
        return (source, ctx) -> {
            ActorDeclaration actorDeclaration = ctx.getAttribute("actorDeclaration");
            Icon target = ctx.createTarget(Icon.class);
            ctx.setElementId(target, actorDeclaration.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenSetSelectorButtonIcon");
            target.setName(getFqName(source) + "::link::Icon");
            target.setIconName("link");
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_ACTION_DEFINITION, description = "Create OpenSetSelectorActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = OpenSetSelectorActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, OpenSetSelectorActionDefinition> viewLinkDeclarationOpenSetSelectorActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            OpenSetSelectorActionDefinition target = ctx.createTarget(OpenSetSelectorActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationOpenSetSelectorActionDefinition");
            target.setName(source.getName() + "::OpenSetSelector");
            target.setIsContainedRelationAction(true);
            // Note: ETL sets selectorFor to ViewLinkDeclarationSetSelectorSetSelectorSetActionDefinition
            // which is defined in viewLinkDeclarationSetSelectorPage.etl (not yet ported)
            return target;
        };
    }

    // =========================================================================
    // Autocomplete Range action/definition (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_AUTOCOMPLETE_RANGE_ACTION_DEFINITION, description = "Create AutocompleteRangeActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = AutocompleteRangeActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, AutocompleteRangeActionDefinition> viewLinkDeclarationAutocompleteRangeActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            AutocompleteRangeActionDefinition target = ctx.createTarget(AutocompleteRangeActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationAutocompleteRangeActionDefinition");
            target.setName(source.getName() + "::Autocomplete");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // Autocomplete Set action/definition (@lazy)
    // =========================================================================

    @TransformRule(name = VIEW_LINK_DECLARATION_AUTOCOMPLETE_SET_ACTION_DEFINITION, description = "Create AutocompleteSetActionDefinition for link")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = AutocompleteSetActionDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, AutocompleteSetActionDefinition> viewLinkDeclarationAutocompleteSetActionDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            AutocompleteSetActionDefinition target = ctx.createTarget(AutocompleteSetActionDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkDeclarationAutocompleteSetActionDefinition");
            target.setName(source.getName() + "::AutocompleteSet");
            target.setIsContainedRelationAction(true);
            target.setTargetType(ctx.equivalent(source.getTransferRelation().getTarget().getReferenceType(),
                    hu.blackbelt.judo.meta.ui.data.ClassType.class, CLASS_TYPE));
            return target;
        };
    }

    // =========================================================================
    // View link create form page rules — ported from viewLinkDeclarationFormPage.etl
    // =========================================================================

    @TransformRule(name = VIEW_LINK_CREATE_FORM_PAGE_DEFINITION, description = "Create PageDefinition for create form page")
    @Greedy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = PageDefinition.class)
    public TransformFunction<UIViewLinkDeclaration, PageDefinition> viewLinkCreateFormPageDefinition() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            if (!containsVisualElement(frontend, source)) return null;
            if (getCreateFormModifier(source) == null) return null;

            TransferRelationDeclaration relation = source.getTransferRelation().getTarget();
            UIViewDeclaration form = getCreateFormModifier(source).getForm();

            PageDefinition target = ctx.createTarget(PageDefinition.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkCreateFormPageDefinition");
            target.setName(getFqName(source) + "::FormPage");
            target.setOpenInDialog(true);
            target.setContainer(ctx.equivalent(form, PageContainer.class, FORM_PAGE_CONTAINER));

            RelationType relType = ctx.equivalent(relation, RelationType.class, RELATION_TYPE);
            target.setDataElement(relType);

            // Link processing
            for (UIViewLinkDeclaration link : getOwnLinks(form)) {
                TransferRelationDeclaration lRelation = link.getTransferRelation().getTarget();

                target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                        VIEW_LINK_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));

                if (getSelectorTableModifier(link) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_OPEN_SET_SELECTOR_DIALOG_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(link) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_UNSET_ACTION, getJslId(source)));
                }
                if (isGetRangeAllowed(lRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_AUTOCOMPLETE_RANGE_ACTION, getJslId(source)));
                }
                if (isSetReferenceAllowed(lRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(link, Action.class,
                            VIEW_LINK_DECLARATION_AUTOCOMPLETE_SET_ACTION, getJslId(source)));
                }
            }

            // Table processing
            for (UIViewTableDeclaration table : getOwnTables(form)) {
                TransferRelationDeclaration tRelation = table.getTransferRelation().getTarget();

                if (getUpdateViewModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_PAGE_ACTION, getJslId(source)));
                }
                if (isFilterSupported(tRelation)) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_FILTER_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_OPEN_ADD_SELECTOR_ACTION, getJslId(source)));
                }
                if (getSelectorTableModifier(table) != null) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_CLEAR_ACTION, getJslId(source)));
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_DECLARATION_BULK_REMOVE_ACTION, getJslId(source)));
                }
                if (table.getReferenceType() instanceof UITagDeclaration) {
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_RANGE_ACTION, getJslId(source)));
                    target.getActions().add(ctx.equivalentDiscriminated(table, Action.class,
                            VIEW_TABLE_TAGS_DECLARATION_AUTOCOMPLETE_ADD_ACTION, getJslId(source)));
                }
            }

            // Standard actions
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_LINK_CREATE_FORM_BACK_ACTION));
            target.getActions().add(ctx.equivalent(source, Action.class,
                    VIEW_LINK_CREATE_FORM_CREATE_ACTION));

            // Add to application pages
            Application app = ctx.equivalent(frontend, Application.class, APPLICATION);
            app.getPages().add(target);

            LOG.debug("ViewLinkCreateFormPageDefinition: {}", target.getName());
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_CREATE_FORM_CREATE_ACTION, description = "Create create action for form page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkCreateFormCreateAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkCreateFormCreateAction");
            target.setName(source.getName() + "::Create");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_CREATE_ACTION_DEFINITION));
            target.setTargetPageDefinition(ctx.equivalent(source, PageDefinition.class,
                    VIEW_LINK_PAGE_DEFINITION));
            return target;
        };
    }

    @TransformRule(name = VIEW_LINK_CREATE_FORM_BACK_ACTION, description = "Create back action for form page")
    @Lazy
    @Transform(type = UIViewLinkDeclaration.class)
    @To(type = Action.class)
    public TransformFunction<UIViewLinkDeclaration, Action> viewLinkCreateFormBackAction() {
        return (source, ctx) -> {
            UIFrontendDeclaration frontend = ctx.getAttribute("frontend");
            Action target = ctx.createTarget(Action.class);
            ctx.setElementId(target, frontend.getName()
                    + "/(jsl/" + getJslId(source) + ")/ViewLinkCreateFormBackAction");
            target.setName(source.getName() + "::Back");
            target.setActionDefinition(ctx.equivalent(getCreateFormModifier(source).getForm(),
                    ActionDefinition.class, FORM_PAGE_CONTAINER_BACK_ACTION_DEFINITION));
            return target;
        };
    }

    // =========================================================================
    // Shared helper (AbstractViewLinkDeclaration)
    // =========================================================================

    private void applyAbstractLink(UIViewLinkDeclaration source, Link target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());
        if (source.getTransferRelation() != null && source.getTransferRelation().getTarget() != null) {
            target.setRelationName(source.getTransferRelation().getTarget().getName());
        }
        target.setLabel(getLabelWithNameFallback(source));

        IconModifier iconMod = getIconModifier(source);
        if (iconMod != null) {
            target.setIcon(ctx.equivalent(source, Icon.class, LINK_ICON));
        }

        target.setRow(1.0);
        Modifier widthMod = getWidth(source);
        target.setCol(widthMod != null ? getModifierDoubleValue(widthMod) : 12.0);

        ConcurrentHashMap<EObject, Integer> posMap = ctx.getAttribute("__pos");
        posMap.put(target, getPos(source));
    }

    /**
     * Gets the equivalent AttributeType for a standalone TransferFieldDeclaration.
     * Used by ViewLinkDeclarationRepresentationColumn to resolve the first string field.
     */
    private AttributeType getTransferFieldDeclarationEquivalentForField(TransferFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (field == null) return null;
        if (isMaps(field)) {
            return ctx.equivalent(field, AttributeType.class, CREATE_MAPPED_TRANSFER_ATTRIBUTE);
        } else if (isReads(field)) {
            return ctx.equivalent(field, AttributeType.class, CREATE_DERIVED_TRANSFER_ATTRIBUTE);
        } else {
            return ctx.equivalent(field, AttributeType.class, CREATE_TRANSIENT_TRANSFER_ATTRIBUTE);
        }
    }
}
