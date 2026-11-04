package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Transfer relation rules for explicit transfer declarations.
 *
 * Ported from structure/transferDeclarationTransferRelation.etl
 */
@TransformationContext(
        source = TransferRelationDeclaration.class,
        target = TransferObjectRelation.class
)
public class TransferRelationRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferRelationRules.class);

    // ========================================================================================
    // Transient transfer relation (not mapped, not reads)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_TRANSFER_RELATION_DECLARATION,
            description = "Transform transient transfer relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isTransientRelation")
    public TransformFunction<TransferRelationDeclaration, TransferObjectRelation> createTransientTransferRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransientTransferObjectRelationForTransferRelationDeclaration");

            populateBaseTransferRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityFor/CreateTransientTransferObjectRelationForTransferRelationDeclaration");
            target.setCardinality(cardinality);

            // Default value
            if (getDefault(source) != null) {
                TransferDeclaration container = (TransferDeclaration) source.eContainer();
                if (container.getMap() == null) {
                    StaticNavigation defaultValue = ctx.equivalent(getDefault(source), StaticNavigation.class,
                            CREATE_DEFAULT_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                    target.setDefaultValue(defaultValue);
                } else {
                    NavigationProperty defaultValue = ctx.equivalent(getDefault(source), NavigationProperty.class,
                            CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                    target.setDefaultValue(defaultValue);
                }
            }

            TransferObjectType transferObj = getTransferDeclarationEquivalent((TransferDeclaration) source.eContainer(), ctx);
            addTransferRelation(transferObj, target);

            LOG.debug("Created TransferObjectRelation (Transient) for TransferRelationDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Derived transfer relation (reads)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DERIVED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION,
            description = "Transform reads transfer relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isReadsRelation")
    public TransformFunction<TransferRelationDeclaration, TransferObjectRelation> createDerivedTransferRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDerivedTransferObjectEmbeddedRelationForTransferRelationDeclaration");

            populateBaseTransferRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityFor/CreateDerivedTransferObjectEmbeddedRelationForTransferRelationDeclaration");
            target.setCardinality(cardinality);

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMap() != null) {
                NavigationProperty binding = ctx.equivalent(source.getGetterExpr(), NavigationProperty.class,
                        CREATE_READS_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION);
                target.setBinding(binding);
            }
            // For unmapped, ETL calls s.getterExpr.equivalent("CreateReadsReferenceExpressionForUnmapped...")
            // which returns null (no such rule exists). The StaticNavigation IS created but NOT used as binding.
            // Match ETL: binding stays null for unmapped transfer derived relations.

            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addTransferRelation(transferObj, target);

            LOG.debug("Created TransferObjectRelation (Derived) for TransferRelationDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Mapped transfer relation (maps)
    // ========================================================================================

    @TransformRule(
            name = CREATE_MAPPED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION,
            description = "Transform mapped transfer relation"
    )
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isMappedRelation")
    public TransformFunction<TransferRelationDeclaration, TransferObjectRelation> createMappedTransferRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMappedTransferObjectEmbeddedRelationForTransferRelationDeclaration");

            populateBaseTransferRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityFor/CreateMappedTransferObjectEmbeddedRelationForTransferRelationDeclaration");
            target.setCardinality(cardinality);

            // Binding based on entity member type
            EObject entityMember = getGetterExprFirstMember(source);
            if (entityMember instanceof EntityFieldDeclaration && !isCalculated((EntityFieldDeclaration) entityMember)) {
                Containment binding = ctx.equivalent(entityMember, Containment.class, CREATE_CONTAINMENT_FROM_FIELD);
                target.setBinding(binding);
            } else if (entityMember instanceof EntityRelationDeclaration && !isCalculated((EntityRelationDeclaration) entityMember)) {
                AssociationEnd binding = ctx.equivalent(entityMember, AssociationEnd.class, CREATE_DECLARED_ASSOCIATION_END);
                target.setBinding(binding);
            } else if (entityMember instanceof EntityRelationOppositeInjected) {
                AssociationEnd binding = ctx.equivalent(entityMember, AssociationEnd.class, CREATE_NAMED_OPPOSITE_ASSOCIATION_END);
                target.setBinding(binding);
            }

            // Default value
            if (getDefault(source) != null) {
                NavigationProperty defaultValue = ctx.equivalent(getDefault(source), NavigationProperty.class,
                        CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                target.setDefaultValue(defaultValue);
            }

            TransferObjectType transferObj = getTransferDeclarationEquivalent((TransferDeclaration) source.eContainer(), ctx);
            addTransferRelation(transferObj, target);

            LOG.debug("Created TransferObjectRelation (Mapped) for TransferRelationDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default value relation for mapped transfer object constructor
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_RELATION_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Transform default modifier to TransferObjectRelation for mapped constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isDefaultForMappedTransferRelation")
    public TransformFunction<DefaultModifier, TransferObjectRelation> createDefaultRelationForMappedConstructor() {
        return (source, ctx) -> {
            TransferRelationDeclaration relDecl = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) relDecl.eContainer();

            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferEntityDefaultValueRelationForMappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + relDecl.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            NavigationProperty binding = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            target.setBinding(binding);

            TransferObjectType targetTO = getTransferDeclarationEquivalent(relDecl.getReferenceType(), ctx);
            target.setTarget(targetTO);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, relDecl, "CreateCardinalityFor/CreateTransferEntityDefaultValueRelationForMappedTransferObjectConstructor");
            target.setCardinality(cardinality);

            TransferObjectType transferObj = getTransferDeclarationEquivalent(transfer, ctx);
            addTransferRelation(transferObj, target);

            return target;
        };
    }

    // ========================================================================================
    // Default value relation for unmapped transfer object constructor
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_RELATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Transform default modifier to TransferObjectRelation for unmapped constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isDefaultForUnmappedTransferRelation")
    public TransformFunction<DefaultModifier, TransferObjectRelation> createDefaultRelationForUnmappedConstructor() {
        return (source, ctx) -> {
            TransferRelationDeclaration relDecl = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) relDecl.eContainer();

            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferEntityDefaultValueRelationForUnmappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + relDecl.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            StaticNavigation binding = ctx.equivalent(source, StaticNavigation.class,
                    CREATE_DEFAULT_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            target.eSet(target.eClass().getEStructuralFeature("binding"), binding);

            // ETL: t.target = s.eContainer.getReferenceType().getPrimitiveDeclarationEquivalent()
            // NOTE: this looks like a bug in the ETL (setting primitive as target for relation)
            // but we follow it exactly
            EObject targetType = ctx.equivalent(relDecl.getReferenceType(), EObject.class);
            if (targetType != null) {
                target.eSet(target.eClass().getEStructuralFeature("target"), targetType);
            }

            Cardinality cardinality = createCardinalityFromModifiable(ctx, relDecl, "CreateCardinalityFor/CreateTransferEntityDefaultValueRelationForUnmappedTransferObjectConstructor");
            target.setCardinality(cardinality);

            TransferObjectType transferObj = getTransferDeclarationEquivalent(transfer, ctx);
            addTransferRelation(transferObj, target);

            return target;
        };
    }

    // --- Shared populate methods ---

    private void populateBaseTransferRelation(TransferRelationDeclaration source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        TransferObjectType targetTO = getTransferDeclarationEquivalent(source.getReferenceType(), ctx);
        target.setTarget(targetTO);

        if (isCreateAllowed(source)) {
            target.setEmbeddedCreate(true);
        }
        if (isUpdateAllowed(source)) {
            target.setEmbeddedUpdate(true);
        }
        if (isDeleteAllowed(source)) {
            target.setEmbeddedDelete(true);
        }
        target.setEmbedded(isAggregation(source));

        // Range: t.range = s.getTransferActionRangeEquivalent()
        if (isGetRangeSupported(source)) {
            EObject rangeEquivalent = getTransferRelationRangeEquivalent(source, ctx);
            if (rangeEquivalent instanceof hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor) {
                target.setRange((hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor) rangeEquivalent);
            }
        }
    }

    // --- Guard methods ---

    public boolean isTransientRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) eObject;
        if (isActorRelated((TransferDeclaration) rel.eContainer())) return false;
        return !isMaps(rel) && !isReads(rel);
    }

    public boolean isReadsRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) eObject;
        if (isActorRelated((TransferDeclaration) rel.eContainer())) return false;
        return isReads(rel);
    }

    public boolean isMappedRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) eObject;
        if (isActorRelated((TransferDeclaration) rel.eContainer())) return false;
        return isMaps(rel);
    }

    public boolean isDefaultForMappedTransferRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) dm.eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() != null;
    }

    public boolean isDefaultForUnmappedTransferRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) dm.eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() == null;
    }
}
