package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.zeta.common.ExtensionMethodRegistry;
import hu.blackbelt.judo.zeta.common.ModelProvider;
import hu.blackbelt.judo.zeta.transformation.core.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformationExecutor;
import hu.blackbelt.judo.zeta.transformation.core.TransformationRegistry;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.AccessCreateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.AccessListBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.AccessValidateCreateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.ActionRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.DeleteBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.GetActionInputRangeBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.GetTemplateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.GetUploadTokenBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RefreshBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationAddReferenceBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationCreateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationGetRangeReferenceBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationListBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationRemoveReferenceBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationSetReferenceBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationUnsetReferenceBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.RelationValidateCreateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.UpdateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.action.ValidateUpdateBehaviourRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.actor.AccessRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.actor.ActorTypeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.AssociationRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.CardinalityRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.ContainmentRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.EntityTypeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.PrimitiveTypedElementRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived.DataPropertyRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived.EntityQueryRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived.ExpressionTypeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.derived.NavigationPropertyRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.namespace.NamespaceRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.data.StaticQueryRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.DefaultTransferAttributeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.DefaultTransferObjectTypeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.DefaultTransferRelationRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.QueryCustomizerRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.TransferAttributeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.TransferObjectTypeRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.TransferRelationChoiceRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.structure.TransferRelationRules;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.type.TypeRules;
import lombok.Builder;
import lombok.NonNull;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class Jsl2PsmZetaTransformation {

    private static final Logger log = LoggerFactory.getLogger(Jsl2PsmZetaTransformation.class);

    private final JslDslModel jslModel;
    private final PsmModel psmModel;
    private final String defaultModelName;
    private final boolean generateBehaviours;

    @Builder
    public Jsl2PsmZetaTransformation(
            @NonNull JslDslModel jslModel,
            @NonNull PsmModel psmModel,
            @NonNull String defaultModelName,
            boolean generateBehaviours) {
        this.jslModel = jslModel;
        this.psmModel = psmModel;
        this.defaultModelName = defaultModelName;
        this.generateBehaviours = generateBehaviours;
    }

    public void execute() {
        log.info("Starting JSL to PSM Zeta transformation for model: {}", defaultModelName);
        long startTime = System.currentTimeMillis();
        long phaseStart;

        // Phase 1: Create registry and register all rule classes
        phaseStart = System.currentTimeMillis();
        TransformationRegistry registry = createRegistry();
        log.info("Phase 1 - Create registry: {}ms", System.currentTimeMillis() - phaseStart);

        // Phase 2: Create transformation context
        phaseStart = System.currentTimeMillis();
        TransformationContext context = createContext(registry);
        log.info("Phase 2 - Create context: {}ms", System.currentTimeMillis() - phaseStart);

        // Phase 3: Create executor
        phaseStart = System.currentTimeMillis();
        TransformationExecutor executor = TransformationExecutor.builder()
                .registry(registry)
                .context(context)
                .parallel(true)
                .etlCompatibilityMode(true)
                .build();
        log.info("Phase 3 - Create executor: {}ms", System.currentTimeMillis() - phaseStart);

        // Phase 4a: Ensure root model exists (equivalent to ETL pre-block)
        // ETL: JUDOPSM.resource.contents.add(defaultModelName.equivalent("CreateRootModel"))
        // Must run BEFORE greedy rules so the root model uses defaultModelName
        ensureRootModelExists(context);

        // Phase 4b: Execute transformation
        phaseStart = System.currentTimeMillis();
        log.info("Starting executor.transform()");
        executor.transform();
        log.info("Phase 4b - executor.transform(): {}ms", System.currentTimeMillis() - phaseStart);

        // Phase 5: Post-processing
        phaseStart = System.currentTimeMillis();
        postProcess(context);
        log.info("Phase 5 - postProcess(): {}ms", System.currentTimeMillis() - phaseStart);

        long duration = System.currentTimeMillis() - startTime;
        log.info("JSL to PSM Zeta transformation completed in {}ms", duration);
    }

    private TransformationRegistry createRegistry() {
        TransformationRegistry registry = new TransformationRegistry();

        // Namespace rules (model + packages must exist first)
        registry.register(NamespaceRules.class);

        // Type rules (primitive types, enums)
        registry.register(TypeRules.class);

        // Entity type rules
        registry.register(EntityTypeRules.class);

        // Cardinality rules (lazy, used by association/containment/derived rules)
        registry.register(CardinalityRules.class);

        // Primitive typed element rules (attributes)
        registry.register(PrimitiveTypedElementRules.class);

        // Association rules (association ends, opposites)
        registry.register(AssociationRules.class);

        // Containment rules (containment from entity fields)
        registry.register(ContainmentRules.class);

        // Expression type rules (lazy getter expressions)
        registry.register(ExpressionTypeRules.class);

        // Derived rules (data properties, navigation properties, entity queries)
        registry.register(DataPropertyRules.class);
        registry.register(NavigationPropertyRules.class);
        registry.register(EntityQueryRules.class);

        // Structure rules (transfer object types)
        registry.register(TransferObjectTypeRules.class);

        // Default transfer object rules (entity -> default representation)
        registry.register(DefaultTransferObjectTypeRules.class);
        registry.register(DefaultTransferAttributeRules.class);
        registry.register(DefaultTransferRelationRules.class);

        // Transfer declaration rules (explicit transfer objects)
        registry.register(TransferAttributeRules.class);
        registry.register(TransferRelationRules.class);
        registry.register(TransferRelationChoiceRules.class);

        // Query customizer rules (guarded by generateBehaviours)
        registry.register(QueryCustomizerRules.class);

        // Static query rules
        registry.register(StaticQueryRules.class);

        // Actor type rules
        registry.register(ActorTypeRules.class);

        // Access rules (actor access declarations)
        registry.register(AccessRules.class);

        // Action rules (transfer action declarations)
        registry.register(ActionRules.class);

        // Behaviour rules (guarded by generateBehaviours)
        registry.register(DeleteBehaviourRules.class);
        registry.register(UpdateBehaviourRules.class);
        registry.register(ValidateUpdateBehaviourRules.class);
        registry.register(RefreshBehaviourRules.class);
        registry.register(GetTemplateBehaviourRules.class);
        registry.register(GetUploadTokenBehaviourRules.class);

        // Relation behaviour rules
        registry.register(RelationListBehaviourRules.class);
        registry.register(RelationCreateBehaviourRules.class);
        registry.register(RelationValidateCreateBehaviourRules.class);
        registry.register(RelationAddReferenceBehaviourRules.class);
        registry.register(RelationRemoveReferenceBehaviourRules.class);
        registry.register(RelationSetReferenceBehaviourRules.class);
        registry.register(RelationUnsetReferenceBehaviourRules.class);
        registry.register(RelationGetRangeReferenceBehaviourRules.class);

        // Access behaviour rules
        registry.register(AccessListBehaviourRules.class);
        registry.register(AccessCreateBehaviourRules.class);
        registry.register(AccessValidateCreateBehaviourRules.class);

        // Get action input range behaviour rules
        registry.register(GetActionInputRangeBehaviourRules.class);

        return registry;
    }

    private TransformationContext createContext(TransformationRegistry registry) {
        ResourceSet sourceResourceSet = jslModel.getResourceSet();
        ResourceSet targetResourceSet = psmModel.getResourceSet();

        ModelProvider modelProvider = new Jsl2PsmModelProvider();
        ExtensionMethodRegistry extensionRegistry = new ExtensionMethodRegistry();

        TransformationContext context = new TransformationContext(
                modelProvider,
                sourceResourceSet,
                targetResourceSet,
                extensionRegistry
        );

        context.setTransformationRegistry(registry);
        context.setUseStructuredIds(true);
        context.setEtlCompatibilityMode(true);

        context.registerResource("jsl", sourceResourceSet);
        context.registerResource("psm", targetResourceSet);
        context.setPreferredSourceAlias("jsl");

        context.setAttribute("defaultModelName", defaultModelName);

        // Set default context attributes matching ETL parameter defaults
        context.setAttribute("entityNamePrefix", "_");
        context.setAttribute("entityNamePostfix", "");
        context.setAttribute("generateDefaultTransferObject", true);
        context.setAttribute("defaultTransferObjectNamePrefix", "");
        context.setAttribute("defaultTransferObjectNamePostfix", "");
        context.setAttribute("defaultParameterNamePrefix", "_");
        context.setAttribute("defaultParameterNamePostfix", "_Parameters");
        context.setAttribute("defaultParameterNameMidfix", "_");
        context.setAttribute("defaultDefaultNamePrefix", "_");
        context.setAttribute("defaultDefaultNamePostfix", "");
        context.setAttribute("defaultDefaultNameMidfix", "_Default_");
        context.setAttribute("defaultReadsNamePrefix", "_");
        context.setAttribute("defaultReadsNamePostfix", "");
        context.setAttribute("defaultReadsNameMidfix", "_Reads_");
        context.setAttribute("defaultRelationRangeNamePrefix", "_");
        context.setAttribute("defaultRelationRangeNamePostfix", "");
        context.setAttribute("defaultRelationRangeNameMidfix", "_RelationRange_");
        context.setAttribute("defaultActionInputParameterRangeNamePrefix", "_");
        context.setAttribute("defaultActionInputParameterRangeNamePostfix", "");
        context.setAttribute("defaultActionInputParameterRangeNameMidfix", "_ActionInputParameterRange_");
        context.setAttribute("generateBehaviours", generateBehaviours);
        context.setAttribute("__jslResourceSet", jslModel.getResourceSet());
        context.setAttribute("__psmResourceSet", psmModel.getResourceSet());

        return context;
    }

    /**
     * Ensures the root PSM Model exists, equivalent to ETL pre-block:
     *   JUDOPSM.resource.contents.add(defaultModelName.equivalent("CreateRootModel"))
     *
     * Creates the root model ONCE using the first ModelDeclaration, then triggers
     * CreateModelPackages for all ModelDeclarations to create package hierarchies
     * for both primary and imported models.
     */
    private void ensureRootModelExists(TransformationContext context) {
        ModelProvider modelProvider = new Jsl2PsmModelProvider();
        Collection<ModelDeclaration> modelDecls = modelProvider.getAllContents(
                jslModel.getResourceSet(), ModelDeclaration.class);

        // Find the primary ModelDeclaration (matching defaultModelName)
        ModelDeclaration primaryDecl = null;
        for (ModelDeclaration modelDecl : modelDecls) {
            if (modelDecl.getName().equals(defaultModelName)) {
                primaryDecl = modelDecl;
                break;
            }
        }
        // Fallback to first if no exact match
        if (primaryDecl == null && !modelDecls.isEmpty()) {
            primaryDecl = modelDecls.iterator().next();
        }

        if (primaryDecl != null) {
            // Create root model only for the primary declaration
            try {
                hu.blackbelt.judo.meta.psm.namespace.Model rootModel = context.equivalent(
                        primaryDecl,
                        hu.blackbelt.judo.meta.psm.namespace.Model.class,
                        Jsl2PsmRuleNames.CREATE_ROOT_MODEL);
                // Store as context attribute so CreateRootModel rule reuses it for other ModelDeclarations
                context.setAttribute("__rootModel", rootModel);
                log.info("Created root model: {} from primary declaration: {}",
                        rootModel.getName(), primaryDecl.getName());
            } catch (Exception e) {
                log.warn("Failed to create root model for {}: {}",
                        primaryDecl.getName(), e.getMessage());
            }

            // Note: Packages are NOT eagerly created here. They are created on-demand
            // when rules call getModelRoot() via ctx.equivalent(decl, Package.class, CREATE_MODEL_PACKAGES).
            // This matches ETL behavior where CreateModelPackages only runs via equivalent() calls.
        }
    }

    /**
     * Post-processing is now handled by @PostExecution hooks in rule classes:
     * - AssociationRules.setPartners() — bidirectional association partner linking
     * - QueryCustomizerRules.setEnumerationOrdinals() — enumeration ordinal assignment
     * - CardinalityRules.normalizeCardinalityIds() — cardinality XMI ID normalization
     * - TypeRules.materializePrimitiveTypes() — force lazy primitive type creation
     */
    private void postProcess(TransformationContext context) {
        // All post-processing moved to @PostExecution hooks
    }

    private class Jsl2PsmModelProvider implements ModelProvider {
        @Override
        public <T extends EObject> Collection<T> getAllContents(ResourceSet resourceSet, Class<T> type) {
            List<T> result = new java.util.ArrayList<>();
            var iterator = resourceSet.getAllContents();
            while (iterator.hasNext()) {
                var next = iterator.next();
                if (type.isInstance(next)) {
                    result.add(type.cast(next));
                }
            }
            return result;
        }

        @Override
        public String getName(EObject element) {
            try {
                var method = element.getClass().getMethod("getName");
                return (String) method.invoke(element);
            } catch (Exception e) {
                return element.eClass().getName();
            }
        }

        @Override
        public String getTypeName(EObject element) {
            return element.eClass().getName();
        }
    }
}
