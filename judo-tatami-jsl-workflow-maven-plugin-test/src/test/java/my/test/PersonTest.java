package my.test;

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
