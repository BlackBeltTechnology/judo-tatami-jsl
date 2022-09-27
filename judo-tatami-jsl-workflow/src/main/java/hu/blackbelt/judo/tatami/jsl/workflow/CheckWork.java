package hu.blackbelt.judo.tatami.jsl.workflow;

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

import hu.blackbelt.judo.tatami.core.workflow.work.*;

import java.util.function.Supplier;

public class CheckWork implements Work {

    final Supplier<Boolean> check;

    CheckWork(Supplier<Boolean> check) {
        this.check = check;
    }

    @Override
    public String getName() {
        return "Check TransformationContext";
    }

    @Override
    public WorkReport call() {
        try {
            if (check.get()) {
                return new DefaultWorkReport(WorkStatus.COMPLETED);
            } else {
                return new DefaultWorkReport(WorkStatus.FAILED);
            }
        } catch (Exception e) {
            return new DefaultWorkReport(WorkStatus.FAILED, e);
        }
    }
}
