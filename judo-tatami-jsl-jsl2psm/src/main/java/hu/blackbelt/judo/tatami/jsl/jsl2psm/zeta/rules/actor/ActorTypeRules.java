package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.actor;

import hu.blackbelt.judo.meta.jsl.jsldsl.ActorDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.ClaimModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.GuardModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.IdentityModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.RealmModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.psm.accesspoint.MappedActorType;
import hu.blackbelt.judo.meta.psm.derived.LogicalExpressionType;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.type.Primitive;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.JslExpressionToJqlExpression;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Actor type rules for JSL to PSM transformation.
 *
 * Ported from actor/actorType.etl (28 rules):
 * - CreateAbstractActorType (@abstract) - base for all actor types
 * - CreateActorType - principal defined, not mapped
 * - CreateMappedActorType - principal defined, mapped
 * - CreateActorTypeWithoutPrincipal - no principal
 * - CreateFilterExpressionForMappedActorType (@lazy) - guard filter expression
 * - CreateMetadataType - _MetadataFor* transfer object
 * - CreateMetadataTypeSecurity - security relation on metadata
 * - CreateMetadataTypeSecurityCardinality - cardinality for security relation
 * - CreateMetadataSecurityType - _MetadataSecurityFor* transfer object
 * - CreateActorStringType (@lazy) - shared StringType for actor attributes
 * - CreateMetadataSecurityType* (9 attributes: Name, OpenIdConfigurationUrl, Issuer, etc.)
 * - CreateGetMetadataOperationForActorType - _metadata operation
 * - CreateGetMetadataOperationForActorTypeBehaviour (@lazy)
 * - CreateGetMetadataOperationOutputParameterForActorType
 * - CreateMetadataOperationOutputParameterForActorTypeCardinality
 * - CreateGetPrincipalOperationForActorType (guarded: principal defined)
 * - CreateGetPrincipalOperationForActorTypeBehaviour (@lazy)
 * - CreateGetPrincipalOperationOutputParameterForActorType
 * - CreatePrincipalOperationOutputParameterForActorTypeCardinality
 * - CreateActorTypeClaim (guarded: principal + claim + identity defined)
 */
@TransformationContext(
        source = ActorDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType.class
)
public class ActorTypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(ActorTypeRules.class);

    // ========================
    // Actor Type Creation Rules
    // ========================

    /**
     * CreateActorType - actor with principal, not mapped.
     * ETL: guard: s.getPrincipal().isDefined() and not s.getPrincipal().map.isDefined()
     */
    @TransformRule(
            name = CREATE_ACTOR_TYPE,
            description = "Transform JSL ActorDeclaration to PSM ActorType (unmapped principal)"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class)
    public TransformFunction<ActorDeclaration, hu.blackbelt.judo.meta.psm.accesspoint.ActorType> createActorType() {
        return (source, ctx) -> {
            TransferDeclaration principal = getPrincipal(source);
            if (principal == null || principal.getMap() != null) {
                return null;
            }

            hu.blackbelt.judo.meta.psm.accesspoint.ActorType target =
                    ctx.createTarget(hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class);

            // Abstract rule body: CreateAbstractActorType
            populateAbstractActorType(source, target, ctx);

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateActorType");
            LOG.debug("Created ActorType: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateMappedActorType - actor with principal, mapped.
     * ETL: guard: s.getPrincipal().isDefined() and s.getPrincipal().map.isDefined()
     */
    @TransformRule(
            name = CREATE_MAPPED_ACTOR_TYPE,
            description = "Transform JSL ActorDeclaration to PSM MappedActorType"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = MappedActorType.class)
    public TransformFunction<ActorDeclaration, MappedActorType> createMappedActorType() {
        return (source, ctx) -> {
            TransferDeclaration principal = getPrincipal(source);
            if (principal == null || principal.getMap() == null) {
                return null;
            }

            MappedActorType target = ctx.createTarget(MappedActorType.class);

            // Abstract rule body: CreateAbstractActorType
            populateAbstractActorType(source, target, ctx);

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMappedActorType");

            // t.entityType = s.getPrincipal().map.entity.getEntityDeclarationEquivalent()
            target.setEntityType(
                    ctx.equivalent(principal.getMap().getEntity(),
                            hu.blackbelt.judo.meta.psm.data.EntityType.class));

            // t.kind = s.getClaim()?.value.value
            ClaimModifier claim = getClaim(source);
            if (claim != null && claim.getValue() != null) {
                target.setKind(claim.getValue().getValue());
            }

            // t.managed = true
            target.setManaged(true);

            // Filter expression for guard
            GuardModifier guard = getGuard(source);
            if (guard != null) {
                LogicalExpressionType filterExpr = ctx.equivalent(source,
                        LogicalExpressionType.class,
                        CREATE_FILTER_EXPRESSION_FOR_MAPPED_ACTOR_TYPE);
                target.setFilter(filterExpr);
            }

            LOG.debug("Created MappedActorType: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateActorTypeWithoutPrincipal - actor without principal.
     * ETL: guard: s.getPrincipal().isUndefined()
     */
    @TransformRule(
            name = CREATE_ACTOR_TYPE_WITHOUT_PRINCIPAL,
            description = "Transform JSL ActorDeclaration to PSM ActorType (no principal)"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class)
    public TransformFunction<ActorDeclaration, hu.blackbelt.judo.meta.psm.accesspoint.ActorType> createActorTypeWithoutPrincipal() {
        return (source, ctx) -> {
            TransferDeclaration principal = getPrincipal(source);
            if (principal != null) {
                return null;
            }

            hu.blackbelt.judo.meta.psm.accesspoint.ActorType target =
                    ctx.createTarget(hu.blackbelt.judo.meta.psm.accesspoint.ActorType.class);

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateActorTypeWithoutPrincipal");
            target.setName(source.getName());

            // Note: ETL sets t.filter for guard, but ActorType (UnmappedTransferObjectType)
            // doesn't have setFilter(). Only MappedTransferObjectType has it.
            // The ETL code may have been relying on dynamic dispatch. We skip it here.

            // Add to model root
            addToModelRoot(source, target, ctx);

            LOG.debug("Created ActorType (without principal): {}", target.getName());
            return target;
        };
    }

    /**
     * Shared logic from @abstract CreateAbstractActorType rule.
     */
    private void populateAbstractActorType(
            ActorDeclaration source,
            hu.blackbelt.judo.meta.psm.accesspoint.AbstractActorType target,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        target.setName(source.getName());

        TransferDeclaration principal = getPrincipal(source);
        if (principal != null) {
            // t.transferObjectType = s.getPrincipal().getTransferDeclarationEquivalent()
            hu.blackbelt.judo.meta.psm.service.TransferObjectType transferObj =
                    getTransferDeclarationEquivalent(principal, ctx);
            target.setTransferObjectType(transferObj);

            // s.getPrincipal().getTransferDeclarationEquivalent().actorType = t
            if (transferObj != null) {
                transferObj.setActorType(target);
            }
        }

        // t.realm = s.getRealm().value.value
        RealmModifier realm = getRealm(source);
        if (realm != null && realm.getValue() != null) {
            target.setRealm(realm.getValue().getValue());
        }

        // s.eContainer.getModelRoot().elements.add(t)
        addToModelRoot(source, target, ctx);
    }

    // ========================
    // Filter Expression Rule
    // ========================

    /**
     * CreateFilterExpressionForMappedActorType (@lazy)
     */
    @TransformRule(
            name = CREATE_FILTER_EXPRESSION_FOR_MAPPED_ACTOR_TYPE,
            description = "Create filter expression for mapped actor type guard"
    )
    @Lazy
    @Transform(type = ActorDeclaration.class)
    @To(type = LogicalExpressionType.class)
    public TransformFunction<ActorDeclaration, LogicalExpressionType> createFilterExpressionForMappedActorType() {
        return (source, ctx) -> {
            GuardModifier guard = getGuard(source);
            if (guard == null || guard.getExpression() == null) {
                return null;
            }

            String entityNamePrefix = ctx.getAttribute("entityNamePrefix") != null
                    ? ctx.getAttribute("entityNamePrefix") : "";
            String entityNamePostfix = ctx.getAttribute("entityNamePostfix") != null
                    ? ctx.getAttribute("entityNamePostfix") : "";

            LogicalExpressionType target = ctx.createTarget(LogicalExpressionType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateFilterExpressionForMappedActorType");

            target.setExpression(JslExpressionToJqlExpression.getJqlForExpression(
                    guard.getExpression(), entityNamePrefix, entityNamePostfix));

            LOG.debug("Created LogicalExpressionType for actor guard: {}", source.getName());
            return target;
        };
    }

    // ========================
    // Metadata Type Rules
    // ========================

    /**
     * CreateMetadataType - _MetadataFor* unmapped transfer object
     */
    @TransformRule(
            name = CREATE_METADATA_TYPE,
            description = "Create metadata transfer object type for actor"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    public TransformFunction<ActorDeclaration, UnmappedTransferObjectType> createMetadataType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            target.setName("_MetadataFor" + source.getName());
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMetadataType");
            addToModelRoot(source, target, ctx);
            return target;
        };
    }

    /**
     * CreateMetadataTypeSecurity - security relation on metadata type
     */
    @TransformRule(
            name = CREATE_METADATA_TYPE_SECURITY,
            description = "Create security relation on metadata type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferObjectRelation.class)
    public TransformFunction<ActorDeclaration, TransferObjectRelation> createMetadataTypeSecurity() {
        return (source, ctx) -> {
            TransferObjectRelation target = ctx.createTarget(TransferObjectRelation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMetadataTypeSecurity");

            target.setName("security");
            target.setEmbedded(true);
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(source) + ")/CreateMetadataTypeSecurityCardinality",
                    0, -1));
            target.setTarget(ctx.equivalent(source, UnmappedTransferObjectType.class,
                    CREATE_METADATA_SECURITY_TYPE));

            // Add to metadata type
            UnmappedTransferObjectType metadataType = ctx.equivalent(source,
                    UnmappedTransferObjectType.class, CREATE_METADATA_TYPE);
            addTransferRelation(metadataType, target);

            return target;
        };
    }

    /**
     * CreateMetadataSecurityType - _MetadataSecurityFor* unmapped transfer object
     */
    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE,
            description = "Create metadata security transfer object type for actor"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    public TransformFunction<ActorDeclaration, UnmappedTransferObjectType> createMetadataSecurityType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMetadataSecurityType");
            target.setName("_MetadataSecurityFor" + source.getName());
            addToModelRoot(source, target, ctx);
            return target;
        };
    }

    // ========================
    // Actor String Type (Lazy, shared singleton)
    // ========================

    /**
     * CreateActorStringType (@lazy) - shared StringType for actor metadata attributes.
     * ETL transforms String, we use ModelDeclaration (same pattern as QueryCustomizer types).
     */
    @TransformRule(
            name = CREATE_ACTOR_STRING_TYPE,
            description = "Create shared StringType for actor metadata attributes"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = StringType.class)
    public TransformFunction<ModelDeclaration, StringType> createActorStringType() {
        return (source, ctx) -> {
            StringType target = ctx.createTarget(StringType.class);
            String idPrefix = source.getName().replaceAll("::", "_");
            ctx.setElementId(target, "(jsl/" + idPrefix + ")/CreateActorStringType");
            target.setName("ActorStringType");
            target.setMaxLength(8192);

            // Add to extensions package (ETL: "extensions".getUniqueModelName().equivalent("CreateModelPackages"))
            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            LOG.debug("Created ActorStringType for model: {}", source.getName());
            return target;
        };
    }

    // ========================
    // Metadata Security Attributes
    // ========================

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_NAME,
            description = "Create 'name' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeName() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "name", true, CREATE_METADATA_SECURITY_TYPE_NAME);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_OPEN_ID_CONFIGURATION_URL,
            description = "Create 'openIdConfigurationUrl' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeOpenIdConfigurationUrl() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "openIdConfigurationUrl", false, CREATE_METADATA_SECURITY_TYPE_OPEN_ID_CONFIGURATION_URL);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_ISSUER,
            description = "Create 'issuer' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeIssuer() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "issuer", false, CREATE_METADATA_SECURITY_TYPE_ISSUER);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_AUTH_ENDPOINT,
            description = "Create 'authEndpoint' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeAuthEndpoint() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "authEndpoint", false, CREATE_METADATA_SECURITY_TYPE_AUTH_ENDPOINT);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_TOKEN_ENDPOINT,
            description = "Create 'tokenEndpoint' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeTokenEndpoint() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "tokenEndpoint", false, CREATE_METADATA_SECURITY_TYPE_TOKEN_ENDPOINT);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_LOGOUT_ENDPOINT,
            description = "Create 'logoutEndpoint' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeLogoutEndpoint() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "logoutEndpoint", false, CREATE_METADATA_SECURITY_TYPE_LOGOUT_ENDPOINT);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_CLIENT_ID,
            description = "Create 'clientId' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeClientId() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "clientId", false, CREATE_METADATA_SECURITY_TYPE_CLIENT_ID);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_CLIENT_BASE_URL,
            description = "Create 'clientBaseUrl' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeClientBaseUrl() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "clientBaseUrl", false, CREATE_METADATA_SECURITY_TYPE_CLIENT_BASE_URL);
    }

    @TransformRule(
            name = CREATE_METADATA_SECURITY_TYPE_DEFAULT_SCOPES,
            description = "Create 'defaultScopes' attribute on metadata security type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createMetadataSecurityTypeDefaultScopes() {
        return (source, ctx) -> createSecurityAttribute(source, ctx,
                "defaultScopes", false, CREATE_METADATA_SECURITY_TYPE_DEFAULT_SCOPES);
    }

    /**
     * Helper to create security metadata attributes.
     * All use "extensions".equivalent("CreateActorStringType") as dataType.
     */
    private TransferAttribute createSecurityAttribute(
            ActorDeclaration source,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx,
            String attrName, boolean required, String ruleNameConstant) {
        TransferAttribute target = ctx.createTarget(TransferAttribute.class);
        ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/" + ruleNameConstant);

        target.setName(attrName);
        target.setRequired(required);

        // t.dataType = "extensions".equivalent("CreateActorStringType")
        // Using ModelDeclaration source pattern (same as QueryCustomizer types)
        StringType actorStringType = ctx.equivalent(
                getModelDeclaration(source), StringType.class, CREATE_ACTOR_STRING_TYPE);
        target.setDataType(actorStringType);

        // Add to security type
        UnmappedTransferObjectType securityType = ctx.equivalent(source,
                UnmappedTransferObjectType.class, CREATE_METADATA_SECURITY_TYPE);
        addTransferAttribute(securityType, target);

        return target;
    }

    // ========================
    // Get Metadata Operation
    // ========================

    /**
     * CreateGetMetadataOperationForActorType
     */
    @TransformRule(
            name = CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE,
            description = "Create _metadata unbound operation for actor type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = UnboundOperation.class)
    public TransformFunction<ActorDeclaration, UnboundOperation> createGetMetadataOperationForActorType() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetMetadataOperationForActorType");

            target.setName("_metadata");
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR));

            addOperation(getActorDeclarationEquivalent(source, ctx), target);

            return target;
        };
    }

    /**
     * CreateGetMetadataOperationForActorTypeBehaviour (@lazy)
     */
    @TransformRule(
            name = CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR,
            description = "Create behaviour for _metadata operation"
    )
    @Lazy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    public TransformFunction<ActorDeclaration, TransferOperationBehaviour> createGetMetadataOperationForActorTypeBehaviour() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetMetadataOperationForActorTypeBehaviour");
            target.setBehaviourType(TransferOperationBehaviourType.GET_METADATA);
            target.setOwner(getActorDeclarationEquivalent(source, ctx));
            return target;
        };
    }

    /**
     * CreateGetMetadataOperationOutputParameterForActorType
     */
    @TransformRule(
            name = CREATE_GET_METADATA_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE,
            description = "Create output parameter for _metadata operation"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = Parameter.class)
    public TransformFunction<ActorDeclaration, Parameter> createGetMetadataOperationOutputParameterForActorType() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetMetadataOperationOutputParameterForActorType");

            target.setName("output");
            target.setType(ctx.equivalent(source, UnmappedTransferObjectType.class, CREATE_METADATA_TYPE));
            target.setWrapAsOptional(false);
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(source) + ")/CreateMetadataOperationOutputParameterForActorTypeCardinality",
                    0, -1));

            // Set as output of metadata operation
            UnboundOperation metadataOp = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_GET_METADATA_OPERATION_FOR_ACTOR_TYPE);
            metadataOp.setOutput(target);

            return target;
        };
    }

    // ========================
    // Get Principal Operation (guarded: principal defined)
    // ========================

    /**
     * CreateGetPrincipalOperationForActorType
     * ETL guard: s.getPrincipal().isDefined()
     */
    @TransformRule(
            name = CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE,
            description = "Create _principal unbound operation for actor type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = UnboundOperation.class)
    public TransformFunction<ActorDeclaration, UnboundOperation> createGetPrincipalOperationForActorType() {
        return (source, ctx) -> {
            if (getPrincipal(source) == null) {
                return null;
            }

            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetPrincipalOperationForActorType");

            target.setName("_principal");
            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR));

            addOperation(getActorDeclarationEquivalent(source, ctx), target);

            return target;
        };
    }

    /**
     * CreateGetPrincipalOperationForActorTypeBehaviour (@lazy)
     * ETL guard: s.getPrincipal().isDefined()
     */
    @TransformRule(
            name = CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE_BEHAVIOUR,
            description = "Create behaviour for _principal operation"
    )
    @Lazy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    public TransformFunction<ActorDeclaration, TransferOperationBehaviour> createGetPrincipalOperationForActorTypeBehaviour() {
        return (source, ctx) -> {
            if (getPrincipal(source) == null) {
                return null;
            }

            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetPrincipalOperationForActorTypeBehaviour");
            target.setBehaviourType(TransferOperationBehaviourType.GET_PRINCIPAL);
            target.setOwner(getActorDeclarationEquivalent(source, ctx));
            return target;
        };
    }

    /**
     * CreateGetPrincipalOperationOutputParameterForActorType
     * ETL guard: s.getPrincipal().isDefined()
     */
    @TransformRule(
            name = CREATE_GET_PRINCIPAL_OPERATION_OUTPUT_PARAMETER_FOR_ACTOR_TYPE,
            description = "Create output parameter for _principal operation"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = Parameter.class)
    public TransformFunction<ActorDeclaration, Parameter> createGetPrincipalOperationOutputParameterForActorType() {
        return (source, ctx) -> {
            TransferDeclaration principal = getPrincipal(source);
            if (principal == null) {
                return null;
            }

            Parameter target = ctx.createTarget(Parameter.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetPrincipalOperationOutputParameterForActorType");

            target.setName("output");
            // t.type = s.getPrincipal().getTransferDeclarationEquivalent()
            target.setType(getTransferDeclarationEquivalent(principal, ctx));
            target.setWrapAsOptional(false);
            target.setCardinality(createCardinality(ctx,
                    "(jsl/" + getJslId(source) + ")/CreatePrincipalOperationOutputParameterForActorTypeCardinality",
                    0, 1));

            // Set as output of principal operation
            UnboundOperation principalOp = ctx.equivalent(source, UnboundOperation.class,
                    CREATE_GET_PRINCIPAL_OPERATION_FOR_ACTOR_TYPE);
            principalOp.setOutput(target);

            return target;
        };
    }

    // ========================
    // Actor Type Claim
    // ========================

    /**
     * CreateActorTypeClaim - creates transfer attribute for the claim on actor type.
     * ETL guard: s.getPrincipal().isDefined() and s.getClaim().isDefined() and s.getIdentity().isDefined()
     */
    @TransformRule(
            name = CREATE_ACTOR_TYPE_CLAIM,
            description = "Create claim transfer attribute on actor type"
    )
    @Greedy
    @Transform(type = ActorDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ActorDeclaration, TransferAttribute> createActorTypeClaim() {
        return (source, ctx) -> {
            TransferDeclaration principal = getPrincipal(source);
            ClaimModifier claim = getClaim(source);
            IdentityModifier identity = getIdentity(source);

            if (principal == null || claim == null || identity == null) {
                return null;
            }

            TransferFieldDeclaration field = identity.getField();
            if (field == null) return null;

            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateActorTypeClaim");

            target.setName(field.getName());
            target.setClaimType(claim.getValue().getValue());

            // t.dataType = field.referenceType.getPrimitiveDeclarationEquivalent()
            Primitive dataType = ctx.equivalent(field.getReferenceType(), Primitive.class);
            target.setDataType(dataType);

            // t.required = field.getTransferFieldDeclarationEquivalent().required
            TransferAttribute fieldEquiv = getTransferFieldDeclarationEquivalent(field, ctx);
            if (fieldEquiv != null) {
                target.setRequired(fieldEquiv.isRequired());
            }

            // t.binding = field.getterExpr.features.first().member.equivalent("CreateAttributeFromField")
            EObject getterMember = getGetterExprFirstMember(field);
            if (getterMember != null) {
                Attribute binding = ctx.equivalent(getterMember, Attribute.class,
                        CREATE_ATTRIBUTE_FROM_FIELD);
                target.setBinding(binding);
            }

            // Add to actor declaration equivalent
            addTransferAttribute(getActorDeclarationEquivalent(source, ctx), target);

            LOG.debug("Created ActorTypeClaim: {}", target.getName());
            return target;
        };
    }

    // ========================
    // Utility methods
    // ========================

    /**
     * Add element to the model root package.
     * Mirrors: s.eContainer.getModelRoot().elements.add(t)
     */
    private void addToModelRoot(ActorDeclaration source,
                                hu.blackbelt.judo.meta.psm.namespace.NamespaceElement target,
                                hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        Package modelPackage = getModelRoot(source, ctx);
        addElement(modelPackage, target);
    }

    /**
     * Get the PSM TransferAttribute equivalent of a TransferFieldDeclaration.
     * Ported from transferFieldDeclaration.eol: getTransferFieldDeclarationEquivalent()
     */
    private TransferAttribute getTransferFieldDeclarationEquivalent(
            TransferFieldDeclaration field,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!isMaps(field) && !isReads(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    "CreateTransientTransferAttribute");
        }
        if (isReads(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    "CreateDerivedTransferAttribute");
        }
        if (isMaps(field)) {
            return ctx.equivalent(field, TransferAttribute.class,
                    "CreateMappedTransferAttribute");
        }
        return null;
    }
}
