package hu.blackbelt.judo.tatami.jsl.jsl2psm;

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
import org.slf4j.Logger;

/**
 * ETL (Epsilon) JSL to PSM engine, contributed by the optional {@code judo-tatami-jsl-jsl2psm-etl} add-on.
 * Jsl2PsmWork looks it up via {@link hu.blackbelt.judo.tatami.core.etl.EtlEngineRegistry} in ETL/DUAL mode only.
 * See change: split-etl-artifacts.
 */
public interface Jsl2PsmEtlEngine {

    /** Maven coordinates of the add-on implementing this engine (named in EtlEngineMissingException). */
    String ARTIFACT_COORDINATES = "hu.blackbelt.judo.tatami:judo-tatami-jsl-jsl2psm-etl";

    /** Runs the ETL transformation into {@code psmModel}. {@code log} may be {@code null}. */
    Jsl2PsmTransformationTrace execute(JslDslModel jslModel, PsmModel psmModel, Logger log, boolean createTrace, boolean generateBehaviours, boolean parallel) throws Exception;
}
