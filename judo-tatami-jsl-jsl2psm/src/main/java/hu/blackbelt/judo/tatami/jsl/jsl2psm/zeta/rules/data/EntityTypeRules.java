package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Entity type rules for JSL to PSM transformation.
 *
 * Ported from data/entityType.etl:
 * - CreateEntityType: transforms EntityDeclaration to PSM EntityType
 *
 * ETL equivalent:
 * <pre>
 * rule CreateEntityType
 *   transform s : JSL!EntityDeclaration
 *   to t : JUDOPSM!EntityType {
 *     t.setId("(jsl/" + s.getId() + ")/CreateEntityType");
 *     t.name = entityNamePrefix + s.name + entityNamePostfix;
 *     t.`abstract` = s.isAbstract();
 *     for (super in s.`extends`) {
 *         t.superEntityTypes.add(super.getEntityDeclarationEquivalent());
 *     }
 *     s.eContainer.getModelRoot().elements.add(t);
 *   }
 * </pre>
 */
@TransformationContext(
        source = EntityDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.data.EntityType.class
)
public class EntityTypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(EntityTypeRules.class);

    @TransformRule(
            name = CREATE_ENTITY_TYPE,
            description = "Transform JSL EntityDeclaration to PSM EntityType"
    )
    @Greedy
    @Transform(type = EntityDeclaration.class)
    @To(type = hu.blackbelt.judo.meta.psm.data.EntityType.class)
    public TransformFunction<EntityDeclaration, hu.blackbelt.judo.meta.psm.data.EntityType> createEntityType() {
        return (source, ctx) -> {
            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";

            hu.blackbelt.judo.meta.psm.data.EntityType target =
                    ctx.createTarget(hu.blackbelt.judo.meta.psm.data.EntityType.class);
            target.setName(entityNamePrefix + source.getName() + entityNamePostfix);
            target.setAbstract(isAbstract(source));

            // Add super entity types
            for (EntityDeclaration superDecl : source.getExtends()) {
                hu.blackbelt.judo.meta.psm.data.EntityType superType =
                        ctx.equivalent(superDecl, hu.blackbelt.judo.meta.psm.data.EntityType.class);
                if (superType != null) {
                    target.getSuperEntityTypes().add(superType);
                }
            }

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEntityType");

            // Add to model root package: s.eContainer.getModelRoot().elements.add(t)
            addToModelPackage(source, target, ctx);

            LOG.debug("Created EntityType: {}", target.getName());
            return target;
        };
    }

    /**
     * Adds an entity to the model root package.
     * Mirrors: s.eContainer.getModelRoot().elements.add(t)
     *
     * In ETL, getModelRoot() on EntityDeclaration first gets getEntityDeclarationEquivalent() then
     * calls getModelRoot() on the result. But for adding the entity itself during creation,
     * we use the model package from the ModelDeclaration container.
     */
    private void addToModelPackage(EntityDeclaration source,
                                   hu.blackbelt.judo.meta.psm.namespace.NamespaceElement target,
                                   hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        EObject container = source.eContainer();
        if (container instanceof hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration) {
            Package modelPackage = ctx.equivalent(container, Package.class);
            addElement(modelPackage, target);
        }
    }
}
