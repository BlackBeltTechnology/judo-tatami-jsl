package hu.blackbelt.judo.tatami.jsl.jsl2psm.actor;

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

import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@Slf4j
public class JslModel2PsmAnonymousActorTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/actor";

    @Override
	public boolean generateBehaviours() {
    	return true;
    }

    @Override
    protected String getTargetTestClasses() {
        return TARGET_TEST_CLASSES;
    }

    @Override
    protected String getTest() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected Logger createLog() {
        return new BufferedSlf4jLogger(log);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testActors() throws Exception {

        jslModel = JslParser.getModelFromFiles(
                "AnonymousActorTestModel",
                List.of(new File("src/test/resources/actor/AnonymousActorTestModel.jsl"))
        );

        transform();

        assertMappedTransferObject("UserTransfer");
        assertThat(assertMappedTransferObject("UserTransfer").getOperations().size(), equalTo(5));
        assertMappedTransferObject("UserTransfer").getOperations().forEach(o -> System.out.println(o.getName()));

        assertCrudOperation(params()
        		.transferName("UserTransfer")
        		.operationName("refreshInstance")
        		.behaviour(TransferOperationBehaviourType.REFRESH)
        		.isBound(true)
        		.inputType("_UserTransferQueryCustomizer")
        		.isMappedInputType(false)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));

        assertCrudOperation(params()
        		.transferName("UserTransfer")
        		.operationName("deleteInstance")
        		.behaviour(TransferOperationBehaviourType.DELETE_INSTANCE)
        		.isBound(true));
        
        assertCrudOperation(params()
        		.transferName("UserTransfer")
        		.operationName("updateInstance")
        		.behaviour(TransferOperationBehaviourType.UPDATE_INSTANCE)
        		.isBound(true)
        		.inputType("UserTransfer")
        		.isMappedInputType(true)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));
        
        assertCrudOperation(params()
        		.transferName("UserTransfer")
        		.operationName("validateUpdateInstance")
        		.behaviour(TransferOperationBehaviourType.VALIDATE_UPDATE)
        		.isBound(true)
        		.inputType("UserTransfer")
        		.isMappedInputType(true)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));

        assertCrudOperation(params()
        		.transferName("UserTransfer")
        		.operationName("default")
        		.behaviour(TransferOperationBehaviourType.GET_TEMPLATE)
        		.isBound(false)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));

        assertCrudOperation(params()
        		.transferName("Actor")
        		.operationName("listOfManager")
        		.behaviour(TransferOperationBehaviourType.LIST)
        		.isBound(false)
        		.inputType("_UserTransferQueryCustomizer")
        		.isMappedInputType(false)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));    

        assertCrudOperation(params()
        		.transferName("Actor")
        		.operationName("createInstanceOfManager")
        		.behaviour(TransferOperationBehaviourType.CREATE_INSTANCE)
        		.isBound(false)
        		.inputType("UserTransfer")
        		.isMappedInputType(true)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));  

        assertCrudOperation(params()
        		.transferName("Actor")
        		.operationName("validateCreateInstanceOfManager")
        		.behaviour(TransferOperationBehaviourType.VALIDATE_CREATE)
        		.isBound(false)
        		.inputType("UserTransfer")
        		.isMappedInputType(true)
        		.outputType("UserTransfer")
        		.isMappedOutputType(true));   
    }
    
    
    @Builder
    @Getter
    private static class AssertCrudOperationParameters {

    	String transferName;
    	String operationName;
    	TransferOperationBehaviourType behaviour;
    	boolean isBound;
		String inputType;
		boolean isMappedInputType;
		String outputType;
		boolean isMappedOutputType;
    }
    
    private static AssertCrudOperationParameters.AssertCrudOperationParametersBuilder params() {
    	return AssertCrudOperationParameters.builder();
    }
    
    private void assertCrudOperation(AssertCrudOperationParameters.AssertCrudOperationParametersBuilder p) {

        TransferOperation operation = assertTransferObjectOperation(p.transferName, p.operationName);
        if (p.isBound) {
        	assertThat(operation, instanceOf(BoundTransferOperation.class));        	
        } else {
        	assertThat(operation, instanceOf(UnboundOperation.class));        	        	
        }
    	assertThat(operation.getBehaviour().getBehaviourType(), equalTo(p.behaviour));
        
        if (p.inputType != null) {
            assertThat(operation.getInput(), is(notNullValue()));
            if (p.isMappedInputType) {
            	assertThat(operation.getInput().getType(), equalTo(assertMappedTransferObject(p.inputType)));            	
            } else {            	
            	assertThat(operation.getInput().getType(), equalTo(assertUnmappedTransferObject(p.inputType)));            	
            }
            // assertThat(operation.getInput().getCardinality().getLower(), equalTo(1));
        } else {
            assertThat(operation.getInput(), is(nullValue()));        	
        }
        if (p.outputType != null) {
            assertThat(operation.getOutput(), is(notNullValue()));        	
            if (p.isMappedOutputType) {
            	assertThat(operation.getOutput().getType(), equalTo(assertMappedTransferObject(p.outputType)));            	
            } else {
            	assertThat(operation.getOutput().getType(), equalTo(assertUnmappedTransferObject(p.outputType)));            	
            }

        	// assertThat(operation.getOutput().getCardinality().getLower(), equalTo(1));        	
        } else {
            assertThat(operation.getOutput(), is(nullValue()));        	
        }
    }
}
