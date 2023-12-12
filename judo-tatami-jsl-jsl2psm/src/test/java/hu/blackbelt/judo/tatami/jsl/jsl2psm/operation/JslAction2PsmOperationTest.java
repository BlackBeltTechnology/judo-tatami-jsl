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
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
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
        assertThat(assertUnmappedTransferObject("UnmappedTransfer").getOperations().size(), equalTo(9));

        assertOperation("UnmappedTransfer", "staticVoidAction", false, false, false, false, false);
        assertOperation("UnmappedTransfer", "staticVoidActionWithUnmappedInput", false, true, false, false, false);
        assertOperation("UnmappedTransfer", "staticVoidActionWithMappedInput", false, true, true, false, false);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputAction", false, false, false, true, false);
        assertOperation("UnmappedTransfer", "staticMappedOutputAction", false, false, false, true, true);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputActionWithUnmappedInput", false, true, false, true, false);
        assertOperation("UnmappedTransfer", "staticMappedOutputActionWithUnmappedInput", false, true, false, true, true);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputActionWithMappedInput", false, true, true, true, false);
        assertOperation("UnmappedTransfer", "staticMappedOutputActionWithMappedInput", false, true, true, true, true);


        assertMappedTransferObject("MappedTransfer");
        assertThat(assertMappedTransferObject("MappedTransfer").getOperations().size(), equalTo(18));

        assertOperation("MappedTransfer", "voidAction", true, false, false, false, false);
        assertOperation("MappedTransfer", "staticVoidAction", false, false, false, false, false);
        assertOperation("MappedTransfer", "voidActionWithUnmappedInput", true, true, false, false, false);
        assertOperation("MappedTransfer", "staticVoidActionWithUnmappedInput", false, true, false, false, false);
        assertOperation("MappedTransfer", "voidActionWithMappedInput", true, true, true, false, false);
        assertOperation("MappedTransfer", "staticVoidActionWithMappedInput", false, true, true, false, false);
        assertOperation("MappedTransfer", "unmappedOutputAction", true, false, false, true, false);
        assertOperation("MappedTransfer", "staticUnmappedOutputAction", false, false, false, true, false);
        assertOperation("MappedTransfer", "mappedOutputAction", true, false, false, true, true);
        assertOperation("MappedTransfer", "staticMappedOutputAction", false, false, false, true, true);
        assertOperation("MappedTransfer", "unmappedOutputActionWithUnmappedInput", true, true, false, true, false);
        assertOperation("MappedTransfer", "staticUnmappedOutputActionWithUnmappedInput", false, true, false, true, false);
        assertOperation("MappedTransfer", "mappedOutputActionWithUnmappedInput", true, true, false, true, true);
        assertOperation("MappedTransfer", "staticMappedOutputActionWithUnmappedInput", false, true, false, true, true);
        assertOperation("MappedTransfer", "unmappedOutputActionWithMappedInput", true, true, true, true, false);
        assertOperation("MappedTransfer", "staticUnmappedOutputActionWithMappedInput", false, true, true, true, false);
        assertOperation("MappedTransfer", "mappedOutputActionWithMappedInput", true, true, true, true, true);
        assertOperation("MappedTransfer", "staticMappedOutputActionWithMappedInput", false, true, true, true, true);

    
        assertMappedTransferObject("MappedFaultTransfer");
        assertThat(assertMappedTransferObject("MappedFaultTransfer").getOperations().size(), equalTo(2));
        assertOperationFaults("MappedFaultTransfer", "faults");
        assertOperationFaults("MappedFaultTransfer", "staticFaults");
        
        assertUnmappedTransferObject("UnmappedFaultTransfer");
        assertThat(assertUnmappedTransferObject("UnmappedFaultTransfer").getOperations().size(), equalTo(2));
        assertOperationFaults("UnmappedFaultTransfer", "faults");
        assertOperationFaults("UnmappedFaultTransfer", "staticFaults");

    }
    
    
    private void assertOperation(String transferName, String operationName, boolean excpectBound, 
    		boolean excpectInput, boolean expectMappedInput, 
    		boolean excpectOutput, boolean expectMappedOutput) {

        TransferOperation operation = assertTransferObjectOperation(transferName, operationName);
        if (!excpectBound) {
        	assertThat(operation,  instanceOf(UnboundOperation.class));
        } else {
        	assertThat(operation, instanceOf(BoundTransferOperation.class));        	
        }
        
        if (excpectInput) {
            assertThat(operation.getInput(), is(notNullValue()));        	
        	if (expectMappedInput) {
            	assertThat(operation.getInput().getType(), equalTo(assertMappedTransferObject("MappedInputParameter")));
            } else {
            	assertThat(operation.getInput().getType(), equalTo(assertUnmappedTransferObject("UnmappedInputParameter")));        	
            }        	
            assertThat(operation.getInput().getCardinality().getLower(), equalTo(1));
        } else {
            assertThat(operation.getInput(), is(nullValue()));        	
        }
        if (excpectOutput) {
            assertThat(operation.getOutput(), is(notNullValue()));        	
        	if (expectMappedOutput) {
            	assertThat(operation.getOutput().getType(), equalTo(assertMappedTransferObject("MappedOutputParameter")));
            } else {
            	assertThat(operation.getOutput().getType(), equalTo(assertUnmappedTransferObject("UnmappedOutputParameter")));        	
            }
            assertThat(operation.getOutput().getCardinality().getLower(), equalTo(1));        	
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
