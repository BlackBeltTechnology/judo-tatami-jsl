package hu.blackbelt.judo.tatami.jsl.jsl2psm;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import hu.blackbelt.epsilon.runtime.execution.api.Log;
import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.jsl.jsldsl.support.JslDslModelResourceSupport;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.data.Attribute;
import hu.blackbelt.judo.meta.psm.data.EntityType;
import hu.blackbelt.judo.meta.psm.data.Relation;
import hu.blackbelt.judo.meta.psm.derived.DataProperty;
import hu.blackbelt.judo.meta.psm.derived.NavigationProperty;
import hu.blackbelt.judo.meta.psm.derived.StaticData;
import hu.blackbelt.judo.meta.psm.derived.StaticNavigation;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import hu.blackbelt.judo.meta.psm.service.MappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.service.TransferAttribute;
import hu.blackbelt.judo.meta.psm.service.TransferObjectRelation;
import hu.blackbelt.judo.meta.psm.service.UnmappedTransferObjectType;
import hu.blackbelt.judo.meta.psm.support.PsmModelResourceSupport;
import hu.blackbelt.judo.meta.psm.type.BinaryType;
import hu.blackbelt.judo.meta.psm.type.BooleanType;
import hu.blackbelt.judo.meta.psm.type.CustomType;
import hu.blackbelt.judo.meta.psm.type.DateType;
import hu.blackbelt.judo.meta.psm.type.EnumerationMember;
import hu.blackbelt.judo.meta.psm.type.EnumerationType;
import hu.blackbelt.judo.meta.psm.type.NumericType;
import hu.blackbelt.judo.meta.psm.type.StringType;
import hu.blackbelt.judo.meta.psm.type.TimeType;
import hu.blackbelt.judo.meta.psm.type.TimestampType;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel.SaveArguments.jslDslSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.calculatePsmValidationScriptURI;
import static hu.blackbelt.judo.meta.psm.PsmEpsilonValidator.validatePsm;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.SaveArguments.psmSaveArgumentsBuilder;
import static hu.blackbelt.judo.meta.psm.runtime.PsmModel.buildPsmModel;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter;
import static hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2Psm.executeJsl2PsmTransformation;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
abstract public class AbstractTest {
    protected static String TEST_SOURCE_MODEL_NAME = "urn:test.judo-meta-jsl";

    protected Log slf4jlog;
    protected JslDslModel jslModel;

    protected String testName;
    protected Map<EObject, List<EObject>> resolvedTrace;
    protected PsmModel psmModel;
    protected Jsl2PsmTransformationTrace jsl2PsmTransformationTrace;

    protected PsmModelResourceSupport psmModelWrapper;
    protected JslDslModelResourceSupport jslModelWrapper;

    @BeforeEach
    void setUp() {
        // Default logger
        slf4jlog = createLog();
    }

    @AfterEach
    void tearDown() throws Exception {
        slf4jlog.close();

        final String traceFileName = testName + "-jsl2psm.model";

        // Saving trace map
        if (jsl2PsmTransformationTrace != null) {
            jsl2PsmTransformationTrace.save(new File(getTargetTestClasses(), traceFileName));

            // Loading trace map
            Jsl2PsmTransformationTrace jsl2PsmTransformationTraceLoaded = Jsl2PsmTransformationTrace
                    .fromModelsAndTrace(
                            psmModel.getName(),
                            jslModel,
                            psmModel,
                            new File(getTargetTestClasses(), traceFileName)
                    );

            // Resolve serialized URI's as EObject map
            resolvedTrace = jsl2PsmTransformationTraceLoaded.getTransformationTrace();

            // Printing trace
            for (EObject e : resolvedTrace.keySet()) {
                for (EObject t : resolvedTrace.get(e)) {
                    log.trace(e.toString() + " -> " + t.toString());
                }
            }	
        }

        jslModel.saveJslDslModel(jslDslSaveArgumentsBuilder().file(new File(getTargetTestClasses(), testName + "-jsl.model")));

        if (psmModel != null) {
            psmModel.savePsmModel(psmSaveArgumentsBuilder().validateModel(false).file(new File(getTargetTestClasses(), testName + "-psm.model")));
            if (!psmModel.isValid()) {
                log.error(psmModel.getDiagnosticsAsString());
            }
            
            assertTrue(psmModel.isValid());        	
        }
    }
    
    protected void transform() throws Exception {
        // Create empty PSM model
        psmModel = buildPsmModel().build();
        psmModelWrapper = PsmModelResourceSupport.psmModelResourceSupportBuilder().resourceSet(psmModel.getResourceSet()).build();
        jslModelWrapper = JslDslModelResourceSupport.jslDslModelResourceSupportBuilder().resourceSet(jslModel.getResourceSet()).build();
    	
    	assertTrue(jslModel.isValid());
//        validateJsl(new Slf4jLog(log), jslModel, calculateEsmValidationScriptURI());

        
        // Make transformation which returns the trace with the serialized URI's
        jsl2PsmTransformationTrace = executeJsl2PsmTransformation(addTransformationParameters(testName, jsl2PsmParameter()
        		.log(slf4jlog)
                .jslModel(jslModel)
                .psmModel(psmModel)
                .createTrace(true)));
        
        assertTrue(psmModel.isValid());
        validatePsm(createLog(), psmModel, calculatePsmValidationScriptURI());
    }

    public Jsl2PsmParameter.Jsl2PsmParameterBuilder addTransformationParameters(String testName, Jsl2PsmParameter.Jsl2PsmParameterBuilder parameters) {
    	return parameters;
    }
    
    abstract protected String getTargetTestClasses();

    abstract protected String getTest();

    abstract protected Log createLog();
    
    
    public Set<EntityType> getEntityTypes() {
    	return psmModelWrapper.getStreamOfPsmDataEntityType().collect(Collectors.toSet());
    }

    public Set<MappedTransferObjectType> getMappedTransferObjectTypes() {
    	return psmModelWrapper.getStreamOfPsmServiceMappedTransferObjectType().collect(Collectors.toSet());
    }

    public Set<UnmappedTransferObjectType> getUnmappedTransferObjectTypes() {
    	return psmModelWrapper.getStreamOfPsmServiceUnmappedTransferObjectType().collect(Collectors.toSet());
    }

    public MappedTransferObjectType assertMappedTransferObject(String name) {
        final Optional<MappedTransferObjectType> to = getMappedTransferObjectTypes().stream().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(to.isPresent());    	
        return to.get();
    }

    public UnmappedTransferObjectType assertUnmappedTransferObject(String name) {
        final Optional<UnmappedTransferObjectType> to = getUnmappedTransferObjectTypes().stream().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(to.isPresent());    	
        return to.get();
    }

    public TransferAttribute assertMappedTransferObjectAttribute(String toName, String attrName) {
    	final Optional<TransferAttribute> attr = assertMappedTransferObject(toName).getAttributes().stream().filter(e -> e.getName().equals(attrName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }


    public TransferObjectRelation assertMappedTransferObjectRelation(String toName, String relName) {
    	final Optional<TransferObjectRelation> rel = assertMappedTransferObject(toName).getRelations().stream().filter(e -> e.getName().equals(relName)).findAny();
        assertTrue(rel.isPresent());    	
        return rel.get();
    }

    public TransferAttribute assertUnmappedTransferObjectAttribute(String toName, String attrName) {
    	final Optional<TransferAttribute> attr = assertUnmappedTransferObject(toName).getAttributes().stream().filter(e -> e.getName().equals(attrName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public TransferObjectRelation assertUnmappedTransferObjectRelation(String toName, String relName) {
    	final Optional<TransferObjectRelation> rel = assertUnmappedTransferObject(toName).getRelations().stream().filter(e -> e.getName().equals(relName)).findAny();
        assertTrue(rel.isPresent());    	
        return rel.get();
    }

    public EntityType assertEntityType(String name) {
        final Optional<EntityType> en = getEntityTypes().stream().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(en.isPresent()); 
        return en.get();
    }

    public DataProperty assertDataProperty(String entityName, String propName) {
    	final Optional<DataProperty> attr = assertEntityType(entityName).getDataProperties().stream().filter(e -> e.getName().equals(propName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public DataProperty assertAllDataProperty(String entityName, String propName) {
    	final Optional<DataProperty> attr = assertEntityType(entityName).getAllDataProperties().stream().filter(e -> e.getName().equals(propName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public StaticData assertStaticData(String name) {
        final Optional<StaticData> staticData = psmModelWrapper.getStreamOfPsmDerivedStaticData().filter(s -> s.getName().equals(name)).findAny();
        assertTrue(staticData.isPresent());
        return staticData.get();
    }

    public StaticNavigation assertStaticNavigation(String name) {
        final Optional<StaticNavigation> staticNavigation = psmModelWrapper.getStreamOfPsmDerivedStaticNavigation().filter(s -> s.getName().equals(name)).findAny();
        assertTrue(staticNavigation.isPresent());
        return staticNavigation.get();
    }


    public NavigationProperty assertNavigationProperty(String entityName, String propName) {
    	final Optional<NavigationProperty> attr = assertEntityType(entityName).getNavigationProperties().stream().filter(e -> e.getName().equals(propName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public NavigationProperty assertAllNavigationProperty(String entityName, String propName) {
    	final Optional<NavigationProperty> attr = assertEntityType(entityName).getAllNavigationProperties().stream().filter(e -> e.getName().equals(propName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public Attribute assertAttribute(String entityName, String attrName) {
    	final Optional<Attribute> attr = assertEntityType(entityName).getAttributes().stream().filter(e -> e.getName().equals(attrName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public Attribute assertAllAttribute(String entityName, String attrName) {
    	final Optional<Attribute> attr = assertEntityType(entityName).getAllAttributes().stream().filter(e -> e.getName().equals(attrName)).findAny();
        assertTrue(attr.isPresent());    	
        return attr.get();
    }

    public Relation assertRelation(String entityName, String relationName) {
    	Optional<Relation> rel = assertEntityType(entityName).getRelations().stream().filter(r -> r.getName().equals(relationName)).findFirst();
        assertTrue(rel.isPresent());
        return rel.get();    	
    }

    public Relation assertAllRelation(String entityName, String relationName) {
    	Optional<Relation> rel = assertEntityType(entityName).getAllRelations().stream().filter(r -> r.getName().equals(relationName)).findFirst();
        assertTrue(rel.isPresent());
        return rel.get();    	
    }

    public StringType assertStringType(String name) {
        final Optional<StringType> t = psmModelWrapper.getStreamOfPsmTypeStringType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public NumericType assertNumericType(String name) {
        final Optional<NumericType> t = psmModelWrapper.getStreamOfPsmTypeNumericType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }


    public BinaryType assertBinaryType(String name) {
        final Optional<BinaryType> t = psmModelWrapper.getStreamOfPsmTypeBinaryType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }
    
    public BooleanType assertBooleanType(String name) {
        final Optional<BooleanType> t = psmModelWrapper.getStreamOfPsmTypeBooleanType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public DateType assertDateType(String name) {
        final Optional<DateType> t = psmModelWrapper.getStreamOfPsmTypeDateType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public TimeType assertTimeType(String name) {
        final Optional<TimeType> t = psmModelWrapper.getStreamOfPsmTypeTimeType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public TimestampType assertTimestampType(String name) {
        final Optional<TimestampType> t = psmModelWrapper.getStreamOfPsmTypeTimestampType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public CustomType assertCustomType(String name) {
        final Optional<CustomType> t = psmModelWrapper.getStreamOfPsmTypeCustomType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public EnumerationType assertEnumerationType(String name) {
        final Optional<EnumerationType> t = psmModelWrapper.getStreamOfPsmTypeEnumerationType().filter(e -> e.getName().equals(name)).findAny();
        assertTrue(t.isPresent()); 
        return t.get();
    }

    public EnumerationMember assertEnumerationMember(String name, String memberName) {
    	Optional<EnumerationMember> m = assertEnumerationType(name).getMembers().stream().filter(e -> e.getName().equals(memberName)).findAny();
        assertTrue(m.isPresent()); 
        return m.get();
    }

}
