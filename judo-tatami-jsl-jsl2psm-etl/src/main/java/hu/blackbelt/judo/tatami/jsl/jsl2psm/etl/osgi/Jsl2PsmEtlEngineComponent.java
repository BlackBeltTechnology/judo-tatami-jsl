package hu.blackbelt.judo.tatami.jsl.jsl2psm.etl.osgi;

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

import hu.blackbelt.judo.tatami.core.etl.EtlEngineRegistry;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.Jsl2PsmEtlEngine;
import hu.blackbelt.judo.tatami.jsl.jsl2psm.etl.Jsl2PsmEtlEngineImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Registers the ETL {@link Jsl2PsmEtlEngine} in the {@link EtlEngineRegistry} while this bundle is active. The script root is
 * the bundle entry the former Jsl2PsmTransformationService used. See change: split-etl-artifacts.
 */
@Component(immediate = true, service = Jsl2PsmEtlEngineComponent.class)
public class Jsl2PsmEtlEngineComponent {

    private Jsl2PsmEtlEngine engine;

    @Activate
    public void activate(BundleContext bundleContext) throws Exception {
        java.net.URI scriptRoot = bundleContext.getBundle()
                .getEntry("/tatami/jsl2psm/transformations/psm/jslToPsm.etl")
                .toURI()
                .resolve(".");
        engine = new Jsl2PsmEtlEngineImpl(scriptRoot);
        EtlEngineRegistry.register(Jsl2PsmEtlEngine.class, engine);
    }

    @Deactivate
    public void deactivate() {
        EtlEngineRegistry.unregister(Jsl2PsmEtlEngine.class, engine);
    }
}
