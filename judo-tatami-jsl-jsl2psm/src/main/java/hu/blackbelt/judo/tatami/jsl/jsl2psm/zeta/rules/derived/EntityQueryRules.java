package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.namespace.Annotation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Entity query rules for JSL to PSM transformation.
 *
 * Ported from derived/entityQuery.etl:
 * - CreateDataPropertyForEntityQuery: calculated non-eager primitive field -> DataProperty
 * - CreateGetterExpressionForEntityQuery: @lazy @greedy expression for entity query
 * - CreateQueryWithoutParameterAnnotationForEntityQuery: annotation on query DataProperty
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = DataProperty.class
)
public class EntityQueryRules {

    private static final Logger LOG = LoggerFactory.getLogger(EntityQueryRules.class);

    /**
     * CreateDataPropertyForEntityQuery
     * guard: s.getReferenceType().isKindOf(JSL!PrimitiveDeclaration) and s.isCalculated() and not s.isEager()
     */
    @TransformRule(
            name = CREATE_DATA_PROPERTY_FOR_ENTITY_QUERY,
            description = "Transform calculated non-eager entity field to DataProperty (entity query)"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = DataProperty.class)
    @Guard(method = "isCalculatedNonEagerPrimitiveField")
    public TransformFunction<EntityFieldDeclaration, DataProperty> createDataPropertyForEntityQuery() {
        return (source, ctx) -> {
            DataProperty target = ctx.createTarget(DataProperty.class);
            target.setName(source.getName());
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDataPropertyForEntityQuery");

            // t.getterExpression = s.equivalent("CreateGetterExpressionForEntityQuery")
            DataExpressionType getterExpr = ctx.equivalent(source, DataExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_ENTITY_QUERY);
            target.setGetterExpression(getterExpr);

            // t.dataType = s.getReferenceType().getPrimitiveDeclarationEquivalent()
            PrimitiveDeclaration primitiveDecl = (PrimitiveDeclaration) source.getReferenceType();
            hu.blackbelt.judo.meta.psm.type.Primitive psmPrimitive =
                    ctx.equivalent(primitiveDecl, hu.blackbelt.judo.meta.psm.type.Primitive.class);
            target.setDataType(psmPrimitive);

            // s.eContainer.getEntityDeclarationEquivalent().dataProperties.add(t)
            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(source.eContainer(), hu.blackbelt.judo.meta.psm.data.EntityType.class);
            addDataProperty(entityType, target);

            LOG.debug("Created DataProperty (Entity Query): {}", target.getName());
            return target;
        };
    }

    /**
     * CreateGetterExpressionForEntityQuery
     * guard: s.getReferenceType().isKindOf(JSL!PrimitiveDeclaration) and s.isCalculated() and not s.isEager()
     */
    @TransformRule(
            name = CREATE_GETTER_EXPRESSION_FOR_ENTITY_QUERY,
            description = "Create DataExpressionType for entity query"
    )
    @Greedy
    @Lazy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isCalculatedNonEagerPrimitiveField")
    public TransformFunction<EntityFieldDeclaration, DataExpressionType> createGetterExpressionForEntityQuery() {
        return (source, ctx) -> {
            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";

            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetterExpressionForEntityQuery");
            target.setExpression(JslExpressionToJqlExpression.getJqlForEntityQuery(source, entityNamePrefix, entityNamePostfix));

            LOG.debug("Created DataExpressionType for Entity Query: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateQueryWithoutParameterAnnotationForEntityQuery
     * guard: same as above
     */
    @TransformRule(
            name = CREATE_QUERY_WITHOUT_PARAMETER_ANNOTATION_FOR_ENTITY_QUERY,
            description = "Create QueryWithoutParameter Annotation for entity query DataProperty"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = Annotation.class)
    @Guard(method = "isCalculatedNonEagerPrimitiveField")
    public TransformFunction<EntityFieldDeclaration, Annotation> createQueryWithoutParameterAnnotationForEntityQuery() {
        return (source, ctx) -> {
            Annotation target = ctx.createTarget(Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryWithoutParameterAnnotationForEntityQuery");
            target.setName("QueryWithoutParameter");

            // s.equivalent("CreateDataPropertyForEntityQuery").annotations.add(t)
            DataProperty dataProp = ctx.equivalent(source, DataProperty.class,
                    CREATE_DATA_PROPERTY_FOR_ENTITY_QUERY);
            if (dataProp != null) {
                dataProp.getAnnotations().add(target);
            }

            LOG.debug("Created QueryWithoutParameter Annotation for entity query: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isCalculatedNonEagerPrimitiveField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypePrimitive(field) && isCalculated(field) && !isEager(field);
    }
}
