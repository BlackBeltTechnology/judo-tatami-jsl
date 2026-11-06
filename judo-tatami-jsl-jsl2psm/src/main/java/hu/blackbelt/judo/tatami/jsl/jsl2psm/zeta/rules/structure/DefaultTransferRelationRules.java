package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Default transfer relation rules for JSL to PSM transformation.
 *
 * Ported from structure/entityDeclarationDefaultTransferRelation.etl
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = TransferObjectRelation.class
)
public class DefaultTransferRelationRules {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTransferRelationRules.class);

    // ========================================================================================
    // Composite relation (EntityFieldDeclaration with entity type)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_FIELD_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform entity field to TransferObjectRelation for default transfer object"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isNonCalculatedEntityFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferObjectRelation> createTransferObjectRelationFromEntityFieldForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectEmbeddedRelationForDefaultTransferObjectType");

            populateCompositeRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityForFieldDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created TransferObjectRelation (field) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_FIELD_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone entity field TransferObjectRelation for inheritance"
    )
    @Lazy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isNonCalculatedField")
    public TransformFunction<EntityFieldDeclaration, TransferObjectRelation> cloneTransferObjectRelationFromEntityFieldForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferObjectEmbeddedRelationForDefaultTransferObjectType");

            populateCompositeRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CloneCardinalityForFieldDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            LOG.debug("Clone TransferObjectRelation (field) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Association relation (EntityRelationDeclaration)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform entity relation to TransferObjectRelation for default transfer object"
    )
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isNonCalculatedEntityRelationForDefault")
    public TransformFunction<EntityRelationDeclaration, TransferObjectRelation> createTransferObjectRelationFromEntityRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectRelationForDefaultTransferObjectType");

            populateAssociationRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityForRelationDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created TransferObjectRelation (relation) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone entity relation TransferObjectRelation for inheritance"
    )
    @Lazy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isNonCalculatedRelation")
    public TransformFunction<EntityRelationDeclaration, TransferObjectRelation> cloneTransferObjectRelationFromEntityRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferObjectRelationForDefaultTransferObjectType");

            populateAssociationRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CloneCardinalityForRelationDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            LOG.debug("Clone TransferObjectRelation (relation) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Opposite injected relation
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform opposite injected to TransferObjectRelation for default transfer object"
    )
    @Transform(type = EntityRelationOppositeInjected.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "shouldGenerateDefaultTransferObject")
    public TransformFunction<EntityRelationOppositeInjected, TransferObjectRelation> createTransferObjectAssociatedOppositeRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType");

            populateOppositeRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityForOppositeAddedRelationDefaultTransfer");
            target.setCardinality(cardinality);

            // entityToAdd.equivalent("CreateEntityDefaultTransferObjectType").relations.add(t)
            EntityRelationDeclaration relationAddedFrom = (EntityRelationDeclaration) source.eContainer();
            EntityDeclaration entityToAdd = (EntityDeclaration) relationAddedFrom.getReferenceType();
            MappedTransferObjectType defaultTO = ctx.equivalent(entityToAdd,
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created TransferObjectRelation (opposite) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone opposite injected TransferObjectRelation for inheritance"
    )
    @Lazy
    @Transform(type = EntityRelationOppositeInjected.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<EntityRelationOppositeInjected, TransferObjectRelation> cloneTransferObjectAssociatedOppositeRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType");

            populateOppositeRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CloneCardinalityForOppositeAddedRelationDefaultTransfer");
            target.setCardinality(cardinality);

            LOG.debug("Clone TransferObjectRelation (opposite) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Derived relation (calculated EntityRelationDeclaration)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_DERIVED_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform calculated entity relation to TransferObjectRelation for default transfer object"
    )
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isCalculatedEntityRelationForDefault")
    public TransformFunction<EntityRelationDeclaration, TransferObjectRelation> createTransferObjectDerivedRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectDerivedRelationForDefaultTransferObjectType");

            populateDerivedRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityForDerivedDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created TransferObjectRelation (derived) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_OBJECT_DERIVED_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone calculated entity relation TransferObjectRelation for inheritance"
    )
    @Lazy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isCalculatedRelation")
    public TransformFunction<EntityRelationDeclaration, TransferObjectRelation> cloneTransferObjectDerivedRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferObjectDerivedRelationForDefaultTransferObjectType");

            populateDerivedRelation(source, target, ctx);

            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CloneCardinalityForDerivedDeclarationDefaultTransfer");
            target.setCardinality(cardinality);

            LOG.debug("Clone TransferObjectRelation (derived) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default relation (DefaultModifier for entity relation)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform default modifier to TransferObjectRelation for default transfer object"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isDefaultValueForEntityRelationForDefault")
    public TransformFunction<DefaultModifier, TransferObjectRelation> createDefaultTransferObjectRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType");

            populateDefaultRelation(source, target, ctx);

            EntityRelationDeclaration relDecl = (EntityRelationDeclaration) source.eContainer();
            Cardinality cardinality = createCardinalityFromModifiable(ctx, relDecl, "CreateCardinalityForDefaultRelationDefaultTransfer");
            target.setCardinality(cardinality);

            // s.eContainer.eContainer.equivalent("CreateEntityDefaultTransferObjectType").relations.add(t)
            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer().eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created DefaultTransferObjectRelation for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_DEFAULT_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone default modifier TransferObjectRelation for inheritance"
    )
    @Lazy
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isDefaultValueForEntityRelationForDefault")
    public TransformFunction<DefaultModifier, TransferObjectRelation> cloneDefaultTransferObjectRelationForDefault() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType");

            populateDefaultRelation(source, target, ctx);

            EntityRelationDeclaration relDecl = (EntityRelationDeclaration) source.eContainer();
            Cardinality cardinality = createCardinalityFromModifiable(ctx, relDecl, "CloneCardinalityForDefaultRelationDefaultTransfer");
            target.setCardinality(cardinality);

            LOG.debug("Clone DefaultTransferObjectRelation for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // --- Shared populate methods ---

    private void populateCompositeRelation(EntityFieldDeclaration source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        // t.target = s.getReferenceType().equivalent("CreateEntityDefaultTransferObjectType")
        MappedTransferObjectType targetTO = ctx.equivalent(source.getReferenceType(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        boolean isContainerAbstract = isAbstract(source.eContainer());
        target.setEmbeddedCreate(!isContainerAbstract);
        target.setEmbeddedUpdate(!isContainerAbstract);
        target.setEmbeddedDelete(!isContainerAbstract);

        target.setEmbedded(isEager(source));

        // t.binding = s.equivalent("CreateContainmentFromField")
        Containment binding = ctx.equivalent(source, Containment.class, CREATE_CONTAINMENT_FROM_FIELD);
        target.setBinding(binding);
    }

    private void populateAssociationRelation(EntityRelationDeclaration source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        MappedTransferObjectType targetTO = ctx.equivalent(source.getReferenceType(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        boolean isContainerAbstract = isAbstract(source.eContainer());
        target.setEmbeddedCreate(!isContainerAbstract);
        target.setEmbeddedUpdate(!isContainerAbstract);
        target.setEmbeddedDelete(!isContainerAbstract);

        // t.binding = s.equivalent("CreateDeclaredAssociationEnd")
        AssociationEnd binding = ctx.equivalent(source, AssociationEnd.class, CREATE_DECLARED_ASSOCIATION_END);
        target.setBinding(binding);
        target.setEmbedded(isEager(source));

        // Default value
        if (getDefault(source) != null) {
            NavigationProperty defaultValue = ctx.equivalent(getDefault(source),
                    NavigationProperty.class, CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
            target.setDefaultValue(defaultValue);
        }
    }

    private void populateOppositeRelation(EntityRelationOppositeInjected source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        EntityRelationDeclaration relationAddedFrom = (EntityRelationDeclaration) source.eContainer();
        EntityDeclaration entityToAdd = (EntityDeclaration) relationAddedFrom.getReferenceType();

        target.setName(source.getName());

        // t.target = relationAddedFrom.eContainer.equivalent("CreateEntityDefaultTransferObjectType")
        MappedTransferObjectType targetTO = ctx.equivalent(relationAddedFrom.eContainer(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        boolean isEntityAbstract = isAbstract(entityToAdd);
        target.setEmbeddedCreate(!isEntityAbstract);
        target.setEmbeddedUpdate(!isEntityAbstract);
        target.setEmbeddedDelete(!isEntityAbstract);

        target.setEmbedded(false);

        // t.binding = s.equivalent("CreateNamedOppositeAssociationEnd")
        AssociationEnd binding = ctx.equivalent(source, AssociationEnd.class, CREATE_NAMED_OPPOSITE_ASSOCIATION_END);
        target.setBinding(binding);
    }

    private void populateDerivedRelation(EntityRelationDeclaration source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        MappedTransferObjectType targetTO = ctx.equivalent(source.getReferenceType(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        target.setEmbeddedCreate(false);
        target.setEmbeddedUpdate(false);
        target.setEmbeddedDelete(false);

        // t.binding = s.equivalent("CreateNavigationProperty")
        NavigationProperty binding = ctx.equivalent(source, NavigationProperty.class, CREATE_NAVIGATION_PROPERTY);
        target.setBinding(binding);
        target.setEmbedded(isEager(source));
    }

    private void populateDefaultRelation(DefaultModifier source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        EntityRelationDeclaration relDecl = (EntityRelationDeclaration) source.eContainer();
        EntityDeclaration entity = (EntityDeclaration) relDecl.eContainer();

        String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
        String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
        String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
        target.setName((prefix != null ? prefix : "") + relDecl.getName()
                + (midfix != null ? midfix : "") + entity.getName() + (postfix != null ? postfix : ""));

        // t.target = relDecl.getReferenceType().equivalent("CreateEntityDefaultTransferObjectType")
        MappedTransferObjectType targetTO = ctx.equivalent(relDecl.getReferenceType(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        target.setEmbeddedCreate(false);
        target.setEmbeddedUpdate(false);
        target.setEmbeddedDelete(false);

        // t.binding = s.equivalent("CreateDefaultNavigationPropertyForDefaultTransferObject")
        NavigationProperty binding = ctx.equivalent(source, NavigationProperty.class,
                CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
        target.setBinding(binding);
        target.setEmbedded(false);
    }

    // --- Guard methods ---

    public boolean isNonCalculatedEntityFieldForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypeEntity(field) && !isCalculated(field);
    }

    public boolean isNonCalculatedField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        return !isCalculated((EntityFieldDeclaration) eObject);
    }

    public boolean isNonCalculatedEntityRelationForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) eObject;
        return isReferenceTypeEntity(rel) && !isCalculated(rel);
    }

    public boolean isNonCalculatedRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        return !isCalculated((EntityRelationDeclaration) eObject);
    }

    public boolean shouldGenerateDefaultTransferObject(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        return generate != null && generate;
    }

    public boolean isCalculatedEntityRelationForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        return isCalculated((EntityRelationDeclaration) eObject);
    }

    public boolean isCalculatedRelation(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        return isCalculated((EntityRelationDeclaration) eObject);
    }

    public boolean isDefaultValueForEntityRelationForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        return dm.eContainer() instanceof EntityRelationDeclaration
                && isReferenceTypeEntity((EntityRelationDeclaration) dm.eContainer())
                && !isCalculated((EntityRelationDeclaration) dm.eContainer());
    }
}
