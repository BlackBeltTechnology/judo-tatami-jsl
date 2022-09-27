package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

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

public class DialectParam {

    private String rdbms;
    private String liquibase;
    private String asm2rdbmsTrace;

    public String getRdbms() {
        return rdbms;
    }

    public void setRdbms(String rdbms) {
        this.rdbms = rdbms;
    }

    public String getLiquibase() {
        return liquibase;
    }

    public void setLiquibase(String liquibase) {
        this.liquibase = liquibase;
    }

    public String getAsm2rdbmsTrace() {
        return asm2rdbmsTrace;
    }

    public void setAsm2rdbmsTrace(String asm2rdbmsTrace) {
        this.asm2rdbmsTrace = asm2rdbmsTrace;
    }
}
