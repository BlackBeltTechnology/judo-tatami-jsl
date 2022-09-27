package hu.blackbelt.judo.tatami.jsl.jsl2psm.osgi;

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
import hu.blackbelt.judo.tatami.core.AbstractModelTracker;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component(immediate = true, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Slf4j
public class Jsl2PsmTransformationJslModelTracker extends AbstractModelTracker<JslDslModel> {

    @Reference
    Jsl2PsmTransformationService jsl2PsmTransformationService;

    Map<String, ServiceRegistration<PsmModel>> registrations = new ConcurrentHashMap<>();
    Map<String, PsmModel> models = new HashMap<>();


    @Activate
    protected void activate(ComponentContext contextPar) {
        componentContext = contextPar;
        openTracker(contextPar.getBundleContext());
    }

    @Deactivate
    protected void deactivate() {
        closeTracker();
        registrations.forEach((k, v) -> { v.unregister(); });
    }

    private ComponentContext componentContext;

    @Override
    public void install(JslDslModel jslModel) {
        String key = jslModel.getName();
        PsmModel psmModel = null;
        if (models.containsKey(key)) {
            log.error("Model already loaded: " + jslModel.getName());
            return;
        }

        try {
            psmModel = jsl2PsmTransformationService.install(jslModel);
            log.info("Registering model: " + psmModel);
            ServiceRegistration<PsmModel> modelServiceRegistration =
                    componentContext.getBundleContext()
                            .registerService(PsmModel.class, psmModel, psmModel.toDictionary());
            models.put(key, psmModel);
            registrations.put(key, modelServiceRegistration);
        } catch (Exception e) {
            log.error("Could not register PSM Model: " + jslModel.getName(), e);
        }
    }

    @Override
    public void uninstall(JslDslModel jslModel) {
        String key = jslModel.getName();
        if (!registrations.containsKey(key)) {
            log.error("Model is not registered: " + jslModel.getName());
        } else {
            jsl2PsmTransformationService.uninstall(jslModel);
            registrations.get(key).unregister();
            registrations.remove(key);
            models.remove(key);
        }
    }

    @Override
    public Class<JslDslModel> getModelClass() {
        return JslDslModel.class;
    }
}
