package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.TransferFieldDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.Parameter;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * GetUploadToken behaviour rules for JSL to PSM transformation.
 *
 * Ported from action/getUploadTokenBehaviour.etl (7 rules):
 *
 * Lazy singleton types (source: ModelDeclaration, ETL uses String "extensions"):
 * - CreateUploadTokenStringType (StringType)
 * - CreateUploadTokenType (UnmappedTransferObjectType)
 * - CreateUploadTokenTypeTokenAttribute (TransferAttribute)
 *
 * Greedy rules (source: TransferFieldDeclaration):
 * - CreateGetUploadTokenBehaviour (TransferOperationBehaviour)
 * - CreateGetUploadTokenOperation (UnboundOperation)
 * - CreateGetUploadTokenOuptutParameter (Parameter)
 * - CreateGetUploadTokenOuputParameterCardinality (Cardinality)
 *
 * Guard: generateBehaviours and s.isGetUploadTokenSupported()
 */
@TransformationContext(
        source = TransferFieldDeclaration.class,
        target = UnboundOperation.class
)
public class GetUploadTokenBehaviourRules {

    private static final Logger LOG = LoggerFactory.getLogger(GetUploadTokenBehaviourRules.class);

    // ===================================================================
    // Guard method
    // ===================================================================

    public boolean isGetUploadTokenSupportedGuard(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferFieldDeclaration)) {
            return false;
        }
        Boolean generateBehaviours = ctx.getAttribute("generateBehaviours");
        if (generateBehaviours == null || !generateBehaviours) {
            return false;
        }
        TransferFieldDeclaration source = (TransferFieldDeclaration) eObject;
        return isGetUploadTokenSupported(source);
    }

    // ===================================================================
    // Lazy singleton types (source: ModelDeclaration)
    // ETL uses String "extensions" as source; Zeta uses ModelDeclaration.
    // ===================================================================

    /**
     * CreateUploadTokenStringType (@lazy) - shared StringType for upload token.
     * ETL: transforms String to JUDOPSM!StringType
     * Zeta: transforms ModelDeclaration (same pattern as CreateActorStringType).
     */
    @TransformRule(
            name = CREATE_UPLOAD_TOKEN_STRING_TYPE,
            description = "Create shared StringType for upload token"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = StringType.class)
    public TransformFunction<ModelDeclaration, StringType> createUploadTokenStringType() {
        return (source, ctx) -> {
            StringType target = ctx.createTarget(StringType.class);
            String idPrefix = source.getName().replaceAll("::", "_");
            ctx.setElementId(target, "(esm/" + idPrefix + ")/CreateUploadTokenStringType");

            target.setName("UploadTokenStringType");
            target.setMaxLength(2048);

            // s.getUniqueModelName().equivalent("CreateModelPackages") -> extensions package
            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            LOG.debug("UploadTokenStringType type created: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateUploadTokenType (@lazy) - shared UnmappedTransferObjectType for upload token.
     * ETL: transforms String to JUDOPSM!UnmappedTransferObjectType
     */
    @TransformRule(
            name = CREATE_UPLOAD_TOKEN_TYPE,
            description = "Create shared UnmappedTransferObjectType for upload token"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    public TransformFunction<ModelDeclaration, UnmappedTransferObjectType> createUploadTokenType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            String idPrefix = source.getName().replaceAll("::", "_");
            ctx.setElementId(target, "(esm/" + idPrefix + ")/CreateUploadTokenType");

            target.setName("UploadToken");

            // s.getUniqueModelName().equivalent("CreateModelPackages") -> extensions package
            Package extensionsPackage = getExtensionsPackage(source, ctx);
            addElement(extensionsPackage, target);

            // t.attributes.add(s.equivalent("CreateUploadTokenTypeTokenAttribute"))
            TransferAttribute tokenAttr = ctx.equivalent(source, TransferAttribute.class,
                    CREATE_UPLOAD_TOKEN_TYPE_TOKEN_ATTRIBUTE);
            if (tokenAttr != null) {
                target.getAttributes().add(tokenAttr);
            }

            LOG.debug("CreateUploadTokenType type created: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateUploadTokenTypeTokenAttribute (@lazy) - "token" attribute on UploadToken type.
     * ETL: transforms String to JUDOPSM!TransferAttribute
     */
    @TransformRule(
            name = CREATE_UPLOAD_TOKEN_TYPE_TOKEN_ATTRIBUTE,
            description = "Create 'token' attribute for UploadToken type"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = TransferAttribute.class)
    public TransformFunction<ModelDeclaration, TransferAttribute> createUploadTokenTypeTokenAttribute() {
        return (source, ctx) -> {
            TransferAttribute target = ctx.createTarget(TransferAttribute.class);
            String idPrefix = source.getName().replaceAll("::", "_");
            ctx.setElementId(target, "(esm/" + idPrefix + ")/CreateUploadTokenTypeTokenAttribute");

            target.setName("token");
            target.setRequired(true);
            target.setDataType(ctx.equivalent(source, StringType.class,
                    CREATE_UPLOAD_TOKEN_STRING_TYPE));

            LOG.debug("Create CreateUploadTokenTypeTokenAttribute: {}", target.getName());
            return target;
        };
    }

    // ===================================================================
    // Greedy rules (source: TransferFieldDeclaration)
    // ===================================================================

    @TransformRule(
            name = CREATE_GET_UPLOAD_TOKEN_BEHAVIOUR,
            description = "Create TransferOperationBehaviour for getUploadToken"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = TransferOperationBehaviour.class)
    @Guard(method = "isGetUploadTokenSupportedGuard")
    public TransformFunction<TransferFieldDeclaration, TransferOperationBehaviour> createGetUploadTokenBehaviour() {
        return (source, ctx) -> {
            TransferOperationBehaviour target = ctx.createTarget(TransferOperationBehaviour.class);
            // Note: ETL uses the ID suffix "CreateGetUploadTokenOperation" (not "CreateGetUploadTokenBehaviour")
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetUploadTokenOperation");

            target.setBehaviourType(TransferOperationBehaviourType.GET_UPLOAD_TOKEN);
            target.setOwner(getTransferFieldDeclarationEquivalent(source, ctx));

            LOG.debug("Created CreateGetUploadTokenBehaviour: {}", source.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_GET_UPLOAD_TOKEN_OPERATION,
            description = "Create UnboundOperation for getUploadToken"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = UnboundOperation.class)
    @Guard(method = "isGetUploadTokenSupportedGuard")
    public TransformFunction<TransferFieldDeclaration, UnboundOperation> createGetUploadTokenOperation() {
        return (source, ctx) -> {
            UnboundOperation target = ctx.createTarget(UnboundOperation.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetUploadTokenOperation");

            target.setName("getUploadTokenFor" + firstToUpperCase(source.getName()));

            // If a member with the same name exists in the container, prefix with underscore
            TransferDeclaration container = (TransferDeclaration) source.eContainer();
            if (hasMemberWithName(container, target.getName())) {
                target.setName("_" + target.getName());
            }

            target.setBehaviour(ctx.equivalent(source, TransferOperationBehaviour.class,
                    CREATE_GET_UPLOAD_TOKEN_BEHAVIOUR));

            // t.output = s.equivalent("CreateGetUploadTokenOuptutParameter")
            Parameter outputParam = ctx.equivalent(source, Parameter.class,
                    CREATE_GET_UPLOAD_TOKEN_OUTPUT_PARAMETER);
            target.setOutput(outputParam);

            // s.eContainer.getTransferDeclarationEquivalent().operations.add(t)
            TransferObjectType transferObj = getTransferDeclarationEquivalent(container, ctx);
            addOperation(transferObj, target);

            LOG.debug("Created CreateGetUploadTokenOperation: {}", source.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_GET_UPLOAD_TOKEN_OUTPUT_PARAMETER,
            description = "Create output Parameter for getUploadToken operation"
    )
    @Greedy
    @Transform(type = TransferFieldDeclaration.class)
    @To(type = Parameter.class)
    @Guard(method = "isGetUploadTokenSupportedGuard")
    public TransformFunction<TransferFieldDeclaration, Parameter> createGetUploadTokenOutputParameter() {
        return (source, ctx) -> {
            Parameter target = ctx.createTarget(Parameter.class);
            // Note: ETL uses "CreateGetUploadTokenOuptutParameter" (with typo preserved)
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateGetUploadTokenOuptutParameter");

            target.setName("output");

            // t.type = "extensions".equivalent("CreateUploadTokenType")
            // In Zeta: use ModelDeclaration as source for the lazy singleton
            ModelDeclaration modelDecl = getModelDeclaration(source);
            target.setType(ctx.equivalent(modelDecl, UnmappedTransferObjectType.class,
                    CREATE_UPLOAD_TOKEN_TYPE));

            target.setWrapAsOptional(false);

            // t.cardinality = s.equivalent("CreateGetUploadTokenOuputParameterCardinality")
            target.setCardinality(createCardinality(ctx, "(jsl/" + getJslId(source) + ")/CreateGetUploadTokenOuputParameterCardinality", 1, 1));

            LOG.debug("Created CreateGetUploadTokenOuptutParameter: {}", source.getName());
            return target;
        };
    }

}
