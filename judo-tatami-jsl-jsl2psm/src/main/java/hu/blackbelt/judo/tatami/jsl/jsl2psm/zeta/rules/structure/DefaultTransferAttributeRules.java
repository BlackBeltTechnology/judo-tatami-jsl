package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Default transfer attribute rules for JSL to PSM transformation.
 *
 * Ported from structure/entityDeclarationDefaultTransferAttribute.etl:
 * - CreateTransferAttributeForDefaultTransferObjectType: non-calculated primitive field -> TransferAttribute
 * - CloneTransferAttributeForDefaultTransferObjectType: lazy clone for inheritance
 * - CreateDerivedTransferAttributeForDefaultTransferObjectType: calculated eager field -> TransferAttribute
 * - CloneDerivedTransferAttributeForDefaultTransferObjectType: lazy clone for inheritance
 * - CreateEntityQueryTransferAttributeForDefaultTransferObjectType: calculated non-eager field -> TransferAttribute
 * - CloneEntityQueryTransferAttributeForDefaultTransferObjectType: lazy clone for inheritance
 * - CreateTransferDefaultValueAttributeForDefaultTransferObjectType: default value -> TransferAttribute
 * - CloneTransferDefaultValueAttributeForDefaultTransferObjectType: lazy clone for inheritance
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = TransferAttribute.class
)
public class DefaultTransferAttributeRules {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTransferAttributeRules.class);

    // ========================================================================================
    // Non-calculated primitive field -> TransferAttribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform non-calculated primitive field to TransferAttribute for default transfer object"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isNonCalculatedPrimitiveFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> createTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferAttributeForDefaultTransferObjectType");

            populateFieldTransferAttribute(source, target, ctx);

            // Add to default transfer object: s.eContainer.equivalent("CreateEntityDefaultTransferObjectType").attributes.add(t)
            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferAttribute(defaultTO, target);

            LOG.debug("Created TransferAttribute (Field) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone non-calculated primitive field TransferAttribute for inheritance"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isNonCalculatedField")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> cloneTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferAttributeForDefaultTransferObjectType");

            populateFieldTransferAttribute(source, target, ctx);

            LOG.debug("Clone TransferAttribute (Field) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Calculated eager field (derived) -> TransferAttribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_DERIVED_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform calculated eager field to TransferAttribute for default transfer object"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isCalculatedEagerFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> createDerivedTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDerivedTransferAttributeForDefaultTransferObjectType");

            populateDerivedTransferAttribute(source, target, ctx);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferAttribute(defaultTO, target);

            LOG.debug("Created TransferAttribute (Derived) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_DERIVED_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone calculated eager field TransferAttribute for inheritance"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isCalculatedEagerFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> cloneDerivedTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneDerivedTransferAttributeForDefaultTransferObjectType");

            populateDerivedTransferAttribute(source, target, ctx);

            LOG.debug("Clone TransferAttribute (Derived) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Calculated non-eager field (entity query) -> TransferAttribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform calculated non-eager field to TransferAttribute for default transfer object"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isCalculatedNonEagerFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> createEntityQueryTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEntityQueryTransferAttributeForDefaultTransferObjectType");

            populateEntityQueryTransferAttribute(source, target, ctx);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferAttribute(defaultTO, target);

            LOG.debug("Created TransferAttribute (Entity Query) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone calculated non-eager field TransferAttribute for inheritance"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isCalculatedNonEagerFieldForDefault")
    public TransformFunction<EntityFieldDeclaration, TransferAttribute> cloneEntityQueryTransferAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneEntityQueryTransferAttributeForDefaultTransferObjectType");

            populateEntityQueryTransferAttribute(source, target, ctx);

            LOG.debug("Clone TransferAttribute (Entity Query) for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default value -> TransferAttribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_DEFAULT_VALUE_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform default value modifier to TransferAttribute for default transfer object"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isDefaultValueForPrimitiveFieldForDefault")
    public TransformFunction<DefaultModifier, TransferAttribute> createTransferDefaultValueAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferDefaultValueAttributeForDefaultTransferObjectType");

            populateDefaultValueTransferAttribute(source, target, ctx);

            // s.eContainer.eContainer.equivalent("CreateEntityDefaultTransferObjectType").attributes.add(t)
            MappedTransferObjectType defaultTO = ctx.equivalent(source.eContainer().eContainer(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferAttribute(defaultTO, target);

            LOG.debug("Created TransferAttribute DefaultValue for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_DEFAULT_VALUE_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Clone default value TransferAttribute for inheritance"
    )
    @Lazy
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<DefaultModifier, TransferAttribute> cloneTransferDefaultValueAttributeForDefaultTransferObjectType() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferDefaultValueAttributeForDefaultTransferObjectType");

            populateDefaultValueTransferAttribute(source, target, ctx);

            LOG.debug("Clone TransferAttribute Default Value for DefaultTransferObjectType: [{}]", target.getName());
            return target;
        };
    }

    // --- Shared populate methods ---

    private void populateFieldTransferAttribute(EntityFieldDeclaration source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setRequired(isRequired(source));
        target.setName(source.getName());

        // t.binding = s.equivalent("CreateAttributeFromField")
        Attribute binding = ctx.equivalent(source, Attribute.class, CREATE_ATTRIBUTE_FROM_FIELD);
        target.setBinding(binding);

        // t.dataType = s.getReferenceType().getPrimitiveDeclarationEquivalent()
        Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
        target.setDataType(dataType);

        // Default value
        if (getDefault(source) != null) {
            DataProperty defaultValue = ctx.equivalent(getDefault(source),
                    DataProperty.class, CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
            target.setDefaultValue(defaultValue);
        }
    }

    private void populateDerivedTransferAttribute(EntityFieldDeclaration source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        // t.binding = s.equivalent("CreateDataProperty")
        DataProperty binding = ctx.equivalent(source, DataProperty.class, CREATE_DATA_PROPERTY);
        target.setBinding(binding);

        // t.dataType - check if primitive or entity
        // ETL uses dynamic typing for both cases; Java API only accepts Primitive,
        // so we use reflective EMF access for EntityType
        if (source.getReferenceType() instanceof PrimitiveDeclaration) {
            Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
        } else if (source.getReferenceType() instanceof EntityDeclaration) {
            hu.blackbelt.judo.meta.psm.data.EntityType dataType = ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            target.eSet(target.eClass().getEStructuralFeature("dataType"), dataType);
        }
    }

    private void populateEntityQueryTransferAttribute(EntityFieldDeclaration source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        // t.binding = s.equivalent("CreateDataPropertyForEntityQuery")
        DataProperty binding = ctx.equivalent(source, DataProperty.class, CREATE_DATA_PROPERTY_FOR_ENTITY_QUERY);
        target.setBinding(binding);

        // ETL uses dynamic typing; Java API only accepts Primitive for setDataType,
        // so we use reflective EMF access for EntityType
        if (source.getReferenceType() instanceof PrimitiveDeclaration) {
            Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
        } else if (source.getReferenceType() instanceof EntityDeclaration) {
            hu.blackbelt.judo.meta.psm.data.EntityType dataType = ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            target.eSet(target.eClass().getEStructuralFeature("dataType"), dataType);
        }
    }

    private void populateDefaultValueTransferAttribute(DefaultModifier source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();

        // t.name = defaultDefaultNamePrefix + field.name + defaultDefaultNameMidfix + entity.name + defaultDefaultNamePostfix
        String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
        String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
        String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
        String entityName = ((EntityDeclaration) field.eContainer()).getName();
        target.setName((prefix != null ? prefix : "") + field.getName()
                + (midfix != null ? midfix : "") + entityName + (postfix != null ? postfix : ""));

        // t.binding = s.equivalent("CreateDefaultValueForPrimitiveEntityMember")
        DataProperty binding = ctx.equivalent(source, DataProperty.class, CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
        target.setBinding(binding);

        // t.dataType = field.getReferenceType().getPrimitiveDeclarationEquivalent()
        Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
        target.setDataType(dataType);
    }

    // --- Guard methods ---

    public boolean isNonCalculatedPrimitiveFieldForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypePrimitive(field) && !isCalculated(field);
    }

    public boolean isNonCalculatedField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        return !isCalculated((EntityFieldDeclaration) eObject);
    }

    public boolean isCalculatedEagerFieldForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isCalculated(field) && isEager(field);
    }

    public boolean isCalculatedNonEagerFieldForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isCalculated(field) && !isEager(field);
    }

    public boolean isDefaultValueForPrimitiveFieldForDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        return dm.eContainer() instanceof EntityFieldDeclaration
                && ((EntityFieldDeclaration) dm.eContainer()).getReferenceType() instanceof PrimitiveDeclaration;
    }
}
