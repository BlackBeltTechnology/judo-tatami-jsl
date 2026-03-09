package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOpposite;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Association rules for JSL to PSM transformation.
 *
 * Ported from data/association.etl:
 * - CreateDeclaredAssociationEnd: non-calculated entity relation -> AssociationEnd
 * - CreateNamedOppositeAssociationEnd: EntityRelationOppositeInjected -> AssociationEnd
 *
 * Note: CreateDefaultValueAnnotationForEntityRelationDeclaration is omitted for now
 * as it depends on transfer object structure rules (later phase).
 */
@TransformationContext(
        source = EntityRelationDeclaration.class,
        target = AssociationEnd.class
)
public class AssociationRules {

    private static final Logger LOG = LoggerFactory.getLogger(AssociationRules.class);

    /**
     * CreateDeclaredAssociationEnd
     * guard: s.getReferenceType().isKindOf(JSL!EntityDeclaration) and not s.isCalculated()
     */
    @TransformRule(
            name = CREATE_DECLARED_ASSOCIATION_END,
            description = "Transform non-calculated entity relation to PSM AssociationEnd"
    )
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = AssociationEnd.class)
    @Guard(method = "isNonCalculatedEntityRelation")
    public TransformFunction<EntityRelationDeclaration, AssociationEnd> createDeclaredAssociationEnd() {
        return (source, ctx) -> {
            AssociationEnd target = ctx.createTarget(AssociationEnd.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDeclaredAssociationEnd");

            target.setName(source.getName());

            // t.target = s.getReferenceType().getEntityDeclarationEquivalent()
            EntityDeclaration refType = (EntityDeclaration) source.getReferenceType();
            hu.blackbelt.judo.meta.psm.data.EntityType targetEntity =
                    ctx.equivalent(refType, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            target.setTarget(targetEntity);

            // t.cardinality - create inline to avoid EMF containment issues with lazy rules
            target.setCardinality(createCardinalityFromModifiable(ctx, source, "CreateCardinalityForRelationDeclaration"));

            // Reverse cascade delete not supported yet
            target.setReverseCascadeDelete(false);

            // NOTE: Partner handling is deferred to postProcess phase to avoid recursive
            // equivalent() calls during greedy execution. In ETL, targets are pre-allocated
            // before rule bodies execute, so equivalent() for partners returns the pre-created
            // target. In Zeta, getOrCreate() doesn't cache until the body completes, causing
            // recursive calls to create duplicate AssociationEnds.

            // Add to entity: s.eContainer.getEntityDeclarationEquivalent().relations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType ownerEntity =
                    ctx.equivalent(source.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addRelation(ownerEntity, target);

            LOG.debug("Created AssociationEnd (declared): {}", target.getName());
            return target;
        };
    }

    /**
     * CreateNamedOppositeAssociationEnd (no guard beyond type)
     */
    @TransformRule(
            name = CREATE_NAMED_OPPOSITE_ASSOCIATION_END,
            description = "Transform EntityRelationOppositeInjected to PSM AssociationEnd"
    )
    @Greedy
    @Transform(type = EntityRelationOppositeInjected.class)
    @To(type = AssociationEnd.class)
    public TransformFunction<EntityRelationOppositeInjected, AssociationEnd> createNamedOppositeAssociationEnd() {
        return (source, ctx) -> {
            AssociationEnd target = ctx.createTarget(AssociationEnd.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateNamedOppositeAssociationEnd");

            target.setName(source.getName());

            // var relationAddedFrom = s.eContainer (the EntityRelationDeclaration that contains the opposite)
            EntityRelationDeclaration relationAddedFrom = (EntityRelationDeclaration) source.eContainer();
            // var entityToAdd = relationAddedFrom.getReferenceType()
            EntityDeclaration entityToAdd = (EntityDeclaration) relationAddedFrom.getReferenceType();

            // t.target = relationAddedFrom.eContainer.getEntityDeclarationEquivalent()
            hu.blackbelt.judo.meta.psm.data.EntityType targetEntity =
                    ctx.equivalent(relationAddedFrom.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            target.setTarget(targetEntity);

            // t.cardinality - create inline to avoid EMF containment issues with lazy rules
            target.setCardinality(createCardinalityFromModifiable(ctx, source, "CreateCardinalityForOppositeAddedRelation"));

            // Reverse cascade delete not supported yet
            target.setReverseCascadeDelete(false);

            // NOTE: Partner handling is deferred to postProcess phase (same as CreateDeclaredAssociationEnd)

            // entityToAdd.getEntityDeclarationEquivalent().relations.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType ownerEntity =
                    ctx.equivalent(entityToAdd, hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addRelation(ownerEntity, target);

            LOG.debug("Created AssociationEnd (named opposite): {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isNonCalculatedEntityRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) eObject;
        return isReferenceTypeEntity(rel) && !isCalculated(rel);
    }
}
