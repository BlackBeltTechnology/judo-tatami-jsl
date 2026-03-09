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
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import hu.blackbelt.judo.meta.ui.ButtonGroup;
import hu.blackbelt.judo.meta.ui.Flex;
import hu.blackbelt.judo.meta.ui.TabController;
import hu.blackbelt.judo.meta.ui.VisualElement;
import hu.blackbelt.judo.meta.ui.data.AttributeType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static helper methods ported from jsl2ui EOL operations.
 * Provides JSL model introspection, ID generation, modifier access,
 * and UI-specific element traversal.
 */
public final class Jsl2UiHelper {

    private static final Logger LOG = LoggerFactory.getLogger(Jsl2UiHelper.class);

    private Jsl2UiHelper() {}

    private static final JslDslModelExtension JSL_UTILS = new JslDslModelExtension();

    // =========================================================================
    // XMI ID operations (ported from id.eol)
    // =========================================================================

    public static String getJslId(EObject source) {
        if (source == null) return null;
        if (source instanceof ModelDeclaration) {
            return ((ModelDeclaration) source).getName().replaceAll("::", "/");
        }
        String name = getNameReflective(source);
        if (name != null && source.eContainer() != null) {
            String containerId = getJslId(source.eContainer());
            if (containerId != null) {
                return containerId + "/" + name;
            }
        }
        Resource resource = source.eResource();
        if (resource instanceof XMIResource) {
            return ((XMIResource) resource).getID(source);
        }
        return null;
    }

    private static String getNameReflective(EObject obj) {
        try {
            java.lang.reflect.Method nameMethod = obj.getClass().getMethod("getName");
            return (String) nameMethod.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static void setId(EObject target, String id) {
        if (target == null || id == null) return;
        Resource resource = target.eResource();
        if (resource instanceof XMIResource) {
            ((XMIResource) resource).setID(target, id);
        }
    }

    public static String purify(String s) {
        if (s == null) return null;
        return s.replace("::", "/");
    }

    // =========================================================================
    // FqName operations (ported from various getFqName() EOL operations)
    // =========================================================================

    public static String getFqName(EObject element) {
        if (element == null) return "";
        if (element instanceof ModelDeclaration) {
            return ((ModelDeclaration) element).getName();
        }
        try {
            java.lang.reflect.Method nameMethod = element.getClass().getMethod("getName");
            String name = (String) nameMethod.invoke(element);
            String containerFqName = getFqName(element.eContainer());
            if (containerFqName.isEmpty()) return name != null ? name : "";
            // TransferFieldDeclaration uses # separator
            if (element instanceof TransferFieldDeclaration) {
                return containerFqName + "#" + name;
            }
            // TransferActionDeclaration also uses # in jsl2psm but :: in jsl2ui
            return containerFqName + "::" + name;
        } catch (Exception e) {
            return "";
        }
    }

    // =========================================================================
    // ModelDeclaration operations (ported from modelDeclaration.eol)
    // =========================================================================

    public static ModelDeclaration getModelDeclaration(EObject jslElement) {
        EObject current = jslElement;
        while (current != null) {
            if (current instanceof ModelDeclaration) {
                return (ModelDeclaration) current;
            }
            current = current.eContainer();
        }
        return null;
    }

    // =========================================================================
    // Modifier helpers (ported from modifiable.eol)
    // =========================================================================

    public static <T extends Modifier> T getModifier(Modifiable modifiable, Class<T> modifierClass) {
        if (modifiable == null) return null;
        return modifiable.getModifiers().stream()
                .filter(modifierClass::isInstance)
                .map(modifierClass::cast)
                .findFirst()
                .orElse(null);
    }

    public static Modifier getModifierByType(Modifiable modifiable, String type) {
        if (modifiable == null) return null;
        return modifiable.getModifiers().stream()
                .filter(m -> type.equals(m.getType()))
                .findFirst()
                .orElse(null);
    }

    public static LabelModifier getLabelModifier(Modifiable modifiable) {
        return getModifier(modifiable, LabelModifier.class);
    }

    public static String getLabelWithNameFallback(Modifiable modifiable) {
        LabelModifier label = getLabelModifier(modifiable);
        if (label == null) {
            return getNameReflective((EObject) modifiable);
        }
        return label.getValue().getValue();
    }

    public static ActionGroupModifier getActionGroupModifier(Modifiable modifiable) {
        return getModifier(modifiable, ActionGroupModifier.class);
    }

    public static IconModifier getIconModifier(Modifiable modifiable) {
        return getModifier(modifiable, IconModifier.class);
    }

    public static OrientationModifier getOrientationModifier(Modifiable modifiable) {
        return getModifier(modifiable, OrientationModifier.class);
    }

    public static HelpModifier getHelpModifier(Modifiable modifiable) {
        return getModifier(modifiable, HelpModifier.class);
    }

    public static boolean isPredictive(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(PredictiveModifier.class::isInstance)
                .map(PredictiveModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static StretchModifier getStretchModifier(Modifiable modifiable) {
        return getModifier(modifiable, StretchModifier.class);
    }

    public static PrecisionModifier getPrecision(Modifiable modifiable) {
        return getModifier(modifiable, PrecisionModifier.class);
    }

    public static ScaleModifier getScale(Modifiable modifiable) {
        return getModifier(modifiable, ScaleModifier.class);
    }

    public static RegexModifier getRegex(Modifiable modifiable) {
        return getModifier(modifiable, RegexModifier.class);
    }

    public static Modifier getRequired(Modifiable modifiable) {
        return getModifierByType(modifiable, "required");
    }

    public static MaxSizeModifier getMaxSize(Modifiable modifiable) {
        return getModifier(modifiable, MaxSizeModifier.class);
    }

    public static MaxFileSizeModifier getMaxFileSize(Modifiable modifiable) {
        return getModifier(modifiable, MaxFileSizeModifier.class);
    }

    public static Modifier getMinSize(Modifiable modifiable) {
        return getModifierByType(modifiable, "min-size");
    }

    public static MimeTypesModifier getMimeType(Modifiable modifiable) {
        return getModifier(modifiable, MimeTypesModifier.class);
    }

    // Note: DetailModifier referenced in EOL but does not exist in current JSL metamodel.
    // Keeping as type-safe Modifier lookup for forward compatibility.
    public static Modifier getDetail(Modifiable modifiable) {
        return getModifierByType(modifiable, "detail");
    }

    public static boolean isOpenInDialog(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(DialogModifier.class::isInstance)
                .map(DialogModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static Modifier getOpposite(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(m -> "opposite".equals(m.getType()) || "opposite-add".equals(m.getType()))
                .findFirst()
                .orElse(null);
    }

    public static Modifier getDefault(Modifiable modifiable) {
        return getModifierByType(modifiable, "default");
    }

    public static Modifier getChoices(Modifiable modifiable) {
        return getModifierByType(modifiable, "choices");
    }

    public static Modifier getBind(Modifiable modifiable) {
        return getModifierByType(modifiable, "bind");
    }

    public static boolean isEventInstead(Modifiable modifiable) {
        return modifiable.getModifiers().stream().noneMatch(m -> "on".equals(m.getType()));
    }

    public static boolean isEventBefore(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(m -> "on".equals(m.getType()))
                .anyMatch(m -> {
                    try {
                        return (Boolean) m.getClass().getMethod("isBefore").invoke(m);
                    } catch (Exception e) { return false; }
                });
    }

    public static boolean isEventAfter(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(m -> "on".equals(m.getType()))
                .anyMatch(m -> {
                    try {
                        return (Boolean) m.getClass().getMethod("isAfter").invoke(m);
                    } catch (Exception e) { return false; }
                });
    }

    public static Modifier getFrame(Modifiable modifiable) {
        return getModifierByType(modifiable, "frame");
    }

    public static Modifier getWidth(Modifiable modifiable) {
        return getModifierByType(modifiable, "width");
    }

    public static Modifier getHAlign(Modifiable modifiable) {
        return getModifierByType(modifiable, "h-align");
    }

    public static Modifier getVAlign(Modifiable modifiable) {
        return getModifierByType(modifiable, "v-align");
    }

    public static Modifier getLines(Modifiable modifiable) {
        return getModifierByType(modifiable, "lines");
    }

    public static Modifier getRows(Modifiable modifiable) {
        return getModifierByType(modifiable, "rows");
    }

    public static double getModifierDoubleValue(Modifier mod) {
        if (mod instanceof WidthModifier) {
            return ((WidthModifier) mod).getValue().doubleValue();
        }
        if (mod instanceof HeightModifier) {
            return ((HeightModifier) mod).getValue().doubleValue();
        }
        if (mod instanceof RowsModifier) {
            return ((RowsModifier) mod).getValue().doubleValue();
        }
        // Fallback: use reflection for modifiers with getValue() returning BigInteger
        try {
            java.lang.reflect.Method m = mod.getClass().getMethod("getValue");
            Object val = m.invoke(mod);
            if (val instanceof java.math.BigInteger) {
                return ((java.math.BigInteger) val).doubleValue();
            }
        } catch (Exception e) { /* ignore */ }
        return 0.0;
    }

    public static int getModifierIntValue(Modifier mod) {
        if (mod instanceof WidthModifier) {
            return ((WidthModifier) mod).getValue().intValue();
        }
        if (mod instanceof HeightModifier) {
            return ((HeightModifier) mod).getValue().intValue();
        }
        if (mod instanceof RowsModifier) {
            return ((RowsModifier) mod).getValue().intValue();
        }
        // Fallback: use reflection for modifiers with getValue() returning BigInteger
        try {
            java.lang.reflect.Method m = mod.getClass().getMethod("getValue");
            Object val = m.invoke(mod);
            if (val instanceof java.math.BigInteger) {
                return ((java.math.BigInteger) val).intValue();
            }
        } catch (Exception e) { /* ignore */ }
        return 0;
    }

    public static boolean isCenter(Modifier mod) {
        if (mod instanceof HAlignModifier) {
            return ((HAlignModifier) mod).isCenter();
        }
        if (mod instanceof VAlignModifier) {
            return ((VAlignModifier) mod).isCenter();
        }
        return false;
    }

    public static boolean isRight(Modifier mod) {
        if (mod instanceof HAlignModifier) {
            return ((HAlignModifier) mod).isRight();
        }
        return false;
    }

    public static boolean isBottom(Modifier mod) {
        if (mod instanceof VAlignModifier) {
            return ((VAlignModifier) mod).isBottom();
        }
        return false;
    }

    public static CreateFormModifier getCreateFormModifier(Modifiable modifiable) {
        return getModifier(modifiable, CreateFormModifier.class);
    }

    public static UpdateViewModifier getUpdateViewModifier(Modifiable modifiable) {
        return getModifier(modifiable, UpdateViewModifier.class);
    }

    public static SelectorTableModifier getSelectorTableModifier(Modifiable modifiable) {
        return getModifier(modifiable, SelectorTableModifier.class);
    }

    public static TextModifier getTextModifier(Modifiable modifiable) {
        return getModifier(modifiable, TextModifier.class);
    }

    public static MenuModifier getMenuModifier(Modifiable modifiable) {
        return getModifier(modifiable, MenuModifier.class);
    }

    public static ProfileModifier getProfileModifier(Modifiable modifiable) {
        return getModifier(modifiable, ProfileModifier.class);
    }

    public static ApplicationTitleModifier getApplicationTitleModifier(Modifiable modifiable) {
        return getModifier(modifiable, ApplicationTitleModifier.class);
    }

    public static DashboardModifier getDashboardModifier(Modifiable modifiable) {
        return getModifier(modifiable, DashboardModifier.class);
    }

    public static boolean isDashBoard(Modifiable modifiable) {
        return modifiable.getModifiers().stream()
                .filter(DashboardModifier.class::isInstance)
                .map(DashboardModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    // =========================================================================
    // Actor operations (ported from actorDeclaration.eol)
    // =========================================================================

    public static IdentityModifier getIdentity(ActorDeclaration actor) {
        return getModifier(actor, IdentityModifier.class);
    }

    public static RealmModifier getRealm(ActorDeclaration actor) {
        return getModifier(actor, RealmModifier.class);
    }

    public static ClaimModifier getClaim(ActorDeclaration actor) {
        return getModifier(actor, ClaimModifier.class);
    }

    public static GuardModifier getGuard(ActorDeclaration actor) {
        return getModifier(actor, GuardModifier.class);
    }

    public static TransferDeclaration getPrincipal(ActorDeclaration actor) {
        IdentityModifier identity = getIdentity(actor);
        if (identity == null) return null;
        TransferFieldDeclaration field = identity.getField();
        if (field == null) return null;
        EObject container = field.eContainer();
        if (container instanceof TransferDeclaration) {
            return (TransferDeclaration) container;
        }
        return null;
    }

    public static TransferDeclaration getIdentityTransferDeclaration(ActorDeclaration actor) {
        IdentityModifier identity = getIdentity(actor);
        if (identity != null) {
            TransferFieldDeclaration field = identity.getField();
            if (field != null && field.eContainer() instanceof TransferDeclaration) {
                return (TransferDeclaration) field.eContainer();
            }
        }
        return null;
    }

    public static Set<EObject> getAllMenuDeclarations(ActorDeclaration actor) {
        Set<EObject> menuDeclarations = new LinkedHashSet<>();
        for (EObject member : actor.getMembers()) {
            if (member instanceof UIMenuGroupDeclaration) {
                menuDeclarations.addAll(getAllMenuDeclarations((UIMenuGroupDeclaration) member));
            } else if (member instanceof UIMenuLinkDeclaration || member instanceof UIMenuTableDeclaration) {
                menuDeclarations.add(member);
            }
        }
        return menuDeclarations;
    }

    public static Set<EObject> getAllMenuDeclarations(UIMenuGroupDeclaration group) {
        Set<EObject> menuDeclarations = new LinkedHashSet<>();
        for (EObject member : group.getMembers()) {
            if (member instanceof UIMenuGroupDeclaration) {
                menuDeclarations.addAll(getAllMenuDeclarations((UIMenuGroupDeclaration) member));
            } else if (member instanceof UIMenuLinkDeclaration || member instanceof UIMenuTableDeclaration) {
                menuDeclarations.add(member);
            }
        }
        return menuDeclarations;
    }

    public static Set<TransferDeclaration> getExposedTransferObjects(ActorDeclaration actor) {
        Set<TransferDeclaration> declarations = actor.getMembers().stream()
                .filter(ActorAccessDeclaration.class::isInstance)
                .map(ActorAccessDeclaration.class::cast)
                .map(ActorAccessDeclaration::getReferenceType)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<TransferDeclaration> collected = new LinkedHashSet<>();
        for (TransferDeclaration vd : declarations) {
            collectExposedTransferObjects(vd, collected);
        }

        TransferDeclaration identity = getIdentityTransferDeclaration(actor);
        if (identity != null && !collected.contains(identity)) {
            collected.add(identity);
        }
        return collected;
    }

    // =========================================================================
    // TransferDeclaration operations (ported from transferDeclaration.eol)
    // =========================================================================

    public static void collectExposedTransferObjects(TransferDeclaration self, Set<TransferDeclaration> transfers) {
        if (transfers.contains(self)) return;
        transfers.add(self);

        Set<TransferRelationDeclaration> relations = getExposedRelations(self);
        List<TransferActionDeclaration> actions = getAllActions(self);

        for (TransferRelationDeclaration relation : relations) {
            if (!transfers.contains(relation.getReferenceType())) {
                collectExposedTransferObjects(relation.getReferenceType(), transfers);
            }
        }

        for (TransferActionDeclaration action : actions) {
            for (ErrorDeclaration error : action.getErrors()) {
                if (!transfers.contains(error)) {
                    transfers.add(error);
                }
            }
            if (action.getParameterType() != null && !transfers.contains(action.getParameterType())) {
                transfers.add(action.getParameterType());
            }
            if (action.getReturn() instanceof TransferDeclaration) {
                TransferDeclaration returnType = (TransferDeclaration) action.getReturn();
                if (!transfers.contains(returnType)) {
                    transfers.add(returnType);
                }
            }
        }
    }

    public static Set<TransferRelationDeclaration> getExposedRelations(TransferDeclaration self) {
        Set<TransferRelationDeclaration> collected = new LinkedHashSet<>();
        collectExposedRelations(self, collected);
        return collected;
    }

    private static void collectExposedRelations(TransferDeclaration self, Set<TransferRelationDeclaration> collected) {
        List<TransferRelationDeclaration> relations = self.getMembers().stream()
                .filter(TransferRelationDeclaration.class::isInstance)
                .map(TransferRelationDeclaration.class::cast)
                .collect(Collectors.toList());
        for (TransferRelationDeclaration relation : relations) {
            collectExposedRelationsFromRelation(relation, collected);
        }
    }

    private static void collectExposedRelationsFromRelation(TransferRelationDeclaration rel, Set<TransferRelationDeclaration> collected) {
        if (collected.contains(rel)) return;
        collected.add(rel);
        if (rel.getReferenceType() != null) {
            for (TransferMemberDeclaration nested : rel.getReferenceType().getMembers()) {
                if (nested instanceof TransferRelationDeclaration) {
                    collectExposedRelationsFromRelation((TransferRelationDeclaration) nested, collected);
                }
            }
        }
    }

    public static List<TransferActionDeclaration> getAllActions(TransferDeclaration self) {
        return self.getMembers().stream()
                .filter(TransferActionDeclaration.class::isInstance)
                .map(TransferActionDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    public static Set<TransferRelationDeclaration> getDirectRelations(TransferDeclaration self) {
        Set<TransferRelationDeclaration> relations = new LinkedHashSet<>();
        relations.addAll(self.getMembers().stream()
                .filter(TransferRelationDeclaration.class::isInstance)
                .map(TransferRelationDeclaration.class::cast)
                .collect(Collectors.toList()));

        if (self instanceof ActorDeclaration) {
            // ActorDeclaration's menu declarations are also relations in context
            // (handled separately in UI rules)
        }

        for (TransferMemberDeclaration member : self.getMembers()) {
            if (member instanceof UIViewPanelDeclaration) {
                relations.addAll(getDirectRelationsFromPanel((UIViewPanelDeclaration) member));
            }
        }
        return relations;
    }

    private static Set<TransferRelationDeclaration> getDirectRelationsFromPanel(UIViewPanelDeclaration panel) {
        Set<TransferRelationDeclaration> relations = new LinkedHashSet<>();
        if (panel instanceof UIViewGroupDeclaration) {
            UIViewGroupDeclaration group = (UIViewGroupDeclaration) panel;
            relations.addAll(group.getMembers().stream()
                    .filter(TransferRelationDeclaration.class::isInstance)
                    .map(TransferRelationDeclaration.class::cast)
                    .collect(Collectors.toList()));
            for (EObject member : group.getMembers()) {
                if (member instanceof UIViewPanelDeclaration) {
                    relations.addAll(getDirectRelationsFromPanel((UIViewPanelDeclaration) member));
                }
            }
        }
        return relations;
    }

    public static Set<TransferFieldDeclaration> getAllPrimitiveFields(TransferDeclaration self) {
        Set<TransferFieldDeclaration> fields = new LinkedHashSet<>();
        fields.addAll(self.getMembers().stream()
                .filter(TransferFieldDeclaration.class::isInstance)
                .map(TransferFieldDeclaration.class::cast)
                .filter(f -> f.getReferenceType() != null)
                .collect(Collectors.toList()));
        for (TransferMemberDeclaration member : self.getMembers()) {
            if (member instanceof UIViewPanelDeclaration) {
                fields.addAll(getAllPrimitiveFieldsFromPanel((UIViewPanelDeclaration) member));
            }
        }
        return fields;
    }

    private static Set<TransferFieldDeclaration> getAllPrimitiveFieldsFromPanel(UIViewPanelDeclaration panel) {
        Set<TransferFieldDeclaration> fields = new LinkedHashSet<>();
        if (panel instanceof UIViewGroupDeclaration) {
            UIViewGroupDeclaration group = (UIViewGroupDeclaration) panel;
            fields.addAll(group.getMembers().stream()
                    .filter(TransferFieldDeclaration.class::isInstance)
                    .map(TransferFieldDeclaration.class::cast)
                    .filter(f -> f.getReferenceType() != null)
                    .collect(Collectors.toList()));
            for (EObject member : group.getMembers()) {
                if (member instanceof UIViewPanelDeclaration) {
                    fields.addAll(getAllPrimitiveFieldsFromPanel((UIViewPanelDeclaration) member));
                }
            }
        } else if (panel instanceof UIViewTabsDeclaration) {
            UIViewTabsDeclaration tabs = (UIViewTabsDeclaration) panel;
            for (UIViewPanelDeclaration p : tabs.getPanels()) {
                fields.addAll(getAllPrimitiveFieldsFromPanel(p));
            }
        }
        return fields;
    }

    public static boolean isDeleteSupported(TransferDeclaration self) {
        if (self.getMap() == null) return false;
        return self.getMembers().stream()
                .anyMatch(m -> "TransferDeleteDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isCreateSupported(TransferDeclaration self) {
        if (self.getMap() == null) return false;
        return self.getMembers().stream()
                .anyMatch(m -> "TransferCreateDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isUpdateSupported(TransferDeclaration self) {
        if (self.getMap() == null) return false;
        return self.getMembers().stream()
                .anyMatch(m -> "TransferUpdateDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isRefreshSupported(TransferDeclaration self) {
        return self.getMap() != null;
    }

    public static boolean isDefinedAsInputParameter(TransferDeclaration self) {
        EObject root = self;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        var iter = root.eAllContents();
        while (iter.hasNext()) {
            EObject obj = iter.next();
            if (obj instanceof TransferActionDeclaration) {
                if (((TransferActionDeclaration) obj).getParameterType() == self) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isGetTemplateSupported(TransferDeclaration self) {
        if (isCreateSupported(self)) return true;
        if (self.getMap() == null) return isDefinedAsInputParameter(self);
        return false;
    }

    public static boolean hasSortableField(TransferDeclaration self) {
        return self.getMembers().stream()
                .filter(TransferFieldDeclaration.class::isInstance)
                .map(TransferFieldDeclaration.class::cast)
                .anyMatch(Jsl2UiHelper::isSortable);
    }

    public static boolean hasFilterableField(TransferDeclaration self) {
        return self.getMembers().stream()
                .filter(TransferFieldDeclaration.class::isInstance)
                .map(TransferFieldDeclaration.class::cast)
                .anyMatch(Jsl2UiHelper::isFilterable);
    }

    // =========================================================================
    // TransferFieldDeclaration operations (ported from transferFieldDeclaration.eol)
    // =========================================================================

    public static boolean isCalculated(TransferFieldDeclaration field) {
        return field.getGetterExpr() != null;
    }

    public static boolean isRequired(TransferFieldDeclaration field) {
        return JSL_UTILS.isRequired((TransferMemberDeclaration) field);
    }

    public static boolean isMaps(TransferDataDeclaration decl) {
        return JSL_UTILS.isMaps(decl);
    }

    public static boolean isReads(TransferDataDeclaration decl) {
        return JSL_UTILS.isReads(decl);
    }

    public static boolean isTransient(TransferDataDeclaration decl) {
        return !isMaps(decl) && !isReads(decl);
    }

    public static TransferDeclaration getTransferContainer(TransferMemberDeclaration member) {
        if (member.eContainer() instanceof TransferDeclaration) {
            return (TransferDeclaration) member.eContainer();
        }
        return null;
    }

    public static boolean isSortable(TransferFieldDeclaration field) {
        String primitive = getPrimitiveTypeName(field);
        if (primitive == null) return false;
        return (isMaps(field) || isReads(field)) && isSortableOrFilterablePrimitive(primitive);
    }

    public static boolean isFilterable(TransferFieldDeclaration field) {
        String primitive = getPrimitiveTypeName(field);
        if (primitive == null) return false;
        return (isMaps(field) || isReads(field)) && isSortableOrFilterablePrimitive(primitive);
    }

    private static String getPrimitiveTypeName(TransferFieldDeclaration field) {
        PrimitiveDeclaration refType = field.getReferenceType();
        if (refType instanceof DataTypeDeclaration) {
            return ((DataTypeDeclaration) refType).getPrimitive();
        }
        return null;
    }

    private static boolean isSortableOrFilterablePrimitive(String primitive) {
        return "string".equals(primitive) || "numeric".equals(primitive) ||
               "date".equals(primitive) || "timestamp".equals(primitive) ||
               "time".equals(primitive) || "boolean".equals(primitive);
    }

    // =========================================================================
    // TransferRelationDeclaration operations (ported from transferRelationDeclaration.eol)
    // =========================================================================

    public static boolean isEager(TransferRelationDeclaration rel) {
        return JSL_UTILS.isEager(rel);
    }

    public static boolean isAggregation(TransferRelationDeclaration rel) {
        return JSL_UTILS.isAggregation(rel);
    }

    public static boolean isRequired(TransferRelationDeclaration rel) {
        return JSL_UTILS.isRequired(rel);
    }

    public static boolean isMany(EObject obj) {
        return JSL_UTILS.isMany(obj);
    }

    public static boolean isListAllowed(TransferRelationDeclaration rel) {
        return isMaps(rel) || isReads(rel);
    }

    public static boolean isTemplateAllowed(TransferRelationDeclaration rel) {
        return isGetTemplateSupported(rel.getReferenceType());
    }

    public static boolean isCreateAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(CreateModifier.class::isInstance)
                .map(CreateModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean isValidateCreateAllowed(TransferRelationDeclaration rel) {
        return isCreateAllowed(rel);
    }

    public static boolean isUpdateAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(UpdateModifier.class::isInstance)
                .map(UpdateModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean isValidateUpdateAllowed(TransferRelationDeclaration rel) {
        return isUpdateAllowed(rel);
    }

    public static boolean isDeleteAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(DeleteModifier.class::isInstance)
                .map(DeleteModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean isChoiceDefined(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream().anyMatch(ChoiceModifier.class::isInstance);
    }

    public static boolean isAddReferenceAllowed(TransferRelationDeclaration rel) {
        int lower = isRequired(rel) && !isMany(rel) ? 1 : 0;
        int upper = isMany(rel) ? -1 : 1;
        return isMaps(rel) && upper != 1 && (lower < upper || upper == -1);
    }

    public static boolean isRemoveReferenceAllowed(TransferRelationDeclaration rel) {
        return isAddReferenceAllowed(rel);
    }

    public static boolean isSetReferenceAllowed(TransferRelationDeclaration rel) {
        return isMaps(rel) && !isRequired(rel);
    }

    public static boolean isUnsetReferenceAllowed(TransferRelationDeclaration rel) {
        return isSetReferenceAllowed(rel);
    }

    public static boolean isGetRangeAllowed(TransferRelationDeclaration rel) {
        return isChoiceDefined(rel);
    }

    public static boolean isRefreshAllowed(TransferRelationDeclaration rel) {
        return isMaps(rel) || isReads(rel);
    }

    public static boolean isOrderSupported(TransferRelationDeclaration rel) {
        return (isMaps(rel) || isReads(rel)) &&
               getAllPrimitiveFields(rel.getReferenceType()).stream().anyMatch(Jsl2UiHelper::isSortable);
    }

    public static boolean isFilterSupported(TransferRelationDeclaration rel) {
        return (isMaps(rel) || isReads(rel)) &&
               getAllPrimitiveFields(rel.getReferenceType()).stream().anyMatch(Jsl2UiHelper::isFilterable);
    }

    // =========================================================================
    // TransferActionDeclaration operations (ported from transferActionDeclaration.eol)
    // =========================================================================

    public static boolean isStatic(TransferActionDeclaration action) {
        return action.getModifiers().stream()
                .filter(StaticModifier.class::isInstance)
                .map(StaticModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean isActionUpdateAllowed(TransferActionDeclaration action) {
        if (action.getReturn() == null) return false;
        return action.getModifiers().stream()
                .filter(UpdateModifier.class::isInstance)
                .map(UpdateModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean isActionDeleteAllowed(TransferActionDeclaration action) {
        if (action.getReturn() == null) return false;
        return action.getModifiers().stream()
                .filter(DeleteModifier.class::isInstance)
                .map(DeleteModifier.class::cast)
                .anyMatch(m -> !m.isFalse());
    }

    public static boolean hasOutput(TransferActionDeclaration action) {
        return action.getReturn() != null;
    }

    // =========================================================================
    // EntityMemberDeclaration operations (ported from entityMemberDeclaration.eol)
    // =========================================================================

    public static boolean isEager(EntityMemberDeclaration member) {
        return JSL_UTILS.isEager(member);
    }

    public static boolean isCalculated(EntityMemberDeclaration member) {
        return JSL_UTILS.isCalculated(member);
    }

    public static boolean isRequired(EntityMemberDeclaration member) {
        return JSL_UTILS.isRequired(member);
    }

    // =========================================================================
    // DataTypeDeclaration / EnumDeclaration operations
    // =========================================================================

    public static ActorDeclaration getActorDeclaration(DataTypeDeclaration dataType) {
        if (dataType.eContainer() instanceof ActorDeclaration) {
            return (ActorDeclaration) dataType.eContainer();
        } else if (dataType.eContainer() instanceof ModelDeclaration) {
            return ((ModelDeclaration) dataType.eContainer()).getDeclarations().stream()
                    .filter(ActorDeclaration.class::isInstance)
                    .map(ActorDeclaration.class::cast)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public static ActorDeclaration getActorDeclaration(EnumDeclaration enumDecl) {
        if (enumDecl.eContainer() instanceof ActorDeclaration) {
            return (ActorDeclaration) enumDecl.eContainer();
        } else if (enumDecl.eContainer() instanceof ModelDeclaration) {
            return ((ModelDeclaration) enumDecl.eContainer()).getDeclarations().stream()
                    .filter(ActorDeclaration.class::isInstance)
                    .map(ActorDeclaration.class::cast)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    // =========================================================================
    // MaxFileSize value conversion
    // =========================================================================

    public static long getMaxFileSizeValue(MaxFileSizeModifier modifier) {
        if (modifier == null) return 0;
        return JSL_UTILS.getMaxFileSizeValue(modifier).longValue();
    }

    // =========================================================================
    // String utilities
    // =========================================================================

    public static String fqNameToCamelCase(String fqName) {
        if (fqName == null) return null;
        String[] parts = fqName.split("(::)|(#)");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }

    // =========================================================================
    // UI visibility operations (ported from visibleDeclaration.eol)
    // =========================================================================

    public static int getPos(EObject element) {
        if (element.eContainer() instanceof UIViewDeclaration) {
            return ((UIViewDeclaration) element.eContainer()).getMembers().indexOf(element);
        } else if (element.eContainer() instanceof UIViewGroupDeclaration) {
            return ((UIViewGroupDeclaration) element.eContainer()).getMembers().indexOf(element);
        } else if (element.eContainer() instanceof UIViewTabsDeclaration) {
            return ((UIViewTabsDeclaration) element.eContainer()).getPanels().indexOf(element);
        } else if (element.eContainer() instanceof UIActionGroupDeclaration) {
            return ((UIActionGroupDeclaration) element.eContainer()).getMembers().indexOf(element);
        }
        return 0;
    }

    public static boolean isNestedInProfile(EObject element) {
        EObject parent = element.eContainer();
        while (parent != null) {
            if (parent instanceof ProfileModifier) {
                return true;
            }
            parent = parent.eContainer();
        }
        return false;
    }

    // =========================================================================
    // ViewGroupDeclaration operations (ported from viewGroupDeclaration.eol)
    // =========================================================================

    public static boolean isFrame(UIViewGroupDeclaration group) {
        Modifier frame = getFrame(group);
        if (frame == null) return false;
        try {
            return (Boolean) frame.getClass().getMethod("isIsTrue").invoke(frame);
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Frontend operations (ported from frontendDeclaration.eol)
    // =========================================================================

    public static ActorDeclaration getActorDeclaration(UIFrontendDeclaration frontend) {
        return frontend.getMap().getActor();
    }

    public static Set<UIMenuTableDeclaration> getDashboardMenuTables(UIFrontendDeclaration frontend) {
        return getExposedVisualElements(frontend).stream()
                .filter(UIMenuTableDeclaration.class::isInstance)
                .map(UIMenuTableDeclaration.class::cast)
                .filter(Jsl2UiHelper::isDashBoard)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<UIMenuLinkDeclaration> getDashboardMenuLinks(UIFrontendDeclaration frontend) {
        return getExposedVisualElements(frontend).stream()
                .filter(UIMenuLinkDeclaration.class::isInstance)
                .map(UIMenuLinkDeclaration.class::cast)
                .filter(Jsl2UiHelper::isDashBoard)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static boolean hasDashboard(UIFrontendDeclaration frontend) {
        return !getDashboardMenuTables(frontend).isEmpty() || !getDashboardMenuLinks(frontend).isEmpty();
    }

    public static Set<EObject> getExposedVisualElements(UIFrontendDeclaration frontend) {
        return getExposedVisualElements(frontend, new LinkedHashSet<>());
    }

    private static Set<EObject> getExposedVisualElements(UIFrontendDeclaration frontend, Set<EObject> ves) {
        if (ves.contains(frontend)) return ves;
        ves.add(frontend);

        MenuModifier menu = getMenuModifier(frontend);
        if (menu != null) {
            collectVisualElementsFromMenuOrProfileMembers(menu.getMembers(), ves);
        }

        ProfileModifier profile = getProfileModifier(frontend);
        if (profile != null) {
            collectVisualElementsFromMenuOrProfileMembers(profile.getMembers(), ves);
        }

        return ves;
    }

    private static void collectVisualElementsFromMenuOrProfileMembers(List<? extends EObject> members, Set<EObject> ves) {
        for (EObject member : members) {
            if (member instanceof UIMenuLinkDeclaration) {
                getExposedVisualElements((UIMenuLinkDeclaration) member, ves);
            } else if (member instanceof UIMenuTableDeclaration) {
                getExposedVisualElements((UIMenuTableDeclaration) member, ves);
            } else if (member instanceof UIMenuGroupDeclaration) {
                getExposedVisualElements((UIMenuGroupDeclaration) member, ves);
            }
        }
    }

    // =========================================================================
    // Visual element traversal (ported from various getExposedVisualElements)
    // =========================================================================

    public static Set<EObject> getExposedVisualElements(UIMenuGroupDeclaration group, Set<EObject> ves) {
        if (ves.contains(group)) return ves;
        ves.add(group);
        collectVisualElementsFromMenuOrProfileMembers(group.getMembers(), ves);
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIMenuLinkDeclaration link, Set<EObject> ves) {
        if (ves.contains(link)) return ves;
        ves.add(link);
        UIViewDeclaration viewDeclaration = link.getReferenceType();
        getExposedVisualElements(viewDeclaration, ves);
        CreateFormModifier createForm = getCreateFormModifier(link);
        if (createForm != null) {
            getExposedVisualElements(createForm.getForm(), ves);
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIMenuTableDeclaration table, Set<EObject> ves) {
        if (ves.contains(table)) return ves;
        ves.add(table);
        getExposedVisualElements(table.getReferenceType(), ves);
        CreateFormModifier createForm = getCreateFormModifier(table);
        if (createForm != null) {
            getExposedVisualElements(createForm.getForm(), ves);
        }
        UpdateViewModifier updateView = getUpdateViewModifier(table);
        if (updateView != null) {
            getExposedVisualElements(updateView.getView(), ves);
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIViewDeclaration view, Set<EObject> ves) {
        if (ves.contains(view)) return ves;
        ves.add(view);
        ves.addAll(view.getMembers().stream().filter(UIViewWidgetDeclaration.class::isInstance).collect(Collectors.toSet()));
        for (EObject member : view.getMembers()) {
            if (member instanceof UIActionDeclaration) {
                getExposedVisualElements((UIActionDeclaration) member, ves);
            } else if (member instanceof UIViewLinkDeclaration) {
                getExposedVisualElements((UIViewLinkDeclaration) member, ves);
            } else if (member instanceof UIViewTableDeclaration) {
                getExposedVisualElements((UIViewTableDeclaration) member, ves);
            } else if (member instanceof UIViewGroupDeclaration) {
                getExposedVisualElements((UIViewGroupDeclaration) member, ves);
            } else if (member instanceof UIViewTabsDeclaration) {
                getExposedVisualElements((UIViewTabsDeclaration) member, ves);
            } else if (member instanceof UIActionGroupDeclaration) {
                getExposedVisualElements((UIActionGroupDeclaration) member, ves);
            }
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIViewGroupDeclaration group, Set<EObject> ves) {
        if (ves.contains(group)) return ves;
        ves.add(group);
        for (EObject member : group.getMembers()) {
            if (member instanceof UIViewLinkDeclaration) {
                getExposedVisualElements((UIViewLinkDeclaration) member, ves);
            } else if (member instanceof UIViewTableDeclaration) {
                getExposedVisualElements((UIViewTableDeclaration) member, ves);
            } else if (member instanceof UIViewGroupDeclaration) {
                getExposedVisualElements((UIViewGroupDeclaration) member, ves);
            } else if (member instanceof UIViewTabsDeclaration) {
                getExposedVisualElements((UIViewTabsDeclaration) member, ves);
            } else if (member instanceof UIActionDeclaration) {
                getExposedVisualElements((UIActionDeclaration) member, ves);
            } else if (member instanceof UIActionGroupDeclaration) {
                getExposedVisualElements((UIActionGroupDeclaration) member, ves);
            } else if (member instanceof UIViewWidgetDeclaration) {
                ves.add(member);
            }
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIViewTabsDeclaration tabs, Set<EObject> ves) {
        if (ves.contains(tabs)) return ves;
        ves.add(tabs);
        for (UIViewPanelDeclaration panel : tabs.getPanels()) {
            if (panel instanceof UIViewWidgetDeclaration) {
                ves.add(panel);
            } else if (panel instanceof UIViewLinkDeclaration) {
                getExposedVisualElements((UIViewLinkDeclaration) panel, ves);
            } else if (panel instanceof UIViewTableDeclaration) {
                getExposedVisualElements((UIViewTableDeclaration) panel, ves);
            } else if (panel instanceof UIViewGroupDeclaration) {
                getExposedVisualElements((UIViewGroupDeclaration) panel, ves);
            } else if (panel instanceof UIViewTabsDeclaration) {
                getExposedVisualElements((UIViewTabsDeclaration) panel, ves);
            }
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIViewLinkDeclaration link, Set<EObject> ves) {
        if (ves.contains(link)) return ves;
        ves.add(link);
        getExposedVisualElements(link.getReferenceType(), ves);
        CreateFormModifier createForm = getCreateFormModifier(link);
        if (createForm != null) {
            getExposedVisualElements(createForm.getForm(), ves);
        }
        SelectorTableModifier selector = getSelectorTableModifier(link);
        if (selector != null) {
            getExposedVisualElements(selector.getRow(), ves);
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIViewTableDeclaration table, Set<EObject> ves) {
        if (ves.contains(table)) return ves;
        ves.add(table);
        getExposedVisualElements(table.getReferenceType(), ves);
        ActionGroupModifier actionGroup = getActionGroupModifier(table);
        if (actionGroup != null) {
            getExposedVisualElements(actionGroup, ves);
        }
        CreateFormModifier createForm = getCreateFormModifier(table);
        if (createForm != null) {
            getExposedVisualElements(createForm.getForm(), ves);
        }
        UpdateViewModifier updateView = getUpdateViewModifier(table);
        if (updateView != null) {
            getExposedVisualElements(updateView.getView(), ves);
        }
        SelectorTableModifier selector = getSelectorTableModifier(table);
        if (selector != null) {
            getExposedVisualElements(selector.getRow(), ves);
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIActionDeclaration action, Set<EObject> ves) {
        if (ves.contains(action)) return ves;
        ves.add(action);
        if (action.getParameterType() != null && action.getParameterType().isForm()) {
            getExposedVisualElements(action.getParameterType(), ves);
        }
        SelectorTableModifier selector = getSelectorTableModifier(action);
        if (selector != null) {
            getExposedVisualElements(selector.getRow(), ves);
        }
        if (action.getReturn() != null) {
            getExposedVisualElements(action.getReturn(), ves);
        }
        return ves;
    }

    public static Set<EObject> getExposedVisualElements(UIActionGroupDeclaration actionGroup, Set<EObject> ves) {
        if (ves.contains(actionGroup)) return ves;
        ves.add(actionGroup);
        for (EObject member : actionGroup.getMembers()) {
            if (member instanceof UIActionDeclaration) {
                getExposedVisualElements((UIActionDeclaration) member, ves);
            }
        }
        return ves;
    }

    private static Set<EObject> getExposedVisualElements(ActionGroupModifier actionGroup, Set<EObject> ves) {
        if (ves.contains(actionGroup)) return ves;
        ves.add(actionGroup);
        for (EObject member : actionGroup.getActions()) {
            if (member instanceof UIActionDeclaration) {
                getExposedVisualElements((UIActionDeclaration) member, ves);
            }
        }
        return ves;
    }

    private static Set<EObject> getExposedVisualElements(UIListItemDeclaration listItem, Set<EObject> ves) {
        if (ves.contains(listItem)) return ves;
        ves.add(listItem);
        if (listItem instanceof UIRowDeclaration) {
            UIRowDeclaration row = (UIRowDeclaration) listItem;
            ves.addAll(row.getMembers().stream().filter(UIRowColumnDeclaration.class::isInstance).collect(Collectors.toSet()));
            for (EObject member : row.getMembers()) {
                if (member instanceof UIActionDeclaration) {
                    getExposedVisualElements((UIActionDeclaration) member, ves);
                }
            }
        } else if (listItem instanceof UICardDeclaration) {
            UICardDeclaration card = (UICardDeclaration) listItem;
            ves.addAll(card.getMembers().stream().filter(UIViewWidgetDeclaration.class::isInstance).collect(Collectors.toSet()));
        } else if (listItem instanceof UITagDeclaration) {
            // tag itself is the leaf
        }
        return ves;
    }

    // =========================================================================
    // UIActionDeclaration operations (ported from viewActionDeclaration.eol)
    // =========================================================================

    public static boolean isCallAction(UIActionDeclaration action) {
        return action.getParameterType() == null;
    }

    public static boolean isOpenOperationSelectorAction(UIActionDeclaration action) {
        if (action.getParameterType() != null) {
            return action.getTransferAction().getMap() != null;
        }
        return false;
    }

    public static boolean isOpenOperationFormAction(UIActionDeclaration action) {
        if (action.getParameterType() != null) {
            return action.getParameterType().isForm();
        }
        return false;
    }

    public static boolean hasActionOutput(UIActionDeclaration action) {
        return hasOutput(action.getTransferAction().getTarget());
    }

    // =========================================================================
    // UIViewPanelDeclaration operations (ported from viewPanelDeclaration.eol)
    // =========================================================================

    public static UIViewDeclaration getViewContainer(UIViewPanelDeclaration panel) {
        if (panel.eContainer() instanceof UIViewDeclaration) {
            return (UIViewDeclaration) panel.eContainer();
        } else if (panel.eContainer() instanceof UIViewPanelDeclaration) {
            return getViewContainer((UIViewPanelDeclaration) panel.eContainer());
        }
        return null;
    }

    // =========================================================================
    // Frontend traversal helpers (ported from menuModifier.eol, profileModifier.eol)
    // =========================================================================

    public static UIFrontendDeclaration getFrontend(EObject element) {
        if (element instanceof UIFrontendDeclaration) {
            return (UIFrontendDeclaration) element;
        }
        if (element instanceof MenuModifier || element instanceof ProfileModifier) {
            return (UIFrontendDeclaration) element.eContainer();
        }
        if (element instanceof UIMenuGroupDeclaration || element instanceof UIMenuLinkDeclaration
                || element instanceof UIMenuTableDeclaration) {
            EObject parent = element.eContainer();
            if (parent != null) {
                return getFrontend(parent);
            }
        }
        return null;
    }

    public static boolean containsVisualElement(UIFrontendDeclaration frontend, EObject element) {
        boolean result = getExposedVisualElements(frontend).contains(element);
        if (!result) {
            LOG.warn(" !!!! MISSING VISUAL ELEMENT: {}", getNameReflective(element));
        }
        return result;
    }

    // =========================================================================
    // uiContainer() - resolve JSL container to equivalent UI container
    // Ported from viewDeclaration.eol, viewGroupDeclaration.eol, etc.
    // =========================================================================

    /**
     * Resolves the UI container for a JSL element's parent.
     * This matches the polymorphic `uiContainer()` EOL operation.
     *
     * @param jslContainer the JSL container (UIViewDeclaration, UIViewGroupDeclaration, etc.)
     * @param ctx the transformation context for resolving equivalents
     * @return the equivalent UI VisualElement that serves as the container
     */
    public static VisualElement resolveUiContainer(EObject jslContainer,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (jslContainer instanceof UIViewDeclaration) {
            UIViewDeclaration view = (UIViewDeclaration) jslContainer;
            if (view.isForm()) {
                return ctx.equivalent(view, Flex.class, Jsl2UiRuleNames.FORM_PAGE_CONTAINER_VISUAL_ELEMENT);
            }
            return ctx.equivalent(view, Flex.class, Jsl2UiRuleNames.VIEW_PAGE_CONTAINER_VISUAL_ELEMENT);
        } else if (jslContainer instanceof UIViewGroupDeclaration) {
            return ctx.equivalent(jslContainer, Flex.class, Jsl2UiRuleNames.GROUP_VISUAL_ELEMENT);
        } else if (jslContainer instanceof UIViewTabsDeclaration) {
            return ctx.equivalent(jslContainer, TabController.class, Jsl2UiRuleNames.TAB_BAR_VISUAL_ELEMENT);
        } else if (jslContainer instanceof UIActionGroupDeclaration) {
            return ctx.equivalent(jslContainer, ButtonGroup.class, Jsl2UiRuleNames.ACTION_GROUP_VISUAL_ELEMENT);
        }
        LOG.warn("Cannot resolve uiContainer for: {}", jslContainer);
        return null;
    }

    /**
     * Gets the primitive type name from a UIViewWidgetDeclaration's transfer field.
     * Used for widget type dispatch in view rules.
     */
    public static String getWidgetPrimitive(UIViewWidgetDeclaration widget) {
        if (widget.getTransferField() == null || widget.getTransferField().getTarget() == null) {
            return null;
        }
        PrimitiveDeclaration refType = widget.getTransferField().getTarget().getReferenceType();
        if (refType instanceof DataTypeDeclaration) {
            return ((DataTypeDeclaration) refType).getPrimitive();
        }
        if (refType instanceof EnumDeclaration) {
            return "enum";
        }
        return null;
    }

    /**
     * Gets the equivalent AttributeType for a UIViewWidgetDeclaration.
     * Matches the ETL operation getTransferFieldDeclarationEquivalent().
     */
    public static AttributeType getTransferFieldDeclarationEquivalent(UIViewWidgetDeclaration widget,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        TransferFieldDeclaration field = widget.getTransferField().getTarget();
        if (field == null) return null;

        if (isMaps(field)) {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_MAPPED_TRANSFER_ATTRIBUTE);
        } else if (isReads(field)) {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_DERIVED_TRANSFER_ATTRIBUTE);
        } else {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_TRANSIENT_TRANSFER_ATTRIBUTE);
        }
    }

    public static AttributeType getColumnTransferFieldEquivalent(UIRowColumnDeclaration column,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (column.getTransferField() == null || column.getTransferField().getTarget() == null) return null;
        TransferFieldDeclaration field = column.getTransferField().getTarget();
        return getTransferFieldAttributeType(field, ctx);
    }

    /**
     * Gets the TransferFieldDeclaration for a UITagDeclaration.
     * Matches the ETL operation UITagDeclaration.getTransferFieldDeclaration().
     */
    public static TransferFieldDeclaration getTransferFieldDeclaration(UITagDeclaration tag) {
        TextModifier textMod = getTextModifier(tag);
        if (textMod == null || textMod.getTransferField() == null) return null;
        return textMod.getTransferField().getTarget();
    }

    /**
     * Gets the equivalent AttributeType for any TransferFieldDeclaration,
     * routing to the correct rule based on maps/reads/transient.
     */
    public static AttributeType getTransferFieldAttributeType(TransferFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (field == null) return null;
        if (isMaps(field)) {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_MAPPED_TRANSFER_ATTRIBUTE);
        } else if (isReads(field)) {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_DERIVED_TRANSFER_ATTRIBUTE);
        } else {
            return ctx.equivalent(field, AttributeType.class, Jsl2UiRuleNames.CREATE_TRANSIENT_TRANSFER_ATTRIBUTE);
        }
    }
}
