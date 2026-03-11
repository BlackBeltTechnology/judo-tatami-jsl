package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorAccessDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ClaimModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.DataTypeDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOpposite;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.jsl.jsldsl.GuardModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.IdentityModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MaxFileSizeModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MaxSizeModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MimeType;
import hu.blackbelt.judo.meta.jsl.jsldsl.MimeTypesModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.Modifiable;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.Modifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrecisionModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.RealmModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.RegexModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.ScaleModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDataDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferMemberDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.NamespaceElement;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.meta.jsl.util.JslDslModelExtension;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public final class Jsl2PsmHelper {

    private static final Logger LOG = LoggerFactory.getLogger(Jsl2PsmHelper.class);

    private Jsl2PsmHelper() {}

    private static final JslDslModelExtension JSL_UTILS = new JslDslModelExtension();

    // --- XMI ID operations (ported from id.eol) ---

    /**
     * Get the JSL ID of a source element.
     *
     * For types that have a hierarchical getId() in ETL (ModelDeclaration, EntityDeclaration,
     * TransferDeclaration, TransferActionDeclaration, etc.), constructs the same hierarchical
     * path. Falls back to XMI resource ID for other types.
     *
     * ETL pattern: ModelDeclaration.getId() = defaultModelName + "/" + name.replaceAll("::", "/")
     *              EntityDeclaration.getId() = eContainer.getId() + "/" + name
     *              TransferActionDeclaration.getId() = eContainer.getId() + "/" + name
     *              etc.
     */
    public static String getJslId(EObject source) {
        if (source == null) return null;

        // For ModelDeclaration: defaultModelName + "/" + name.replaceAll("::", "/")
        if (source instanceof ModelDeclaration) {
            ModelDeclaration modelDecl = (ModelDeclaration) source;
            String defaultModelName = modelDecl.getName().replaceAll("::", "_");
            return defaultModelName + "/" + modelDecl.getName().replaceAll("::", "/");
        }

        // For named JSL types that use eContainer.getId() + "/" + name pattern
        String name = getNameReflective(source);
        if (name != null && source.eContainer() != null) {
            String containerId = getJslId(source.eContainer());
            if (containerId != null) {
                return containerId + "/" + name;
            }
        }

        // Fallback to XMI resource ID
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

    // --- Thread-safe collection operations ---

    public static void addElement(Package pkg, NamespaceElement element) {
        if (pkg != null && element != null) {
            synchronized (pkg) {
                pkg.getElements().add(element);
            }
        }
    }

    public static void addPackage(Model model, Package pkg) {
        if (model != null && pkg != null) {
            synchronized (model) {
                model.getPackages().add(pkg);
            }
        }
    }

    public static void addSubPackage(Package parent, Package child) {
        if (parent != null && child != null) {
            synchronized (parent) {
                parent.getPackages().add(child);
            }
        }
    }

    public static void addEnumerationMember(EnumerationType enumType, EnumerationMember member) {
        if (enumType != null && member != null) {
            synchronized (enumType) {
                enumType.getMembers().add(member);
            }
        }
    }

    // --- Extensions package helper ---

    /**
     * Get or create the "extensions" package for singleton types like ActorStringType,
     * QueryCustomizerStringType, etc. The ETL creates this via "extensions".equivalent("CreateModelPackages")
     * which creates a Package named "extensions" under the model root.
     *
     * This method creates the package lazily and caches it in the context.
     */
    public static Package getExtensionsPackage(
            ModelDeclaration modelDecl,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Package cached = ctx.getAttribute("__extensionsPackage");
        if (cached != null) {
            return cached;
        }
        // Create extensions package
        Package extensionsPkg = ctx.create(Package.class);
        String modelName = modelDecl.getName().replaceAll("::", "_");
        ctx.setElementId(extensionsPkg, "(jsl/" + modelName + "/extensions)/CreateModelPackages");
        extensionsPkg.setName("extensions");

        // Add to model root - get the model through the model package
        Package modelPkg = ctx.equivalent(modelDecl, Package.class, Jsl2PsmRuleNames.CREATE_MODEL_PACKAGES);
        if (modelPkg != null && modelPkg.eContainer() instanceof Model) {
            Model model = (Model) modelPkg.eContainer();
            addPackage(model, extensionsPkg);
        }
        ctx.setAttribute("__extensionsPackage", extensionsPkg);
        return extensionsPkg;
    }

    // --- JSL ID helpers (ported from modelDeclaration.eol, dataTypeDeclaration.eol, etc.) ---

    public static String getModelDeclarationId(ModelDeclaration modelDecl, String defaultModelName) {
        return defaultModelName.replaceAll("::", "_") + "/" + modelDecl.getName().replaceAll("::", "/");
    }

    public static String getDataTypeDeclarationId(DataTypeDeclaration dataType) {
        EObject container = dataType.eContainer();
        if (container instanceof ModelDeclaration) {
            return getJslId(container) + "/" + dataType.getName();
        }
        return getJslId(container) + "/" + dataType.getName();
    }

    // --- Modifier helpers (ported from modifiable.eol) ---

    public static <T extends Modifier> T getModifier(Modifiable modifiable, String type, Class<T> modifierClass) {
        if (modifiable == null) return null;
        return modifiable.getModifiers().stream()
                .filter(m -> type.equals(m.getType()))
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

    public static PrecisionModifier getPrecision(Modifiable modifiable) {
        return getModifier(modifiable, "precision", PrecisionModifier.class);
    }

    public static ScaleModifier getScale(Modifiable modifiable) {
        return getModifier(modifiable, "scale", ScaleModifier.class);
    }

    public static MaxSizeModifier getMaxSize(Modifiable modifiable) {
        return getModifier(modifiable, "max-size", MaxSizeModifier.class);
    }

    public static RegexModifier getRegex(Modifiable modifiable) {
        return getModifier(modifiable, "regex", RegexModifier.class);
    }

    public static MimeTypesModifier getMimeType(Modifiable modifiable) {
        return getModifier(modifiable, "mime-type", MimeTypesModifier.class);
    }

    public static MaxFileSizeModifier getMaxFileSize(Modifiable modifiable) {
        return getModifier(modifiable, "max-file-size", MaxFileSizeModifier.class);
    }

    // --- MaxFileSizeModifier value conversion (ported from dataTypeDeclaration.eol) ---

    public static long getMaxFileSizeValue(MaxFileSizeModifier modifier) {
        if (modifier == null) return 0;
        return JSL_UTILS.getMaxFileSizeValue(modifier).longValue();
    }

    // --- String utilities (ported from utils.eol) ---

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

    // --- JSL model utilities (ported from entityDeclaration.eol, entityMemberDeclaration.eol) ---

    public static boolean isAbstract(EObject entityDeclaration) {
        if (entityDeclaration instanceof EntityDeclaration) {
            return JSL_UTILS.isAbstract((EntityDeclaration) entityDeclaration);
        }
        return false;
    }

    public static boolean isCalculated(EntityMemberDeclaration member) {
        return JSL_UTILS.isCalculated(member);
    }

    public static boolean isRequired(EntityMemberDeclaration member) {
        return JSL_UTILS.isRequired(member);
    }

    public static boolean isRequired(TransferMemberDeclaration member) {
        return JSL_UTILS.isRequired(member);
    }

    public static boolean isMany(EObject obj) {
        return JSL_UTILS.isMany(obj);
    }

    public static boolean isEager(EntityMemberDeclaration member) {
        return JSL_UTILS.isEager(member);
    }

    public static EObject getReferenceType(EntityMemberDeclaration member) {
        return member.getReferenceType();
    }

    public static boolean isReferenceTypePrimitive(EntityMemberDeclaration member) {
        return member.getReferenceType() instanceof PrimitiveDeclaration;
    }

    public static boolean isReferenceTypeEntity(EntityMemberDeclaration member) {
        return member.getReferenceType() instanceof EntityDeclaration;
    }

    /**
     * Get inherited members of an entity (flattened from parent chain).
     * Ported from entityDeclaration.eol: getInheritedMembers()
     */
    public static java.util.List<EntityMemberDeclaration> getInheritedMembers(EntityDeclaration entity) {
        java.util.List<EntityMemberDeclaration> members = new java.util.ArrayList<>();
        members.addAll(entity.getMembers());
        for (EntityDeclaration parent : entity.getExtends()) {
            members.addAll(getInheritedMembers(parent));
        }
        return members;
    }

    /**
     * Check if entity has getDefault() defined.
     * Ported from modifiable.eol: s.getDefault()
     */
    public static Modifier getDefault(Modifiable modifiable) {
        return getModifierByType(modifiable, "default");
    }

    public static EntityRelationOpposite getOpposite(EntityRelationDeclaration relation) {
        return JSL_UTILS.opposite(relation);
    }

    public static EntityRelationDeclaration getOppositeType(EntityRelationOpposite opposite) {
        return JSL_UTILS.oppositeType(opposite);
    }

    // --- Thread-safe PSM entity operations ---

    public static void addRelation(hu.blackbelt.judo.meta.psm.data.EntityType entity,
                                    hu.blackbelt.judo.meta.psm.data.Relation relation) {
        if (entity != null && relation != null) {
            synchronized (entity) {
                entity.getRelations().add(relation);
            }
        }
    }

    public static void addAttribute(hu.blackbelt.judo.meta.psm.data.EntityType entity, Attribute attribute) {
        if (entity != null && attribute != null) {
            synchronized (entity) {
                entity.getAttributes().add(attribute);
            }
        }
    }

    public static void addDataProperty(hu.blackbelt.judo.meta.psm.data.EntityType entity, DataProperty prop) {
        if (entity != null && prop != null) {
            synchronized (entity) {
                entity.getDataProperties().add(prop);
            }
        }
    }

    public static void addNavigationProperty(hu.blackbelt.judo.meta.psm.data.EntityType entity,
                                               NavigationProperty prop) {
        if (entity != null && prop != null) {
            synchronized (entity) {
                entity.getNavigationProperties().add(prop);
            }
        }
    }

    // --- Transfer object related helpers ---

    public static boolean isActorRelated(TransferDeclaration transferDecl) {
        return transferDecl instanceof ActorDeclaration;
    }

    public static void addTransferAttribute(TransferObjectType transferObj, TransferAttribute attr) {
        if (transferObj != null && attr != null) {
            synchronized (transferObj) {
                transferObj.getAttributes().add(attr);
            }
        }
    }

    public static void addTransferRelation(TransferObjectType transferObj, TransferObjectRelation rel) {
        if (transferObj != null && rel != null) {
            synchronized (transferObj) {
                transferObj.getRelations().add(rel);
            }
        }
    }

    /**
     * Get the PSM model root package for a JSL element.
     * Equivalent to ETL's s.eContainer().getModelRoot() which calls
     * self.name.equivalent("CreateModelPackages").
     */
    public static Package getModelRoot(EObject jslElement,
                                        hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        EObject container = jslElement.eContainer();
        if (container instanceof ModelDeclaration) {
            return ctx.equivalent(container, Package.class, Jsl2PsmRuleNames.CREATE_MODEL_PACKAGES);
        }
        // Navigate up to ModelDeclaration
        while (container != null && !(container instanceof ModelDeclaration)) {
            container = container.eContainer();
        }
        if (container != null) {
            return ctx.equivalent(container, Package.class, Jsl2PsmRuleNames.CREATE_MODEL_PACKAGES);
        }
        return null;
    }

    // --- Transfer data declaration helpers (ported from transferFieldDeclaration.eol, transferRelationDeclaration.eol) ---

    public static boolean isMaps(TransferDataDeclaration decl) {
        return JSL_UTILS.isMaps(decl);
    }

    public static boolean isReads(TransferDataDeclaration decl) {
        return JSL_UTILS.isReads(decl);
    }

    public static boolean isAggregation(TransferRelationDeclaration decl) {
        return JSL_UTILS.isAggregation(decl);
    }

    public static boolean isEager(TransferDataDeclaration decl) {
        return JSL_UTILS.isEager(decl);
    }

    /**
     * Get the PSM TransferObjectType equivalent of a JSL TransferDeclaration.
     * Ported from transferDeclaration.eol: getTransferDeclarationEquivalent()
     */
    public static hu.blackbelt.judo.meta.psm.service.TransferObjectType getTransferDeclarationEquivalent(
            TransferDeclaration source,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (source.getMap() == null) {
            return ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType.class,
                    Jsl2PsmRuleNames.CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE);
        } else {
            return ctx.equivalent(source,
                    hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType.class,
                    Jsl2PsmRuleNames.CREATE_MAPPED_TRANSFER_OBJECT_TYPE);
        }
    }

    /**
     * Check if a TransferRelationDeclaration has create allowed.
     * Ported from transferRelationDeclaration.eol: isCreateAllowed()
     */
    public static boolean isCreateAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(m -> "create".equals(m.getType()))
                .anyMatch(m -> !isFalseModifier(m));
    }

    public static boolean isUpdateAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(m -> "update".equals(m.getType()))
                .anyMatch(m -> !isFalseModifier(m));
    }

    public static boolean isDeleteAllowed(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .filter(m -> "delete".equals(m.getType()))
                .anyMatch(m -> !isFalseModifier(m));
    }

    public static boolean isGetRangeSupported(TransferRelationDeclaration rel) {
        return rel.getModifiers().stream()
                .anyMatch(hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier.class::isInstance);
    }

    private static boolean isFalseModifier(Modifier m) {
        try {
            java.lang.reflect.Method method = m.getClass().getMethod("isFalse");
            return (Boolean) method.invoke(m);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the ModelDeclaration ancestor of a JSL element.
     * Navigates up the containment hierarchy to find the ModelDeclaration.
     * Used for singleton type lookups that ETL does via "extensions".equivalent(...).
     */
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

    // --- Actor declaration helpers (ported from actorDeclaration.eol) ---

    /**
     * Get the identity modifier of an actor declaration.
     * Ported from actorDeclaration.eol: getIdentity()
     */
    public static IdentityModifier getIdentity(ActorDeclaration actor) {
        if (actor == null) return null;
        return actor.getModifiers().stream()
                .filter(IdentityModifier.class::isInstance)
                .map(IdentityModifier.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the principal transfer declaration of an actor.
     * Ported from actorDeclaration.eol: getPrincipal()
     *   return self.getIdentity()?.field?.eContainer
     */
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

    /**
     * Get the realm modifier of an actor declaration.
     * Ported from actorDeclaration.eol: getRealm()
     */
    public static RealmModifier getRealm(ActorDeclaration actor) {
        if (actor == null) return null;
        return actor.getModifiers().stream()
                .filter(RealmModifier.class::isInstance)
                .map(RealmModifier.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the claim modifier of an actor declaration.
     * Ported from actorDeclaration.eol: getClaim()
     */
    public static ClaimModifier getClaim(ActorDeclaration actor) {
        if (actor == null) return null;
        return actor.getModifiers().stream()
                .filter(ClaimModifier.class::isInstance)
                .map(ClaimModifier.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the guard modifier of an actor declaration.
     * Ported from actorDeclaration.eol: getGuard()
     */
    public static GuardModifier getGuard(ActorDeclaration actor) {
        if (actor == null) return null;
        return actor.getModifiers().stream()
                .filter(GuardModifier.class::isInstance)
                .map(GuardModifier.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the PSM actor type equivalent of a JSL ActorDeclaration.
     * Ported from actorDeclaration.eol: getActorDeclarationEquivalent()
     *
     * Dispatches to CreateActorType, CreateMappedActorType, or CreateActorTypeWithoutPrincipal
     * based on whether principal is defined and whether it's mapped.
     */
    public static hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType getActorDeclarationEquivalent(
            ActorDeclaration actor,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        TransferDeclaration principal = getPrincipal(actor);
        if (principal != null && principal.getMap() == null) {
            return ctx.equivalent(actor,
                    hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class,
                    Jsl2PsmRuleNames.CREATE_ACTOR_TYPE);
        } else if (principal != null && principal.getMap() != null) {
            return ctx.equivalent(actor,
                    hu.blackbelt.judo.meta.psm.accesspoint.MappedActorType.class,
                    Jsl2PsmRuleNames.CREATE_MAPPED_ACTOR_TYPE);
        } else {
            return ctx.equivalent(actor,
                    hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class,
                    Jsl2PsmRuleNames.CREATE_ACTOR_TYPE_WITHOUT_PRINCIPAL);
        }
    }

    // --- FqName helpers (ported from various *.eol files) ---

    /**
     * Get the fully qualified name of a JSL element.
     * Ported from various getFqName() operations:
     * - ModelDeclaration: self.name
     * - TransferDeclaration (etc.): self.eContainer.getFqName() + "::" + self.name
     * - TransferActionDeclaration: self.eContainer.getFqName() + "#" + self.name
     */
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
            if (element instanceof hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration) {
                return containerFqName + "#" + name;
            }
            return containerFqName + "::" + name;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Get the PSM range equivalent for a TransferActionDeclaration.
     * Ported from action.eol: getTransferActionRangeEquivalent()
     *
     * Returns NavigationProperty (mapped) or StaticNavigation (unmapped).
     */
    public static EObject getTransferActionRangeEquivalent(
            hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration action,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier choice = getChoiceModifier(action);
        if (choice == null) return null;

        TransferDeclaration container = (TransferDeclaration) action.eContainer();
        if (container.getMap() != null) {
            return ctx.equivalent(choice, NavigationProperty.class,
                    Jsl2PsmRuleNames.CREATE_ACTION_INPUT_PARAMETER_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_ACTION_DECLARATION);
        } else {
            return ctx.equivalent(choice, hu.blackbelt.judo.meta.psm.derived.StaticNavigation.class,
                    Jsl2PsmRuleNames.CREATE_ACTION_INPUT_PARAMETER_RANGE_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION);
        }
    }

    // --- Action declaration helpers (ported from transferActionDeclaration.eol, action.eol) ---

    public static boolean isStatic(hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration action) {
        return JSL_UTILS.isStatic(action);
    }

    public static boolean isActionUpdateAllowed(hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration action) {
        if (action.getReturn() == null) return false;
        return action.getModifiers().stream()
                .filter(m -> "update".equals(m.getType()))
                .anyMatch(m -> !isFalseModifier(m));
    }

    public static boolean isActionDeleteAllowed(hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration action) {
        if (action.getReturn() == null) return false;
        return action.getModifiers().stream()
                .filter(m -> "delete".equals(m.getType()))
                .anyMatch(m -> !isFalseModifier(m));
    }

    public static hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier getChoiceModifier(
            hu.blackbelt.judo.meta.jsl.jsldsl.TransferActionDeclaration action) {
        return action.getModifiers().stream()
                .filter(hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier.class::isInstance)
                .map(hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the range equivalent for a TransferRelationDeclaration (NavigationProperty or StaticNavigation).
     * Ported from transferRelationDeclaration.eol: getTransferActionRangeEquivalent()
     */
    public static EObject getTransferRelationRangeEquivalent(
            TransferRelationDeclaration rel,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier choice = rel.getModifiers().stream()
                .filter(hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier.class::isInstance)
                .map(hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier.class::cast)
                .findFirst()
                .orElse(null);
        if (choice == null) return null;

        TransferDeclaration container = (TransferDeclaration) rel.eContainer();
        if (container.getMap() != null) {
            return ctx.equivalent(choice, NavigationProperty.class,
                    Jsl2PsmRuleNames.CREATE_RELATION_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_RELATION);
        } else {
            return ctx.equivalent(choice, hu.blackbelt.judo.meta.psm.derived.StaticNavigation.class,
                    Jsl2PsmRuleNames.CREATE_RELATION_RANG_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION);
        }
    }

    // --- Thread-safe entity operations for bound operations ---

    public static void addBoundOperation(hu.blackbelt.judo.meta.psm.data.EntityType entity,
                                          hu.blackbelt.judo.meta.psm.data.BoundOperation op) {
        if (entity != null && op != null) {
            synchronized (entity) {
                entity.getOperations().add(op);
            }
        }
    }

    // --- Thread-safe TransferObjectType operations helpers ---

    public static void addOperation(hu.blackbelt.judo.meta.psm.service.TransferObjectType transferObj,
                                     hu.blackbelt.judo.meta.psm.service.TransferOperation operation) {
        if (transferObj != null && operation != null) {
            synchronized (transferObj) {
                transferObj.getOperations().add(operation);
            }
        }
    }

    /**
     * Get the first member from a getter expression's features.
     * Ported from ETL: s.getterExpr.features.first().member
     */
    public static EObject getGetterExprFirstMember(TransferDataDeclaration decl) {
        if (decl.getGetterExpr() == null) return null;
        try {
            var features = decl.getGetterExpr().eGet(
                    decl.getGetterExpr().eClass().getEStructuralFeature("features"));
            if (features instanceof java.util.List<?> featureList && !featureList.isEmpty()) {
                EObject first = (EObject) featureList.get(0);
                var memberFeature = first.eClass().getEStructuralFeature("member");
                if (memberFeature != null) {
                    return (EObject) first.eGet(memberFeature);
                }
            }
        } catch (Exception e) {
            // Reflective access failed
        }
        return null;
    }

    // --- Behaviour support helpers (ported from transferDeclaration.eol, transferRelationDeclaration.eol) ---

    public static boolean isCreateSupported(TransferDeclaration transferDecl) {
        if (transferDecl.getMap() == null) return false;
        return transferDecl.getMembers().stream()
                .anyMatch(m -> "TransferCreateDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isUpdateSupported(TransferDeclaration transferDecl) {
        if (transferDecl.getMap() == null) return false;
        return transferDecl.getMembers().stream()
                .anyMatch(m -> "TransferUpdateDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isDeleteSupported(TransferDeclaration transferDecl) {
        if (transferDecl.getMap() == null) return false;
        return transferDecl.getMembers().stream()
                .anyMatch(m -> "TransferDeleteDeclaration".equals(m.eClass().getName()));
    }

    public static boolean isGetTemplateSupported(TransferDeclaration transferDecl) {
        if (isCreateSupported(transferDecl)) {
            return true;
        }
        if (transferDecl.getMap() == null) {
            return isDefinedAsInputParameter(transferDecl);
        }
        return false;
    }

    public static EObject getCreateEvent(TransferDeclaration transferDecl) {
        return transferDecl.getMembers().stream()
                .filter(m -> "TransferCreateDeclaration".equals(m.eClass().getName()))
                .findFirst()
                .orElse(null);
    }

    public static String firstToUpperCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static boolean isAddReferenceAllowed(TransferRelationDeclaration rel) {
        if (!isMaps(rel)) return false;
        if (!(rel.eContainer() instanceof TransferDeclaration)) return false;
        TransferDeclaration container = (TransferDeclaration) rel.eContainer();
        if (!isUpdateSupported(container)) return false;
        boolean many = isMany(rel);
        return many; // add/remove only meaningful for multi-valued
    }

    public static boolean isRemoveReferenceAllowed(TransferRelationDeclaration rel) {
        return isAddReferenceAllowed(rel);
    }

    public static boolean isSetReferenceAllowed(TransferRelationDeclaration rel) {
        if (!isMaps(rel)) return false;
        if (!(rel.eContainer() instanceof TransferDeclaration)) return false;
        TransferDeclaration container = (TransferDeclaration) rel.eContainer();
        if (!isUpdateSupported(container)) return false;
        return !JSL_UTILS.isRequired(rel);
    }

    public static boolean isUnsetReferenceAllowed(TransferRelationDeclaration rel) {
        return isSetReferenceAllowed(rel);
    }

    /**
     * Get the mapped PSM TransferObjectRelation equivalent of a TransferRelationDeclaration.
     * Ported from transferRelationDeclaration.eol: getMappedTransferRelationEquivalent()
     */
    public static TransferObjectRelation getMappedTransferRelationEquivalent(
            TransferRelationDeclaration rel,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (isReads(rel)) {
            return ctx.equivalent(rel, TransferObjectRelation.class,
                    Jsl2PsmRuleNames.CREATE_DERIVED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION);
        } else if (isMaps(rel)) {
            return ctx.equivalent(rel, TransferObjectRelation.class,
                    Jsl2PsmRuleNames.CREATE_MAPPED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION);
        } else {
            return ctx.equivalent(rel, TransferObjectRelation.class,
                    Jsl2PsmRuleNames.CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_TRANSFER_RELATION_DECLARATION);
        }
    }

    /**
     * Check if a TransferDeclaration has a member with the given name.
     * Uses reflective access since TransferMemberDeclaration doesn't have getName() directly.
     * Ported from ETL: s.members.exists(m | m.name == name)
     */
    public static boolean hasMemberWithName(TransferDeclaration transferDecl, String name) {
        if (transferDecl == null || name == null) return false;
        return transferDecl.getMembers().stream().anyMatch(m -> {
            try {
                java.lang.reflect.Method nameMethod = m.getClass().getMethod("getName");
                String memberName = (String) nameMethod.invoke(m);
                return name.equals(memberName);
            } catch (Exception e) {
                return false;
            }
        });
    }

    // --- TransferFieldDeclaration helpers for behaviour rules ---

    /**
     * Get the PSM TransferAttribute equivalent of a TransferFieldDeclaration.
     * Ported from transferFieldDeclaration.eol: getTransferFieldDeclarationEquivalent()
     */
    public static TransferAttribute getTransferFieldDeclarationEquivalent(
            TransferFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isMaps(field) && !isReads(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    Jsl2PsmRuleNames.CREATE_TRANSIENT_TRANSFER_ATTRIBUTE);
        }
        if (isReads(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    Jsl2PsmRuleNames.CREATE_DERIVED_TRANSFER_ATTRIBUTE);
        }
        if (isMaps(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    Jsl2PsmRuleNames.CREATE_MAPPED_TRANSFER_ATTRIBUTE);
        }
        return null;
    }

    /**
     * Check if a TransferFieldDeclaration supports getUploadToken behaviour.
     * Ported from transferFieldDeclaration.eol: isGetUploadTokenSupported()
     */
    public static boolean isGetUploadTokenSupported(TransferFieldDeclaration field) {
        if (isReads(field) && !isMaps(field)) {
            return false;
        }
        PrimitiveDeclaration refPrimitive = field.getReferenceType();
        if (!(refPrimitive instanceof DataTypeDeclaration)) {
            return false;
        }
        DataTypeDeclaration refType = (DataTypeDeclaration) refPrimitive;
        if (refType.getPrimitive() == null || !"binary".equals(refType.getPrimitive())) {
            return false;
        }
        EObject container = field.eContainer();
        if (container instanceof TransferDeclaration) {
            TransferDeclaration transferDecl = (TransferDeclaration) container;
            if (isCreateSupported(transferDecl)) {
                return true;
            }
            if (transferDecl.getMap() == null) {
                return isDefinedAsInputParameter(transferDecl);
            }
        }
        return false;
    }

    /**
     * Check if a TransferDeclaration is used as an input parameter for any TransferActionDeclaration.
     * Ported from transferDeclaration.eol: isDefinedAsInputParameter()
     */
    public static boolean isDefinedAsInputParameter(TransferDeclaration transferDecl) {
        // Find all TransferActionDeclaration instances in the model and check if any
        // has this TransferDeclaration as its parameter type
        EObject root = transferDecl;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        java.util.Iterator<EObject> iter = root.eAllContents();
        while (iter.hasNext()) {
            EObject obj = iter.next();
            if (obj instanceof TransferActionDeclaration) {
                TransferActionDeclaration action = (TransferActionDeclaration) obj;
                if (action.getParameterType() == transferDecl) {
                    return true;
                }
            }
        }
        return false;
    }

    // =========================================================================
    // Sortable / Filterable helpers (ported from transferFieldDeclaration.eol)
    // =========================================================================

    /**
     * Check if a TransferFieldDeclaration is sortable.
     * Ported from transferFieldDeclaration.eol: isSortable()
     *
     * A field is sortable if it maps or reads AND its reference type is a
     * non-binary primitive (string, numeric, date, timestamp, time, boolean).
     */
    public static boolean isSortable(TransferFieldDeclaration field) {
        if (field.getReferenceType() == null) return false;
        String primitive = getPrimitiveKind(field.getReferenceType());
        if (primitive == null) return false;
        return (isMaps(field) || isReads(field)) && isSortablePrimitive(primitive);
    }

    /**
     * Check if a TransferFieldDeclaration is filterable.
     * Ported from transferFieldDeclaration.eol: isFilterable()
     *
     * Same logic as isSortable — a field is filterable if it maps or reads
     * AND its reference type is a non-binary primitive.
     */
    public static boolean isFilterable(TransferFieldDeclaration field) {
        if (field.getReferenceType() == null) return false;
        String primitive = getPrimitiveKind(field.getReferenceType());
        if (primitive == null) return false;
        return (isMaps(field) || isReads(field)) && isSortablePrimitive(primitive);
    }

    /**
     * Check if a TransferDeclaration has at least one sortable field.
     * Ported from transferDeclaration.eol: hasSortableField()
     */
    public static boolean hasSortableField(TransferDeclaration td) {
        return td.getMembers().stream()
                .filter(TransferFieldDeclaration.class::isInstance)
                .map(TransferFieldDeclaration.class::cast)
                .anyMatch(Jsl2PsmHelper::isSortable);
    }

    private static boolean isSortablePrimitive(String primitive) {
        return "string".equals(primitive)
                || "numeric".equals(primitive)
                || "date".equals(primitive)
                || "timestamp".equals(primitive)
                || "time".equals(primitive)
                || "boolean".equals(primitive);
    }

    private static String getPrimitiveKind(PrimitiveDeclaration refType) {
        if (refType instanceof DataTypeDeclaration) {
            return ((DataTypeDeclaration) refType).getPrimitive();
        }
        return null;
    }

    // =========================================================================
    // Cardinality creation helpers
    // =========================================================================

    /**
     * Create a new Cardinality instance inline.
     * This avoids issues with @Lazy @Greedy rules in ETL compatibility mode
     * where EMF containment semantics cause cardinalities to be moved between owners.
     */
    public static Cardinality createCardinality(
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String elementId, int lower, int upper) {
        Cardinality c = ctx.create(Cardinality.class);
        ctx.setElementId(c, elementId);
        c.setLower(lower);
        c.setUpper(upper);
        return c;
    }

    /**
     * Create cardinality from a Modifiable source element.
     * lower = isRequired && !isMany ? 1 : 0; upper = isMany ? -1 : 1
     */
    public static Cardinality createCardinalityFromModifiable(
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            EObject source, String suffix) {
        boolean required = false;
        boolean many = false;
        if (source instanceof EntityFieldDeclaration) {
            required = isRequired((EntityFieldDeclaration) source);
            many = isMany((EntityFieldDeclaration) source);
        } else if (source instanceof EntityRelationDeclaration) {
            required = isRequired((EntityRelationDeclaration) source);
            many = isMany((EntityRelationDeclaration) source);
        } else if (source instanceof TransferRelationDeclaration) {
            required = isRequired((TransferRelationDeclaration) source);
            many = isMany((TransferRelationDeclaration) source);
        } else if (source instanceof EntityRelationOppositeInjected) {
            many = isMany((EntityRelationOppositeInjected) source);
        }
        int lower = required && !many ? 1 : 0;
        int upper = many ? -1 : 1;
        return createCardinality(ctx, "(jsl/" + getJslId(source) + ")/" + suffix, lower, upper);
    }

    // =========================================================================
    // Inline clone helpers for default transfer object inheritance
    // These replace @Lazy @Greedy clone rules that return null in ETL
    // compatibility mode due to the same issue as cardinality rules.
    // =========================================================================

    /**
     * Clone a non-calculated primitive field as a TransferAttribute for inheritance.
     * Each call creates a NEW instance (like ETL equivalentDiscriminated).
     * The discriminator ensures unique XMI IDs per child transfer object.
     */
    public static TransferAttribute cloneFieldTransferAttribute(
            EntityFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferAttribute target = ctx.create(TransferAttribute.class);
        ctx.setElementId(target, discriminator + "/CloneTransferAttributeForDefaultTransferObjectType/" + getJslId(field));
        target.setRequired(isRequired(field));
        target.setName(field.getName());
        Attribute binding = ctx.equivalent(field, Attribute.class, Jsl2PsmRuleNames.CREATE_ATTRIBUTE_FROM_FIELD);
        target.setBinding(binding);
        Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
        target.setDataType(dataType);
        if (getDefault(field) != null) {
            DataProperty defaultValue = ctx.equivalent(getDefault(field),
                    DataProperty.class,
                    Jsl2PsmRuleNames.CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
            target.setDefaultValue(defaultValue);
        }
        return target;
    }

    /**
     * Clone a default value modifier as a TransferAttribute for inheritance.
     */
    public static TransferAttribute cloneDefaultValueTransferAttribute(
            Modifier dm,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferAttribute target = ctx.create(TransferAttribute.class);
        ctx.setElementId(target, discriminator + "/CloneTransferDefaultValueAttributeForDefaultTransferObjectType/" + getJslId(dm));
        EntityFieldDeclaration field = (EntityFieldDeclaration) dm.eContainer();
        String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
        String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
        String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
        String entityName = ((EntityDeclaration) field.eContainer()).getName();
        target.setName((prefix != null ? prefix : "") + field.getName()
                + (midfix != null ? midfix : "") + entityName + (postfix != null ? postfix : ""));
        DataProperty binding = ctx.equivalent(dm,
                DataProperty.class,
                Jsl2PsmRuleNames.CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
        target.setBinding(binding);
        Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
        target.setDataType(dataType);
        return target;
    }

    /**
     * Clone a calculated eager (derived) field as a TransferAttribute for inheritance.
     */
    public static TransferAttribute cloneDerivedTransferAttribute(
            EntityFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferAttribute target = ctx.create(TransferAttribute.class);
        ctx.setElementId(target, discriminator + "/CloneDerivedTransferAttributeForDefaultTransferObjectType/" + getJslId(field));
        target.setName(field.getName());
        DataProperty binding = ctx.equivalent(field, DataProperty.class, Jsl2PsmRuleNames.CREATE_DATA_PROPERTY);
        target.setBinding(binding);
        if (field.getReferenceType() instanceof PrimitiveDeclaration) {
            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
        } else if (field.getReferenceType() instanceof EntityDeclaration) {
            hu.blackbelt.judo.meta.psm.data.EntityType dataType = ctx.equivalent(field.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, Jsl2PsmRuleNames.CREATE_ENTITY_TYPE);
            target.eSet(target.eClass().getEStructuralFeature("dataType"), dataType);
        }
        return target;
    }

    /**
     * Clone a calculated non-eager (entity query) field as a TransferAttribute for inheritance.
     */
    public static TransferAttribute cloneEntityQueryTransferAttribute(
            EntityFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferAttribute target = ctx.create(TransferAttribute.class);
        ctx.setElementId(target, discriminator + "/CloneEntityQueryTransferAttributeForDefaultTransferObjectType/" + getJslId(field));
        target.setName(field.getName());
        DataProperty binding = ctx.equivalent(field, DataProperty.class,
                Jsl2PsmRuleNames.CREATE_DATA_PROPERTY_FOR_ENTITY_QUERY);
        target.setBinding(binding);
        if (field.getReferenceType() instanceof PrimitiveDeclaration) {
            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
        } else if (field.getReferenceType() instanceof EntityDeclaration) {
            hu.blackbelt.judo.meta.psm.data.EntityType dataType = ctx.equivalent(field.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, Jsl2PsmRuleNames.CREATE_ENTITY_TYPE);
            target.eSet(target.eClass().getEStructuralFeature("dataType"), dataType);
        }
        return target;
    }

    /**
     * Clone an entity field (containment) as a TransferObjectRelation for inheritance.
     */
    public static TransferObjectRelation cloneEntityFieldRelation(
            EntityFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferObjectRelation target = ctx.create(TransferObjectRelation.class);
        ctx.setElementId(target, discriminator + "/CloneTransferObjectRelationFromEntityFieldForDefaultTransferObjectType/" + getJslId(field));
        target.setName(field.getName());
        // ETL: extends abstract rule which sets embedded = s.isEager() (true by default for EntityFieldDeclaration)
        target.setEmbedded(isEager(field));
        // ETL: embeddedCreate/Update/Delete = not s.eContainer.isAbstract()
        boolean isContainerAbstract = isAbstract(field.eContainer());
        target.setEmbeddedCreate(!isContainerAbstract);
        target.setEmbeddedUpdate(!isContainerAbstract);
        target.setEmbeddedDelete(!isContainerAbstract);
        Containment binding = ctx.equivalent(field, Containment.class,
                Jsl2PsmRuleNames.CREATE_CONTAINMENT_FROM_FIELD);
        target.setBinding(binding);
        EntityDeclaration refEntity = (EntityDeclaration) field.getReferenceType();
        MappedTransferObjectType refTO = ctx.equivalent(refEntity,
                MappedTransferObjectType.class, Jsl2PsmRuleNames.CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(refTO);
        target.setCardinality(createCardinalityFromModifiable(ctx, field,
                discriminator + "/CloneCardinalityFromEntityFieldForDefaultTransferObjectType/" + getJslId(field)));
        return target;
    }

    /**
     * Clone an entity relation (association) as a TransferObjectRelation for inheritance.
     */
    public static TransferObjectRelation cloneEntityRelation(
            EntityRelationDeclaration rel,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferObjectRelation target = ctx.create(TransferObjectRelation.class);
        ctx.setElementId(target, discriminator + "/CloneTransferObjectRelationFromEntityRelationForDefaultTransferObjectType/" + getJslId(rel));
        target.setName(rel.getName());
        // ETL: extends abstract rule which sets embedded = s.isEager() (false by default for EntityRelationDeclaration)
        target.setEmbedded(isEager(rel));
        // ETL: embeddedCreate/Update/Delete = not s.eContainer.isAbstract()
        boolean isContainerAbstract = isAbstract(rel.eContainer());
        target.setEmbeddedCreate(!isContainerAbstract);
        target.setEmbeddedUpdate(!isContainerAbstract);
        target.setEmbeddedDelete(!isContainerAbstract);
        AssociationEnd binding = ctx.equivalent(rel, AssociationEnd.class,
                Jsl2PsmRuleNames.CREATE_DECLARED_ASSOCIATION_END);
        target.setBinding(binding);
        EntityDeclaration refEntity = (EntityDeclaration) rel.getReferenceType();
        MappedTransferObjectType refTO = ctx.equivalent(refEntity,
                MappedTransferObjectType.class, Jsl2PsmRuleNames.CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(refTO);
        target.setCardinality(createCardinalityFromModifiable(ctx, rel,
                discriminator + "/CloneCardinalityFromEntityRelationForDefaultTransferObjectType/" + getJslId(rel)));
        // Clone default value if present
        if (getDefault(rel) != null) {
            // Note: default value is handled separately via cloneDefaultRelation
        }
        return target;
    }

    /**
     * Clone a default value relation as a TransferObjectRelation for inheritance.
     */
    public static TransferObjectRelation cloneDefaultRelation(
            Modifier dm,
            EntityRelationDeclaration rel,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferObjectRelation target = ctx.create(TransferObjectRelation.class);
        ctx.setElementId(target, discriminator + "/CloneDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType/" + getJslId(dm));
        String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
        String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
        String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
        String entityName = ((EntityDeclaration) rel.eContainer()).getName();
        target.setName((prefix != null ? prefix : "") + rel.getName()
                + (midfix != null ? midfix : "") + entityName + (postfix != null ? postfix : ""));
        // ETL: extends abstract rule which sets embedded = false for default value relations
        target.setEmbedded(false);
        target.setEmbeddedCreate(false);
        target.setEmbeddedUpdate(false);
        target.setEmbeddedDelete(false);
        NavigationProperty binding = ctx.equivalent(dm,
                NavigationProperty.class,
                Jsl2PsmRuleNames.CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
        target.setBinding(binding);
        EntityDeclaration refEntity = (EntityDeclaration) rel.getReferenceType();
        MappedTransferObjectType refTO = ctx.equivalent(refEntity,
                MappedTransferObjectType.class, Jsl2PsmRuleNames.CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(refTO);
        target.setCardinality(createCardinalityFromModifiable(ctx, rel,
                discriminator + "/CloneDefaultCardinalityFromEntityRelationForDefaultTransferObjectType/" + getJslId(dm)));
        return target;
    }

    /**
     * Clone a derived relation as a TransferObjectRelation for inheritance.
     */
    public static TransferObjectRelation cloneDerivedRelation(
            EntityRelationDeclaration rel,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String discriminator) {
        TransferObjectRelation target = ctx.create(TransferObjectRelation.class);
        ctx.setElementId(target, discriminator + "/CloneTransferObjectDerivedRelationForDefaultTransferObjectType/" + getJslId(rel));
        target.setName(rel.getName());
        // ETL: extends abstract rule which sets embedded = s.isEager() (false by default for derived relations)
        target.setEmbedded(isEager(rel));
        // ETL: embeddedCreate/Update/Delete = false for derived relations
        target.setEmbeddedCreate(false);
        target.setEmbeddedUpdate(false);
        target.setEmbeddedDelete(false);
        NavigationProperty binding = ctx.equivalent(rel, NavigationProperty.class,
                Jsl2PsmRuleNames.CREATE_NAVIGATION_PROPERTY);
        target.setBinding(binding);
        EntityDeclaration refEntity = (EntityDeclaration) rel.getReferenceType();
        MappedTransferObjectType refTO = ctx.equivalent(refEntity,
                MappedTransferObjectType.class, Jsl2PsmRuleNames.CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(refTO);
        target.setCardinality(createCardinalityFromModifiable(ctx, rel,
                discriminator + "/CloneDerivedCardinalityForDefaultTransferObjectType/" + getJslId(rel)));
        return target;
    }
}
