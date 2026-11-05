package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.namespace;

import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.psm.namespace.Model;
import hu.blackbelt.judo.meta.psm.namespace.Namespace;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.zeta.annotation.Lazy;
import hu.blackbelt.judo.zeta.annotation.To;
import hu.blackbelt.judo.zeta.annotation.Transform;
import hu.blackbelt.judo.zeta.annotation.TransformRule;
import hu.blackbelt.judo.zeta.annotation.TransformationContext;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.addPackage;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.addSubPackage;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.CREATE_MODEL_PACKAGES;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.CREATE_ROOT_MODEL;

/**
 * Namespace rules for JSL to PSM transformation.
 *
 * Ported from namespace.etl:
 * - CreateRootModel: @lazy rule, creates PSM Model from ModelDeclaration
 * - CreateModelPackages: Creates package hierarchy from ModelDeclaration name
 *
 * Note: The original ETL rules transform String objects. In Zeta, we transform
 * ModelDeclaration (an EObject) and extract the name String from it.
 */
@TransformationContext(
        source = ModelDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.namespace.NamedElement.class
)
public class NamespaceRules {

    private static final Logger LOG = LoggerFactory.getLogger(NamespaceRules.class);

    /**
     * Creates the root PSM Model from a ModelDeclaration.
     *
     * Ported from namespace.etl CreateRootModel (was @lazy, transforms String to Model).
     * In Zeta we transform ModelDeclaration and use defaultModelName from context.
     *
     * ETL equivalent:
     * <pre>
     * @lazy
     * rule CreateRootModel
     *     transform s : String
     *     to t : JUDOPSM!Model {
     *         t.setId("(jsl/" + s.replaceAll("::", "/") + ")/CreateRootModel");
     *         t.name = s.replaceAll("::", "_");
     * }
     * </pre>
     */
    @TransformRule(
            name = CREATE_ROOT_MODEL,
            description = "Create root PSM Model from ModelDeclaration"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = Model.class)
    public TransformFunction<ModelDeclaration, Model> createRootModel() {
        return (source, ctx) -> {
            // Return cached root model if already created (for imported/non-primary ModelDeclarations)
            Model cachedRoot = ctx.getAttribute("__rootModel");
            if (cachedRoot != null) {
                return cachedRoot;
            }

            String defaultModelName = ctx.getAttribute("defaultModelName");
            String modelName = defaultModelName != null ? defaultModelName : source.getName();

            Model target = ctx.createTarget(Model.class);
            target.setName(modelName.replaceAll("::", "_"));
            ctx.addToResource(target);

            ctx.setElementId(target, "(jsl/" + modelName.replaceAll("::", "/") + ")/CreateRootModel");

            // Cache the root model so subsequent calls for other ModelDeclarations return the same instance
            ctx.setAttribute("__rootModel", target);

            LOG.debug("Created root model: {}", target.getName());
            return target;
        };
    }

    /**
     * Creates the package hierarchy from a ModelDeclaration.
     *
     * Ported from namespace.etl CreateModelPackages (transforms String to Package).
     * Creates nested packages from the "::" separated model declaration name.
     *
     * ETL equivalent:
     * <pre>
     * rule CreateModelPackages
     *     transform s : String
     *     to t : JUDOPSM!Package {
     *         var psmModel = defaultModelName.equivalent("CreateRootModel");
     *         var fragments = s.split("::");
     *         var current : JUDOPSM!Namespace = psmModel;
     *         for (a in fragments) {
     *             // create or find existing sub-package
     *         }
     * }
     * </pre>
     */
    @TransformRule(
            name = CREATE_MODEL_PACKAGES,
            description = "Create package hierarchy from ModelDeclaration name"
    )
    @Lazy
    @Transform(type = ModelDeclaration.class)
    @To(type = Package.class)
    public TransformFunction<ModelDeclaration, Package> createModelPackages() {
        return (source, ctx) -> {
            String modelDeclName = source.getName();
            // Get the root PSM model - prefer cached attribute, fallback to lazy rule
            Model psmModel = ctx.getAttribute("__rootModel");
            if (psmModel == null) {
                psmModel = ctx.executeParentRule(CREATE_ROOT_MODEL, source);
            }

            String[] fragments = modelDeclName.split("::");
            Namespace current = psmModel;
            Package lastPackage = null;

            for (int i = 0; i < fragments.length; i++) {
                String fragmentName = fragments[i];
                boolean isLast = (i == fragments.length - 1);

                // Check if package already exists
                Package existing = findPackageByName(current, fragmentName);
                if (existing != null) {
                    current = existing;
                    if (isLast) {
                        lastPackage = existing;
                    }
                    continue;
                }

                // Create new package
                Package newPkg;
                if (isLast) {
                    // Use the target created by Zeta framework for the last fragment
                    newPkg = ctx.createTarget(Package.class);
                } else {
                    newPkg = ctx.create(Package.class);
                }
                newPkg.setName(fragmentName);

                String idBase = modelDeclName.replaceAll("::", "_");
                ctx.setElementId(newPkg,
                        "(jsl/" + idBase + "/" + fragmentName + ")/CreateModelPackages");

                // Add to parent
                if (current instanceof Model) {
                    addPackage((Model) current, newPkg);
                } else if (current instanceof Package) {
                    addSubPackage((Package) current, newPkg);
                }

                LOG.debug("Created Package: {} for {}", fragmentName, newPkg);
                current = newPkg;
                if (isLast) {
                    lastPackage = newPkg;
                }
            }

            return lastPackage;
        };
    }

    private static Package findPackageByName(Namespace namespace, String name) {
        if (namespace instanceof Model) {
            return ((Model) namespace).getPackages().stream()
                    .filter(p -> name.equals(p.getName()))
                    .findFirst().orElse(null);
        } else if (namespace instanceof Package) {
            return ((Package) namespace).getPackages().stream()
                    .filter(p -> name.equals(p.getName()))
                    .findFirst().orElse(null);
        }
        return null;
    }
}
