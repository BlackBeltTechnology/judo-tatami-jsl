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

import hu.blackbelt.judo.meta.jsl.jsldsl.runtime.JslDslModel;
import hu.blackbelt.judo.meta.psm.runtime.PsmModel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.net.URI;
import java.util.List;

@Builder(builderMethodName = "defaultWorkflowSetupParameters")
@Getter
public class DefaultWorkflowSetupParameters {

    private String modelVersion;

    /**
     * When psmModel is defined psmModelSourceURI is ignored.
     */
    private PsmModel psmModel;

    private URI psmModelSourceURI;

    /**
     * When jslModel is defined jslModelSourceURI is ignored.
     */
    private JslDslModel jslModel;

    private URI jslModelSourceURI;

    @NonNull
    private String modelName;

    @NonNull
    private List<String> dialectList;

    @Builder.Default
    private Boolean runInParallel = true;

    @Builder.Default
    private Boolean enableMetrics = true;

    @Builder.Default
    private Boolean ignoreJsl2Psm = false;

    @Builder.Default
    private Boolean ignoreJsl2PsmTrace = false;

    @Builder.Default
    private Boolean ignorePsm2Asm = false;

    @Builder.Default
    private Boolean ignorePsm2Measure = false;

    @Builder.Default
    private Boolean ignorePsm2AsmTrace = false;

    @Builder.Default
    private Boolean ignorePsm2MeasureTrace = false;

    @Builder.Default
    private Boolean ignoreAsm2Rdbms = false;

    @Builder.Default
    private Boolean ignoreAsm2RdbmsTrace = false;

    @Builder.Default
    private Boolean ignoreRdbms2Liquibase = false;

    @Builder.Default
    private Boolean ignoreAsm2Expression = false;

    @Builder.Default
    private Boolean validateModels = false;

}
