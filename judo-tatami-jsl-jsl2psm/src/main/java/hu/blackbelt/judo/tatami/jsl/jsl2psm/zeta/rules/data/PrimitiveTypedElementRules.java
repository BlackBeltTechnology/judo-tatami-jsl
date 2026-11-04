package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.namespace.Annotation;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Primitive typed element rules for JSL to PSM transformation.
 *
 * Ported from data/primitiveTypedElement.etl:
 * - CreateAttributeFromField: non-calculated EntityFieldDeclaration with primitive type -> Attribute
 * - CreateDefaultValueForPrimitiveEntityMember: DefaultModifier on entity field -> DataProperty
 * - CreateDefaultDataExpressionTypeForPrimitiveEntityMember: DefaultModifier on entity field -> DataExpressionType
 * - CreateDefaultValueAnnotationForPrimitiveEntityMember: DefaultModifier on entity field -> Annotation
 * - CreateReads*: Expression on transfer field reads -> StaticData/DataProperty/DataExpressionType
 * - CreateDefault*ForConstructor: DefaultModifier on transfer field constructor -> DataProperty/StaticData/DataExpressionType
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.data.PrimitiveTypedElement.class
)
public class PrimitiveTypedElementRules {

    private static final Logger LOG = LoggerFactory.getLogger(PrimitiveTypedElementRules.class);

    // ========================================================================================
    // Entity field attribute creation
    // ========================================================================================

    @TransformRule(
            name = CREATE_ATTRIBUTE_FROM_FIELD,
            description = "Transform non-calculated EntityFieldDeclaration with primitive type to PSM Attribute"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = Attribute.class)
    @Guard(method = "isNonCalculatedPrimitiveField")
    public TransformFunction<EntityFieldDeclaration, Attribute> createAttributeFromField() {
        return (source, ctx) -> {
            Attribute target = ctx.createTarget(Attribute.class);
            target.setRequired(isRequired(source));
            target.setName(source.getName());
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateAttributeFromField");

            PrimitiveDeclaration primitiveDecl = (PrimitiveDeclaration) source.getReferenceType();
            Primitive psmPrimitive = ctx.equivalent(primitiveDecl, Primitive.class);
            target.setDataType(psmPrimitive);
            target.setIdentifier(source.isIdentifier());

            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(source.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addAttribute(entityType, target);

            LOG.debug("Created Attribute (Field): {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Entity field default value (DataProperty + DataExpressionType + Annotation)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER,
            description = "Create DataProperty for entity field default value"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = DataProperty.class)
    @Guard(method = "isDefaultForPrimitiveEntityField")
    public TransformFunction<DefaultModifier, DataProperty> createDefaultValueForPrimitiveEntityMember() {
        return (source, ctx) -> {
            EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();
            EntityDeclaration entity = (EntityDeclaration) field.eContainer();

            DataProperty target = ctx.createTarget(DataProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultValueForPrimitiveEntityMember");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + entity.getName() + (postfix != null ? postfix : ""));

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(entity, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addDataProperty(entityType, target);

            LOG.debug("Created DataProperty for Default Value: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_PRIMITIVE_ENTITY_MEMBER,
            description = "Create DataExpressionType for entity field default value"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isDefaultForPrimitiveEntityField")
    public TransformFunction<DefaultModifier, DataExpressionType> createDefaultDataExpressionTypeForPrimitiveEntityMember() {
        return (source, ctx) -> {
            EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();
            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultDataExpressionTypeForPrimitiveEntityMember");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source.getExpression(),
                    entityNamePrefix != null ? entityNamePrefix : "_",
                    entityNamePostfix != null ? entityNamePostfix : ""));

            DataProperty prop = ctx.equivalent(source, DataProperty.class,
                    CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
            prop.setGetterExpression(target);

            LOG.debug("Created DataExpressionType for Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_VALUE_ANNOTATION_FOR_PRIMITIVE_ENTITY_MEMBER,
            description = "Create annotation for entity field default value"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = Annotation.class)
    @Guard(method = "isDefaultForPrimitiveEntityField")
    public TransformFunction<DefaultModifier, Annotation> createDefaultValueAnnotationForPrimitiveEntityMember() {
        return (source, ctx) -> {
            EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();
            Annotation target = ctx.createTarget(Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultValueAnnotationForPrimitiveEntityMember");
            target.setName("DefaultValue");

            DataProperty prop = ctx.equivalent(source, DataProperty.class,
                    CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER);
            prop.getAnnotations().add(target);

            LOG.debug("Created DefaultValue Annotation for entity member default: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Transfer field reads (Expression -> StaticData/DataProperty/DataExpressionType)
    // ========================================================================================

    @TransformRule(
            name = CREATE_READS_DATA_EXPRESSION_TYPE_FOR_TRANSFER_FIELD_DECLARATION,
            description = "Create DataExpressionType for transfer field reads expression"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isExpressionForPrimitiveTransferFieldReads")
    public TransformFunction<Expression, DataExpressionType> createReadsDataExpressionType() {
        return (source, ctx) -> {
            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsDataExpressionTypeForTransferFieldDeclaration");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source,
                    entityNamePrefix != null ? entityNamePrefix : "_",
                    entityNamePostfix != null ? entityNamePostfix : ""));

            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();
            if (transfer.getMap() != null) {
                DataProperty prop = ctx.equivalent(source, DataProperty.class,
                        CREATE_READS_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION);
                prop.setGetterExpression(target);
            } else {
                StaticData staticData = ctx.equivalent(source, StaticData.class,
                        CREATE_READS_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION);
                staticData.setGetterExpression(target);
            }

            LOG.debug("Created DataExpressionType for TransferField Reads: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_READS_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION,
            description = "Create StaticData for unmapped transfer object transfer field reads"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = StaticData.class)
    @Guard(method = "isExpressionForUnmappedPrimitiveTransferFieldReads")
    public TransformFunction<Expression, StaticData> createReadsStaticData() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            StaticData target = ctx.createTarget(StaticData.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsStaticDataForUnmappedTransferObjectTransferFieldDeclaration");

            String prefix = ctx.getAttribute("defaultReadsNamePrefix");
            String midfix = ctx.getAttribute("defaultReadsNameMidfix");
            String postfix = ctx.getAttribute("defaultReadsNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            Package modelRoot = getModelRoot(transfer, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticData for TransferField Reads: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_READS_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION,
            description = "Create DataProperty for mapped transfer object transfer field reads"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = DataProperty.class)
    @Guard(method = "isExpressionForMappedPrimitiveTransferFieldReads")
    public TransformFunction<Expression, DataProperty> createReadsDataProperty() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            DataProperty target = ctx.createTarget(DataProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsDataPropertyForMappedTransferObjectTransferFieldDeclaration");

            String prefix = ctx.getAttribute("defaultReadsNamePrefix");
            String midfix = ctx.getAttribute("defaultReadsNameMidfix");
            String postfix = ctx.getAttribute("defaultReadsNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            EntityDeclaration entity = transfer.getMap().getEntity();
            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(entity, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addDataProperty(entityType, target);

            LOG.debug("Created DataProperty for TransferField Reads: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Transfer object constructor defaults (mapped)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create DataExpressionType for mapped transfer object field default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isDefaultForMappedTransferField")
    public TransformFunction<DefaultModifier, DataExpressionType> createDefaultDataExpressionTypeForMappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultDataExpressionTypeForMappedTransferObjectConstructor");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source.getExpression(),
                    entityNamePrefix != null ? entityNamePrefix : "_",
                    entityNamePostfix != null ? entityNamePostfix : ""));

            DataProperty prop = ctx.equivalent(source, DataProperty.class,
                    CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            prop.setGetterExpression(target);

            LOG.debug("Created DataExpressionType for Transfer Object Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create DataProperty for mapped transfer object field default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = DataProperty.class)
    @Guard(method = "isDefaultForMappedTransferField")
    public TransformFunction<DefaultModifier, DataProperty> createDefaultDataPropertyForMappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            DataProperty target = ctx.createTarget(DataProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultDataPropertyForMappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            EntityDeclaration entity = transfer.getMap().getEntity();
            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(entity, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addDataProperty(entityType, target);

            LOG.debug("Created DataProperty for Transfer Object Default Value: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Transfer object constructor defaults (unmapped)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create DataExpressionType for unmapped transfer object field default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isDefaultForUnmappedTransferField")
    public TransformFunction<DefaultModifier, DataExpressionType> createDefaultDataExpressionTypeForUnmappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultDataExpressionTypeForUnmappedTransferObjectConstructor");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    source.getExpression(),
                    entityNamePrefix != null ? entityNamePrefix : "_",
                    entityNamePostfix != null ? entityNamePostfix : ""));

            StaticData staticData = ctx.equivalent(source, StaticData.class,
                    CREATE_DEFAULT_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            staticData.setGetterExpression(target);

            LOG.debug("Created DataExpressionType for Transfer Object Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create StaticData for unmapped transfer object field default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = StaticData.class)
    @Guard(method = "isDefaultForUnmappedTransferField")
    public TransformFunction<DefaultModifier, StaticData> createDefaultStaticDataForUnmappedConstructor() {
        return (source, ctx) -> {
            TransferFieldDeclaration field = (TransferFieldDeclaration) source.eContainer();
            TransferDeclaration transfer = (TransferDeclaration) field.eContainer();

            StaticData target = ctx.createTarget(StaticData.class);
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultStaticDataForUnmappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + transfer.getName() + (postfix != null ? postfix : ""));

            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);
            target.setRequired(false);

            Package modelRoot = getModelRoot(transfer, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticData for Transfer Object Default Value: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isNonCalculatedPrimitiveField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypePrimitive(field) && !isCalculated(field);
    }

    public boolean isDefaultForPrimitiveEntityField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) dm.eContainer();
        return field.getReferenceType() instanceof PrimitiveDeclaration;
    }

    public boolean isExpressionForPrimitiveTransferFieldReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof Expression)) return false;
        Expression expr = (Expression) eObject;
        if (!(expr.eContainer() instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) expr.eContainer();
        if (!(field.getReferenceType() instanceof PrimitiveDeclaration)) return false;
        return isReads(field) && field.getGetterExpr() == expr;
    }

    public boolean isExpressionForUnmappedPrimitiveTransferFieldReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isExpressionForPrimitiveTransferFieldReads(eObject, ctx)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject.eContainer();
        TransferDeclaration transfer = (TransferDeclaration) field.eContainer();
        return transfer.getMap() == null;
    }

    public boolean isExpressionForMappedPrimitiveTransferFieldReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isExpressionForPrimitiveTransferFieldReads(eObject, ctx)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject.eContainer();
        TransferDeclaration transfer = (TransferDeclaration) field.eContainer();
        return transfer.getMap() != null;
    }

    public boolean isDefaultForMappedTransferField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferFieldDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) dm.eContainer().eContainer();
        return transfer.getMap() != null;
    }

    public boolean isDefaultForUnmappedTransferField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof TransferFieldDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) dm.eContainer().eContainer();
        return transfer.getMap() == null;
    }
}
