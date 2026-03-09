package hu.blackbelt.judo.tatami.jsl.jsl2psm.perf;

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
 * Generates valid JSL model strings with characteristics matching
 * the RackInspect real-world model (~70 entities, diverse patterns).
 *
 * <p>For each entity, generates:
 * <ul>
 *   <li>4 attributes (string, numeric, boolean, timestamp)</li>
 *   <li>2 relations (association + containment)</li>
 *   <li>2 mapped transfers with CRUD events</li>
 *   <li>1 derived field</li>
 * </ul>
 *
 * <p>Also generates enumerations and an actor declaration.
 */
public class RealisticJslModelGenerator {

    private static final String MODEL_NAME = "GeneratedPerfModel";

    /**
     * Generate a JSL model string with the given number of entities.
     */
    public static String generate(int entityCount) {
        StringBuilder sb = new StringBuilder();

        sb.append("model ").append(MODEL_NAME).append(";\n\n");

        // Type declarations
        sb.append("type string String min-size:0 max-size:255;\n");
        sb.append("type numeric Integer precision:9 scale:0;\n");
        sb.append("type numeric Decimal precision:15 scale:4;\n");
        sb.append("type boolean Boolean;\n");
        sb.append("type date Date;\n");
        sb.append("type time Time;\n");
        sb.append("type timestamp Timestamp;\n");
        sb.append("\n");

        // Enumerations (1 per 10 entities)
        int enumCount = Math.max(1, entityCount / 10);
        for (int i = 0; i < enumCount; i++) {
            sb.append("enum Status").append(i).append(" {\n");
            sb.append("    Active = 0;\n");
            sb.append("    Inactive = 1;\n");
            sb.append("    Pending = 2;\n");
            sb.append("    Archived = 3;\n");
            sb.append("}\n\n");
        }

        // Generate entities
        for (int i = 0; i < entityCount; i++) {
            generateEntity(sb, i, entityCount, enumCount);
        }

        // Generate mapped transfers (2 per entity)
        for (int i = 0; i < entityCount; i++) {
            generateMappedTransfer(sb, i, "Detail");
            generateMappedTransfer(sb, i, "Summary");
        }

        // Generate an actor
        sb.append("// Actor declaration\n");
        sb.append("transfer UserTransfer maps Entity0 as e {\n");
        sb.append("    field String name <=> e.name0;\n");
        sb.append("}\n\n");
        sb.append("actor MainActor\n");
        sb.append("    realm: \"APP\"\n");
        sb.append("    claim: \"email\"\n");
        sb.append("    identity: UserTransfer::name\n");
        sb.append("{\n");
        sb.append("    access Entity0DetailTransfer[] items <= Entity0.all()");
        sb.append(" create: true update: true delete: true;\n");
        sb.append("};\n\n");

        // Error declarations
        sb.append("error ValidationError {\n");
        sb.append("}\n\n");
        sb.append("error NotFoundError {\n");
        sb.append("}\n");

        return sb.toString();
    }

    private static void generateEntity(StringBuilder sb, int index, int entityCount, int enumCount) {
        String name = "Entity" + index;

        sb.append("entity ").append(name);
        // Some entities extend others
        if (index > 0 && index % 5 == 0) {
            sb.append(" extends Entity").append(index - 1);
        }
        sb.append(" {\n");

        // 4 attributes
        sb.append("    identifier String name").append(index).append(";\n");
        sb.append("    field Integer value").append(index).append(";\n");
        sb.append("    field Boolean active").append(index).append(";\n");
        sb.append("    field Timestamp created").append(index).append(";\n");

        // Enum field (if applicable)
        int enumIdx = index % enumCount;
        sb.append("    field Status").append(enumIdx).append(" status").append(index).append(";\n");

        // 1 derived field
        sb.append("    field String label").append(index)
                .append(" <= self.name").append(index).append(";\n");

        // Relations: association to next entity, containment to a related entity
        int relTarget1 = (index + 1) % entityCount;
        int relTarget2 = (index + 2) % entityCount;
        sb.append("    relation Entity").append(relTarget1)
                .append(" related").append(index).append(";\n");
        sb.append("    relation Entity").append(relTarget2)
                .append("[] items").append(index).append(";\n");

        sb.append("}\n\n");
    }

    private static void generateMappedTransfer(StringBuilder sb, int index, String suffix) {
        String entityName = "Entity" + index;
        String transferName = entityName + suffix + "Transfer";

        sb.append("transfer ").append(transferName)
                .append(" maps ").append(entityName).append(" as e {\n");

        // Map attributes
        sb.append("    field String name <= e.name").append(index).append(";\n");
        sb.append("    field Integer value <= e.value").append(index).append(";\n");
        sb.append("    field Boolean active <= e.active").append(index).append(";\n");

        // CRUD events for Detail transfers
        if ("Detail".equals(suffix)) {
            sb.append("    event create create").append(transferName).append(";\n");
            sb.append("    event update update").append(transferName).append(";\n");
            sb.append("    event delete delete").append(transferName).append(";\n");
        }

        sb.append("}\n\n");
    }

    /**
     * Returns the model name used by the generator.
     */
    public static String getModelName() {
        return MODEL_NAME;
    }
}
