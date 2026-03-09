package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Transfer attribute rules for explicit transfer declarations.
 *
 * Ported from structure/transferDeclarationTransferAttribute.etl
 */
@TransformationContext(
        source = TransferFieldDeclaration.class,
        target = TransferAttribute.class
)
public class TransferAttributeRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferAttributeRules.class);

    // ========================================================================================
    // Transient transfer attribute (not mapped, not reads)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSIENT_TRANSFER_ATTRIBUTE,
            description = "Transform transient transfer field to TransferAttribute"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isTransientPrimitiveField")
    public TransformFunction<TransferFieldDeclaration, TransferAttribute> createTransientTransferAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransientTransferAttribute");

            populateBaseTransferAttribute(source, target, ctx);

            // Default value handling
            if (getDefault(source) != null) {
                TransferDeclaration container = (TransferDeclaration) source.eContainer();
                if (container.getMap() == null) {
                    StaticData defaultValue = ctx.equivalent(getDefault(source), StaticData.class,
                            CREATE_DEFAULT_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                    target.setDefaultValue(defaultValue);
                } else {
                    DataProperty defaultValue = ctx.equivalent(getDefault(source), DataProperty.class,
                            CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                    target.setDefaultValue(defaultValue);
                }
            }

            TransferObjectType transferObj = getTransferDeclarationEquivalent((TransferDeclaration) source.eContainer(), ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute (Transient) for TransferFieldDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Derived transfer attribute (reads)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DERIVED_TRANSFER_ATTRIBUTE,
            description = "Transform reads transfer field to TransferAttribute"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isReadsPrimitiveField")
    public TransformFunction<TransferFieldDeclaration, TransferAttribute> createDerivedTransferAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDerivedTransferAttribute");

            populateBaseTransferAttribute(source, target, ctx);

            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (container.getMap() != null) {
                DataProperty binding = ctx.equivalent(source.getGetterExpr(), DataProperty.class,
                        CREATE_READS_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION);
                target.setBinding(binding);
            } else {
                StaticData binding = ctx.equivalent(source.getGetterExpr(), StaticData.class,
                        CREATE_READS_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION);
                target.setBinding(binding);
            }

            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute (Derived) for TransferFieldDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Mapped transfer attribute (maps)
    // ========================================================================================

    @TransformRule(
            name = CREATE_MAPPED_TRANSFER_ATTRIBUTE,
            description = "Transform mapped transfer field to TransferAttribute"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedPrimitiveField")
    public TransformFunction<TransferFieldDeclaration, TransferAttribute> createMappedTransferAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMappedTransferAttribute");

            populateBaseTransferAttribute(source, target, ctx);

            // Binding: getterExpr.features.first().member -> CreateAttributeFromField
            EObject member = getGetterExprFirstMember(source);
            if (member instanceof EntityFieldDeclaration && !isCalculated((EntityFieldDeclaration) member)) {
                Attribute binding = ctx.equivalent(member, Attribute.class, CREATE_ATTRIBUTE_FROM_FIELD);
                target.setBinding(binding);
            }

            // Default value
            if (getDefault(source) != null) {
                DataProperty defaultValue = ctx.equivalent(getDefault(source), DataProperty.class,
                        CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
                target.setDefaultValue(defaultValue);
            } else if (member instanceof EntityFieldDeclaration && getDefault((EntityFieldDeclaration) member) != null) {
                DataProperty defaultValue = ctx.equivalent(getDefault((EntityFieldDeclaration) member),
                        DataProperty.class, CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
                target.setDefaultValue(defaultValue);
            }

            TransferObjectType transferObj = getTransferDeclarationEquivalent((TransferDeclaration) source.eContainer(), ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute (Mapped) for TransferFieldDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Mapped transfer attribute entity default (entity-level default, no transfer-level default)
    // ========================================================================================

    @TransformRule(
            name = CREATE_MAPPED_TRANSFER_ATTRIBUTE_ENTITY_DEFAULT,
            description = "Transform mapped transfer field with entity default to TransferAttribute"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedFieldWithEntityDefault")
    public TransformFunction<TransferFieldDeclaration, TransferAttribute> createMappedTransferAttributeEntityDefault() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMappedTransferAttributeEntityDefault");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            // ETL: s.eContainer.eContainer.name = ModelDeclaration name (not TransferDeclaration name)
            String containerName = ((hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration) source.eContainer().eContainer()).getName();
            target.setName((prefix != null ? prefix : "") + source.getName()
                    + (midfix != null ? midfix : "") + containerName + (postfix != null ? postfix : ""));

            EObject member = getGetterExprFirstMember(source);
            DataProperty binding = ctx.equivalent(getDefault((EntityFieldDeclaration) member),
                    DataProperty.class, CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
            target.setBinding(binding);

            Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            TransferObjectType transferObj = getTransferDeclarationEquivalent((TransferDeclaration) source.eContainer(), ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute EntityDefault for TransferFieldDeclaration: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default value attribute for mapped transfer object constructor
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Transform default modifier to TransferAttribute for mapped transfer constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isDefaultForMappedTransferField")
    public TransformFunction<DefaultModifier, TransferAttribute> createTransferEntityDefaultValueAttributeForMappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateTransferEntityDefaultValueAttributeForMappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            DataProperty binding = ctx.equivalent(source, DataProperty.class,
                    CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            target.setBinding(binding);

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);

            TransferObjectType transferObj = getTransferDeclarationEquivalent(transfer, ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute DefaultValue for MappedTransferObjectConstructor: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default value attribute for unmapped transfer object constructor
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Transform default modifier to TransferAttribute for unmapped transfer constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isDefaultForUnmappedTransferField")
    public TransformFunction<DefaultModifier, TransferAttribute> createTransferEntityDefaultValueAttributeForUnmappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateTransferEntityDefaultValueAttributeForUnmappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            StaticData binding = ctx.equivalent(source, StaticData.class,
                    CREATE_DEFAULT_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            target.setBinding(binding);

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);

            TransferObjectType transferObj = getTransferDeclarationEquivalent(transfer, ctx);
            addTransferAttribute(transferObj, target);

            LOG.debug("Created TransferAttribute DefaultValue for UnmappedTransferObjectConstructor: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Annotation for default value attribute (mapped)
    // ========================================================================================

    @TransformRule(
            name = CREATE_ANNOTATION_FOR_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create annotation for default value TransferAttribute in mapped constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = hu.blackbelt.judo.meta.psm.namespace.Annotation.class)
    @Guard(method = "isDefaultForMappedTransferField")
    public TransformFunction<DefaultModifier, hu.blackbelt.judo.meta.psm.namespace.Annotation> createAnnotationForMappedDefaultValue() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();

            hu.blackbelt.judo.meta.psm.namespace.Annotation target =
                    ctx.createTarget(hu.blackbelt.judo.meta.psm.namespace.Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateAnnotationForTransferEntityDefaultValueAttributeForMappedTransferObjectConstructor");

            target.setName("TransferObjectAttributeWithDefaultValue");

            TransferAttribute defaultAttr = ctx.equivalent(source, TransferAttribute.class,
                    CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            defaultAttr.getAnnotations().add(target);

            LOG.debug("Created annotation for TransferAttribute DefaultValue (mapped): [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Annotation for default value attribute (unmapped)
    // ========================================================================================

    @TransformRule(
            name = CREATE_ANNOTATION_FOR_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create annotation for default value TransferAttribute in unmapped constructor"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = hu.blackbelt.judo.meta.psm.namespace.Annotation.class)
    @Guard(method = "isDefaultForUnmappedTransferField")
    public TransformFunction<DefaultModifier, hu.blackbelt.judo.meta.psm.namespace.Annotation> createAnnotationForUnmappedDefaultValue() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();

            hu.blackbelt.judo.meta.psm.namespace.Annotation target =
                    ctx.createTarget(hu.blackbelt.judo.meta.psm.namespace.Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateAnnotationForTransferEntityDefaultValueAttributeForUnmappedTransferObjectConstructor");

            target.setName("TransferObjectAttributeWithDefaultValue");

            TransferAttribute defaultAttr = ctx.equivalent(source, TransferAttribute.class,
                    CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            defaultAttr.getAnnotations().add(target);

            LOG.debug("Created annotation for TransferAttribute DefaultValue (unmapped): [{}]", target.getName());
            return target;
        };
    }

    // --- Shared populate methods ---

    private void populateBaseTransferAttribute(TransferFieldDeclaration source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setRequired(isRequired(source));
        target.setName(source.getName());

        Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
        target.setDataType(dataType);
    }

    // --- Guard methods ---

    public boolean isTransientPrimitiveField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!(field.getReferenceType() instanceof PrimitiveDeclaration)) return false;
        return !isMaps(field) && !isReads(field);
    }

    public boolean isReadsPrimitiveField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!(field.getReferenceType() instanceof PrimitiveDeclaration)) return false;
        return isReads(field);
    }

    public boolean isMappedPrimitiveField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!(field.getReferenceType() instanceof PrimitiveDeclaration)) return false;
        return isMaps(field);
    }

    public boolean isMappedFieldWithEntityDefault(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!isMaps(field)) return false;
        EObject member = getGetterExprFirstMember(field);
        return member instanceof EntityFieldDeclaration
                && getDefault((EntityFieldDeclaration) member) != null
                && getDefault(field) == null;
    }

    public boolean isDefaultForMappedTransferField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferFieldDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) dm.eContainer().eContainer();
        return transfer.getMap() != null;
    }

    public boolean isDefaultForUnmappedTransferField(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferFieldDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) dm.eContainer().eContainer();
        return transfer.getMap() == null;
    }
}
