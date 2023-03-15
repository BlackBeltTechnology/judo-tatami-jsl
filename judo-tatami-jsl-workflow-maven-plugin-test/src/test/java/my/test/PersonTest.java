package my.test;

/*-
 * #%L
 * Judo :: Tatami :: JSL :: Workflow :: Maven :: Plugin :: TEST
 * %%
 * Copyright (C) 2018 - 2023 BlackBelt Technology
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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import hu.blackbelt.judo.runtime.core.bootstrap.JudoDefaultModule;
import hu.blackbelt.judo.runtime.core.bootstrap.JudoModelLoader;
import hu.blackbelt.judo.runtime.core.bootstrap.dao.rdbms.hsqldb.JudoHsqldbModules;
import hu.blackbelt.judo.runtime.core.dao.rdbms.hsqldb.HsqldbDialect;
import my.test.test.guice.test.TestDaoModules;
import my.test.test.sdk.test.test.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonTest {
    public static String MODEL_NAME = "Test";

    private Injector injector;

    @Inject
    Person.PersonDao personDao;

    @BeforeEach
    protected void init() throws Exception {
        JudoModelLoader modelLoader = JudoModelLoader
                .loadFromDirectory(MODEL_NAME, new File("target/generated-sources/model"), new HsqldbDialect(), true);

        injector = Guice.createInjector(
                JudoHsqldbModules.builder().build(),
                new TestDaoModules(),
                new JudoDefaultModule(this, modelLoader)
        );
    }

    @Test
    public void testFullName() {
        Person person = personDao.create(Person.builder().withFirstName("John").withLastName("Doe").build());

        assertEquals(Optional.of("John Doe"), person.getFullName());
    }
}
