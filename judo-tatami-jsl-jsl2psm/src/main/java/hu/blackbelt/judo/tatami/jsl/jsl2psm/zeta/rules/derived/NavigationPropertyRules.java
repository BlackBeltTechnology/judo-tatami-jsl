package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Navigation property rules for JSL to PSM transformation.
 *
 * Ported from derived/navigationProperty.etl:
 * - CreateNavigationProperty: calculated EntityRelationDeclaration with entity type -> NavigationProperty
 */
@TransformationContext(
        source = EntityRelationDeclaration.class,
        target = NavigationProperty.class
)
public class NavigationPropertyRules {

    private static final Logger LOG = LoggerFactory.getLogger(NavigationPropertyRules.class);

    /**
     * CreateNavigationProperty
     * guard: s.getReferenceType().isKindOf(JSL!EntityDeclaration) and s.isCalculated()
     */
    @TransformRule(
            name = CREATE_NAVIGATION_PROPERTY,
            description = "Transform calculated entity relation to NavigationProperty"
    )
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isCalculatedEntityRelation")
    public TransformFunction<EntityRelationDeclaration, NavigationProperty> createNavigationProperty() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            target.setName(source.getName());
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateNavigationProperty");

            // t.getterExpression = s.equivalent("CreateGetterExpressionForReferenceType")
            ReferenceExpressionType getterExpr = ctx.equivalent(source, ReferenceExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_REFERENCE_TYPE);
            target.setGetterExpression(getterExpr);

            // t.cardinality = s.equivalentDiscriminated("CreateCardinalityForDerivedDeclaration", t.getId())
            Cardinality cardinality = createCardinalityFromModifiable(ctx, source, "CreateCardinalityForDerivedDeclaration");
            target.setCardinality(cardinality);

            // t.target = s.getReferenceType().getEntityDeclarationEquivalent()
            EntityDeclaration refType = (EntityDeclaration) source.getReferenceType();
            hu.blackbelt.judo.meta.psm.data.EntityType targetEntity =
                    ctx.equivalent(refType, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            target.setTarget(targetEntity);

            // s.eContainer.getEntityDeclarationEquivalent().navigationProperties.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType ownerEntity =
                    ctx.equivalent(source.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addNavigationProperty(ownerEntity, target);

            LOG.debug("Created NavigationProperty: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isCalculatedEntityRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) eObject;
        return isReferenceTypeEntity(rel) && isCalculated(rel);
    }
}
