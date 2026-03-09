package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityFieldDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EntityRelationDeclaration;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Expression type rules for JSL to PSM transformation.
 *
 * Ported from derived/expressionType.etl:
 * - CreateGetterExpressionForDataType: @lazy, EntityFieldDeclaration -> DataExpressionType
 * - CreateGetterExpressionForReferenceType: @lazy, EntityRelationDeclaration -> ReferenceExpressionType
 */
@TransformationContext(
        source = hu.blackbelt.judo.meta.jsl.jsldsl.EntityMemberDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.derived.ExpressionType.class
)
public class ExpressionTypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(ExpressionTypeRules.class);

    /**
     * CreateGetterExpressionForDataType
     * guard: s.isCalculated()
     */
    @TransformRule(
            name = CREATE_GETTER_EXPRESSION_FOR_DATA_TYPE,
            description = "Create DataExpressionType for calculated EntityFieldDeclaration"
    )
    @Lazy
    @Transform(type = EntityFieldDeclaration.class)
    @To(type = DataExpressionType.class)
    @Guard(method = "isCalculatedField")
    public TransformFunction<EntityFieldDeclaration, DataExpressionType> createGetterExpressionForDataType() {
        return (source, ctx) -> {
            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";

            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetterExpressionForDataType");
            target.setExpression(JslExpressionToJqlExpression.getJqlForDerived(source, entityNamePrefix, entityNamePostfix));

            LOG.debug("Created DataExpressionType for Data Property: {}", source.getName());
            return target;
        };
    }

    /**
     * CreateGetterExpressionForReferenceType
     * guard: s.isCalculated()
     */
    @TransformRule(
            name = CREATE_GETTER_EXPRESSION_FOR_REFERENCE_TYPE,
            description = "Create ReferenceExpressionType for calculated EntityRelationDeclaration"
    )
    @Lazy
    @Transform(type = EntityRelationDeclaration.class)
    @To(type = ReferenceExpressionType.class)
    @Guard(method = "isCalculatedRelation")
    public TransformFunction<EntityRelationDeclaration, ReferenceExpressionType> createGetterExpressionForReferenceType() {
        return (source, ctx) -> {
            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";

            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetterExpressionForReferenceType");
            target.setExpression(JslExpressionToJqlExpression.getJqlForDerived(source, entityNamePrefix, entityNamePostfix));

            LOG.debug("Created ReferenceExpressionType for Reference Type: {}", source.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isCalculatedField(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return eObject instanceof EntityFieldDeclaration && isCalculated((EntityFieldDeclaration) eObject);
    }

    public boolean isCalculatedRelation(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return eObject instanceof EntityRelationDeclaration && isCalculated((EntityRelationDeclaration) eObject);
    }
}
