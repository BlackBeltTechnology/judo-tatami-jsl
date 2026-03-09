package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.ChoiceModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Transfer relation choice (range) rules for JSL to PSM transformation.
 *
 * Ported from structure/transferDeclarationTransferRelation.etl (lines 140-233):
 * - CreateRelationRangeReferenceExpressionTypeForMappedTransferObjectRelation
 * - CreateRelationRangeNavigationPropertyForMappedTransferObjectRelation
 * - CreateRelationRangeReferenceExpressionTypeForUnmappedTransferObjectRelation
 * - CreateRelationRangStaticNavigationForUnmappedTransferObjectRelation
 * - CreateRelationRangeTransferObjectRelation
 * - CreateCardinalityForGetRelationRangeEntityRelation (@Lazy)
 * - CreateCardinalityForGetRelationRangeTransferObjectRelation (@Lazy)
 */
@TransformationContext(
        source = ChoiceModifier.class,
        target = TransferObjectRelation.class
)
public class TransferRelationChoiceRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferRelationChoiceRules.class);

    // ========================================================================================
    // Mapped: ChoiceModifier -> ReferenceExpressionType + NavigationProperty
    // ========================================================================================

    @TransformRule(
            name = CREATE_RELATION_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_RELATION,
            description = "Create ReferenceExpressionType for mapped transfer relation choices"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isChoiceOnMappedTransferRelation")
    public TransformFunction<ChoiceModifier, ReferenceExpressionType> createRelationRangeReferenceExpressionTypeForMappedTransferObjectRelation() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateRelationRangeReferenceExpressionTypeForMappedTransferObjectRelation");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source.getExpression(), entityNamePrefix, entityNamePostfix));

            NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_RELATION_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_RELATION);
            if (navProp != null) navProp.setGetterExpression(target);

            LOG.debug("Created ReferenceExpressionType for Relation Range: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_RELATION_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_RELATION,
            description = "Create NavigationProperty for mapped transfer relation choices"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isChoiceOnMappedTransferRelation")
    public TransformFunction<ChoiceModifier, NavigationProperty> createRelationRangeNavigationPropertyForMappedTransferObjectRelation() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateRelationRangeNavigationPropertyForMappedTransferObjectRelation");

            String prefix = ctx.getAttribute("defaultRelationRangeNamePrefix");
            String midfix = ctx.getAttribute("defaultRelationRangeNameMidfix");
            String postfix = ctx.getAttribute("defaultRelationRangeNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(rel) + ")/CreateCardinalityForGetRelationRangeEntityRelation", 0, -1));

            // s.eContainer.eContainer.map.entity.getEntityDeclarationEquivalent().navigationProperties.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType = ctx.equivalent(
                    transferDecl.getMap().getEntity(),
                    hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(entityType, target);

            LOG.debug("Created NavigationProperty for Relation Range: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Unmapped: ChoiceModifier -> ReferenceExpressionType + StaticNavigation
    // ========================================================================================

    @TransformRule(
            name = CREATE_RELATION_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION,
            description = "Create ReferenceExpressionType for unmapped transfer relation choices"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isChoiceOnUnmappedTransferRelation")
    public TransformFunction<ChoiceModifier, ReferenceExpressionType> createRelationRangeReferenceExpressionTypeForUnmappedTransferObjectRelation() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateRelationRangeReferenceExpressionTypeForUnmappedTransferObjectRelation");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(source.getExpression(), entityNamePrefix, entityNamePostfix));

            StaticNavigation staticNav = ctx.equivalent(source, StaticNavigation.class,
                    CREATE_RELATION_RANG_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION);
            if (staticNav != null) staticNav.setGetterExpression(target);

            LOG.debug("Created ReferenceExpressionType for Relation Range: {}", target.getExpression());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_RELATION_RANG_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION,
            description = "Create StaticNavigation for unmapped transfer relation choices"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = StaticNavigation.class)
    @Guard(method = "isChoiceOnUnmappedTransferRelation")
    public TransformFunction<ChoiceModifier, StaticNavigation> createRelationRangStaticNavigationForUnmappedTransferObjectRelation() {
        return (source, ctx) -> {
            StaticNavigation target = ctx.createTarget(StaticNavigation.class);
            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();
            ctx.setElementId(target, "(jsl/" + getJslId(rel) + ")/CreateRelationRangStaticNavigationForUnmappedTransferObjectRelation");

            String prefix = ctx.getAttribute("defaultRelationRangeNamePrefix");
            String midfix = ctx.getAttribute("defaultRelationRangeNameMidfix");
            String postfix = ctx.getAttribute("defaultRelationRangeNamePostfix");
            target.setName((prefix != null ? prefix : "") + rel.getName()
                    + (midfix != null ? midfix : "") + transferDecl.getName() + (postfix != null ? postfix : ""));

            target.setTarget(((hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType) getTransferDeclarationEquivalent(rel.getReferenceType(), ctx)).getEntityType());
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(rel) + ")/CreateCardinalityForGetRelationRangeEntityRelation", 0, -1));

            // s.eContainer.eContainer.eContainer.getModelRoot().elements.add(t)
            Package modelRoot = getModelRoot(rel, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticNavigation for Relation Range: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // CreateRelationRangeTransferObjectRelation
    // ========================================================================================

    @TransformRule(
            name = CREATE_RELATION_RANGE_TRANSFER_OBJECT_RELATION,
            description = "Create TransferObjectRelation for relation range"
    )
    @Greedy
    @Transform(type = ChoiceModifier.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isChoiceOnGetRangeSupportedTransferRelation")
    public TransformFunction<ChoiceModifier, TransferObjectRelation> createRelationRangeTransferObjectRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            // Note: ETL uses "(esm/" prefix here, not "(jsl/"
            ctx.setElementId(target, "(esm/" + getJslId(source) + ")/CreateRelationRangeTransferObjectRelation");

            TransferRelationDeclaration rel = (TransferRelationDeclaration) source.eContainer();
            TransferDeclaration transferDecl = (TransferDeclaration) rel.eContainer();

            // t.binding = s.eContainer.getTransferActionRangeEquivalent()
            EObject rangeEquivalent = getTransferRelationRangeEquivalent(rel, ctx);
            target.setBinding((hu.blackbelt.judo.meta.psm.derived.ReferenceAccessor) rangeEquivalent);

            // t.name = s.eContainer.getTransferActionRangeEquivalent().name
            if (rangeEquivalent instanceof hu.blackbelt.judo.meta.psm.namespace.NamedElement) {
                target.setName(((hu.blackbelt.judo.meta.psm.namespace.NamedElement) rangeEquivalent).getName());
            }

            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(rel) + ")/CreateCardinalityForGetRelationRangeTransferObjectRelation", 0, -1));
            target.setTarget(getTransferDeclarationEquivalent(rel.getReferenceType(), ctx));

            // s.eContainer.eContainer.getTransferDeclarationEquivalent().relations.add(t)
            addTransferRelation(getTransferDeclarationEquivalent(transferDecl, ctx), target);

            LOG.debug("Created TransferObjectRelation for Relation Range: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isChoiceOnMappedTransferRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        ChoiceModifier cm = (ChoiceModifier) eObject;
        if (!(cm.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) cm.eContainer().eContainer();
        return transfer.getMap() != null;
    }

    public boolean isChoiceOnUnmappedTransferRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        ChoiceModifier cm = (ChoiceModifier) eObject;
        if (!(cm.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferDeclaration transfer = (TransferDeclaration) cm.eContainer().eContainer();
        return transfer.getMap() == null;
    }

    public boolean isChoiceOnGetRangeSupportedTransferRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof ChoiceModifier)) return false;
        ChoiceModifier cm = (ChoiceModifier) eObject;
        if (!(cm.eContainer() instanceof TransferRelationDeclaration)) return false;
        TransferRelationDeclaration rel = (TransferRelationDeclaration) cm.eContainer();
        return isGetRangeSupported(rel);
    }
}
