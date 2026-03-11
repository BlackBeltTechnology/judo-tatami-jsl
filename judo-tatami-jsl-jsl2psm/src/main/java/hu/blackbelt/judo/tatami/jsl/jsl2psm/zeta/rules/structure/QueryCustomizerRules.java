package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.BooleanType;
import hu.blackbelt.judo.meta.psm.type.Cardinality;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Query customizer rules for transfer declarations.
 *
 * Ported from structure/transferDeclarationQueryCustomizer.etl
 * All rules are guarded by generateBehaviours context attribute.
 */
@TransformationContext(
        source = TransferDeclaration.class,
        target = UnmappedTransferObjectType.class
)
public class QueryCustomizerRules {

    private static final Logger LOG = LoggerFactory.getLogger(QueryCustomizerRules.class);

    // ========================================================================================
    // Query customizer type
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_TYPE,
            description = "Create query customizer type for mapped transfer declaration"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, UnmappedTransferObjectType> createQueryCustomizerType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerType");
            target.setName("_" + source.getName() + "QueryCustomizer");
            target.setQueryCustomizer(true);

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Query customizer type created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query customizer string type (lazy, transforms String -> StringType)
    // ========================================================================================

    /**
     * Creates the QueryCustomizer string type singleton per model.
     * ETL equivalent transforms String "extensions" to StringType.
     * In Zeta, we use ModelDeclaration as source (EObject requirement).
     */
    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_STRING_TYPE,
            description = "Create query customizer string type"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = StringType.class)
    public TransformFunction<ModelDeclaration, StringType> createQueryCustomizerStringType() {
        return (source, ctx) -> {
            StringType target = ctx.createTarget(StringType.class);
            String modelName = source.getName();
            ctx.setElementId(target, "(jsl/" + modelName.replaceAll("::", "_") + ")/CreateQueryCustomizerStringType");
            target.setName("QueryCustomizerStringType");
            target.setMaxLength(8192);

            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            LOG.debug("CreateQueryCustomizerStringType type created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Mask attribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_MASK_ATTRIBUTE,
            description = "Create mask attribute for query customizer"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferAttribute> createQueryCustomizerMaskAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerMaskAttribute");
            target.setName("_mask");
            target.setRequired(false);

            StringType dataType = ctx.equivalent(getModelDeclaration(source), StringType.class, CREATE_QUERY_CUSTOMIZER_STRING_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType qcType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE);
            addTransferAttribute(qcType, target);

            LOG.debug("CreateQueryCustomizerMaskAttribute created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Identifier attribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_IDENTIFIER_ATTRIBUTE,
            description = "Create identifier attribute for query customizer"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferAttribute> createQueryCustomizerIdentifierAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerIdentifierAttribute");
            target.setName("_identifier");
            target.setRequired(false);

            StringType dataType = ctx.equivalent(getModelDeclaration(source), StringType.class, CREATE_QUERY_CUSTOMIZER_STRING_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType qcType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE);
            addTransferAttribute(qcType, target);

            LOG.debug("CreateQueryCustomizerIdentifierAttribute created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Order by relation
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_TYPE_ORDER_BY_RELATION,
            description = "Create order by relation for query customizer"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isMappedWithBehavioursAndSortable")
    public TransformFunction<TransferDeclaration, TransferObjectRelation> createQueryCustomizerOrderByRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerTypeOrderByRelation");
            target.setName("_orderBy");

            UnmappedTransferObjectType orderingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_ORDERING_TYPE);
            target.setTarget(orderingType);
            target.setEmbedded(true);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerTypeOrderByCardinality", 0, -1);
            target.setCardinality(cardinality);

            UnmappedTransferObjectType qcType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE);
            addTransferRelation(qcType, target);

            return target;
        };
    }

    // ========================================================================================
    // Seek relation
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_TYPE_SEEK_RELATION,
            description = "Create seek relation for query customizer"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferObjectRelation> createQueryCustomizerSeekRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerTypeSeekRelation");
            target.setName("_seek");

            UnmappedTransferObjectType seekingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE);
            target.setTarget(seekingType);
            target.setEmbedded(true);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerTypeSeekCardinality", 0, 1);
            target.setCardinality(cardinality);

            UnmappedTransferObjectType qcType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_TYPE);
            addTransferRelation(qcType, target);

            LOG.debug("CreateQueryCustomizerTypeSeekRelation created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query ordering type
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_ORDERING_TYPE,
            description = "Create ordering type for transfer declaration"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "isMappedWithBehavioursAndSortable")
    public TransformFunction<TransferDeclaration, UnmappedTransferObjectType> createQueryOrderingType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryOrderingType");
            target.setName("_" + source.getName() + "OrderingType");
            target.setQueryCustomizer(true);

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("CreateQueryOrderingType created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query ordering type enumeration attribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_ORDERING_TYPE_ENUMERATION_ATTRIBUTE,
            description = "Create enumeration attribute for ordering type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehavioursAndSortable")
    public TransformFunction<TransferDeclaration, TransferAttribute> createQueryOrderingTypeEnumerationAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryOrderingTypeEnumerationAttribute/Attribute");
            target.setName("attribute");
            target.setRequired(true);

            EnumerationType enumType = ctx.equivalent(source, EnumerationType.class, CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION);
            target.eSet(target.eClass().getEStructuralFeature("dataType"), enumType);

            UnmappedTransferObjectType orderingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_ORDERING_TYPE);
            addTransferAttribute(orderingType, target);

            LOG.debug("CreateQueryOrderingTypeEnumerationAttribute: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query ordering type descending attribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_ORDERING_TYPE_DESCENDING_ATTRIBUTE,
            description = "Create descending attribute for ordering type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehavioursAndSortable")
    public TransformFunction<TransferDeclaration, TransferAttribute> createQueryOrderingTypeDescendingAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryOrderingTypeDescendingAttribute");
            target.setName("descending");
            target.setRequired(false);

            BooleanType dataType = ctx.equivalent(getModelDeclaration(source), BooleanType.class, CREATE_QUERY_CUSTOMIZER_BOOLEAN_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType orderingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_ORDERING_TYPE);
            addTransferAttribute(orderingType, target);

            LOG.debug("CreateQueryOrderingTypeDescendingAttribute: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query customizer ordering enumeration
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION,
            description = "Create ordering enumeration for transfer declaration"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = EnumerationType.class)
    @Guard(method = "isMappedWithBehavioursAndSortable")
    public TransformFunction<TransferDeclaration, EnumerationType> createQueryCustomizerOrderingEnumeration() {
        return (source, ctx) -> {
            EnumerationType target = ctx.createTarget(EnumerationType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerOrderingEnumeration");
            target.setName("_" + source.getName() + "OrderingAttributes");

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("CreateQueryCustomizerOrderingEnumeration created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Query customizer ordering enumeration member
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION_MEMBER,
            description = "Create ordering enumeration member for sortable field"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = EnumerationMember.class)
    @Guard(method = "isSortableFieldWithBehaviours")
    public TransformFunction<TransferFieldDeclaration, EnumerationMember> createQueryCustomizerOrderingEnumerationMember() {
        return (source, ctx) -> {
            EnumerationMember target = ctx.createTarget(EnumerationMember.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerOrderingEnumerationDerivedTransferAttribute");
            target.setName(source.getName());
            target.setOrdinal(-1); // Will be set in post-processing

            EnumerationType enumType = ctx.equivalent(source.eContainer(), EnumerationType.class, CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION);
            addEnumerationMember(enumType, target);

            LOG.debug("CreateQueryCustomizerOrderingEnumerationMember: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Seek last item type
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM,
            description = "Create seek last item type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, UnmappedTransferObjectType> createQueryCustomizerSeekLastItem() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekLastItem");
            target.setName("_" + source.getName() + "SeekLastItem");

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("CreateQueryCustomizerSeekLastItem created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Seek last item attribute
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM_ATTRIBUTE,
            description = "Create seek last item attribute"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isFilterableOrSortableFieldWithBehaviours")
    public TransformFunction<TransferFieldDeclaration, TransferAttribute> createQueryCustomizerSeekLastItemAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekLastItemAttribute");
            target.setName(source.getName());
            target.setRequired(false);

            hu.blackbelt.judo.meta.psm.type.Primitive dataType = ctx.equivalent(source.getReferenceType(),
                    hu.blackbelt.judo.meta.psm.type.Primitive.class);
            target.setDataType(dataType);

            UnmappedTransferObjectType seekLastItem = ctx.equivalent(source.eContainer(),
                    UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM);
            addTransferAttribute(seekLastItem, target);

            LOG.debug("CreateQueryCustomizerSeekLastItemAttribute: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Seeking type
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE,
            description = "Create seeking type for query customizer"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, UnmappedTransferObjectType> createQueryCustomizerSeekingType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekingType");
            target.setName("_" + source.getName() + "Seek");
            target.setQueryCustomizer(true);

            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Query seeking type created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Seek last item relation
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_SEEK_LAST_ITEM_RELATION,
            description = "Create last item relation for seeking type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferObjectRelation.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferObjectRelation> createQuerySeekLastItemRelation() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQuerySeekLastItemRelation");
            target.setName("lastItem");

            UnmappedTransferObjectType seekLastItem = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM);
            target.setTarget(seekLastItem);
            target.setEmbedded(true);

            Cardinality cardinality = createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateQuerySeekLastItemRelationCardinality", 0, 1);
            target.setCardinality(cardinality);

            UnmappedTransferObjectType seekingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE);
            addTransferRelation(seekingType, target);

            LOG.debug("CreateQuerySeekLastItemRelation created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Integer type (lazy, transforms String -> NumericType)
    // ========================================================================================

    /**
     * Creates the QueryCustomizer integer type singleton per model.
     * ETL equivalent transforms String "extensions" to NumericType.
     * In Zeta, we use ModelDeclaration as source (EObject requirement).
     */
    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_INTEGER_TYPE,
            description = "Create query customizer integer type"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = NumericType.class)
    public TransformFunction<ModelDeclaration, NumericType> createQueryCustomizerIntegerType() {
        return (source, ctx) -> {
            NumericType target = ctx.createTarget(NumericType.class);
            String modelName = source.getName();
            ctx.setElementId(target, "(jsl/" + modelName.replaceAll("::", "_") + ")/CreateQueryCustomizerLimitOffsetIntegerType");
            target.setName("QueryCustomizerLimitOffsetIntegerType");
            target.setPrecision(9);
            target.setScale(0);

            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            LOG.debug("CreateQueryCustomizerIntegerType created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Boolean type (lazy, transforms String -> BooleanType)
    // ========================================================================================

    /**
     * Creates the QueryCustomizer boolean type singleton per model.
     * ETL equivalent transforms String "extensions" to BooleanType.
     * In Zeta, we use ModelDeclaration as source (EObject requirement).
     */
    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_BOOLEAN_TYPE,
            description = "Create query customizer boolean type"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = BooleanType.class)
    public TransformFunction<ModelDeclaration, BooleanType> createQueryCustomizerBooleanType() {
        return (source, ctx) -> {
            BooleanType target = ctx.createTarget(BooleanType.class);
            String modelName = source.getName();
            ctx.setElementId(target, "(jsl/" + modelName.replaceAll("::", "_") + ")/CreateQueryCustomizerBooleanType");
            target.setName("QueryCustomizerBooleanType");

            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            LOG.debug("CreateQueryCustomizerBooleanType created: {}", target.getName());
            return target;
        };
    }

    // ========================================================================================
    // Seeking type attributes (limit, offset, reverse)
    // ========================================================================================

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_LIMIT_ATTRIBUTE,
            description = "Create limit attribute for seeking type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferAttribute> createSeekingTypeLimitAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekingTypeLimitAttribute");
            target.setName("limit");
            target.setRequired(false);

            NumericType dataType = ctx.equivalent(getModelDeclaration(source), NumericType.class, CREATE_QUERY_CUSTOMIZER_INTEGER_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType seekingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE);
            addTransferAttribute(seekingType, target);

            LOG.debug("CreateQueryCustomizerSeekingTypeLimitAttribute created: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_OFFSET_ATTRIBUTE,
            description = "Create offset attribute for seeking type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferAttribute> createSeekingTypeOffsetAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekingTypeOffsetAttribute");
            target.setName("offset");
            target.setRequired(false);

            NumericType dataType = ctx.equivalent(getModelDeclaration(source), NumericType.class, CREATE_QUERY_CUSTOMIZER_INTEGER_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType seekingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE);
            addTransferAttribute(seekingType, target);

            LOG.debug("CreateQueryCustomizerSeekingTypeOffsetAttribute created: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_REVERSE_ATTRIBUTE,
            description = "Create reverse attribute for seeking type"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = TransferAttribute.class)
    @Guard(method = "isMappedWithBehaviours")
    public TransformFunction<TransferDeclaration, TransferAttribute> createSeekingTypeReverseAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateQueryCustomizerSeekingTypeReverseAttribute");
            target.setName("reverse");
            target.setRequired(false);

            BooleanType dataType = ctx.equivalent(getModelDeclaration(source), BooleanType.class, CREATE_QUERY_CUSTOMIZER_BOOLEAN_TYPE);
            target.setDataType(dataType);

            UnmappedTransferObjectType seekingType = ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE);
            addTransferAttribute(seekingType, target);

            LOG.debug("CreateQueryCustomizerSeekingTypeReverseAttribute created: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isMappedWithBehaviours(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        if (!(eObject instanceof TransferDeclaration)) return false;
        TransferDeclaration td = (TransferDeclaration) eObject;
        return td.getMap() != null;
    }

    public boolean isMappedWithBehavioursAndSortable(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isMappedWithBehaviours(eObject, ctx)) return false;
        TransferDeclaration td = (TransferDeclaration) eObject;
        return hasSortableField(td);
    }

    public boolean isSortableFieldWithBehaviours(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!(field.eContainer() instanceof TransferDeclaration)) return false;
        TransferDeclaration td = (TransferDeclaration) field.eContainer();
        return td.getMap() != null && isSortable(field);
    }

    public boolean isFilterableOrSortableFieldWithBehaviours(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) return false;
        if (!(eObject instanceof TransferFieldDeclaration)) return false;
        TransferFieldDeclaration field = (TransferFieldDeclaration) eObject;
        if (!(field.eContainer() instanceof TransferDeclaration)) return false;
        TransferDeclaration td = (TransferDeclaration) field.eContainer();
        return td.getMap() != null && (isFilterable(field) || isSortable(field));
    }

    // --- Post-execution hook ---

    /**
     * Assigns sequential ordinals to EnumerationMembers with ordinal == -1.
     * Mirrors the ETL @post block in jslToPsm.etl.
     */
    @PostExecution
    public void setEnumerationOrdinals(hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        org.eclipse.emf.ecore.resource.ResourceSet psmResourceSet = ctx.getAttribute("__psmResourceSet");
        if (psmResourceSet == null) return;

        java.util.Set<EnumerationType> enumsToFix = new java.util.LinkedHashSet<>();
        var iterator = psmResourceSet.getAllContents();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next instanceof EnumerationMember) {
                EnumerationMember member = (EnumerationMember) next;
                if (member.getOrdinal() == -1 && member.eContainer() instanceof EnumerationType) {
                    enumsToFix.add((EnumerationType) member.eContainer());
                }
            }
        }
        for (EnumerationType enumType : enumsToFix) {
            int index = 0;
            for (EnumerationMember member : enumType.getMembers()) {
                member.setOrdinal(index++);
            }
            LOG.debug("@PostExecution: set ordinals for enumeration {}", enumType.getName());
        }
    }
}
