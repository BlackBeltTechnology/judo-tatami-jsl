package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.Expression;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.data.Containment;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Containment rules for JSL to PSM transformation.
 *
 * Ported from data/containment.etl.
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = Containment.class
)
public class ContainmentRules {

    private static final Logger LOG = LoggerFactory.getLogger(ContainmentRules.class);

    // ========================================================================================
    // CreateContainmentFromField
    // ========================================================================================

    @TransformRule(
            name = CREATE_CONTAINMENT_FROM_FIELD,
            description = "Transform non-calculated entity field with entity type to PSM Containment"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = Containment.class)
    @Guard(method = "isNonCalculatedEntityField")
    public TransformFunction<EntityFieldDeclaration, Containment> createContainmentFromField() {
        return (source, ctx) -> {
            Containment target = ctx.createTarget(Containment.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateContainmentFromField");

            target.setName(source.getName());

            EntityDeclaration refType = (EntityDeclaration) source.getReferenceType();
            hu.blackbelt.judo.meta.psm.data.EntityType targetEntity =
                    ctx.equivalent(refType, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            target.setTarget(targetEntity);

            target.setCardinality(createCardinalityFromModifiable(ctx, source, "CreateCardinalityForFieldDeclaration"));

            hu.blackbelt.judo.meta.psm.data.EntityType ownerEntity =
                    ctx.equivalent(source.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addRelation(ownerEntity, target);

            LOG.debug("Created Containment: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Entity TransferField Reads
    // ========================================================================================

    @TransformRule(
            name = CREATE_READS_REFERENCE_EXPRESSION_TYPE_FOR_TRANSFER_RELATION_DECLARATION,
            description = "Create ReferenceExpressionType for transfer relation reads expression"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isExpressionForTransferRelationReads")
    public TransformFunction<Expression, ReferenceExpressionType> createReadsReferenceExpressionTypeForTransferRelationDeclaration() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsReferenceExpressionTypeForTransferRelationDeclaration");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source, entityNamePrefix, entityNamePostfix));

            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
            if (transferDecl.getMap() != null) {
                NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                        CREATE_READS_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION);
                if (navProp != null) navProp.setGetterExpression(target);
            } else {
                StaticNavigation staticNav = ctx.equivalent(source, StaticNavigation.class,
                        CREATE_READS_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION);
                if (staticNav != null) staticNav.setGetterExpression(target);
            }

            LOG.debug("Created ReferenceExpressionType for TransferRelation Reads: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_READS_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION,
            description = "Create StaticNavigation for unmapped transfer object transfer relation reads"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = StaticNavigation.class)
    @Guard(method = "isExpressionForUnmappedTransferRelationReads")
    public TransformFunction<Expression, StaticNavigation> createReadsStaticNavigationForUnmappedTransferObjectTransferRelationDeclaration() {
        return (source, ctx) -> {
            StaticNavigation target = ctx.createTarget(StaticNavigation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsStaticNavigationForUnmappedTransferObjectTransferRelationDeclaration");

            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();

            String prefix = ctx.getAttribute("defaultReadsNamePrefix");
            String midfix = ctx.getAttribute("defaultReadsNameMidfix");
            String postfix = ctx.getAttribute("defaultReadsNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinalityFromModifiable(ctx, rel, "CreateCardinalityForTransferRelationDeclaration"));

            Package modelRoot = getModelRoot(rel, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticNavigation for TransferRelation Reads: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_READS_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION,
            description = "Create NavigationProperty for mapped transfer object transfer relation reads"
    )
    @Greedy
    @Transform(type = Expression.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isExpressionForMappedTransferRelationReads")
    public TransformFunction<Expression, NavigationProperty> createReadsNavigationPropertyForMappedTransferObjectTransferRelationDeclaration() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateReadsNavigationPropertyForMappedTransferObjectTransferRelationDeclaration");

            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();

            String prefix = ctx.getAttribute("defaultReadsNamePrefix");
            String midfix = ctx.getAttribute("defaultReadsNameMidfix");
            String postfix = ctx.getAttribute("defaultReadsNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinalityFromModifiable(ctx, rel, "CreateCardinalityForTransferRelationDeclaration"));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    transferDecl.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(entityType, target);

            LOG.debug("Created NavigationProperty for TransferRelation Reads: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Transfer Object Constructors Relation Default Expression
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create ReferenceExpressionType for mapped transfer object constructor default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isDefaultForMappedTransferRelation")
    public TransformFunction<DefaultModifier, ReferenceExpressionType> createDefaultReferenceExpressionTypeForMappedTransferObjectConstructor() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateDefaultReferenceExpressionTypeForMappedTransferObjectConstructor");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source.getExpression(), entityNamePrefix, entityNamePostfix));

            NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            if (navProp != null) navProp.setGetterExpression(target);

            LOG.debug("Created ReferenceExpressionType for Transfer Object Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create NavigationProperty for mapped transfer object constructor default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isDefaultForMappedTransferRelation")
    public TransformFunction<DefaultModifier, NavigationProperty> createDefaultNavigationPropertyForMappedTransferObjectConstructor() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateDefaultNavigationPropertyForMappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinalityFromModifiable(ctx, rel, "CreateCardinalityForTransferRelationDeclaration"));

            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    transferDecl.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(entityType, target);

            LOG.debug("Created NavigationProperty for Transfer Object Default Value: {}", source);
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create ReferenceExpressionType for unmapped transfer object constructor default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isDefaultForUnmappedTransferRelation")
    public TransformFunction<DefaultModifier, ReferenceExpressionType> createDefaultReferenceExpressionTypeForUnmappedTransferObjectConstructor() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateDefaultReferenceExpressionTypeForUnmappedTransferObjectConstructor");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source.getExpression(), entityNamePrefix, entityNamePostfix));

            StaticNavigation staticNav = ctx.equivalent(source, StaticNavigation.class,
                    CREATE_DEFAULT_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR);
            if (staticNav != null) staticNav.setGetterExpression(target);

            LOG.debug("Created ReferenceExpressionType for Transfer Object Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR,
            description = "Create StaticNavigation for unmapped transfer object constructor default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = StaticNavigation.class)
    @Guard(method = "isDefaultForUnmappedTransferRelation")
    public TransformFunction<DefaultModifier, StaticNavigation> createDefaultStaticNavigationForUnmappedTransferObjectConstructor() {
        return (source, ctx) -> {
            StaticNavigation target = ctx.createTarget(StaticNavigation.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateDefaultStaticNavigationForUnmappedTransferObjectConstructor");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinalityFromModifiable(ctx, rel, "CreateCardinalityForTransferRelationDeclaration"));

            Package modelRoot = getModelRoot(rel, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticNavigation for Transfer Object Default Value: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Default Transfer Object Relation Default Expression
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_DEFAULT_TRANSFER_OBJECT,
            description = "Create ReferenceExpressionType for default transfer object relation default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isDefaultForEntityRelationField")
    public TransformFunction<DefaultModifier, ReferenceExpressionType> createDefaultReferenceExpressionTypeForDefaultTransferObject() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultReferenceExpressionTypeForDefaultTransferObject");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source.getExpression(), entityNamePrefix, entityNamePostfix));

            NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
            if (navProp != null) navProp.setGetterExpression(target);

            LOG.debug("Created ReferenceExpressionType for Transfer Object Default Value: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT,
            description = "Create NavigationProperty for default transfer object relation default"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isDefaultForEntityRelationField")
    public TransformFunction<DefaultModifier, NavigationProperty> createDefaultNavigationPropertyForDefaultTransferObject() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            EntityFieldDeclaration field = (EntityFieldDeclaration) source.eContainer();
            EntityDeclaration entity = (EntityDeclaration) field.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(field) + ")/CreateDefaultNavigationPropertyForDefaultTransferObject");

            String prefix = ctx.getAttribute("defaultDefaultNamePrefix");
            String midfix = ctx.getAttribute("defaultDefaultNameMidfix");
            String postfix = ctx.getAttribute("defaultDefaultNamePostfix");
            target.setName((prefix != null ? prefix : "") + field.getName()
                    + (midfix != null ? midfix : "") + entity.getName() + (postfix != null ? postfix : ""));

            target.setTarget(ctx.equivalent(field.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE));

            target.setCardinality(createCardinalityFromModifiable(ctx, field, "CreateCardinalityForRelationDeclaration"));

            hu.blackbelt.judo.meta.psm.data.EntityType ownerEntity =
                    ctx.equivalent(entity, hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(ownerEntity, target);

            LOG.debug("Created NavigationProperty for Transfer Object Default Value: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isNonCalculatedEntityField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypeEntity(field) && !isCalculated(field);
    }

    public boolean isExpressionForTransferRelationReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof Expression)) return false;
        Expression expr = (Expression) eObject;
        if (!(expr.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) expr.eContainer();
        return isReads(rel) && rel.getGetterExpr() == expr
                && rel.getReferenceType() instanceof TransferDeclaration;
    }

    public boolean isExpressionForUnmappedTransferRelationReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isExpressionForTransferRelationReads(eObject, ctx)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) ((Expression) eObject).eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() == null;
    }

    public boolean isExpressionForMappedTransferRelationReads(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isExpressionForTransferRelationReads(eObject, ctx)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) ((Expression) eObject).eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() != null;
    }

    public boolean isDefaultForMappedTransferRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        if (!(((DefaultModifier) eObject).eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) ((DefaultModifier) eObject).eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() != null;
    }

    public boolean isDefaultForUnmappedTransferRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        if (!(((DefaultModifier) eObject).eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) ((DefaultModifier) eObject).eContainer();
        TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
        return transferDecl.getMap() == null;
    }

    public boolean isDefaultForEntityRelationField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        if (!(((DefaultModifier) eObject).eContainer() instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) ((DefaultModifier) eObject).eContainer();
        return field.getReferenceType() instanceof EntityDeclaration && !isCalculated(field);
    }
}
