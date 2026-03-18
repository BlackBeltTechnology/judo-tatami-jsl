package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationOppositeInjected;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Default transfer object type rules for JSL to PSM transformation.
 *
 * Ported from structure/entityDeclarationDefaultTransferObjectType.etl:
 * - CreateEntityDefaultTransferObjectType: EntityDeclaration -> MappedTransferObjectType (default representation)
 */
@TransformationContext(
        source = EntityDeclaration.class,
        target = MappedTransferObjectType.class
)
public class DefaultTransferObjectTypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTransferObjectTypeRules.class);

    /**
     * CreateEntityDefaultTransferObjectType
     * guard: generateDefaultTransferObject
     */
    @TransformRule(
            name = CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE,
            description = "Transform EntityDeclaration to default MappedTransferObjectType"
    )
    @Greedy
    @Transform(type = EntityDeclaration.class)
    @To(type = MappedTransferObjectType.class)
    @Guard(method = "shouldGenerateDefaultTransferObject")
    public TransformFunction<EntityDeclaration, MappedTransferObjectType> createEntityDefaultTransferObjectType() {
        return (source, ctx) -> {
            MappedTransferObjectType target = ctx.createTarget(MappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEntityDefaultTransferObjectType");

            // t.entityType = s.getEntityDeclarationEquivalent()
            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(source, hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            target.setEntityType(entityType);

            // t.entityType.defaultRepresentation = t
            entityType.setDefaultRepresentation(target);

            // t.name = defaultTransferObjectNamePrefix + s.name + defaultTransferObjectNamePostfix
            String prefix = ctx.getAttribute("defaultTransferObjectNamePrefix");
            String postfix = ctx.getAttribute("defaultTransferObjectNamePostfix");
            target.setName((prefix != null ? prefix : "") + source.getName() + (postfix != null ? postfix : ""));

            target.setQueryCustomizer(false);

            // Handle inherited members from parent entities
            // ETL uses equivalentDiscriminated(ruleName, t.getId()) which creates
            // a unique clone per (source, discriminator) pair. We pass the target's
            // XMI ID as discriminator so each child TO gets its own clones.
            String discriminator = "(jsl/" + getJslId(source) + ")/CreateEntityDefaultTransferObjectType";
            for (EntityDeclaration parentEntity : source.getExtends()) {
                List<EntityMemberDeclaration> inheritedMembers = getInheritedMembers(parentEntity);

                for (EntityMemberDeclaration im : inheritedMembers) {
                    if (im instanceof EntityFieldDeclaration && !isCalculated(im)) {
                        EntityFieldDeclaration field = (EntityFieldDeclaration) im;
                        if (isReferenceTypePrimitive(field)) {
                            // Clone primitive attribute inline
                            TransferAttribute clonedAttr = cloneFieldTransferAttribute(field, ctx, discriminator);
                            addTransferAttribute(target, clonedAttr);
                            // Clone default value attribute if present
                            if (getDefault(field) != null) {
                                TransferAttribute defaultAttr = cloneDefaultValueTransferAttribute(
                                        getDefault(field), ctx, discriminator);
                                addTransferAttribute(target, defaultAttr);
                            }
                        } else if (isReferenceTypeEntity(field)) {
                            // Clone embedded relation inline
                            TransferObjectRelation clonedRel = cloneEntityFieldRelation(field, ctx, discriminator);
                            addTransferRelation(target, clonedRel);
                        }
                    } else if (im instanceof EntityRelationDeclaration && !isCalculated(im)) {
                        // Clone association relation inline
                        EntityRelationDeclaration relDecl = (EntityRelationDeclaration) im;
                        TransferObjectRelation clonedRel = cloneEntityRelation(relDecl, ctx, discriminator);
                        addTransferRelation(target, clonedRel);
                        // Clone default value relation if present
                        if (getDefault(relDecl) != null) {
                            TransferObjectRelation defaultRel = cloneDefaultRelation(
                                    getDefault(relDecl), relDecl, ctx, discriminator);
                            addTransferRelation(target, defaultRel);
                        }
                    } else if (im instanceof EntityFieldDeclaration && isCalculated(im)) {
                        EntityFieldDeclaration field = (EntityFieldDeclaration) im;
                        if (!isEager(field)) {
                            // Clone entity query transfer attribute inline
                            TransferAttribute clonedAttr = cloneEntityQueryTransferAttribute(field, ctx, discriminator);
                            addTransferAttribute(target, clonedAttr);
                        } else {
                            // Clone derived transfer attribute inline
                            TransferAttribute clonedAttr = cloneDerivedTransferAttribute(field, ctx, discriminator);
                            addTransferAttribute(target, clonedAttr);
                        }
                    } else if (im instanceof EntityRelationDeclaration && isCalculated(im)) {
                        // Clone derived relation inline, passing current entity/TO to avoid
                        // recursive ctx.equivalent() when relation target is self
                        TransferObjectRelation clonedRel = cloneDerivedRelation(
                                (EntityRelationDeclaration) im, ctx, discriminator, source, target);
                        addTransferRelation(target, clonedRel);
                    }
                }

                // ETL: for (o in JSL!EntityRelationOppositeInjected.all().select(o | o.eContainer.getReferenceType() == entity))
                //          t.relations.add(o.equivalent("CloneTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType"));
                for (EntityRelationOppositeInjected opposite : findOppositesTargeting(parentEntity, ctx)) {
                    TransferObjectRelation clonedOpp = ctx.equivalent(opposite,
                            TransferObjectRelation.class, CLONE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE);
                    addTransferRelation(target, clonedOpp);
                }
            }

            // Add to model root package
            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created default MappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    // --- Helper methods ---

    /**
     * Find all EntityRelationOppositeInjected instances where the containing relation's
     * reference type equals the given entity. This matches ETL's:
     * JSL!EntityRelationOppositeInjected.all().select(o | o.eContainer.getReferenceType() == entity)
     */
    private static List<EntityRelationOppositeInjected> findOppositesTargeting(
            EntityDeclaration entity,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        List<EntityRelationOppositeInjected> result = new ArrayList<>();
        for (org.eclipse.emf.ecore.resource.Resource resource : ctx.getSourceResourceSet().getResources()) {
            var it = resource.getAllContents();
            while (it.hasNext()) {
                EObject obj = it.next();
                if (obj instanceof EntityRelationOppositeInjected) {
                    EntityRelationOppositeInjected opposite = (EntityRelationOppositeInjected) obj;
                    if (opposite.eContainer() instanceof EntityRelationDeclaration) {
                        EntityRelationDeclaration rel = (EntityRelationDeclaration) opposite.eContainer();
                        if (rel.getReferenceType() == entity) {
                            result.add(opposite);
                        }
                    }
                }
            }
        }
        return result;
    }

    // --- Guard methods ---

    public boolean shouldGenerateDefaultTransferObject(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityDeclaration)) return false;
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        return generate != null && generate;
    }
}
