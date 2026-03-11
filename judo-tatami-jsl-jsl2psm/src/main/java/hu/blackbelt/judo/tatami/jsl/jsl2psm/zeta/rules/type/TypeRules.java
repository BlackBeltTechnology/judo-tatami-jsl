package hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.rules.type;

import hu.blackbelt.judo.meta.jsl.jsldsl.DataTypeDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EnumDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.NavigationBaseDeclarationReference;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrimitiveDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.QueryParameterDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.EnumLiteral;
import hu.blackbelt.judo.meta.jsl.jsldsl.MaxFileSizeModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MaxSizeModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.MimeType;
import hu.blackbelt.judo.meta.jsl.jsldsl.MimeTypesModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.ModelDeclaration;
import hu.blackbelt.judo.meta.jsl.jsldsl.PrecisionModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.RegexModifier;
import hu.blackbelt.judo.meta.jsl.jsldsl.ScaleModifier;
import hu.blackbelt.judo.meta.psm.namespace.Package;
import hu.blackbelt.judo.meta.psm.type.BinaryType;
import hu.blackbelt.judo.meta.psm.type.BooleanType;
import hu.blackbelt.judo.meta.psm.type.DateType;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.meta.psm.type.TimeType;
import hu.blackbelt.judo.meta.psm.type.TimestampType;
import hu.blackbelt.judo.zeta.annotation.*;
import hu.blackbelt.judo.zeta.transformation.core.TransformFunction;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmHelper.*;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.zeta.Jsl2PsmRuleNames.*;

/**
 * Type rules for JSL to PSM transformation.
 *
 * Ported from type.etl: 7 primitive type rules (@greedy) + CreateEnumerationType + CreateEnumerationMember.
 *
 * All primitive type rules share the same guard pattern from ETL:
 *   guard: s.`primitive` == "TYPE" and not s.eContainer.name.equivalent("CreateModelPackages").elements.collect(e|e.name).contains(s.name)
 *
 * The guard ensures we don't create duplicate types when they already exist in the model package.
 */
@TransformationContext(
        source = DataTypeDeclaration.class,
        target = hu.blackbelt.judo.meta.psm.type.Type.class
)
public class TypeRules {

    private static final Logger LOG = LoggerFactory.getLogger(TypeRules.class);

    // --- Primitive type rules ---

    @TransformRule(
            name = CREATE_NUMERIC_TYPE,
            description = "Transform JSL DataTypeDeclaration (numeric) to PSM NumericType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = NumericType.class)
    @Guard(method = "isNumericType")
    public TransformFunction<DataTypeDeclaration, NumericType> createNumericType() {
        return (source, ctx) -> {
            NumericType target = ctx.createTarget(NumericType.class);
            target.setName(source.getName());

            PrecisionModifier precision = getPrecision(source);
            if (precision != null) {
                target.setPrecision(precision.getValue().intValue());
            }
            ScaleModifier scale = getScale(source);
            if (scale != null) {
                target.setScale(scale.getValue().intValue());
            }

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateNumericType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created NumericType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_DATE_TYPE,
            description = "Transform JSL DataTypeDeclaration (date) to PSM DateType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = DateType.class)
    @Guard(method = "isDateType")
    public TransformFunction<DataTypeDeclaration, DateType> createDateType() {
        return (source, ctx) -> {
            DateType target = ctx.createTarget(DateType.class);
            target.setName(source.getName());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateDateType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created DateType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_TIME_TYPE,
            description = "Transform JSL DataTypeDeclaration (time) to PSM TimeType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = TimeType.class)
    @Guard(method = "isTimeType")
    public TransformFunction<DataTypeDeclaration, TimeType> createTimeType() {
        return (source, ctx) -> {
            TimeType target = ctx.createTarget(TimeType.class);
            target.setName(source.getName());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTimeType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created TimeType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_TIMESTAMP_TYPE,
            description = "Transform JSL DataTypeDeclaration (timestamp) to PSM TimestampType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = TimestampType.class)
    @Guard(method = "isTimestampType")
    public TransformFunction<DataTypeDeclaration, TimestampType> createTimestampType() {
        return (source, ctx) -> {
            TimestampType target = ctx.createTarget(TimestampType.class);
            target.setName(source.getName());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateTimestampType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created TimestampType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BOOLEAN_TYPE,
            description = "Transform JSL DataTypeDeclaration (boolean) to PSM BooleanType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = BooleanType.class)
    @Guard(method = "isBooleanType")
    public TransformFunction<DataTypeDeclaration, BooleanType> createBooleanType() {
        return (source, ctx) -> {
            BooleanType target = ctx.createTarget(BooleanType.class);
            target.setName(source.getName());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBooleanType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created BooleanType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_STRING_TYPE,
            description = "Transform JSL DataTypeDeclaration (string) to PSM StringType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = StringType.class)
    @Guard(method = "isStringType")
    public TransformFunction<DataTypeDeclaration, StringType> createStringType() {
        return (source, ctx) -> {
            StringType target = ctx.createTarget(StringType.class);
            target.setName(source.getName());

            MaxSizeModifier maxSize = getMaxSize(source);
            if (maxSize != null) {
                target.setMaxLength(maxSize.getValue().intValue());
            }

            RegexModifier regex = getRegex(source);
            if (regex != null && regex.getRegex() != null && regex.getRegex().getValue() != null) {
                target.setRegExp(regex.getRegex().getValue());
            }

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateStringType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created StringType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_BINARY_TYPE,
            description = "Transform JSL DataTypeDeclaration (binary) to PSM BinaryType"
    )
    @Greedy
    @Transform(type = DataTypeDeclaration.class)
    @To(type = BinaryType.class)
    @Guard(method = "isBinaryType")
    public TransformFunction<DataTypeDeclaration, BinaryType> createBinaryType() {
        return (source, ctx) -> {
            BinaryType target = ctx.createTarget(BinaryType.class);
            target.setName(source.getName());

            MimeTypesModifier mimeTypes = getMimeType(source);
            if (mimeTypes != null) {
                for (MimeType mimeType : mimeTypes.getValues()) {
                    if (mimeType.getValue() != null) {
                        target.getMimeTypes().add(mimeType.getValue().getValue());
                    }
                }
            }

            MaxFileSizeModifier maxFileSize = getMaxFileSize(source);
            if (maxFileSize != null) {
                target.setMaxFileSize(getMaxFileSizeValue(maxFileSize));
            }

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateBinaryType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created BinaryType: {}", target.getName());
            return target;
        };
    }

    // --- Enumeration rules ---

    @TransformRule(
            name = CREATE_ENUMERATION_TYPE,
            description = "Transform JSL EnumDeclaration to PSM EnumerationType"
    )
    @Greedy
    @Transform(type = EnumDeclaration.class)
    @To(type = EnumerationType.class)
    public TransformFunction<EnumDeclaration, EnumerationType> createEnumerationType() {
        return (source, ctx) -> {
            EnumerationType target = ctx.createTarget(EnumerationType.class);
            target.setName(source.getName());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEnumerationType");
            addToModelPackage(source, target, ctx);

            LOG.debug("Created EnumerationType: {}", target.getName());
            return target;
        };
    }

    @TransformRule(
            name = CREATE_ENUMERATION_MEMBER,
            description = "Transform JSL EnumLiteral to PSM EnumerationMember"
    )
    @Greedy
    @Transform(type = EnumLiteral.class)
    @To(type = EnumerationMember.class)
    public TransformFunction<EnumLiteral, EnumerationMember> createEnumerationMember() {
        return (source, ctx) -> {
            EnumerationMember target = ctx.createTarget(EnumerationMember.class);
            target.setName(source.getName());
            target.setOrdinal(source.getValue().intValue());

            ctx.setElementId(target, "(jsl/" + getJslId(source) + ")/CreateEnumerationMember");

            // Add to parent enum type
            EnumerationType parentEnum = ctx.equivalent(source.eContainer(), EnumerationType.class);
            addEnumerationMember(parentEnum, target);

            LOG.debug("Created EnumerationMember: {}", target.getName());
            return target;
        };
    }

    // --- Guard methods ---

    /**
     * Guard for primitive type rules. Checks:
     * 1. The DataTypeDeclaration has the correct primitive type
     * 2. No element with the same name already exists in the model package
     *
     * ETL guard pattern:
     *   s.`primitive` == "TYPE" and not s.eContainer.name.equivalent("CreateModelPackages").elements.collect(e|e.name).contains(s.name)
     */
    private boolean isPrimitiveType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx, String primitiveType) {
        if (!(eObject instanceof DataTypeDeclaration)) {
            return false;
        }
        DataTypeDeclaration dataType = (DataTypeDeclaration) eObject;
        if (!primitiveType.equals(dataType.getPrimitive())) {
            return false;
        }
        return !existsInModelPackage(dataType, ctx);
    }

    public boolean isNumericType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "numeric");
    }

    public boolean isDateType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "date");
    }

    public boolean isTimeType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "time");
    }

    public boolean isTimestampType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "timestamp");
    }

    public boolean isBooleanType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "boolean");
    }

    public boolean isStringType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "string");
    }

    public boolean isBinaryType(EObject eObject, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        return isPrimitiveType(eObject, ctx, "binary");
    }

    // --- Helper methods ---

    /**
     * Checks if an element with the same name already exists in the model package.
     * This mirrors the ETL guard:
     *   not s.eContainer.name.equivalent("CreateModelPackages").elements.collect(e|e.name).contains(s.name)
     */
    private boolean existsInModelPackage(DataTypeDeclaration dataType, hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (dataType.eContainer() instanceof ModelDeclaration) {
            ModelDeclaration modelDecl = (ModelDeclaration) dataType.eContainer();
            try {
                Package modelPackage = ctx.equivalent(modelDecl, Package.class);
                if (modelPackage != null) {
                    return modelPackage.getElements().stream()
                            .anyMatch(e -> dataType.getName().equals(e.getName()));
                }
            } catch (Exception e) {
                // Package not yet created - element doesn't exist
                return false;
            }
        }
        return false;
    }

    /**
     * Adds a namespace element to the model root package.
     * Mirrors: s.eContainer.getModelRoot().elements.add(t)
     */
    private void addToModelPackage(EObject source, hu.blackbelt.judo.meta.psm.namespace.NamespaceElement target,
                                   hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        if (source.eContainer() instanceof ModelDeclaration) {
            ModelDeclaration modelDecl = (ModelDeclaration) source.eContainer();
            Package modelPackage = ctx.equivalent(modelDecl, Package.class);
            addElement(modelPackage, target);
        }
    }

    // --- Post-execution hook ---

    /**
     * Materializes primitive types referenced via NavigationBaseDeclarationReference.
     * Mirrors ETL @post block:
     * {@code for (c in JSL!NavigationBaseDeclarationReference.all().select(n | n.reference.isKindOf(JSL!PrimitiveDeclaration))) {
     *     var dummy = c.reference.getPrimitiveDeclarationEquivalent();
     * }}
     *
     * This ensures primitive types that are only referenced indirectly (e.g., via query parameter types)
     * still get their PSM equivalents created even if no @Greedy rule matched them directly.
     */
    @PostExecution
    public void materializePrimitiveTypes(hu.blackbelt.judo.zeta.transformation.core.TransformationContext ctx) {
        org.eclipse.emf.ecore.resource.ResourceSet jslResourceSet = ctx.getAttribute("__jslResourceSet");
        if (jslResourceSet == null) return;

        var iterator = jslResourceSet.getAllContents();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next instanceof NavigationBaseDeclarationReference) {
                NavigationBaseDeclarationReference navRef = (NavigationBaseDeclarationReference) next;
                if (navRef.getReference() instanceof PrimitiveDeclaration) {
                    PrimitiveDeclaration primitiveDecl = (PrimitiveDeclaration) navRef.getReference();
                    try {
                        ctx.equivalent(primitiveDecl, hu.blackbelt.judo.meta.psm.type.Primitive.class);
                    } catch (Exception e) {
                        LOG.debug("Could not materialize primitive type: {}", e.getMessage());
                    }
                }
            }
        }
        LOG.debug("@PostExecution: materialized primitive types from NavigationBaseDeclarationReference");
    }
}
