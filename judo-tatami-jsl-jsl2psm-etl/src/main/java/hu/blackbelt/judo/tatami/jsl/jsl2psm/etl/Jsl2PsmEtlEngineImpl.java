package hu.blackbelt.judo.tatami.jsl.jsl2psm.etl;

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
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmEtlEngine;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmTransformationTrace;
import org.slf4j.Logger;

import java.net.URI;

/**
 * ETL implementation of {@link Jsl2PsmEtlEngine}: delegates to {@link Jsl2Psm#executeJsl2PsmTransformation}.
 * Found via META-INF/services (plain Java) or registered by {@code Jsl2PsmEtlEngineComponent} (OSGi, with the bundle's script root).
 * See change: split-etl-artifacts.
 */
public class Jsl2PsmEtlEngineImpl implements Jsl2PsmEtlEngine {

    private final URI scriptRoot;

    /** Scripts resolved next to this class (jar / target/classes). */
    public Jsl2PsmEtlEngineImpl() {
        this(Jsl2Psm.calculateJsl2PsmTransformationScriptURI());
    }

    public Jsl2PsmEtlEngineImpl(URI scriptRoot) {
        this.scriptRoot = scriptRoot;
    }

    @Override
    public Jsl2PsmTransformationTrace execute(JslDslModel jslModel, PsmModel psmModel, Logger log, boolean createTrace, boolean generateBehaviours, boolean parallel) throws Exception {
        return Jsl2Psm.executeJsl2PsmTransformation(Jsl2Psm.Jsl2PsmParameter.jsl2PsmParameter()
                .jslModel(jslModel)
                .psmModel(psmModel)
                .log(log)
                .scriptUri(scriptRoot)
                .createTrace(createTrace)
                .generateBehaviours(generateBehaviours)
                .parallel(parallel));
    }
}
