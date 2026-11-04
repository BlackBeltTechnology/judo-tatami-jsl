package hu.blackbelt.judo.tatami.jsl.jsl2ui.perf;

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

/**
 * Generates valid JSL model strings with UI declarations (views, rows, frontends)
 * for performance testing the JSL2UI transformation.
 *
 * <p>For each entity, generates:
 * <ul>
 *   <li>3 attributes (string, numeric, boolean)</li>
 *   <li>1 mapped transfer with update event</li>
 *   <li>1 row declaration</li>
 *   <li>1 view declaration with widget group</li>
 * </ul>
 *
 * <p>Also generates an actor with access declarations, a frontend with menu,
 * and standard type/widget declarations.
 */
public class RealisticJslUiModelGenerator {

    private static final String MODEL_NAME = "GeneratedUiPerfModel";

    public static String generate(int entityCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("model ").append(MODEL_NAME).append(";\n\n");

        // Type declarations
        sb.append("type string String min-size:0 max-size:255;\n");
        sb.append("type numeric Integer precision:9 scale:0;\n");
        sb.append("type boolean Boolean;\n\n");

        // Widget declarations
        sb.append("widget string StringWidget;\n");
        sb.append("widget numeric NumericWidget;\n");
        sb.append("widget boolean BooleanWidget;\n\n");

        // Entities
        for (int i = 0; i < entityCount; i++) {
            sb.append("entity Entity").append(i).append(" {\n");
            sb.append("    field String name").append(i).append(";\n");
            sb.append("    field Integer value").append(i).append(";\n");
            sb.append("    field Boolean active").append(i).append(";\n");
            sb.append("}\n\n");
        }

        // Transfers with update event
        for (int i = 0; i < entityCount; i++) {
            sb.append("transfer Entity").append(i).append("Transfer(Entity").append(i).append(" e) {\n");
            sb.append("    field String name <= e.name").append(i).append(";\n");
            sb.append("    field Integer value <= e.value").append(i).append(";\n");
            sb.append("    field Boolean active <= e.active").append(i).append(";\n");
            sb.append("    event update onUpdate;\n");
            sb.append("}\n\n");
        }

        // Rows
        for (int i = 0; i < entityCount; i++) {
            sb.append("row Entity").append(i).append("Row(Entity").append(i).append("Transfer t) {\n");
            sb.append("    column String name <= t.name;\n");
            sb.append("}\n\n");
        }

        // Views
        for (int i = 0; i < entityCount; i++) {
            sb.append("view Entity").append(i).append("View(Entity").append(i).append("Transfer t) {\n");
            sb.append("    group main {\n");
            sb.append("        widget StringWidget name <= t.name;\n");
            sb.append("        widget NumericWidget value <= t.value;\n");
            sb.append("        widget BooleanWidget active <= t.active;\n");
            sb.append("    }\n");
            sb.append("}\n\n");
        }

        // Actor with access declarations
        sb.append("actor PerfActor {\n");
        for (int i = 0; i < entityCount; i++) {
            sb.append("    access Entity").append(i).append("Transfer[] items").append(i)
                    .append(" <= Entity").append(i).append(".all() update;\n");
        }
        sb.append("}\n\n");

        // Frontend with menu
        sb.append("frontend PerfMenu(PerfActor a)\n");
        sb.append("    menu: {\n");
        for (int i = 0; i < entityCount; i++) {
            sb.append("        table Entity").append(i).append("Row[] items").append(i)
                    .append(" <= a.items").append(i)
                    .append(" label:\"Entity").append(i).append("\"")
                    .append(" view:Entity").append(i).append("View;\n");
        }
        sb.append("    };\n");

        return sb.toString();
    }

    public static String getModelName() {
        return MODEL_NAME;
    }
}
