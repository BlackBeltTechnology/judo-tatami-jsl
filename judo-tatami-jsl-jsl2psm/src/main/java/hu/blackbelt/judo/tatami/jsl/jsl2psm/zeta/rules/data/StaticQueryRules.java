package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data;

import hu.blackbelt.judo.meta.jsl.jsldsl.EntityDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryParameterDeclaration;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.derived.DataExpressionType;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.ReferenceExpressionType;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.namespace.Annotation;
import hu.blackbelt.judo.meta.psm.namespace.AnnotationDetail;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Static query rules for JSL to PSM transformation.
 *
 * Ported from data/query.etl
 */
@TransformationContext(
        source = QueryDeclaration.class,
        target = TransferAttribute.class
)
public class StaticQueryRules {

    private static final Logger LOG = LoggerFactory.getLogger(StaticQueryRules.class);

    // ========================================================================================
    // Entity query transfer attribute (primitive query on entity)
    // ========================================================================================

    @TransformRule(
            name = CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_STATIC_QUERY,
            description = "Transform primitive entity query to TransferAttribute"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isPrimitiveEntityQuery")
    public TransformFunction<QueryDeclaration, TransferAttribute> createEntityQueryTransferAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEntityQueryTransferAttributeForStaticQuery");

            populateQueryTransferAttribute(source, target, ctx);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.getEntity(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferAttribute(defaultTO, target);

            LOG.debug("Created TransferAttribute (Entity Query) for StaticQuery: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_STATIC_QUERY,
            description = "Clone primitive entity query TransferAttribute for inheritance"
    )
    @Lazy
    @Transform(type = QueryDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isPrimitiveEntityQuery")
    public TransformFunction<QueryDeclaration, TransferAttribute> cloneEntityQueryTransferAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneEntityQueryTransferAttributeForStaticQuery");

            populateQueryTransferAttribute(source, target, ctx);

            LOG.debug("Clone TransferAttribute (Entity Query) for StaticQuery: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Entity query transfer object relation (entity query on entity)
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_ENTITY_QUERY_RELATION_FOR_STATIC_QUERY,
            description = "Transform entity query to TransferObjectRelation"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isEntityEntityQuery")
    public TransformFunction<QueryDeclaration, TransferObjectRelation> createTransferObjectEntityQueryRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectEntityQueryRelationForStaticQuery");

            populateQueryRelation(source, target, ctx);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateTransferObjectEntityQueryRelationForStaticQuery/Cardinality", 0, isMany(source) ? -1 : 1);
            target.setCardinality(cardinality);

            MappedTransferObjectType defaultTO = ctx.equivalent(source.getEntity(),
                    MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
            addTransferRelation(defaultTO, target);

            LOG.debug("Created TransferObjectRelation for StaticQuery: [{}]", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CLONE_TRANSFER_OBJECT_QUERY_RELATION_FOR_STATIC_QUERY,
            description = "Clone entity query TransferObjectRelation for inheritance"
    )
    @Lazy
    @Transform(type = QueryDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isEntityEntityQueryForClone")
    public TransformFunction<QueryDeclaration, TransferObjectRelation> cloneTransferObjectQueryRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CloneTransferObjectQueryRelationForStaticQuery");

            populateQueryRelation(source, target, ctx);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CloneTransferObjectQueryRelationForStaticQuery/Cardinality", 0, isMany(source) ? -1 : 1);
            target.setCardinality(cardinality);

            LOG.debug("Clone TransferObjectRelation for StaticQuery: [{}]", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Unmapped transfer object for query parameters
    // ========================================================================================

    @TransformRule(
            name = CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY,
            description = "Create parameter type for parameterized query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "hasParameters")
    public TransformFunction<QueryDeclaration, UnmappedTransferObjectType> createUnmappedTransferObjectForStaticQuery() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnmappedTransferObjectForStaticQuery");

            String prefix = ctx.getAttribute("defaultParameterNamePrefix");
            String midfix = ctx.getAttribute("defaultParameterNameMidfix");
            String postfix = ctx.getAttribute("defaultParameterNamePostfix");

            if (source.getEntity() == null) {
                target.setName((prefix != null ? prefix : "") + source.eContainer().eGet(
                        source.eContainer().eClass().getEStructuralFeature("name"))
                        + (midfix != null ? midfix : "") + source.getName() + (postfix != null ? postfix : ""));
                Package modelRoot = getModelRoot(source, ctx);
                addElement(modelRoot, target);
            } else {
                target.setName((prefix != null ? prefix : "") + source.getEntity().getName()
                        + (midfix != null ? midfix : "") + source.getName() + (postfix != null ? postfix : ""));
                Package modelRoot = getModelRoot(source.getEntity(), ctx);
                addElement(modelRoot, target);
            }

            LOG.debug("Created UnmappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Annotations for parameter object
    // ========================================================================================

    @TransformRule(
            name = CREATE_ORIGINAL_NAME_ANNOTATION_FOR_STATIC_QUERY,
            description = "Create OriginalName annotation for static query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = Annotation.class)
    @Guard(method = "hasParameters")
    public TransformFunction<QueryDeclaration, Annotation> createOriginalNameAnnotation() {
        return (source, ctx) -> {
            Annotation target = ctx.createTarget(Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateOriginalNameAnnotationForStaticQuery");
            target.setName("OriginalSource");

            UnmappedTransferObjectType paramObj = ctx.equivalent(source, UnmappedTransferObjectType.class,
                    CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY);
            paramObj.getAnnotations().add(target);

            return target;
        };
    }

    @TransformRule(
            name = CREATE_PARAMETER_OBJECT_ANNOTATION_FOR_STATIC_QUERY,
            description = "Create ParameterObject annotation for static query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = Annotation.class)
    @Guard(method = "hasParameters")
    public TransformFunction<QueryDeclaration, Annotation> createParameterObjectAnnotation() {
        return (source, ctx) -> {
            Annotation target = ctx.createTarget(Annotation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateParameterObjectAnnotationForStaticQuery");
            target.setName("ParameterObject");

            UnmappedTransferObjectType paramObj = ctx.equivalent(source, UnmappedTransferObjectType.class,
                    CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY);
            paramObj.getAnnotations().add(target);

            return target;
        };
    }

    @TransformRule(
            name = CREATE_ORIGINAL_NAME_ANNOTATION_DETAIL_FOR_STATIC_QUERY,
            description = "Create OriginalName annotation detail for static query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = AnnotationDetail.class)
    @Guard(method = "hasParameters")
    public TransformFunction<QueryDeclaration, AnnotationDetail> createOriginalNameAnnotationDetail() {
        return (source, ctx) -> {
            AnnotationDetail target = ctx.createTarget(AnnotationDetail.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateOriginalNameAnnotationDetailForStaticQuery");
            target.setName("Name");

            if (source.getEntity() == null) {
                target.setValue(source.getName());
            } else {
                target.setValue(source.getEntity().getName() + "::" + source.getName());
            }

            Annotation annotation = ctx.equivalent(source, Annotation.class,
                    CREATE_ORIGINAL_NAME_ANNOTATION_FOR_STATIC_QUERY);
            annotation.getDetails().add(target);

            return target;
        };
    }

    // ========================================================================================
    // Transfer attribute for query parameters
    // ========================================================================================

    @TransformRule(
            name = CREATE_TRANSFER_OBJECT_FOR_STATIC_QUERY_PARAMETER_DECLARATION,
            description = "Transform query parameter to TransferAttribute"
    )
    @Greedy
    @Transform(type = QueryParameterDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isQueryParameterInQuery")
    public TransformFunction<QueryParameterDeclaration, TransferAttribute> createTransferObjectForStaticQueryParameter() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTransferObjectForStaticQueryParameterDeclaration");
            target.setName(source.getName());

            if (source.getReferenceType() instanceof PrimitiveDeclaration) {
                Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
                target.setDataType(dataType);
            } else {
                // EntityType - use reflective EMF
                EntityType dataType = ctx.equivalent(source.getReferenceType(), EntityType.class, CREATE_ENTITY_TYPE);
                target.eSet(target.eClass().getEStructuralFeature("dataType"), dataType);
            }

            UnmappedTransferObjectType paramObj = ctx.equivalent(source.eContainer(),
                    UnmappedTransferObjectType.class, CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY);
            addTransferAttribute(paramObj, target);

            LOG.debug("Created TransferAttribute for QueryParameter: {}", source.getName());
            return target;
        };
    }

    // ========================================================================================
    // Static data for standalone query (entity == null, primitive type)
    // ========================================================================================

    @TransformRule(
            name = CREATE_STATIC_DATA_FOR_STATIC_QUERY,
            description = "Create static data for standalone primitive query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = StaticData.class)
    @Guard(method = "isStandalonePrimitiveQuery")
    public TransformFunction<QueryDeclaration, StaticData> createStaticDataForStaticQuery() {
        return (source, ctx) -> {
            StaticData target = ctx.createTarget(StaticData.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateStaticDataForStaticQuery");
            target.setName(source.getName());

            DataExpressionType getterExpr = ctx.equivalent(source, DataExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_DATA_TYPE);
            target.setGetterExpression(getterExpr);

            Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
            target.setDataType(dataType);

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticData for Query: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Data property for entity query (entity != null, primitive type)
    // ========================================================================================

    @TransformRule(
            name = CREATE_DATA_PROPERTY_FOR_STATIC_QUERY,
            description = "Create data property for entity primitive query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = DataProperty.class)
    @Guard(method = "isPrimitiveEntityQuery")
    public TransformFunction<QueryDeclaration, DataProperty> createDataPropertyForStaticQuery() {
        return (source, ctx) -> {
            DataProperty target = ctx.createTarget(DataProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDataPropertyForStaticQuery");
            target.setName(source.getName());

            DataExpressionType getterExpr = ctx.equivalent(source, DataExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_DATA_TYPE);
            target.setGetterExpression(getterExpr);

            Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
            target.setDataType(dataType);

            EntityType entityType = ctx.equivalent(source.getEntity(), EntityType.class, CREATE_ENTITY_TYPE);
            addDataProperty(entityType, target);

            LOG.debug("Created DataProperty for Query: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Static navigation for standalone query (entity == null, entity type)
    // ========================================================================================

    @TransformRule(
            name = CREATE_STATIC_NAVIGATION_FOR_STATIC_QUERY,
            description = "Create static navigation for standalone entity query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = StaticNavigation.class)
    @Guard(method = "isStandaloneEntityQuery")
    public TransformFunction<QueryDeclaration, StaticNavigation> createStaticNavigationForStaticQuery() {
        return (source, ctx) -> {
            StaticNavigation target = ctx.createTarget(StaticNavigation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateStaticNavigationForStaticQuery");
            target.setName(source.getName());

            ReferenceExpressionType getterExpr = ctx.equivalent(source, ReferenceExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_REFERENCE_TYPE);
            target.setGetterExpression(getterExpr);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateStaticNavigationForStaticQuery/Cardinality", 0, isMany(source) ? -1 : 1);
            target.setCardinality(cardinality);

            EntityType targetEntity = ctx.equivalent(source.getReferenceType(), EntityType.class, CREATE_ENTITY_TYPE);
            target.setTarget(targetEntity);

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created StaticNavigation for Query: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Navigation property for entity query (entity != null, entity type)
    // ========================================================================================

    @TransformRule(
            name = CREATE_NAVIGATION_PROPERTY_FOR_STATIC_QUERY,
            description = "Create navigation property for entity query"
    )
    @Greedy
    @Transform(type = QueryDeclaration.class)
    @To(type = NavigationProperty.class)
    @Guard(method = "isEntityEntityQuery")
    public TransformFunction<QueryDeclaration, NavigationProperty> createNavigationPropertyForStaticQuery() {
        return (source, ctx) -> {
            NavigationProperty target = ctx.createTarget(NavigationProperty.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateNavigationPropertyForStaticQuery");
            target.setName(source.getName());

            ReferenceExpressionType getterExpr = ctx.equivalent(source, ReferenceExpressionType.class,
                    CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_REFERENCE_TYPE);
            target.setGetterExpression(getterExpr);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateNavigationPropertyForStaticQuery/Cardinality", 0, isMany(source) ? -1 : 1);
            target.setCardinality(cardinality);

            EntityType targetEntity = ctx.equivalent(source.getReferenceType(), EntityType.class, CREATE_ENTITY_TYPE);
            target.setTarget(targetEntity);

            EntityType entityType = ctx.equivalent(source.getEntity(), EntityType.class, CREATE_ENTITY_TYPE);
            addNavigationProperty(entityType, target);

            LOG.debug("Created NavigationProperty for Query: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Getter expression for parameterized data query (lazy)
    // ========================================================================================

    @TransformRule(
            name = CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_DATA_TYPE,
            description = "Create getter expression for parameterized data query"
    )
    @Lazy
    @Transform(type = QueryDeclaration.class)
    @To(type = DataExpressionType.class)
    public TransformFunction<QueryDeclaration, DataExpressionType> createGetterExpressionForDataQuery() {
        return (source, ctx) -> {
            DataExpressionType target = ctx.createTarget(DataExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetterExpressionForStaticQueryParametrizedDataType");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(getJqlForStaticQuery(source, entityNamePrefix, entityNamePostfix));

            if (!source.getParameters().isEmpty()) {
                UnmappedTransferObjectType paramType = ctx.equivalent(source, UnmappedTransferObjectType.class,
                        CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY);
                target.setParameterType(paramType);
            }

            LOG.debug("Created DataExpressionType for StaticQuery: {}", source.getName());
            return target;
        };
    }

    // ========================================================================================
    // Getter expression for parameterized reference query (lazy)
    // ========================================================================================

    @TransformRule(
            name = CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_REFERENCE_TYPE,
            description = "Create getter expression for parameterized reference query"
    )
    @Lazy
    @Transform(type = QueryDeclaration.class)
    @To(type = ReferenceExpressionType.class)
    public TransformFunction<QueryDeclaration, ReferenceExpressionType> createGetterExpressionForReferenceQuery() {
        return (source, ctx) -> {
            ReferenceExpressionType target = ctx.createTarget(ReferenceExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetterExpressionForStaticQueryParametrizedReferenceType");

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix");
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix");
            target.setExpression(getJqlForStaticQuery(source, entityNamePrefix, entityNamePostfix));

            if (!source.getParameters().isEmpty()) {
                UnmappedTransferObjectType paramType = ctx.equivalent(source, UnmappedTransferObjectType.class,
                        CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY);
                target.setParameterType(paramType);
            }

            LOG.debug("Created ReferenceExpressionType for StaticQuery: {}", source.getName());
            return target;
        };
    }

    // --- Shared populate methods ---

    private void populateQueryTransferAttribute(QueryDeclaration source, TransferAttribute target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        DataProperty binding = ctx.equivalent(source, DataProperty.class, CREATE_DATA_PROPERTY_FOR_STATIC_QUERY);
        target.setBinding(binding);

        Primitive dataType = ctx.equivalent(source.getReferenceType(), Primitive.class);
        target.setDataType(dataType);
    }

    private void populateQueryRelation(QueryDeclaration source, TransferObjectRelation target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        MappedTransferObjectType targetTO = ctx.equivalent(source.getReferenceType(),
                MappedTransferObjectType.class, CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE);
        target.setTarget(targetTO);

        target.setEmbeddedCreate(false);
        target.setEmbeddedUpdate(false);
        target.setEmbeddedDelete(false);

        NavigationProperty binding = ctx.equivalent(source, NavigationProperty.class,
                CREATE_NAVIGATION_PROPERTY_FOR_STATIC_QUERY);
        target.setBinding(binding);
        target.setEmbedded(false);
    }

    // --- Guard methods ---

    public boolean isPrimitiveEntityQuery(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration q = (QueryDeclaration) eObject;
        return q.getReferenceType() instanceof PrimitiveDeclaration && q.getEntity() != null;
    }

    public boolean isEntityEntityQuery(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generate = ctx.getAttribute("generateDefaultTransferObject");
        if (generate == null || !generate) return false;
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration q = (QueryDeclaration) eObject;
        return q.getReferenceType() instanceof EntityDeclaration && q.getEntity() != null;
    }

    public boolean isEntityEntityQueryForClone(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration q = (QueryDeclaration) eObject;
        return q.getReferenceType() instanceof EntityDeclaration && q.getEntity() != null;
    }

    public boolean hasParameters(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryDeclaration)) return false;
        return !((QueryDeclaration) eObject).getParameters().isEmpty();
    }

    public boolean isStandalonePrimitiveQuery(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration q = (QueryDeclaration) eObject;
        return q.getReferenceType() instanceof PrimitiveDeclaration && q.getEntity() == null;
    }

    public boolean isStandaloneEntityQuery(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryDeclaration)) return false;
        QueryDeclaration q = (QueryDeclaration) eObject;
        return q.getReferenceType() instanceof EntityDeclaration && q.getEntity() == null;
    }

    public boolean isQueryParameterInQuery(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof QueryParameterDeclaration)) return false;
        return eObject.eContainer() instanceof QueryDeclaration;
    }

    // --- Expression helper ---

    private static String getJqlForStaticQuery(QueryDeclaration query, String entityNamePrefix, String entityNamePostfix) {
        return hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression.getJqlForStaticQuery(
                query,
                entityNamePrefix != null ? entityNamePrefix : "_",
                entityNamePostfix != null ? entityNamePostfix : "");
    }
}
