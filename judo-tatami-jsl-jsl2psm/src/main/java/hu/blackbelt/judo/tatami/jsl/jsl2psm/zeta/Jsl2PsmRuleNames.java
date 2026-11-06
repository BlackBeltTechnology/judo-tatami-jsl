package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta;

public final class Jsl2PsmRuleNames {

    private Jsl2PsmRuleNames() {}

    // === namespace.etl ===
    public static final String CREATE_ROOT_MODEL = "CreateRootModel";
    public static final String CREATE_MODEL_PACKAGES = "CreateModelPackages";

    // === type.etl ===
    public static final String CREATE_NUMERIC_TYPE = "CreateNumericType";
    public static final String CREATE_DATE_TYPE = "CreateDateType";
    public static final String CREATE_TIME_TYPE = "CreateTimeType";
    public static final String CREATE_TIMESTAMP_TYPE = "CreateTimestampType";
    public static final String CREATE_ENUMERATION_TYPE = "CreateEnumerationType";
    public static final String CREATE_ENUMERATION_MEMBER = "CreateEnumerationMember";
    public static final String CREATE_BOOLEAN_TYPE = "CreateBooleanType";
    public static final String CREATE_STRING_TYPE = "CreateStringType";
    public static final String CREATE_BINARY_TYPE = "CreateBinaryType";

    // === data/entityType.etl ===
    public static final String CREATE_ENTITY_TYPE = "CreateEntityType";

    // === data/association.etl ===
    public static final String CREATE_DEFAULT_VALUE_ANNOTATION_FOR_ENTITY_RELATION_DECLARATION = "CreateDefaultValueAnnotationForEntityRelationDeclaration";
    public static final String CREATE_NAMED_OPPOSITE_ASSOCIATION_END = "CreateNamedOppositeAssociationEnd";
    public static final String CREATE_DECLARED_ASSOCIATION_END = "CreateDeclaredAssociationEnd";

    // === data/cardinality.etl ===
    public static final String CREATE_CARDINALITY_FOR_RELATION_DECLARATION = "CreateCardinalityForRelationDeclaration";
    public static final String CREATE_CARDINALITY_FOR_FIELD_DECLARATION = "CreateCardinalityForFieldDeclaration";
    public static final String CREATE_CARDINALITY_FOR_OPPOSITE_ADDED_RELATION = "CreateCardinalityForOppositeAddedRelation";
    public static final String CREATE_CARDINALITY_FOR_DERIVED_DECLARATION = "CreateCardinalityForDerivedDeclaration";
    public static final String CREATE_CARDINALITY_FOR_STATIC_QUERY_DECLARATION = "CreateCardinalityForStaticQueryDeclaration";
    public static final String CREATE_CARDINALITY_FOR_TRANSFER_RELATION_DECLARATION = "CreateCardinalityForTransferRelationDeclaration";

    // === data/containment.etl ===
    public static final String CREATE_CONTAINMENT_FROM_FIELD = "CreateContainmentFromField";
    public static final String CREATE_READS_REFERENCE_EXPRESSION_TYPE_FOR_TRANSFER_RELATION_DECLARATION = "CreateReadsReferenceExpressionTypeForTransferRelationDeclaration";
    public static final String CREATE_READS_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION = "CreateReadsStaticNavigationForUnmappedTransferObjectTransferRelationDeclaration";
    public static final String CREATE_READS_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_RELATION_DECLARATION = "CreateReadsNavigationPropertyForMappedTransferObjectTransferRelationDeclaration";
    public static final String CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultReferenceExpressionTypeForMappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultNavigationPropertyForMappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultReferenceExpressionTypeForUnmappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultStaticNavigationForUnmappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_REFERENCE_EXPRESSION_TYPE_FOR_DEFAULT_TRANSFER_OBJECT = "CreateDefaultReferenceExpressionTypeForDefaultTransferObject";
    public static final String CREATE_DEFAULT_NAVIGATION_PROPERTY_FOR_DEFAULT_TRANSFER_OBJECT = "CreateDefaultNavigationPropertyForDefaultTransferObject";

    // === data/primitiveTypedElement.etl ===
    public static final String CREATE_DEFAULT_VALUE_ANNOTATION_FOR_PRIMITIVE_ENTITY_MEMBER = "CreateDefaultValueAnnotationForPrimitiveEntityMember";
    public static final String CREATE_PRIMITIVE_TYPED_ELEMENT = "CreatePrimitiveTypedElement";
    public static final String CREATE_ATTRIBUTE_FROM_FIELD = "CreateAttributeFromField";
    public static final String CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_PRIMITIVE_ENTITY_MEMBER = "CreateDefaultDataExpressionTypeForPrimitiveEntityMember";
    public static final String CREATE_DEFAULT_VALUE_FOR_PRIMITIVE_ENTITY_MEMBER = "CreateDefaultValueForPrimitiveEntityMember";
    public static final String CREATE_READS_DATA_EXPRESSION_TYPE_FOR_TRANSFER_FIELD_DECLARATION = "CreateReadsDataExpressionTypeForTransferFieldDeclaration";
    public static final String CREATE_READS_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION = "CreateReadsStaticDataForUnmappedTransferObjectTransferFieldDeclaration";
    public static final String CREATE_READS_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_TRANSFER_FIELD_DECLARATION = "CreateReadsDataPropertyForMappedTransferObjectTransferFieldDeclaration";
    public static final String CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultDataExpressionTypeForMappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_DATA_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultDataPropertyForMappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_DATA_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultDataExpressionTypeForUnmappedTransferObjectConstructor";
    public static final String CREATE_DEFAULT_STATIC_DATA_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateDefaultStaticDataForUnmappedTransferObjectConstructor";

    // === derived/dataProperty.etl ===
    public static final String CREATE_DATA_PROPERTY = "CreateDataProperty";

    // === derived/entityQuery.etl ===
    public static final String CREATE_QUERY_WITHOUT_PARAMETER_ANNOTATION_FOR_ENTITY_QUERY = "CreateQueryWithoutParameterAnnotationForEntityQuery";
    public static final String CREATE_DATA_PROPERTY_FOR_ENTITY_QUERY = "CreateDataPropertyForEntityQuery";
    public static final String CREATE_GETTER_EXPRESSION_FOR_ENTITY_QUERY = "CreateGetterExpressionForEntityQuery";

    // === derived/expressionType.etl ===
    public static final String CREATE_GETTER_EXPRESSION_FOR_DATA_TYPE = "CreateGetterExpressionForDataType";
    public static final String CREATE_GETTER_EXPRESSION_FOR_REFERENCE_TYPE = "CreateGetterExpressionForReferenceType";

    // === derived/navigationProperty.etl ===
    public static final String CREATE_NAVIGATION_PROPERTY = "CreateNavigationProperty";

    // === data/query.etl (static queries) ===
    public static final String ABSTRACT_CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_STATIC_QUERY = "AbstractCreateEntityQueryTransferAttributeForStaticQuery";
    public static final String CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_STATIC_QUERY = "CreateEntityQueryTransferAttributeForStaticQuery";
    public static final String CLONE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_STATIC_QUERY = "CloneEntityQueryTransferAttributeForStaticQuery";
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_ENTITY_QUERY_RELATION_FOR_STATIC_QUERY = "AbstractCreateTransferObjectEntityQueryRelationForStaticQuery";
    public static final String CREATE_TRANSFER_OBJECT_ENTITY_QUERY_RELATION_FOR_STATIC_QUERY = "CreateTransferObjectEntityQueryRelationForStaticQuery";
    public static final String CLONE_TRANSFER_OBJECT_QUERY_RELATION_FOR_STATIC_QUERY = "CloneTransferObjectQueryRelationForStaticQuery";
    public static final String CREATE_UNMAPPED_TRANSFER_OBJECT_FOR_STATIC_QUERY = "CreateUnmappedTransferObjectForStaticQuery";
    public static final String CREATE_ORIGINAL_NAME_ANNOTATION_FOR_STATIC_QUERY = "CreateOriginalNameAnnotationForStaticQuery";
    public static final String CREATE_PARAMETER_OBJECT_ANNOTATION_FOR_STATIC_QUERY = "CreateParameterObjectAnnotationForStaticQuery";
    public static final String CREATE_ORIGINAL_NAME_ANNOTATION_DETAIL_FOR_STATIC_QUERY = "CreateOriginalNameAnnotationDetailForStaticQuery";
    public static final String CREATE_TRANSFER_OBJECT_FOR_STATIC_QUERY_PARAMETER_DECLARATION = "CreateTransferObjectForStaticQueryParameterDeclaration";
    public static final String CREATE_STATIC_DATA_FOR_STATIC_QUERY = "CreateStaticDataForStaticQuery";
    public static final String CREATE_DATA_PROPERTY_FOR_STATIC_QUERY = "CreateDataPropertyForStaticQuery";
    public static final String CREATE_STATIC_NAVIGATION_FOR_STATIC_QUERY = "CreateStaticNavigationForStaticQuery";
    public static final String CREATE_NAVIGATION_PROPERTY_FOR_STATIC_QUERY = "CreateNavigationPropertyForStaticQuery";
    public static final String CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_DATA_TYPE = "CreateGetterExpressionForStaticQueryParametrizedDataType";
    public static final String CREATE_GETTER_EXPRESSION_FOR_STATIC_QUERY_PARAMETRIZED_REFERENCE_TYPE = "CreateGetterExpressionForStaticQueryParametrizedReferenceType";

    // === structure/entityDeclarationDefaultTransferObjectType.etl ===
    public static final String CREATE_ENTITY_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateEntityDefaultTransferObjectType";

    // === structure/entityDeclarationDefaultTransferAttribute.etl ===
    public static final String ABSTRACT_CREATE_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferAttributeForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferAttributeForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferAttributeForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_DERIVED_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateDerivedTransferAttributeForDefaultTransferObjectType";
    public static final String CREATE_DERIVED_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateDerivedTransferAttributeForDefaultTransferObjectType";
    public static final String CLONE_DERIVED_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneDerivedTransferAttributeForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateEntityQueryTransferAttributeForDefaultTransferObjectType";
    public static final String CREATE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateEntityQueryTransferAttributeForDefaultTransferObjectType";
    public static final String CLONE_ENTITY_QUERY_TRANSFER_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneEntityQueryTransferAttributeForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_TRANSFER_DEFAULT_VALUE_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferDefaultValueAttributeForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_DEFAULT_VALUE_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferDefaultValueAttributeForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_DEFAULT_VALUE_ATTRIBUTE_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferDefaultValueAttributeForDefaultTransferObjectType";

    // === structure/entityDeclarationDefaultTransferRelation.etl ===
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_FIELD_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferObjectRelationFromEntityFieldForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_FIELD_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferObjectRelationFromEntityFieldForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_FIELD_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferObjectRelationFromEntityFieldForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_OBJECT_ASSOCIATED_OPPOSITE_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferObjectAssociatedOppositeRelationForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_DERIVED_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateTransferObjectDerivedRelationForDefaultTransferObjectType";
    public static final String CREATE_TRANSFER_OBJECT_DERIVED_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateTransferObjectDerivedRelationForDefaultTransferObjectType";
    public static final String CLONE_TRANSFER_OBJECT_DERIVED_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneTransferObjectDerivedRelationForDefaultTransferObjectType";
    public static final String ABSTRACT_CREATE_DEFAULT_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "AbstractCreateDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";
    public static final String CREATE_DEFAULT_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CreateDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";
    public static final String CLONE_DEFAULT_TRANSFER_OBJECT_RELATION_FROM_ENTITY_RELATION_FOR_DEFAULT_TRANSFER_OBJECT_TYPE = "CloneDefaultTransferObjectRelationFromEntityRelationForDefaultTransferObjectType";

    // === structure/transferDeclarationTransferObjectType.etl ===
    public static final String CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE = "CreateUnmappedTransferObjectType";
    public static final String CREATE_MAPPED_TRANSFER_OBJECT_TYPE = "CreateMappedTransferObjectType";

    // === structure/transferDeclarationTransferAttribute.etl ===
    public static final String ABSTRACT_CREATE_TRANSFER_ATTRIBUTE_FOR_TRANSFER_FIELD_DECLARATION = "AbstractCreateTransferAttributeForTransferFieldDeclaration";
    public static final String CREATE_TRANSIENT_TRANSFER_ATTRIBUTE = "CreateTransientTransferAttribute";
    public static final String CREATE_DERIVED_TRANSFER_ATTRIBUTE = "CreateDerivedTransferAttribute";
    public static final String CREATE_MAPPED_TRANSFER_ATTRIBUTE = "CreateMappedTransferAttribute";
    public static final String CREATE_MAPPED_TRANSFER_ATTRIBUTE_ENTITY_DEFAULT = "CreateMappedTransferAttributeEntityDefault";
    public static final String CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateTransferEntityDefaultValueAttributeForMappedTransferObjectConstructor";
    public static final String CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateTransferEntityDefaultValueAttributeForUnmappedTransferObjectConstructor";
    public static final String CREATE_ANNOTATION_FOR_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateAnnotationForTransferEntityDefaultValueAttributeForMappedTransferObjectConstructor";
    public static final String CREATE_ANNOTATION_FOR_TRANSFER_ENTITY_DEFAULT_VALUE_ATTRIBUTE_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateAnnotationForTransferEntityDefaultValueAttributeForUnmappedTransferObjectConstructor";

    // === structure/transferDeclarationTransferRelation.etl ===
    public static final String ABSTRACT_CREATE_TRANSFER_OBJECT_RELATION_FOR_TRANSFER_RELATION_DECLARATION = "AbstractCreateTransferObjectRelationForTransferRelationDeclaration";
    public static final String CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_TRANSFER_RELATION_DECLARATION = "CreateTransientTransferObjectRelationForTransferRelationDeclaration";
    public static final String CREATE_DERIVED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION = "CreateDerivedTransferObjectEmbeddedRelationForTransferRelationDeclaration";
    public static final String CREATE_MAPPED_TRANSFER_OBJECT_EMBEDDED_RELATION_FOR_TRANSFER_RELATION_DECLARATION = "CreateMappedTransferObjectEmbeddedRelationForTransferRelationDeclaration";
    public static final String CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_RELATION_FOR_MAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateTransferEntityDefaultValueRelationForMappedTransferObjectConstructor";
    public static final String CREATE_TRANSFER_ENTITY_DEFAULT_VALUE_RELATION_FOR_UNMAPPED_TRANSFER_OBJECT_CONSTRUCTOR = "CreateTransferEntityDefaultValueRelationForUnmappedTransferObjectConstructor";
    public static final String CREATE_RELATION_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_OBJECT_RELATION = "CreateRelationRangeReferenceExpressionTypeForMappedTransferObjectRelation";
    public static final String CREATE_RELATION_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_OBJECT_RELATION = "CreateRelationRangeNavigationPropertyForMappedTransferObjectRelation";
    public static final String CREATE_RELATION_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION = "CreateRelationRangeReferenceExpressionTypeForUnmappedTransferObjectRelation";
    public static final String CREATE_RELATION_RANG_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_OBJECT_RELATION = "CreateRelationRangStaticNavigationForUnmappedTransferObjectRelation";
    public static final String CREATE_RELATION_RANGE_TRANSFER_OBJECT_RELATION = "CreateRelationRangeTransferObjectRelation";
    public static final String CREATE_CARDINALITY_FOR_GET_RELATION_RANGE_ENTITY_RELATION = "CreateCardinalityForGetRelationRangeEntityRelation";
    public static final String CREATE_CARDINALITY_FOR_GET_RELATION_RANGE_TRANSFER_OBJECT_RELATION = "CreateCardinalityForGetRelationRangeTransferObjectRelation";

    // === structure/transferDeclarationQueryCustomizer.etl ===
    public static final String CREATE_QUERY_CUSTOMIZER_TYPE = "CreateQueryCustomizerType";
    public static final String CREATE_QUERY_CUSTOMIZER_STRING_TYPE = "CreateQueryCustomizerStringType";
    public static final String CREATE_QUERY_CUSTOMIZER_MASK_ATTRIBUTE = "CreateQueryCustomizerMaskAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_IDENTIFIER_ATTRIBUTE = "CreateQueryCustomizerIdentifierAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_TYPE_ORDER_BY_RELATION = "CreateQueryCustomizerTypeOrderByRelation";
    public static final String CREATE_QUERY_CUSTOMIZER_TYPE_ORDER_BY_CARDINALITY = "CreateQueryCustomizerTypeOrderByCardinality";
    public static final String CREATE_QUERY_CUSTOMIZER_TYPE_SEEK_RELATION = "CreateQueryCustomizerTypeSeekRelation";
    public static final String CREATE_QUERY_CUSTOMIZER_TYPE_SEEK_CARDINALITY = "CreateQueryCustomizerTypeSeekCardinality";
    public static final String CREATE_QUERY_ORDERING_TYPE = "CreateQueryOrderingType";
    public static final String CREATE_QUERY_ORDERING_TYPE_ENUMERATION_ATTRIBUTE = "CreateQueryOrderingTypeEnumerationAttribute";
    public static final String CREATE_QUERY_ORDERING_TYPE_DESCENDING_ATTRIBUTE = "CreateQueryOrderingTypeDescendingAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION = "CreateQueryCustomizerOrderingEnumeration";
    public static final String CREATE_QUERY_CUSTOMIZER_ORDERING_ENUMERATION_MEMBER = "CreateQueryCustomizerOrderingEnumerationMember";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM = "CreateQueryCustomizerSeekLastItem";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEK_LAST_ITEM_ATTRIBUTE = "CreateQueryCustomizerSeekLastItemAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE = "CreateQueryCustomizerSeekingType";
    public static final String CREATE_QUERY_SEEK_LAST_ITEM_RELATION = "CreateQuerySeekLastItemRelation";
    public static final String CREATE_QUERY_SEEK_LAST_ITEM_RELATION_CARDINALITY = "CreateQuerySeekLastItemRelationCardinality";
    public static final String CREATE_QUERY_CUSTOMIZER_INTEGER_TYPE = "CreateQueryCustomizerIntegerType";
    public static final String CREATE_QUERY_CUSTOMIZER_BOOLEAN_TYPE = "CreateQueryCustomizerBooleanType";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_LIMIT_ATTRIBUTE = "CreateQueryCustomizerSeekingTypeLimitAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_OFFSET_ATTRIBUTE = "CreateQueryCustomizerSeekingTypeOffsetAttribute";
    public static final String CREATE_QUERY_CUSTOMIZER_SEEKING_TYPE_REVERSE_ATTRIBUTE = "CreateQueryCustomizerSeekingTypeReverseAttribute";

    // === action/action.etl ===
    public static final String ABSTRACT_TRANSFER_UNBOUND_ACTION_DECLARATION = "AbstractTransferUnboundActionDeclaration";
    public static final String ABSTRACT_TRANSFER_BOUND_ACTION_DECLARATION = "AbstractTransferBoundActionDeclaration";
    public static final String ABSTRACT_TRANSFER_ACTION_DECLARATION_RETURN_PARAMETER = "AbstractTransferActionDeclarationReturnParameter";
    public static final String ABSTRACT_TRANSFER_ACTION_DECLARATION_INPUT_PARAMETER = "AbstractTransferActionDeclarationInputParameter";
    public static final String CREATE_EMPTY_OPERATION_BODY = "CreateEmptyOperationBody";
    public static final String CREATE_FAULT_PARAMETER = "CreateFaultParameter";
    public static final String CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE = "CreateUnboundOperationForUnmappedTransferObjectType";
    public static final String CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER = "CreateUnboundOperationForUnmappedTransferObjectTypeInputParameter";
    public static final String CREATE_UNBOUND_OPERATION_FOR_UNMAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER = "CreateUnboundOperationForUnmappedTransferObjectTypeOutputParameter";
    public static final String CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE = "CreateUnboundOperationForMappedTransferObjectType";
    public static final String CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER = "CreateUnboundOperationForMappedTransferObjectTypeInputParameter";
    public static final String CREATE_UNBOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER = "CreateUnboundOperationForMappedTransferObjectTypeOutputParameter";
    public static final String CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE = "CreateBoundOperationForEntityType";
    public static final String CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateBoundOperationForEntityTypeInputParameter";
    public static final String CREATE_BOUND_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateBoundOperationForEntityTypeOutputParameter";
    public static final String CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE = "CreateBoundOperationForMappedTransferObjectType";
    public static final String CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_INPUT_PARAMETER = "CreateBoundOperationForMappedTransferObjectTypeInputParameter";
    public static final String CREATE_BOUND_OPERATION_FOR_MAPPED_TRANSFER_OBJECT_TYPE_OUTPUT_PARAMETER = "CreateBoundOperationForMappedTransferObjectTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_TRANSFER_ACTION_DECLARATION_INPUT = "CreateCardinalityForTransferActionDeclarationInput";
    public static final String CREATE_CARDINALITY_FOR_TRANSFER_ACTION_DECLARATION_OUTPUT = "CreateCardinalityForTransferActionDeclarationOutput";
    public static final String CREATE_CARDINALITY_FOR_TRANSFER_ACTION_DECLARATION_ENTITY_INPUT = "CreateCardinalityForTransferActionDeclarationEntityInput";
    public static final String CREATE_CARDINALITY_FOR_TRANSFER_ACTION_DECLARATION_ENTITY_OUTPUT = "CreateCardinalityForTransferActionDeclarationEntityOutput";
    public static final String CREATE_CARDINALITY_FOR_FAULT_PARAMETER = "CreateCardinalityForFaultParameter";
    public static final String CREATE_ACTION_INPUT_PARAMETER_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_MAPPED_TRANSFER_ACTION_DECLARATION = "CreateActionInputParameterRangeReferenceExpressionTypeForMappedTransferActionDeclaration";
    public static final String CREATE_ACTION_INPUT_PARAMETER_RANGE_NAVIGATION_PROPERTY_FOR_MAPPED_TRANSFER_ACTION_DECLARATION = "CreateActionInputParameterRangeNavigationPropertyForMappedTransferActionDeclaration";
    public static final String CREATE_ACTION_INPUT_PARAMETER_RANGE_REFERENCE_EXPRESSION_TYPE_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION = "CreateActionInputParameterRangeReferenceExpressionTypeForUnmappedTransferActionDeclaration";
    public static final String CREATE_ACTION_INPUT_PARAMETER_RANGE_STATIC_NAVIGATION_FOR_UNMAPPED_TRANSFER_ACTION_DECLARATION = "CreateActionInputParameterRangeStaticNavigationForUnmappedTransferActionDeclaration";
    public static final String CREATE_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION = "CreateActionInputParameterRangeTransferObjectRelation";
    public static final String CREATE_CARDINALITY_FOR_GET_ACTION_INPUT_PARAMETER_RANGE_ENTITY_RELATION = "CreateCardinalityForGetActionInputParameterRangeEntityRelation";
    public static final String CREATE_CARDINALITY_FOR_GET_ACTION_INPUT_PARAMETER_RANGE_TRANSFER_OBJECT_RELATION = "CreateCardinalityForGetActionInputParameterRangeTransferObjectRelation";

    // === action/deleteBehaviour.etl ===
    public static final String CREATE_DELETE_OPERATION_FOR_ENTITY_TYPE = "CreateDeleteOperationForEntityType";
    public static final String CREATE_DELETE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateDeleteBehaviourForTransferType";
    public static final String CREATE_DELETE_OPERATION_FOR_TRANSFER_TYPE = "CreateDeleteOperationForTransferType";

    // === action/updateBehaviour.etl ===
    public static final String CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE = "CreateUpdateOperationForEntityType";
    public static final String CREATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateUpdateBehaviourForTransferType";
    public static final String CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE = "CreateUpdateOperationForTransferType";
    public static final String CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateUpdateOperationForTransferTypeInputParameter";
    public static final String CREATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateUpdateOperationForTransferTypeOutputParameter";
    public static final String CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateUpdateOperationForEntityTypeInputParameter";
    public static final String CREATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateUpdateOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_UPDATE_TRANSFER_TYPE_INPUT = "CreateCardinalityForUpdateTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_UPDATE_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForUpdateTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_UPDATE_ENTITY_TYPE_INPUT = "CreateCardinalityForUpdateEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_UPDATE_ENTITY_TYPE_OUTPUT = "CreateCardinalityForUpdateEntityTypeOutput";

    // === action/validateUpdateBehaviour.etl ===
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE = "CreateValidateUpdateOperationForEntityType";
    public static final String CREATE_VALIDATE_UPDATE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateValidateUpdateBehaviourForTransferType";
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE = "CreateValidateUpdateOperationForTransferType";
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateValidateUpdateOperationForTransferTypeInputParameter";
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateValidateUpdateOperationForTransferTypeOutputParameter";
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateValidateUpdateOperationForEntityTypeInputParameter";
    public static final String CREATE_VALIDATE_UPDATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateValidateUpdateOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_UPDATE_TRANSFER_TYPE_INPUT = "CreateCardinalityForValidateUpdateTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_UPDATE_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForValidateUpdateTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_UPDATE_ENTITY_TYPE_INPUT = "CreateCardinalityForValidateUpdateEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_UPDATE_ENTITY_TYPE_OUTPUT = "CreateCardinalityForValidateUpdateEntityTypeOutput";

    // === action/refreshBehaviour.etl ===
    public static final String CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE = "CreateRefreshOperationForEntityType";
    public static final String CREATE_REFRESH_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateRefreshBehaviourForTransferType";
    public static final String CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE = "CreateRefreshOperationForTransferType";
    public static final String CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateRefreshOperationForTransferTypeInputParameter";
    public static final String CREATE_REFRESH_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateRefreshOperationForTransferTypeOutputParameter";
    public static final String CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateRefreshOperationForEntityTypeInputParameter";
    public static final String CREATE_REFRESH_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateRefreshOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_REFRESH_TRANSFER_TYPE_INPUT = "CreateCardinalityForRefreshTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_REFRESH_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForRefreshTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_REFRESH_ENTITY_TYPE_INPUT = "CreateCardinalityForRefreshEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_REFRESH_ENTITY_TYPE_OUTPUT = "CreateCardinalityForRefreshEntityTypeOutput";

    // === action/getTemplateBehaviour.etl ===
    public static final String CREATE_GET_TEMPLATE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateGetTemplateBehaviourForTransferType";
    public static final String CREATE_GET_TEMPLATE_OPERATION_FOR_TRANSFER_TYPE = "CreateGetTemplateOperationForTransferType";
    public static final String CREATE_GET_TEMPLATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateGetTemplateOperationForTransferTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_GET_TEMPLATE_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForGetTemplateTransferTypeOutput";

    // === action/getUploadTokenBehaviour.etl ===
    public static final String CREATE_UPLOAD_TOKEN_STRING_TYPE = "CreateUploadTokenStringType";
    public static final String CREATE_UPLOAD_TOKEN_TYPE = "CreateUploadTokenType";
    public static final String CREATE_UPLOAD_TOKEN_TYPE_TOKEN_ATTRIBUTE = "CreateUploadTokenTypeTokenAttribute";
    public static final String CREATE_GET_UPLOAD_TOKEN_BEHAVIOUR = "CreateGetUploadTokenBehaviour";
    public static final String CREATE_GET_UPLOAD_TOKEN_OPERATION = "CreateGetUploadTokenOperation";
    public static final String CREATE_GET_UPLOAD_TOKEN_OUTPUT_PARAMETER = "CreateGetUploadTokenOuptutParameter";
    public static final String CREATE_GET_UPLOAD_TOKEN_OUTPUT_PARAMETER_CARDINALITY = "CreateGetUploadTokenOuputParameterCardinality";

    // === action/relationListBehaviour.etl ===
    public static final String CREATE_LIST_OPERATION_FOR_ENTITY_TYPE = "CreateListOperationForEntityType";
    public static final String CREATE_LIST_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateListBehaviourForTransferType";
    public static final String CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE = "CreateListOperationForTransferType";
    public static final String CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateListOperationForTransferTypeInputParameter";
    public static final String CREATE_LIST_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateListOperationForTransferTypeOutputParameter";
    public static final String CREATE_LIST_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateListOperationForEntityTypeInputParameter";
    public static final String CREATE_LIST_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateListOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_LIST_TRANSFER_TYPE_INPUT = "CreateCardinalityForListTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_LIST_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForListTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_LIST_ENTITY_TYPE_INPUT = "CreateCardinalityForListEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_LIST_ENTITY_TYPE_OUTPUT = "CreateCardinalityForListEntityTypeOutput";

    // === action/relationCreateBehaviour.etl ===
    public static final String CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE = "CreateCreateOperationForEntityType";
    public static final String CREATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateCreateBehaviourForTransferType";
    public static final String CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE = "CreateCreateOperationForTransferType";
    public static final String CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateCreateOperationForTransferTypeInputParameter";
    public static final String CREATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateCreateOperationForTransferTypeOutputParameter";
    public static final String CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateCreateOperationForEntityTypeInputParameter";
    public static final String CREATE_CREATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateCreateOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_CREATE_TRANSFER_TYPE_INPUT = "CreateCardinalityForCreateTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_CREATE_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForCreateTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_CREATE_ENTITY_TYPE_INPUT = "CreateCardinalityForCreateEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_CREATE_ENTITY_TYPE_OUTPUT = "CreateCardinalityForCreateEntityTypeOutput";

    // === action/relationValidateCreateBehaviour.etl ===
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE = "CreateValidateCreateOperationForEntityType";
    public static final String CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateValidateCreateBehaviourForTransferType";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE = "CreateValidateCreateOperationForTransferType";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateValidateCreateOperationForTransferTypeInputParameter";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateValidateCreateOperationForTransferTypeOutputParameter";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateValidateCreateOperationForEntityTypeInputParameter";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ENTITY_TYPE_OUTPUT_PARAMETER = "CreateValidateCreateOperationForEntityTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_TRANSFER_TYPE_INPUT = "CreateCardinalityForValidateCreateTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForValidateCreateTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_ENTITY_TYPE_INPUT = "CreateCardinalityForValidateCreateEntityTypeInput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_ENTITY_TYPE_OUTPUT = "CreateCardinalityForValidateCreateEntityTypeOutput";

    // === action/relationAddReferenceBehaviour.etl ===
    public static final String CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE = "CreateAddReferenceOperationForEntityType";
    public static final String CREATE_ADD_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateAddReferenceBehaviourForTransferType";
    public static final String CREATE_ADD_REFERENCE_OPERATION_FOR_TRANSFER_TYPE = "CreateAddReferenceOperationForTransferType";
    public static final String CREATE_ADD_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateAddReferenceOperationForTransferTypeInputParameter";
    public static final String CREATE_ADD_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateAddReferenceOperationForEntityTypeInputParameter";
    public static final String CREATE_CARDINALITY_FOR_ADD_REFERENCE_TRANSFER_TYPE_INPUT = "CreateCardinalityForAddReferenceTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_ADD_REFERENCE_ENTITY_TYPE_INPUT = "CreateCardinalityForAddReferenceEntityTypeInput";

    // === action/relationRemoveReferenceBehaviour.etl ===
    public static final String CREATE_REMOVE_REFERENCE_OPERATION_FOR_ENTITY_TYPE = "CreateRemoveReferenceOperationForEntityType";
    public static final String CREATE_REMOVE_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateRemoveReferenceBehaviourForTransferType";
    public static final String CREATE_REMOVE_REFERENCE_OPERATION_FOR_TRANSFER_TYPE = "CreateRemoveReferenceOperationForTransferType";
    public static final String CREATE_REMOVE_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateRemoveReferenceOperationForTransferTypeInputParameter";
    public static final String CREATE_REMOVE_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateRemoveReferenceOperationForEntityTypeInputParameter";
    public static final String CREATE_CARDINALITY_FOR_REMOVE_REFERENCE_TRANSFER_TYPE_INPUT = "CreateCardinalityForRemoveReferenceTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_REMOVE_REFERENCE_ENTITY_TYPE_INPUT = "CreateCardinalityForRemoveReferenceEntityTypeInput";

    // === action/relationSetReferenceBehaviour.etl ===
    public static final String CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE = "CreateSetReferenceOperationForEntityType";
    public static final String CREATE_SET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateSetReferenceBehaviourForTransferType";
    public static final String CREATE_SET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE = "CreateSetReferenceOperationForTransferType";
    public static final String CREATE_SET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateSetReferenceOperationForTransferTypeInputParameter";
    public static final String CREATE_SET_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateSetReferenceOperationForEntityTypeInputParameter";
    public static final String CREATE_CARDINALITY_FOR_SET_REFERENCE_TRANSFER_TYPE_INPUT = "CreateCardinalityForSetReferenceTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_SET_REFERENCE_ENTITY_TYPE_INPUT = "CreateCardinalityForSetReferenceEntityTypeInput";

    // === action/relationUnsetReferenceBehaviour.etl ===
    public static final String CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE = "CreateUnsetReferenceOperationForEntityType";
    public static final String CREATE_UNSET_REFERENCE_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateUnsetReferenceBehaviourForTransferType";
    public static final String CREATE_UNSET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE = "CreateUnsetReferenceOperationForTransferType";
    public static final String CREATE_UNSET_REFERENCE_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateUnsetReferenceOperationForTransferTypeInputParameter";
    public static final String CREATE_UNSET_REFERENCE_OPERATION_FOR_ENTITY_TYPE_INPUT_PARAMETER = "CreateUnsetReferenceOperationForEntityTypeInputParameter";
    public static final String CREATE_CARDINALITY_FOR_UNSET_REFERENCE_TRANSFER_TYPE_INPUT = "CreateCardinalityForUnsetReferenceTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_UNSET_REFERENCE_ENTITY_TYPE_INPUT = "CreateCardinalityForUnsetReferenceEntityTypeInput";

    // === action/relationGetRangeReferenceBehaviour.etl ===
    public static final String CREATE_GET_RANGE_RELATION_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateGetRangeRelationBehaviourForTransferType";
    public static final String CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE = "CreateGetRangeRelationOperationForTransferType";
    public static final String CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateGetRangeRelationOperationForTransferTypeInputParameter";
    public static final String CREATE_GET_RANGE_RELATION_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateGetRangeRelationOperationForTransferTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT = "CreateCardinalityForGetRangeRelationTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForGetRangeRelationTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_OWNER = "CreateCardinalityForGetRangeRelationTransferTypeInputOwner";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_QUERY_CUSTOMIZER = "CreateCardinalityForGetRangeRelationTransferTypeInputQueryCustomizer";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_OWNER = "CreateTransferObjectRelationForGetRangeRelationTransferTypeInputOwner";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_RELATION_TRANSFER_TYPE_INPUT_QUERY_CUSTOMIZER = "CreateTransferObjectRelationForGetRangeRelationTransferTypeInputQueryCustomizer";
    public static final String CREATE_GET_RANGE_INPUT_TYPE = "CreateGetRangeInputType";

    // === action/getActionInputRangeBehaviour.etl ===
    public static final String CREATE_GET_RANGE_ACTION_INPUT_BEHAVIOUR_FOR_TRANSFER_TYPE = "CreateGetRangeActionInputBehaviourForTransferType";
    public static final String CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_MAPPED_TRANSFER_TYPE = "CreateGetRangeActionInputOperationForMappedTransferType";
    public static final String CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_UNMAPPED_TRANSFER_TYPE = "CreateGetRangeActionInputOperationForUnmappedTransferType";
    public static final String CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_TRANSFER_TYPE_INPUT_PARAMETER = "CreateGetRangeActionInputOperationForTransferTypeInputParameter";
    public static final String CREATE_GET_RANGE_ACTION_INPUT_OPERATION_FOR_TRANSFER_TYPE_OUTPUT_PARAMETER = "CreateGetRangeActionInputOperationForTransferTypeOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT = "CreateCardinalityForGetRangeActionInputTransferTypeInput";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_OUTPUT = "CreateCardinalityForGetRangeActionInputTransferTypeOutput";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_OWNER = "CreateCardinalityForGetRangeActionInputTransferTypeInputParameterOwner";
    public static final String CREATE_CARDINALITY_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_QUERY_CUSTOMIZER = "CreateCardinalityForGetRangeActionInputTransferTypeInputParameterQueryCustomizer";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_OWNER = "CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterOwner";
    public static final String CREATE_TRANSFER_OBJECT_RELATION_FOR_GET_RANGE_ACTION_INPUT_TRANSFER_TYPE_INPUT_PARAMETER_QUERY_CUSTOMIZER = "CreateTransferObjectRelationForGetRangeActionInputTransferTypeInputParameterQueryCustomizer";
    public static final String CREATE_GET_RANGE_INPUT_TYPE_FOR_INPUT_PARAMETER = "CreateGetRangeInputTypeForInputParameter";

    // === action/accessListBehaviour.etl ===
    public static final String CREATE_LIST_BEHAVIOUR_FOR_ACCESS = "CreateListBehaviourForAccess";
    public static final String CREATE_LIST_OPERATION_FOR_ACCESS = "CreateListOperationForAccess";
    public static final String CREATE_LIST_OPERATION_FOR_ACCESS_INPUT_PARAMETER = "CreateListOperationForAccessInputParameter";
    public static final String CREATE_LIST_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER = "CreateListOperationForAccessOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_LIST_ACCESS_INPUT = "CreateCardinalityForListAccessInput";
    public static final String CREATE_CARDINALITY_FOR_LIST_ACCESS_OUTPUT = "CreateCardinalityForListAccessOutput";

    // === action/accessCreateBehaviour.etl ===
    public static final String CREATE_CREATE_BEHAVIOUR_FOR_ACCESS = "CreateCreateBehaviourForAccess";
    public static final String CREATE_CREATE_OPERATION_FOR_ACCESS = "CreateCreateOperationForAccess";
    public static final String CREATE_CREATE_OPERATION_FOR_ACCESS_INPUT_PARAMETER = "CreateCreateOperationForAccessInputParameter";
    public static final String CREATE_CREATE_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER = "CreateCreateOperationForAccessOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_CREATE_ACCESS_INPUT = "CreateCardinalityForCreateAccessInput";
    public static final String CREATE_CARDINALITY_FOR_CREATE_ACCESS_OUTPUT = "CreateCardinalityForCreateAccessOutput";

    // === action/accessValidateCreateBehaviour.etl ===
    public static final String CREATE_VALIDATE_CREATE_BEHAVIOUR_FOR_ACCESS = "CreateValidateCreateBehaviourForAccess";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS = "CreateValidateCreateOperationForAccess";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS_INPUT_PARAMETER = "CreateValidateCreateOperationForAccessInputParameter";
    public static final String CREATE_VALIDATE_CREATE_OPERATION_FOR_ACCESS_OUTPUT_PARAMETER = "CreateValidateCreateOperationForAccessOutputParameter";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_ACCESS_INPUT = "CreateCardinalityForValidateCreateAccessInput";
    public static final String CREATE_CARDINALITY_FOR_VALIDATE_CREATE_ACCESS_OUTPUT = "CreateCardinalityForValidateCreateAccessOutput";

    // === actor/actorType.etl ===
    public static final String CREATE_ABSTRACT_ACTOR_TYPE = "CreateAbstractActorType";
    public static final String CREATE_ACTOR_TYPE = "CreateActorType";
    public static final String CREATE_MAPPED_ACTOR_TYPE = "CreateMappedActorType";
    public static final String CREATE_ACTOR_TYPE_WITHOUT_PRINCIPAL = "CreateActorTypeWithoutPrincipal";
    public static final String CREATE_FILTER_EXPRESSION_FOR_MAPPED_ACTOR_TYPE = "CreateFilterExpressionForMappedActorType";
    public static final String CREATE_METADATA_TYPE = "CreateMetadataType";
    public static final String CREATE_METADATA_TYPE_SECURITY = "CreateMetadataTypeSecurity";
    public static final String CREATE_METADATA_TYPE_SECURITY_CARDINALITY = "CreateMetadataTypeSecurityCardinality";
    public static final String CREATE_METADATA_SECURITY_TYPE = "CreateMetadataSecurityType";
    public static final String CREATE_ACTOR_STRING_TYPE = "CreateActorStringType";
    public static final String CREATE_METADATA_SECURITY_TYPE_NAME = "CreateMetadataSecurityTypeName";
    public static final String CREATE_METADATA_SECURITY_TYPE_OPEN_ID_CONFIGURATION_URL = "CreateMetadataSecurityTypeOpenIdConfigurationUrl";
    public static final String CREATE_METADATA_SECURITY_TYPE_ISSUER = "CreateMetadataSecurityTypeIssuer";
    public static final String CREATE_METADATA_SECURITY_TYPE_AUTH_ENDPOINT = "CreateMetadataSecurityTypeAuthEndpoint";
    public static final String CREATE_METADATA_SECURITY_TYPE_TOKEN_ENDPOINT = "CreateMetadataSecurityTypeTokenEndpoint";
    public static final String CREATE_METADATA_SECURITY_TYPE_LOGOUT_ENDPOINT = "CreateMetadataSecurityTypeLogoutEndpoint";
    public static final String CREATE_METADATA_SECURITY_TYPE_CLIENT_ID = "CreateMetadataSecurityTypeClientId";
    public static final String CREATE_METADATA_SECURITY_TYPE_CLIENT_BASE_URL = "CreateMetadataSecurityTypeClientBaseUrl";
    public static final String CREATE_METADATA_SECURITY_TYPE_DEFAULT_SCOPES = "CreateMetadataSecurityTypeDefaultScopes";
    public static final String CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE = "CreateGetMetadataOperationForActorType";
    public static final String CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR = "CreateGetMetadataOperationForActorTypeBehaviour";
    public static final String CREATE_GET_METADATA_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE = "CreateGetMetadataOperationOutputParameterForActorType";
    public static final String CREATE_METADATA_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE_CARDINALITY = "CreateMetadataOperationOutputParameterForActorTypeCardinality";
    public static final String CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE = "CreateGetPrincipalOperationForActorType";
    public static final String CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR = "CreateGetPrincipalOperationForActorTypeBehaviour";
    public static final String CREATE_GET_PRINCIPAL_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE = "CreateGetPrincipalOperationOutputParameterForActorType";
    public static final String CREATE_PRINCIPAL_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE_CARDINALITY = "CreatePrincipalOperationOutputParameterForActorTypeCardinality";
    public static final String CREATE_ACTOR_TYPE_CLAIM = "CreateActorTypeClaim";

    // === actor/access.etl ===
    public static final String CREATE_TRANSIENT_TRANSFER_OBJECT_RELATION_FOR_ACTOR_ACCESS_DECLARATION = "CreateTransientTransferObjectRelationForActorAccessDeclaration";
    public static final String CREATE_CARDINALITY_FOR_ACCESS_DECLARATION = "CreateCardinalityForAccessDeclaration";
}
