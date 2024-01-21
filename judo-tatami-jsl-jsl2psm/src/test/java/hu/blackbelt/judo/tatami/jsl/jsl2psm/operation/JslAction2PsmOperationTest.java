package hu.blackbelt.judo.tatami.jsl.jsl2psm.operation;

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

import org.slf4j.Logger;
import hu.blackbelt.epsilon.runtime.execution.impl.BufferedSlf4jLogger;
import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.psm.service.BoundTransferOperation;
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;

@Slf4j
public class JslAction2PsmOperationTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/operation";

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
    void testActions() throws Exception {

        jslModel = JslParser.getModelFromFiles(
                "ActionsTestModel",
                List.of(new File("src/test/resources/operation/ActionsTestModel.jsl"))
        );

        transform();

        assertUnmappedTransferObject("UnmappedTransfer");
        assertThat(assertUnmappedTransferObject("UnmappedTransfer").getOperations().size(), equalTo(12));

        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticVoidAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticVoidActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticVoidActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticUnmappedOutputAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticMappedOutputAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticUnmappedOutputActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticMappedOutputActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticUnmappedOutputActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("UnmappedTransfer")
        		.operationName("staticMappedOutputActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(true));


        assertMappedTransferObject("MappedTransfer");
        assertThat(assertMappedTransferObject("MappedTransfer").getOperations().size(), equalTo(25));

        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("voidAction")
        		.excpectBound(true)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticVoidAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("voidActionWithUnmappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticVoidActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("voidActionWithMappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticVoidActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(false)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("unmappedOutputAction")
        		.excpectBound(true)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false)
        		);
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticUnmappedOutputAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("mappedOutputAction")
        		.excpectBound(true)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true)
        		.deleteOnResult(true)
        		.updateOnResult(true));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticMappedOutputAction")
        		.excpectBound(false)
        		.excpectInput(false)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("unmappedOutputActionWithUnmappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticUnmappedOutputActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("mappedOutputActionWithUnmappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true)
        		.deleteOnResult(true)
        		.updateOnResult(true));

        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticMappedOutputActionWithUnmappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(false)
        		.excpectOutput(true)
        		.expectMappedOutput(true));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("unmappedOutputActionWithMappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticUnmappedOutputActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(false));
        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("mappedOutputActionWithMappedInput")
        		.excpectBound(true)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(true)
        		.deleteOnResult(true)
        		.updateOnResult(true));

        
        assertOperation(param()
        		.transferName("MappedTransfer")
        		.operationName("staticMappedOutputActionWithMappedInput")
        		.excpectBound(false)
        		.excpectInput(true)
        		.expectMappedInput(true)
        		.excpectOutput(true)
        		.expectMappedOutput(true));


    	assertThat(assertUnmappedTransferObject("UnmappedInputParameter").getOperations().size(),  equalTo(1));
    	assertThat(assertTransferObjectOperation("UnmappedInputParameter", "default"),  instanceOf(UnboundOperation.class));
    	assertThat(assertTransferObjectOperation("UnmappedInputParameter", "default").getBehaviour().getBehaviourType(),  
    			equalTo(TransferOperationBehaviourType.GET_TEMPLATE));

    	assertThat(assertMappedTransferObject("MappedInputParameter").getOperations().size(),  equalTo(1));
    	assertThat(assertTransferObjectOperation("MappedInputParameter", "refreshInstance").getBehaviour().getBehaviourType(),  
    			equalTo(TransferOperationBehaviourType.REFRESH));

    
        assertMappedTransferObject("MappedFaultTransfer");
        assertThat(assertMappedTransferObject("MappedFaultTransfer").getOperations().size(), equalTo(3));
        assertOperationFaults("MappedFaultTransfer", "faults");
        assertOperationFaults("MappedFaultTransfer", "staticFaults");
    	assertThat(assertTransferObjectOperation("MappedFaultTransfer", "refreshInstance").getBehaviour().getBehaviourType(),  
    			equalTo(TransferOperationBehaviourType.REFRESH));
        
        assertUnmappedTransferObject("UnmappedFaultTransfer");
        assertThat(assertUnmappedTransferObject("UnmappedFaultTransfer").getOperations().size(), equalTo(1));
        assertOperationFaults("UnmappedFaultTransfer", "staticFaults");

        
    	assertThat(assertTransferObjectOperation("UnmappedInputParameter", "default").getBehaviour().getBehaviourType(),  
    			equalTo(TransferOperationBehaviourType.GET_TEMPLATE));

    }
    
    @Builder
    private static class AssertOperationParameters {
    	String transferName;
    	String operationName; 
    	boolean excpectBound; 
		boolean excpectInput; 
		boolean expectMappedInput; 
		boolean excpectOutput;
		boolean expectMappedOutput;
		boolean deleteOnResult;
		boolean updateOnResult;
    }
    
    private AssertOperationParameters.AssertOperationParametersBuilder param() {
    	return AssertOperationParameters.builder();
    }
    
    private void assertOperation(AssertOperationParameters.AssertOperationParametersBuilder p) {

        TransferOperation operation = assertTransferObjectOperation(p.transferName, p.operationName);
        if (!p.excpectBound) {
        	assertThat(operation,  instanceOf(UnboundOperation.class));
        } else {
        	assertThat(operation, instanceOf(BoundTransferOperation.class));        	
        }
        
        if (p.excpectInput) {
            assertThat(operation.getInput(), is(notNullValue()));        	
        	if (p.expectMappedInput) {
            	assertThat(operation.getInput().getType(), equalTo(assertMappedTransferObject("MappedInputParameter")));
            } else {
            	assertThat(operation.getInput().getType(), equalTo(assertUnmappedTransferObject("UnmappedInputParameter")));        	
            }        	
            assertThat(operation.getInput().getCardinality().getLower(), equalTo(1));
        } else {
            assertThat(operation.getInput(), is(nullValue()));        	
        }
        if (p.excpectOutput) {
            assertThat(operation.getOutput(), is(notNullValue()));        	
        	if (p.expectMappedOutput) {
            	assertThat(operation.getOutput().getType(), equalTo(assertMappedTransferObject("MappedOutputParameter")));
            } else {
            	assertThat(operation.getOutput().getType(), equalTo(assertUnmappedTransferObject("UnmappedOutputParameter")));        	
            }
            assertThat(operation.getOutput().getCardinality().getLower(), equalTo(1)); 
            
            assertThat(operation.isDeleteOnResult(), equalTo(p.deleteOnResult));
            assertThat(operation.isUpdateOnResult(), equalTo(p.updateOnResult));
        } else {
            assertThat(operation.getOutput(), is(nullValue()));        	
        }
    }
    
    
    private void assertOperationFaults(String transferName, String operationName) {
        TransferOperation operation = assertTransferObjectOperation(transferName, operationName);
        assertThat(operation.getFaults().size(), equalTo(2));
        
        assertThat(operation.getFaults(), hasItems(
        		hasProperty("name", equalTo("Fault1")),
        		hasProperty("name", equalTo("Fault2"))
		));

    	
    }


}
