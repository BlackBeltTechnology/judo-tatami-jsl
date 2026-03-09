package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Data property rules for JSL to PSM transformation.
 *
 * Ported from derived/dataProperty.etl:
 * - CreateDataProperty: calculated & eager EntityFieldDeclaration with primitive type -> DataProperty
 */
@TransformationContext(
        source = EntityFieldDeclaration.class,
        target = DataProperty.class
)
public class DataPropertyRules {

    private static final Logger LOG = LoggerFactory.getLogger(DataPropertyRules.class);

    /**
     * CreateDataProperty
     * guard: s.getReferenceType().isKindOf(JSL!PrimitiveDeclaration) and s.isCalculated() and s.isEager()
     */
    @TransformRule(
            name = CREATE_DATA_PROPERTY,
            description = "Transform calculated eager EntityFieldDeclaration with primitive type to DataProperty"
    )
    @Greedy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = DataProperty.class)
    @Guard(method = "isCalculatedEagerPrimitiveField")
    public TransformFunction<EntityFieldDeclaration, DataProperty> createDataProperty() {
        return (source, ctx) -> {
            DataProperty target = ctx.createTarget(DataProperty.class);
            target.setName(source.getName());
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDataProperty");

            // t.getterExpression = s.equivalent("CreateGetterExpressionForDataType")
            DataExpressionType getterExpr = ctx.equivalent(source, DataExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_DATA_TYPE);
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

            LOG.debug("Created DataProperty: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isCalculatedEagerPrimitiveField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof EntityFieldDeclaration)) return false;
        EntityFieldDeclaration field = (EntityFieldDeclaration) eObject;
        return isReferenceTypePrimitive(field) && isCalculated(field) && isEager(field);
    }
}
