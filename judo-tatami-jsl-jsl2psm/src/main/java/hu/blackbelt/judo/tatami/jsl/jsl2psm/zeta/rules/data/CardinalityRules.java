package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferRelationDeclaration;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Cardinality rules for JSL to PSM transformation.
 *
 * Ported from data/cardinality.etl: 6 @lazy @greedy rules creating Cardinality objects.
 *
 * All rules are @Lazy @Greedy - they are triggered via equivalentDiscriminated() calls
 * from association, containment, and query rules.
 */
@TransformationContext(
        source = hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration.class,
        target = Cardinality.class
)
public class CardinalityRules {

    private static final Logger LOG = LoggerFactory.getLogger(CardinalityRules.class);

    /**
     * CreateCardinalityForRelationDeclaration
     * guard: not s.isCalculated()
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_RELATION_DECLARATION,
            description = "Create Cardinality for non-calculated EntityRelationDeclaration"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = Cardinality.class)
    @Guard(method = "isNonCalculatedRelation")
    public TransformFunction<EntityRelationDeclaration, Cardinality> createCardinalityForRelationDeclaration() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForRelationDeclaration");

            target.setLower(isRequired(source) && !isMany(source) ? 1 : 0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for RelationDeclaration: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    /**
     * CreateCardinalityForFieldDeclaration
     * guard: not s.isCalculated()
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_FIELD_DECLARATION,
            description = "Create Cardinality for non-calculated EntityFieldDeclaration"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = Cardinality.class)
    @Guard(method = "isNonCalculatedField")
    public TransformFunction<EntityFieldDeclaration, Cardinality> createCardinalityForFieldDeclaration() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForFieldDeclaration");

            target.setLower(isRequired(source) && !isMany(source) ? 1 : 0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for FieldDeclaration: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    /**
     * CreateCardinalityForOppositeAddedRelation (no guard)
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_OPPOSITE_ADDED_RELATION,
            description = "Create Cardinality for EntityRelationOppositeInjected"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityRelationOppositeInjected.class)
    @To(type = Cardinality.class)
    public TransformFunction<EntityRelationOppositeInjected, Cardinality> createCardinalityForOppositeAddedRelation() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForOppositeAddedRelation");

            target.setLower(0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for OppositeAddedRelation: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    /**
     * CreateCardinalityForDerivedDeclaration
     * guard: s.getReferenceType().isKindOf(JSL!EntityDeclaration) and s.isCalculated()
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_DERIVED_DECLARATION,
            description = "Create Cardinality for calculated EntityRelationDeclaration to entity"
    )
    @Lazy
    @Greedy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = Cardinality.class)
    @Guard(method = "isDerivedEntityRelation")
    public TransformFunction<EntityRelationDeclaration, Cardinality> createCardinalityForDerivedDeclaration() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForDerivedDeclaration");

            target.setLower(0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for DerivedDeclaration: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    /**
     * CreateCardinalityForStaticQueryDeclaration
     * guard: s.referenceType.isKindOf(JSL!EntityDeclaration)
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_STATIC_QUERY_DECLARATION,
            description = "Create Cardinality for static query with entity reference"
    )
    @Lazy
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = Cardinality.class)
    @Guard(method = "isStaticQueryWithEntityRef")
    public TransformFunction<QueryDeclaration, Cardinality> createCardinalityForStaticQueryDeclaration() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForStaticQueryDeclaration");

            target.setLower(0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for StaticQueryDeclaration: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    /**
     * CreateCardinalityForTransferRelationDeclaration (no guard)
     */
    @TransformRule(
            name = CREATE_CARDINALITY_FOR_TRANSFER_RELATION_DECLARATION,
            description = "Create Cardinality for TransferRelationDeclaration"
    )
    @Lazy
    @Greedy
    @Transform(type = TransferRelationDeclaration.class)
    @To(type = Cardinality.class)
    public TransformFunction<TransferRelationDeclaration, Cardinality> createCardinalityForTransferRelationDeclaration() {
        return (source, ctx) -> {
            Cardinality target = ctx.createTarget(Cardinality.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateCardinalityForTransferRelationDeclaration");

            target.setLower(isRequired(source) && !isMany(source) ? 1 : 0);
            target.setUpper(isMany(source) ? -1 : 1);

            LOG.debug("Created Cardinality for TransferRelationDeclaration: lower={}, upper={}", target.getLower(), target.getUpper());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isNonCalculatedRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return eObject instanceof EntityRelationDeclaration && !isCalculated((EntityRelationDeclaration) eObject);
    }

    public boolean isNonCalculatedField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return eObject instanceof EntityFieldDeclaration && !isCalculated((EntityFieldDeclaration) eObject);
    }

    public boolean isDerivedEntityRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) eObject;
        return isReferenceTypeEntity(rel) && isCalculated(rel);
    }

    public boolean isStaticQueryWithEntityRef(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration query = (QueryDeclaration) eObject;
        return query.getReferenceType() instanceof EntityDeclaration;
    }

    // --- Post-execution hook ---

    /**
     * Normalizes all Cardinality XMI IDs to match ETL @post block behavior:
     * {@code c.setId(c.eContainer.getId() + "/cardinality")}
     */
    @PostExecution
    public void normalizeCardinalityIds(hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        org.eclipse.emf.ecore.resource.ResourceSet psmResourceSet = ctx.getAttribute("__psmResourceSet");
        if (psmResourceSet == null) return;

        var iterator = psmResourceSet.getAllContents();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next instanceof Cardinality) {
                Cardinality c = (Cardinality) next;
                if (c.eContainer() != null) {
                    String containerId = ctx.getElementId(c.eContainer());
                    if (containerId != null) {
                        ctx.setElementId(c, containerId + "/cardinality");
                    }
                }
            }
        }
        LOG.debug("@PostExecution: normalized cardinality IDs");
    }
}
