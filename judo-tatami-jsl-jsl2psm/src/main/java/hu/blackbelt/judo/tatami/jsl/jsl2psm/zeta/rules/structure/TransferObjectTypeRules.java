package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure;

import hu.blackbelt.judo.meta.jsl.jsldsl.TransferDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Transfer object type rules for JSL to PSM transformation.
 *
 * Ported from structure/transferDeclarationTransferObjectType.etl:
 * - CreateUnmappedTransferObjectType: TransferDeclaration without map -> UnmappedTransferObjectType
 * - CreateMappedTransferObjectType: TransferDeclaration with map -> MappedTransferObjectType
 */
@TransformationContext(
        source = TransferDeclaration.class,
        target = UnmappedTransferObjectType.class
)
public class TransferObjectTypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(TransferObjectTypeRules.class);

    /**
     * CreateUnmappedTransferObjectType
     * guard: s.map.isUndefined() and not s.isActorRelated()
     */
    @TransformRule(
            name = CREATE_UNMAPPED_TRANSFER_OBJECT_TYPE,
            description = "Transform unmapped TransferDeclaration to PSM UnmappedTransferObjectType"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = UnmappedTransferObjectType.class)
    @Guard(method = "isUnmappedNonActorTransfer")
    public TransformFunction<TransferDeclaration, UnmappedTransferObjectType> createUnmappedTransferObjectType() {
        return (source, ctx) -> {
            UnmappedTransferObjectType target = ctx.createTarget(UnmappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateUnmappedTransferObjectType");

            target.setName(source.getName());

            // s.eContainer.getModelRoot().elements.add(t)
            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created UnmappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    /**
     * CreateMappedTransferObjectType
     * guard: s.map.isDefined() and not s.isActorRelated()
     */
    @TransformRule(
            name = CREATE_MAPPED_TRANSFER_OBJECT_TYPE,
            description = "Transform mapped TransferDeclaration to PSM MappedTransferObjectType"
    )
    @Greedy
    @Transform(type = TransferDeclaration.class)
    @To(type = MappedTransferObjectType.class)
    @Guard(method = "isMappedNonActorTransfer")
    public TransformFunction<TransferDeclaration, MappedTransferObjectType> createMappedTransferObjectType() {
        return (source, ctx) -> {
            MappedTransferObjectType target = ctx.createTarget(MappedTransferObjectType.class);
            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateMappedTransferObjectType");

            // t.entityType = s.map.entity.getEntityDeclarationEquivalent()
            hu.blackbelt.judo.meta.psm.data.EntityType entityType =
                    ctx.equivalent(source.getMap().getEntity(),
                            hu.blackbelt.judo.meta.psm.data.EntityType.class, CREATE_ENTITY_TYPE);
            target.setEntityType(entityType);

            target.setName(source.getName());

            // s.eContainer.getModelRoot().elements.add(t)
            Package modelRoot = getModelRoot(source, ctx);
            addElement(modelRoot, target);

            LOG.debug("Created MappedTransferObjectType: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    public boolean isUnmappedNonActorTransfer(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferDeclaration)) return false;
        TransferDeclaration td = (TransferDeclaration) eObject;
        return td.getMap() == null && !isActorRelated(td);
    }

    public boolean isMappedNonActorTransfer(EObject eObject,
            hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (!(eObject instanceof TransferDeclaration)) return false;
        TransferDeclaration td = (TransferDeclaration) eObject;
        return td.getMap() != null && !isActorRelated(td);
    }
}
