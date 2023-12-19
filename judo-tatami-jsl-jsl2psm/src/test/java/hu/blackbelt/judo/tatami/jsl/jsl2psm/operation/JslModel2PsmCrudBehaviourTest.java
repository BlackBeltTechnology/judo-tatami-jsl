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
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviour;
import hu.blackbelt.judo.meta.psm.service.TransferOperationBehaviourType;
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

@Slf4j
public class JslModel2PsmCrudBehaviourTest extends AbstractTest {
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
    void testBehaviours() throws Exception {

        jslModel = JslParser.getModelFromFiles(
                "CrudBehaviourTestModel",
                List.of(new File("src/test/resources/operation/CrudBehaviourTestModel.jsl"))
        );

        transform();

        assertMappedTransferObject("MappedTransfer");
        assertThat(assertMappedTransferObject("MappedTransfer").getOperations().size(), equalTo(6));

        assertCrudOperation("MappedTransfer", "deleteInstance",
        		TransferOperationBehaviourType.DELETE_INSTANCE, true, null, false, null, false);
        assertCrudOperation("MappedTransfer", "updateInstance",
        		TransferOperationBehaviourType.UPDATE_INSTANCE, true, "MappedTransfer", true, "MappedTransfer", true);
        assertCrudOperation("MappedTransfer", "validateUpdateInstance",
        		TransferOperationBehaviourType.VALIDATE_UPDATE, true, "MappedTransfer", true, "MappedTransfer", true);

        assertCrudOperation("MappedTransfer", "createInstanceForRelationCrudBehaviourTestModelMappedTransferCreateEntities", 
        		TransferOperationBehaviourType.CREATE_INSTANCE, true, "CreateTransfer", true, "CreateTransfer", true);    
        assertCrudOperation("MappedTransfer", "default", 
        		TransferOperationBehaviourType.GET_TEMPLATE, false, null, false, "MappedTransfer", true);
  
        assertCrudOperation("MappedTransfer", "getUploadTokenForBinary", 
        		TransferOperationBehaviourType.GET_UPLOAD_TOKEN, false, null, false, "UploadToken", false);
  }
    
    
    private void assertCrudOperation(String transferName, String operationName, TransferOperationBehaviourType behaviour, boolean isBound, 
    		String inputType, boolean isMappedInputType, 
    		String outputType, boolean isMappedOutputType) {

        TransferOperation operation = assertTransferObjectOperation(transferName, operationName);
        if (isBound) {
        	assertThat(operation, instanceOf(BoundTransferOperation.class));        	
        } else {
        	assertThat(operation, instanceOf(UnboundOperation.class));        	        	
        }
    	assertThat(operation.getBehaviour().getBehaviourType(), equalTo(behaviour));
        
        if (inputType != null) {
            assertThat(operation.getInput(), is(notNullValue()));
            if (isMappedInputType) {
            	assertThat(operation.getInput().getType(), equalTo(assertMappedTransferObject(inputType)));            	
            } else {            	
            	assertThat(operation.getInput().getType(), equalTo(assertUnmappedTransferObject(inputType)));            	
            }
            // assertThat(operation.getInput().getCardinality().getLower(), equalTo(1));
        } else {
            assertThat(operation.getInput(), is(nullValue()));        	
        }
        if (outputType != null) {
            assertThat(operation.getOutput(), is(notNullValue()));        	
            if (isMappedOutputType) {
            	assertThat(operation.getOutput().getType(), equalTo(assertMappedTransferObject(outputType)));            	
            } else {
            	assertThat(operation.getOutput().getType(), equalTo(assertUnmappedTransferObject(outputType)));            	
            }

        	// assertThat(operation.getOutput().getCardinality().getLower(), equalTo(1));        	
        } else {
            assertThat(operation.getOutput(), is(nullValue()));        	
        }
    }
}
