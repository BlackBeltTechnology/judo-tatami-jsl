package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.DefaultModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOpposite;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.psm.data.AssociationEnd;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.namespace.Annotation;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Association rules for JSL to PSM transformation.
 *
 * Ported from data/association.etl:
 * - CreateDeclaredAssociationEnd: non-calculated entity relation -> AssociationEnd
 * - CreateNamedOppositeAssociationEnd: EntityRelationOppositeInjected -> AssociationEnd
 *
 * - CreateDefaultValueAnnotationForEntityRelationDeclaration: DefaultModifier on entity relation -> Annotation
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

    // ========================================================================================
    // Default value annotation for entity relation declaration
    // ========================================================================================

    @TransformRule(
            name = CREATE_DEFAULT_VALUE_ANNOTATION_FOR_ENTITY_RELATION_DECLARATION,
            description = "Create annotation for entity relation default value"
    )
    @Greedy
    @Transform(type = DefaultModifier.class)
    @To(type = Annotation.class)
    @Guard(method = "isDefaultForEntityRelation")
    public TransformFunction<DefaultModifier, Annotation> createDefaultValueAnnotationForEntityRelationDeclaration() {
        return (source, ctx) -> {
            EntityRelationDeclaration relDecl = (EntityRelationDeclaration) source.eContainer();
            Annotation target = ctx.createTarget(Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(relDecl) + ")/CreateDefaultValueAnnotationForEntityRelationDeclaration");
            target.setName("DefaultValue");

            NavigationProperty navProp = ctx.equivalent(source, NavigationProperty.class,
                    CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT);
            navProp.getAnnotations().add(target);

            LOG.debug("Created DefaultValue Annotation for entity relation declaration: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isDefaultForEntityRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof DefaultModifier)) return false;
        DefaultModifier dm = (DefaultModifier) eObject;
        if (!(dm.eContainer() instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) dm.eContainer();
        return isReferenceTypeEntity(rel) && !isCalculated(rel);
    }

    public boolean isNonCalculatedEntityRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityRelationDeclaration)) return false;
        EntityRelationDeclaration rel = (EntityRelationDeclaration) eObject;
        return isReferenceTypeEntity(rel) && !isCalculated(rel);
    }

    // --- Post-execution hook ---

    /**
     * Sets AssociationEnd.partner references after all rules have completed.
     * Deferred from rule bodies because Zeta caches targets after rule body completion,
     * making recursive equivalent() calls create duplicates for circular references.
     */
    @PostExecution
    public void setPartners(hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        ResourceSet jslResourceSet = ctx.getAttribute("__jslResourceSet");
        if (jslResourceSet == null) return;

        Collection<EntityRelationDeclaration> allRelations = getAllContents(jslResourceSet, EntityRelationDeclaration.class);

        for (EntityRelationDeclaration relDecl : allRelations) {
            if (isCalculated(relDecl) || !isReferenceTypeEntity(relDecl)) {
                continue;
            }

            AssociationEnd declaredEnd = ctx.equivalent(relDecl,
                    AssociationEnd.class, CREATE_DECLARED_ASSOCIATION_END);
            if (declaredEnd == null) continue;

            EntityRelationOpposite opposite = getOpposite(relDecl);
            if (opposite == null) continue;

            EntityRelationDeclaration oppositeTypeRef = getOppositeType(opposite);
            if (oppositeTypeRef != null) {
                AssociationEnd partnerEnd = ctx.equivalent(oppositeTypeRef,
                        AssociationEnd.class, CREATE_DECLARED_ASSOCIATION_END);
                if (partnerEnd != null) {
                    declaredEnd.setPartner(partnerEnd);
                }
            }

            if (opposite instanceof EntityRelationOppositeInjected) {
                EntityRelationOppositeInjected injected = (EntityRelationOppositeInjected) opposite;
                if (injected.getName() != null && !injected.getName().isEmpty()) {
                    AssociationEnd partnerEnd = ctx.equivalent(injected,
                            AssociationEnd.class, CREATE_NAMED_OPPOSITE_ASSOCIATION_END);
                    if (partnerEnd != null) {
                        declaredEnd.setPartner(partnerEnd);
                    }
                }
            }
        }

        Collection<EntityRelationOppositeInjected> allOpposites = getAllContents(jslResourceSet, EntityRelationOppositeInjected.class);
        for (EntityRelationOppositeInjected injected : allOpposites) {
            AssociationEnd oppositeEnd = ctx.equivalent(injected,
                    AssociationEnd.class, CREATE_NAMED_OPPOSITE_ASSOCIATION_END);
            if (oppositeEnd == null) continue;

            EntityRelationDeclaration relationAddedFrom = (EntityRelationDeclaration) injected.eContainer();
            AssociationEnd partner = ctx.equivalent(relationAddedFrom,
                    AssociationEnd.class, CREATE_DECLARED_ASSOCIATION_END);
            if (partner != null) {
                oppositeEnd.setPartner(partner);
            }
        }

        LOG.debug("@PostExecution: set association partners");
    }

    private static <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
        java.util.List<T> result = new java.util.ArrayList<>();
        var iterator = resourceSet.getAllContents();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (type.isInstance(next)) {
                result.add(type.cast(next));
            }
        }
        return result;
    }
}
