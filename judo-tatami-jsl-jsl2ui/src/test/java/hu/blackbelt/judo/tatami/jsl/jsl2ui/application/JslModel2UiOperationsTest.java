package hu.blackbelt.judo.tatami.jsl.jsl2ui.application;

import hu.blackbelt.judo.meta.jsl.runtime.JslParser;
import hu.blackbelt.judo.meta.ui.Application;
import hu.blackbelt.judo.tatami.jsl.jsl2ui.AbstractTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class JslModel2UiOperationsTest extends AbstractTest {
    private static final String TARGET_TEST_CLASSES = "target/test-classes/operations";

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
        return log;
    }

    private static String createModelString(String name) {
        return """
            model %s;
            import judo::types;

            error Error1 {
            }

            error ErrorWithDefaults {
                field String withDefault default:"Hello!";
            }

            // mapped transfer

            entity Entity1 {
                field Integer number;
            }

            transfer Transfer1(Entity1 e1) {
                field Integer number <=> e1.number;
            }

            view View1(Transfer1 t1) {
                widget Integer number <=> t1.number;
            }

            table Table1(Transfer1 t1) {
                column Integer number <= t1.number;
            }

            // unmapped transfers

            transfer Transfer2 {
                field Integer number;
            }

            view View2(Transfer2 t2) {
                widget Integer number <=> t2.number;
            }

            // test

            transfer TransferX(Entity1 e1) {
                field Integer number <=> e1.number;

                action void myAction1() throws Error1,ErrorWithDefaults;
                action Transfer1 myAction2(Transfer1 input choices:Entity1.all());
                // action Transfer2 myAction3(Transfer2 input);

                event create createTX;
                event update updateTX;
            }

            view ViewX(TransferX tx) {
                widget Integer number <=> tx.number;

                group level1 label:"Yo" icon:"text" {
                    action void myAction1() <= tx.myAction1;
                }
                action View1 myAction2(View1 input selector:Table1) <= tx.myAction2;
                // action View2 myAction3(View2 input) <= tx.myAction3;
            }

            form FormX(TransferX tx) {
                widget Integer number <=> tx.number;
            }

            table TableX(TransferX tx) {
                column Integer number <= tx.number;
            }

            actor A {
                access TransferX[] txs <= Entity1.all() create:true update:true;
            }

            menu M(A a) {
                table TableX vxs <= a.txs view:ViewX form:FormX;
            }
        """.formatted(name);
    }

    @BeforeAll
    static void prepareTestFolders() throws IOException {
        if (!Files.exists(Paths.get(TARGET_TEST_CLASSES))) {
            Files.createDirectories(Paths.get(TARGET_TEST_CLASSES));
        }
    }

    @Test
    void testOperationsOnViews() throws Exception {
        jslModel = JslParser.getModelFromStrings("OperationsOnViews", List.of(createModelString("OperationsOnViews")));

        transform();

        List<Application> apps = uiModelWrapper.getStreamOfUiApplication().toList();

        assertEquals(1, apps.size());

        Application application = apps.get(0);
    }
}
