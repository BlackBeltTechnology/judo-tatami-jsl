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
import hu.blackbelt.judo.meta.psm.service.TransferOperation;
import hu.blackbelt.judo.meta.psm.service.UnboundOperation;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.AbstractTest;
import lombok.extern.slf4j.Slf4j;

import org.hamcrest.core.IsEqual;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(18, assertUnmappedTransferObject("UnmappedTransfer").getOperations().size());

        assertOperation("UnmappedTransfer", "voidAction", false, false, false, false, false);
        assertOperation("UnmappedTransfer", "staticVoidAction", false, false, false, false, false);
        assertOperation("UnmappedTransfer", "voidActionWithUnmappedInput", false, true, false, false, false);
        assertOperation("UnmappedTransfer", "staticVoidActionWithUnmappedInput", false, true, false, false, false);
        assertOperation("UnmappedTransfer", "voidActionWithMappedInput", false, true, true, false, false);
        assertOperation("UnmappedTransfer", "staticVoidActionWithMappedInput", false, true, true, false, false);
        assertOperation("UnmappedTransfer", "unmappedOutputAction", false, false, false, true, false);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputAction", false, false, false, true, false);
        assertOperation("UnmappedTransfer", "mappedOutputAction", false, false, false, true, true);
        assertOperation("UnmappedTransfer", "staticMappedOutputAction", false, false, false, true, true);
        assertOperation("UnmappedTransfer", "unmappedOutputActionWithUnmappedInput", false, true, false, true, false);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputActionWithUnmappedInput", false, true, false, true, false);
        assertOperation("UnmappedTransfer", "mappedOutputActionWithUnmappedInput", false, true, false, true, true);
        assertOperation("UnmappedTransfer", "staticMappedOutputActionWithUnmappedInput", false, true, false, true, true);
        assertOperation("UnmappedTransfer", "unmappedOutputActionWithMappedInput", false, true, true, true, false);
        assertOperation("UnmappedTransfer", "staticUnmappedOutputActionWithMappedInput", false, true, true, true, false);
        assertOperation("UnmappedTransfer", "mappedOutputActionWithMappedInput", false, true, true, true, true);
        assertOperation("UnmappedTransfer", "staticMappedOutputActionWithMappedInput", false, true, true, true, true);

        
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
    }
    
    
    private void assertOperation(String transferName, String operationName, boolean excpectBound, 
    		boolean excpectInput, boolean expectMappedInput, 
    		boolean excpectOutput, boolean expectMappedOutput) {

        TransferOperation operation = assertTransferObjectOperation(transferName, operationName);
        if (!excpectBound) {
        	assertTrue(operation instanceof UnboundOperation);
        }
        
        if (excpectInput) {
            if (expectMappedInput) {
            	assertEquals(assertMappedTransferObject("MappedInputParameter"), operation.getInput().getType());
            } else {
            	assertEquals(assertUnmappedTransferObject("UnmappedInputParameter"), operation.getInput().getType());        	
            }        	
            assertThat(operation.getInput().getCardinality().getLower(), IsEqual.equalTo(0));
        } else {
            assertTrue(operation.getInput() == null);        	
        }
        if (excpectOutput) {
            if (expectMappedOutput) {
            	assertEquals(assertMappedTransferObject("MappedOutputParameter"), operation.getOutput().getType());
            } else {
            	assertEquals(assertUnmappedTransferObject("UnmappedOutputParameter"), operation.getOutput().getType());        	
            }
            assertThat(operation.getOutput().getCardinality().getLower(), IsEqual.equalTo(0));        	
        } else {
            assertTrue(operation.getOutput() == null);        	
        }
    }

}
